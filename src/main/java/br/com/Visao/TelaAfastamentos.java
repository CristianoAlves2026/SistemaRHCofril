/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Visao;


import br.com.Conexao.ConexaoPostgres;
import java.awt.Color;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 * @author Lavanderia
 */
public class TelaAfastamentos extends javax.swing.JFrame {

     // ==================================================
    // RENDERER — ÍCONE E COR SÓ SE TIVER RETORNO
    // ==================================================
    class RendererPersonalizado extends javax.swing.tree.DefaultTreeCellRenderer {

        private final Color COR_NOME = new Color(60, 60, 60);
        private final Color COR_AFASTADO = new Color(160, 110, 60);
        private final Color COR_RETORNO = new Color(120, 120, 120);

        private final Color COR_SELECAO_FUNDO = new Color(220, 232, 242);
        private final Color COR_SELECAO_TEXTO = Color.WHITE;

        @Override
        public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree arvore,
                Object valor, boolean selecionado, boolean expandido,
                boolean folha, int linha, boolean foco) {

            super.getTreeCellRendererComponent(arvore, valor, selecionado,
                    expandido, folha, linha, foco);

            String textoOriginal = valor.toString();
            String textoFormatado;

            setClosedIcon(null);
            setOpenIcon(null);
            setLeafIcon(null);

            if (textoOriginal.startsWith("Afastado")) {
                textoFormatado = "⚠️ " + textoOriginal;
                setFont(getFont().deriveFont(java.awt.Font.PLAIN, 12f));
            } else if (textoOriginal.startsWith("Retorno")) {
                textoFormatado = "✅ " + textoOriginal;
                setFont(getFont().deriveFont(java.awt.Font.PLAIN, 12f));
            } else if (textoOriginal.contains("aguardando retorno")) {
                // Sem retorno → SEM ÍCONE, mesma cor do Afastado
                textoFormatado = textoOriginal;
                setFont(getFont().deriveFont(java.awt.Font.PLAIN, 12f));
            } else {
                textoFormatado = "👤 " + textoOriginal;
                setFont(getFont().deriveFont(java.awt.Font.BOLD, 13f));
            }

            if (selecionado) {
                setBackground(COR_SELECAO_FUNDO);
                setForeground(COR_SELECAO_TEXTO);
            } else {
                if (textoOriginal.startsWith("Afastado")) {
                    setForeground(COR_AFASTADO);
                } else if (textoOriginal.startsWith("Retorno")) {
                    setForeground(COR_RETORNO);
                } else if (textoOriginal.contains("aguardando retorno")) {
                    // Sem retorno → mesma cor do Afastado
                    setForeground(COR_AFASTADO);
                } else {
                    setForeground(COR_NOME);
                }
            }

            setText(textoFormatado);
            return this;
        }
    }

    // ==================================================
    // carregarAfastados — SEM ÍCONE SEM RETORNO
    // ==================================================
    public void carregarAfastados() {
        DefaultTreeModel modelo = (DefaultTreeModel) jTreeAfastados.getModel();
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) modelo.getRoot();
        raiz.removeAllChildren();

        SimpleDateFormat formatoData = new SimpleDateFormat("dd.MM.yyyy");
        java.util.Date hoje = new java.util.Date();

        String sql = """
            SELECT 
                a."ID_FUNC",
                f."NOME",
                f."STATUS",
                a."DATA_AFASTAMENTO",
                a."DATA_RETORNO",
                a."MOTIVO"
            FROM "Afastados" a
            INNER JOIN "Funcionarios" f ON a."ID_FUNC" = f."ID"
            ORDER BY 
                f."NOME" ASC,
                a."ID_FUNC" ASC,
                a."DATA_AFASTAMENTO" ASC,
                a."DATA_RETORNO" ASC NULLS LAST
            """;

        Map<Integer, DefaultMutableTreeNode> mapaFuncionarios = new HashMap<>();

        try (Connection conn = ConexaoPostgres.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int idFunc = rs.getInt("ID_FUNC");
                String nome = rs.getString("NOME");
                String status = rs.getString("STATUS");
                java.sql.Date dtAfast = rs.getDate("DATA_AFASTAMENTO");
                java.sql.Date dtRetorno = rs.getDate("DATA_RETORNO");
                String motivo = rs.getString("MOTIVO");

                if (motivo == null) motivo = "";
                if (status == null || status.isBlank()) status = "NÃO INFORMADO";
                if (dtAfast == null) continue;

                String nomeComStatus = nome + " - " + status;

                DefaultMutableTreeNode noFuncionario = mapaFuncionarios.get(idFunc);
                if (noFuncionario == null) {
                    noFuncionario = new DefaultMutableTreeNode(nomeComStatus);
                    mapaFuncionarios.put(idFunc, noFuncionario);
                    raiz.add(noFuncionario);
                }

                String linhaAfast = String.format("Afastado - %s - %s",
                        formatoData.format(dtAfast), motivo);
                DefaultMutableTreeNode noAfast = new DefaultMutableTreeNode(linhaAfast);
                noFuncionario.add(noAfast);

                long dias;
                String linhaInfo;

                if (dtRetorno != null) {
                    // Com retorno → ícone e cor de Retorno
                    dias = (dtRetorno.getTime() - dtAfast.getTime()) / (1000 * 60 * 60 * 24);
                    String textoDias = (dias == 1) ? "dia de afastamento" : "dias de afastamento";
                    linhaInfo = String.format("Retorno   - %s - (%d %s)",
                            formatoData.format(dtRetorno), dias, textoDias);
                } else {
                    // Sem retorno → sem ícone, mesma cor do Afastado
                    dias = (hoje.getTime() - dtAfast.getTime()) / (1000 * 60 * 60 * 24);
                    String textoDias = (dias == 1) ? "dia afastado - aguardando retorno" : "dias afastado - aguardando retorno";
                    linhaInfo = String.format("(%d %s)", dias, textoDias);
                }

                noAfast.add(new DefaultMutableTreeNode(linhaInfo));
            }

            modelo.reload();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar afastamentos:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // SEU CONSTRUTOR — com a linha que liga o Renderer
    public TelaAfastamentos() {
        initComponents();
        
        // ✅ Adicione ESTAS 3 linhas logo abaixo do initComponents()
        jTreeAfastados.setCellRenderer(new RendererPersonalizado());
        jTreeAfastados.setRootVisible(false);
        jTreeAfastados.setRowHeight(24);
        
        setLocationRelativeTo(null);
        setTitle("Afastamentos de Funcionários");
        carregarAfastados();
    }


    
    
    
     
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTreeAfastados = new javax.swing.JTree();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTreeAfastados.setRootVisible(false);
        jTreeAfastados.setShowsRootHandles(true);
        jScrollPane1.setViewportView(jTreeAfastados);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaAfastamentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaAfastamentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaAfastamentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAfastamentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaAfastamentos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTreeAfastados;
    // End of variables declaration//GEN-END:variables
}
