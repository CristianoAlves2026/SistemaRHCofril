package br.com.Conexao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexaoPostgres {

    private static HikariDataSource dataSource;

    static {

        try {

            // Carrega o driver PostgreSQL
            Class.forName("org.postgresql.Driver");

            HikariConfig config = new HikariConfig();

            //*****************************************
            //BANCO DE PRODUÇÃO/ BANCO OFICIAL
            config.setJdbcUrl(
                "jdbc:postgresql://ep-blue-frost-ad6pf00o-pooler.c-2.us-east-1.aws.neon.tech/bdcofril?sslmode=require&tcpKeepAlive=true"
                );
            
            

            //******************************************
            //BRANCH banco_teste_bdcofril - BANCO DE TESTES//
            /*config.setJdbcUrl(
                    "jdbc:postgresql://ep-late-breeze-adjle4e4-pooler.c-2.us-east-1.aws.neon.tech/bdcofril?sslmode=require&tcpKeepAlive=true"
            );*/
            //******************************************************

            config.setUsername("neondb_owner");

            config.setPassword("npg_z8fDyvuC5ScI");


            /*
            ===============================
            CONFIGURAÇÃO IDEAL ATÉ 10 USUÁRIOS
            ===============================
             */
            config.setMaximumPoolSize(10);

            config.setMinimumIdle(0);

            config.setIdleTimeout(60000);

            config.setConnectionTimeout(60000);

            config.setMaxLifetime(900000);

            config.setValidationTimeout(5000);

            config.setKeepaliveTime(0);

            config.setPoolName("PoolPostgres");


            /*
            ===============================
            CACHE DE PREPARED STATEMENTS
            ===============================
             */
            config.addDataSourceProperty("cachePrepStmts", "true");

            config.addDataSourceProperty("prepStmtCacheSize", "250");

            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            config.addDataSourceProperty("useServerPrepStmts", "true");

            config.addDataSourceProperty("tcpKeepAlive", "true");


            /*
            ===============================
            RECONEXÃO AUTOMÁTICA
            ===============================
             */
            config.setInitializationFailTimeout(-1);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Erro ao inicializar PostgreSQL:\n" + e.getMessage());

        }

    }


    /*
    ===============================
    MÉTODO conectar()
    ===============================
     */
    public static Connection conectar() throws SQLException {

        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Pool de conexões não inicializado");
        }

        long inicio = System.currentTimeMillis();

        javax.swing.JDialog dialog = new javax.swing.JDialog();
        dialog.setTitle("Aguarde");
        dialog.setModal(false); // não bloqueia
        dialog.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(null);
        javax.swing.JLabel label = new javax.swing.JLabel("Servidor iniciando. Aguarde alguns segundos...", javax.swing.SwingConstants.CENTER);
        dialog.add(label);

        Connection conn = null;
        try {
            // Tenta pegar conexão
            conn = dataSource.getConnection();

            long tempo = System.currentTimeMillis() - inicio;

            // Se demorou mais de 2 segundos, mostra a tela de “acordando”
            if (tempo > 1000) {
                javax.swing.SwingUtilities.invokeLater(() -> dialog.setVisible(true));
            }

            return conn;

        } catch (SQLException ex) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        "Não foi possível conectar ao servidor.\nVerifique sua internet ou tente novamente.");
            });
            throw ex;

        } finally {
            // Fecha o diálogo caso tenha sido aberto
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (dialog.isVisible()) {
                    dialog.dispose();
                }
            });
        }
    }
    
    
    /*
===============================
RETORNA O TIPO DE BANCO ATUAL
===============================
*/
public static String getTipoBanco() {
    if (dataSource != null && !dataSource.isClosed()) {
        String url = dataSource.getJdbcUrl();
        // Verifica a presença da chave do endpoint de teste na URL do Neon Tech
        if (url != null && url.contains("ep-late-breeze-adjle4e4")) {
            return "Banco de Teste";
        }
    }
    return "Banco de Produção";
}

}
