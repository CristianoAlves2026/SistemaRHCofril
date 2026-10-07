package br.com.Graficos;

import br.com.Conexao.ConexaoPostgres;
import org.jfree.data.general.DefaultPieDataset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class GraficoDAO {

    /**
     * Consulta no PostgreSQL e retorna apenas empresas com 30 ou mais funcionarios ativos.
     */
    public DefaultPieDataset obterDadosAtivosPorEmpresa() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        // Adicionado HAVING COUNT(*) >= 30 para filtrar os grupos por quantidade
        String sql = "SELECT \"EMPRESA\", COUNT(*) AS total "
                   + "FROM \"Funcionarios\" "
                   + "WHERE UPPER(\"STATUS\") = 'ATIVO' "
                   + "GROUP BY \"EMPRESA\" "
                   + "HAVING COUNT(*) >= 30";

        try (Connection conexao = ConexaoPostgres.conectar();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int contador = 0;
            while (rs.next()) {
                String empresa = rs.getString("EMPRESA");
                int totalAtivos = rs.getInt("total");

                //System.out.println("Empresa (>=30): " + empresa + " | Total: " + totalAtivos);

                dataset.setValue(empresa, totalAtivos);
                contador++;
            }

            if (contador == 0) {
                JOptionPane.showMessageDialog(null, 
                    "Nenhuma empresa possui 30 ou mais funcionários com STATUS = 'ATIVO'.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            System.err.println("Erro na consulta do gráfico: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Erro SQL ao consultar dados do gráfico:\n" + e.getMessage(), 
                "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        }

        return dataset;
    }
}