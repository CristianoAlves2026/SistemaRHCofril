/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package br.com.Visao;

import javax.swing.JDialog;

/**
 *
 * @author Lavanderia
 */
public class TelaNotificaçõesAnaliticas extends javax.swing.JDialog {

    /**
     * Creates new form TelaNotificaçõesAnaliticas
     */
    public TelaNotificaçõesAnaliticas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jTableNotificacaosAnaliticas.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    }

    public void preencherComAniversariantes(java.util.List<br.com.Utilidades.FuncionarioNotificacao> lista) {
        setTitle("Aniversariantes do Dia");
        try {
            // 1. Criamos um modelo do zero com as 4 colunas que você precisa
            String[] colunas = {"EMPRESA", "NOME", "SETOR", "NASCIMENTO", "IDADE"};
            javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(null, colunas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Deixa a tabela não-editável (bloqueia dois cliques do usuário)
                }
            };

            // 2. Formatador de data
            java.time.LocalDate hoje = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter formatter
                    = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (br.com.Utilidades.FuncionarioNotificacao f : lista) {

                int idade = 0;

                if (f.getNascimento() != null) {
                    idade = java.time.Period.between(f.getNascimento(), hoje).getYears();
                }

                Object[] linha = new Object[]{
                    f.getEmpresa() != null ? f.getEmpresa() : "",
                    f.getNome() != null ? f.getNome() : "",
                    f.getSetor() != null ? f.getSetor() : "",
                    f.getNascimento() != null ? f.getNascimento().format(formatter) : "",
                    idade > 0 ? idade : ""
                };

                modelo.addRow(linha);
            }

            // 4. Vincula o modelo novo à sua tabela física da tela
            jTableNotificacaosAnaliticas.setModel(modelo);
            configurarOrdenacao();
            ajustarLarguraColunas();
            ajustarTamanhoDialog();

        } catch (Exception e) {
            // Se houver qualquer falha visual, ela será mostrada explicitamente no console
            System.out.println("Erro crítico ao desenhar a tabela analítica:");
            e.printStackTrace();
        }
    }

    public void preencherComExperiencia(java.util.List<br.com.Utilidades.FuncionarioNotificacao> lista) {
        setTitle("Funcionários em Período de Experiência");
        String[] colunas = {
            "EMPRESA",
            "NOME",
            "SETOR",
            "ADMISSÃO",
            "DATA LIMITE",
            "VENCE EM"
        };

        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel(null, colunas) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        java.time.format.DateTimeFormatter formatter
                = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        java.time.LocalDate hoje = java.time.LocalDate.now();

        for (br.com.Utilidades.FuncionarioNotificacao f : lista) {

            java.time.LocalDate vencimento = f.getAdmissao().plusDays(90);

            long diasParaVencer
                    = java.time.temporal.ChronoUnit.DAYS.between(hoje, vencimento);

            modelo.addRow(new Object[]{
                f.getEmpresa(),
                f.getNome(),
                f.getSetor(),
                f.getAdmissao(),
                vencimento.format(formatter),
                diasParaVencer + " Dias"

            });

        }

        jTableNotificacaosAnaliticas.setModel(modelo);
        jTableNotificacaosAnaliticas.getColumnModel()
                .getColumn(3)
                .setCellRenderer(criarRendererData());

        jTableNotificacaosAnaliticas.getColumnModel()
                .getColumn(4)
                .setCellRenderer(criarRendererData());

        configurarOrdenacao();
        ajustarLarguraColunas();
        ajustarTamanhoDialog();
    }

    public void preencherComFuncionariosEmProcesso(
            java.util.List<br.com.Utilidades.FuncionarioNotificacao> lista) {

        setTitle("Funcionários em Processo");

        String[] colunas = {
            "EMPRESA",
            "NOME",
            "SETOR",
            "STATUS"
        };

        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel(null, colunas) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (br.com.Utilidades.FuncionarioNotificacao f : lista) {

            modelo.addRow(new Object[]{
                f.getEmpresa(),
                f.getNome(),
                f.getSetor(),
                f.getStatus()
            });

        }

        jTableNotificacaosAnaliticas.setModel(modelo);
        configurarOrdenacao();
        ajustarLarguraColunas();
        ajustarTamanhoDialog();
    }

    private void configurarOrdenacao() {

        javax.swing.table.DefaultTableModel modelo
                = (javax.swing.table.DefaultTableModel) jTableNotificacaosAnaliticas.getModel();

        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter
                = new javax.swing.table.TableRowSorter<>(modelo);

        jTableNotificacaosAnaliticas.setRowSorter(sorter);

        int colunaNome = modelo.findColumn("NOME");

        if (colunaNome >= 0) {
            sorter.setSortKeys(java.util.Collections.singletonList(
                    new javax.swing.RowSorter.SortKey(
                            colunaNome,
                            javax.swing.SortOrder.ASCENDING)));

            sorter.sort();
        }
    }

    private void ajustarLarguraColunas() {

        final int margem = 10;

        for (int coluna = 0; coluna < jTableNotificacaosAnaliticas.getColumnCount(); coluna++) {

            javax.swing.table.TableColumn tableColumn
                    = jTableNotificacaosAnaliticas.getColumnModel().getColumn(coluna);

            int largura = 0;

            // Largura do cabeçalho
            javax.swing.table.TableCellRenderer headerRenderer
                    = tableColumn.getHeaderRenderer();

            if (headerRenderer == null) {
                headerRenderer = jTableNotificacaosAnaliticas.getTableHeader().getDefaultRenderer();
            }

            java.awt.Component comp = headerRenderer.getTableCellRendererComponent(
                    jTableNotificacaosAnaliticas,
                    tableColumn.getHeaderValue(),
                    false,
                    false,
                    0,
                    coluna);

            largura = comp.getPreferredSize().width;

            // Largura dos dados
            for (int linha = 0; linha < jTableNotificacaosAnaliticas.getRowCount(); linha++) {

                javax.swing.table.TableCellRenderer renderer
                        = jTableNotificacaosAnaliticas.getCellRenderer(linha, coluna);

                comp = renderer.getTableCellRendererComponent(
                        jTableNotificacaosAnaliticas,
                        jTableNotificacaosAnaliticas.getValueAt(linha, coluna),
                        false,
                        false,
                        linha,
                        coluna);

                largura = Math.max(largura, comp.getPreferredSize().width);
            }

            tableColumn.setPreferredWidth(largura + margem);
        }

    }

    private void ajustarTamanhoDialog() {

        jTableNotificacaosAnaliticas.doLayout();

        int larguraTabela = 0;

        for (int i = 0; i < jTableNotificacaosAnaliticas.getColumnCount(); i++) {
            larguraTabela += jTableNotificacaosAnaliticas
                    .getColumnModel()
                    .getColumn(i)
                    .getPreferredWidth();
        }

        int alturaTabela
                = jTableNotificacaosAnaliticas.getTableHeader().getPreferredSize().height
                + (jTableNotificacaosAnaliticas.getRowCount()
                * jTableNotificacaosAnaliticas.getRowHeight());

        // limites
        int larguraMax = java.awt.Toolkit.getDefaultToolkit()
                .getScreenSize().width - 100;

        //int alturaMax = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - 150;
        java.awt.Dimension tela = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        int alturaMax = (int) (tela.height * 0.70);

        int largura = Math.min(larguraTabela + 30, larguraMax);
        int altura = Math.min(alturaTabela + 50, alturaMax);

        jScrollPane1.setPreferredSize(
                new java.awt.Dimension(
                        largura,
                        altura
                )
        );

        jPanel1.setPreferredSize(
                new java.awt.Dimension(
                        largura,
                        altura
                )
        );

        pack();

        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    private javax.swing.table.DefaultTableCellRenderer criarRendererData() {

        return new javax.swing.table.DefaultTableCellRenderer() {

            private final java.time.format.DateTimeFormatter formatter
                    = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void setValue(Object value) {

                if (value instanceof java.time.LocalDate) {
                    setText(((java.time.LocalDate) value).format(formatter));
                } else {
                    super.setValue(value);
                }

            }
        };
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableNotificacaosAnaliticas = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Aniversariantes do Dia");
        setAlwaysOnTop(true);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jScrollPane1.setForeground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setPreferredSize(null);

        jTableNotificacaosAnaliticas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTableNotificacaosAnaliticas);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 791, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            java.util.logging.Logger.getLogger(TelaNotificaçõesAnaliticas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaNotificaçõesAnaliticas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaNotificaçõesAnaliticas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaNotificaçõesAnaliticas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TelaNotificaçõesAnaliticas dialog = new TelaNotificaçõesAnaliticas(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableNotificacaosAnaliticas;
    // End of variables declaration//GEN-END:variables
}
