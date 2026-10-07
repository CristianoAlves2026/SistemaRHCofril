/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Classes.Relatorios;

import br.com.Conexao.ConexaoPostgres;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author Lavanderia
 */
public class Relatorio002 extends javax.swing.JFrame {

    public Relatorio002() {

        initComponents();
        setLocationRelativeTo(null);
        configurarTabela();
        carregarEmpresas();

        jTextFieldBusca.addActionListener(e -> buscarSetores());
        jButtonGerar.addActionListener(e -> {

    if (jCheckBoxTodos.isSelected()) {
        gerarRelatorioFuncSetorTodos();
    } else {
        gerarRelatorio();
    }

});

    }

    private void configurarTabela() {

        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel(
                        new Object[]{"", "Setor"},
                        0
                ) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {

                if (columnIndex == 0) {
                    return Boolean.class;
                }

                return String.class;

            }

            @Override
            public boolean isCellEditable(int row, int column) {

                return column == 0;

            }

        };

        jTableSetores.setModel(modelo);

        // AJUSTE DAS COLUNAS
        jTableSetores.getColumnModel()
                .getColumn(0)
                .setMinWidth(30);

        jTableSetores.getColumnModel()
                .getColumn(0)
                .setMaxWidth(30);

        jTableSetores.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(30);

        jTableSetores.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(300);

        // impede redimensionamento da coluna checkbox
        jTableSetores.getColumnModel()
                .getColumn(0)
                .setResizable(false);

    }

    private void buscarSetores() {

        String textoBusca = jTextFieldBusca.getText().trim();

        javax.swing.table.DefaultTableModel modelo
                = (javax.swing.table.DefaultTableModel) jTableSetores.getModel();

        modelo.setRowCount(0);

        String sql
                = "SELECT \"NOME_SETOR\" "
                + "FROM \"Setor\" "
                + "WHERE \"NOME_SETOR\" ILIKE ? "
                + "ORDER BY \"NOME_SETOR\"";

        try (
                 java.sql.Connection conn
                = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst
                = conn.prepareStatement(sql);) {

            pst.setString(1, "%" + textoBusca + "%");

            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                modelo.addRow(new Object[]{
                    false,
                    rs.getString("NOME_SETOR")
                });

            }

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Erro ao buscar setores:\n" + e.getMessage()
            );

        }

    }

    private java.util.List<String> getSetoresSelecionados() {

        java.util.List<String> lista
                = new java.util.ArrayList<>();

        for (int i = 0; i < jTableSetores.getRowCount(); i++) {

            Boolean marcado
                    = (Boolean) jTableSetores.getValueAt(i, 0);

            if (marcado != null && marcado) {

                lista.add(
                        jTableSetores.getValueAt(i, 1).toString()
                );

            }

        }

        return lista;

    }

    private void gerarRelatorioFuncSetorTodos() {

    java.util.List<String> setoresSelecionados = getSetoresSelecionados();

    if (setoresSelecionados.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Selecione pelo menos um setor.");
        return;
    }

    String empresaSelecionada = jComboBoxEmpresa.getSelectedItem().toString();

    StringBuilder sql = new StringBuilder();

    sql.append("""
        SELECT "SETOR","NOME","FUNCAO","ADMISSAO"
        FROM "Funcionarios"
        WHERE "EMPRESA" = ?
        AND "STATUS" = 'ATIVO'
        AND "SETOR" IN (
    """);

    for (int i = 0; i < setoresSelecionados.size(); i++) {
        sql.append("?");
        if (i < setoresSelecionados.size() - 1) {
            sql.append(",");
        }
    }

    sql.append(")");

    // ORDENAÇÃO
    if (jRadioButtonNome.isSelected()) {
        sql.append(" ORDER BY \"SETOR\", \"NOME\" ");
    } else {
        sql.append(" ORDER BY \"SETOR\" ASC, \"ADMISSAO\" DESC ");
    }

    try (
        java.sql.Connection conn = ConexaoPostgres.conectar();
        java.sql.PreparedStatement pst = conn.prepareStatement(sql.toString())
    ) {

        int index = 1;

        pst.setString(index++, empresaSelecionada);

        for (String setor : setoresSelecionados) {
            pst.setString(index++, setor);
        }

        // ✅ AGORA CENTRALIZADO
        executarGeracaoPDF(pst);

    } catch (Exception e) {

        JOptionPane.showMessageDialog(
                null,
                "Erro ao gerar relatório:\n" + e.getMessage()
        );
    }
}

    private void gerarRelatorio() {

    java.util.List<String> setoresSelecionados = getSetoresSelecionados();

    if (setoresSelecionados.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Selecione pelo menos um setor.");
        return;
    }

    String empresaSelecionada = jComboBoxEmpresa.getSelectedItem().toString();

    java.util.Date dataInicial = jDateChooser2.getDate();
    java.util.Date dataFinal = jDateChooser3.getDate();

    // VALIDAÇÃO
    if ((dataInicial != null && dataFinal == null) ||
        (dataInicial == null && dataFinal != null)) {

        JOptionPane.showMessageDialog(null, "Informe data inicial e final.");
        return;
    }

    if (dataInicial != null && dataFinal != null && dataInicial.after(dataFinal)) {
        JOptionPane.showMessageDialog(null, "Data inicial maior que final.");
        return;
    }

    StringBuilder sql = new StringBuilder();

    sql.append("""
        SELECT "SETOR","NOME","FUNCAO","ADMISSAO"
        FROM "Funcionarios"
        WHERE "EMPRESA" = ?
        AND "STATUS" = 'ATIVO'
    """);

    // ✅ AGORA SEMPRE POR ADMISSÃO
    if (dataInicial != null && dataFinal != null) {
        sql.append(" AND \"ADMISSAO\" BETWEEN ? AND ? ");
    }

    // SETORES
    sql.append(" AND \"SETOR\" IN (");

    for (int i = 0; i < setoresSelecionados.size(); i++) {
        sql.append("?");
        if (i < setoresSelecionados.size() - 1) {
            sql.append(",");
        }
    }

    sql.append(")");

    // ORDENAÇÃO
    if (jRadioButtonNome.isSelected()) {
        sql.append(" ORDER BY \"SETOR\", \"NOME\" ");
    } else {
        sql.append(" ORDER BY \"SETOR\" ASC, \"ADMISSAO\" DESC ");
    }

    try (
        java.sql.Connection conn = ConexaoPostgres.conectar();
        java.sql.PreparedStatement pst = conn.prepareStatement(sql.toString())
    ) {

        int index = 1;

        pst.setString(index++, empresaSelecionada);

        // DATAS
        if (dataInicial != null && dataFinal != null) {
            pst.setDate(index++, new java.sql.Date(dataInicial.getTime()));
            pst.setDate(index++, new java.sql.Date(dataFinal.getTime()));
        }

        // SETORES
        for (String setor : setoresSelecionados) {
            pst.setString(index++, setor);
        }

        executarGeracaoPDF(pst);

    } catch (Exception e) {

        JOptionPane.showMessageDialog(
                null,
                "Erro ao gerar relatório:\n" + e.getMessage()
        );
    }
}
    
    private void executarGeracaoPDF(java.sql.PreparedStatement pst) throws Exception {

    try (java.sql.ResultSet rs = pst.executeQuery()) {

        if (!rs.isBeforeFirst()) {
            JOptionPane.showMessageDialog(null, "Nenhum funcionário encontrado.");
            return;
        }

        java.time.LocalTime agora = java.time.LocalTime.now();
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("HH-mm-ss");

        String pasta = "F:\\Lavanderia\\Backup\\Relatorios\\";
        new java.io.File(pasta).mkdirs();

        String caminho = pasta + "Relat. Func. Setor "
                + agora.format(formatter) + ".pdf";

        com.lowagie.text.Document document =
                new com.lowagie.text.Document(
                        com.lowagie.text.PageSize.A4.rotate()
                );

        com.lowagie.text.pdf.PdfWriter.getInstance(
                document,
                new java.io.FileOutputStream(caminho)
        );

        document.open();

        com.lowagie.text.Font titulo = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 18,
                com.lowagie.text.Font.BOLD);

        com.lowagie.text.Font empresaFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 14,
                com.lowagie.text.Font.BOLD);

        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 13,
                com.lowagie.text.Font.BOLD);

        com.lowagie.text.Font setorFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 12,
                com.lowagie.text.Font.BOLD);

        document.add(new com.lowagie.text.Paragraph(
                "RELATÓRIO DE FUNCIONÁRIOS ATIVOS POR SETOR", titulo));

        document.add(new com.lowagie.text.Paragraph(" "));

        document.add(new com.lowagie.text.Paragraph(
                "Empresa: " + jComboBoxEmpresa.getSelectedItem(), empresaFont));

        document.add(new com.lowagie.text.Paragraph(" "));

        com.lowagie.text.pdf.PdfPTable tabela =
                new com.lowagie.text.pdf.PdfPTable(4);

        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{3, 5, 5, 4});

        tabela.addCell(cabecalho("SETOR", headerFont));
        tabela.addCell(cabecalho("NOME", headerFont));
        tabela.addCell(cabecalho("FUNÇÃO", headerFont));
        tabela.addCell(cabecalho("ADMISSÃO", headerFont));

        java.util.Map<String, Integer> totaisSetor = new java.util.HashMap<>();
        java.util.List<String[]> dados = new java.util.ArrayList<>();

        while (rs.next()) {

            String setor = rs.getString("SETOR");

            totaisSetor.put(
                    setor,
                    totaisSetor.getOrDefault(setor, 0) + 1
            );

            java.sql.Date dataSql = rs.getDate("ADMISSAO");

            String dataFormatada = "";

            if (dataSql != null) {
                java.time.LocalDate data = dataSql.toLocalDate();
                java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                dataFormatada = data.format(fmt);
            }

            dados.add(new String[]{
                setor,
                rs.getString("NOME"),
                rs.getString("FUNCAO"),
                dataFormatada
            });
        }

        String setorAtual = "";
        int totalFuncionarios = 0;
        boolean linhaCinza = false;

        for (String[] linha : dados) {

            String setor = linha[0];
            boolean mudouSetor = !setor.equals(setorAtual);

            if (mudouSetor && !setorAtual.isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    tabela.addCell(vazio());
                    tabela.addCell(vazio());
                    tabela.addCell(vazio());
                    tabela.addCell(vazio());
                }
            }

            com.lowagie.text.pdf.PdfPCell c1;

            if (mudouSetor) {
                c1 = new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase(
                                setor + " (" + totaisSetor.get(setor) + ")",
                                setorFont
                        )
                );
                setorAtual = setor;
            } else {
                c1 = new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase("")
                );
            }

            com.lowagie.text.pdf.PdfPCell c2 =
                    new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(linha[1]));

            com.lowagie.text.pdf.PdfPCell c3 =
                    new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(linha[2]));

            com.lowagie.text.pdf.PdfPCell c4 =
                    new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(linha[3]));

            if (linhaCinza) {
                com.lowagie.text.pdf.GrayColor cinza =
                        new com.lowagie.text.pdf.GrayColor(0.93f);

                c1.setBackgroundColor(cinza);
                c2.setBackgroundColor(cinza);
                c3.setBackgroundColor(cinza);
                c4.setBackgroundColor(cinza);
            }

            c1.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            c2.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            c3.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            c4.setBorder(com.lowagie.text.Rectangle.NO_BORDER);

            tabela.addCell(c1);
            tabela.addCell(c2);
            tabela.addCell(c3);
            tabela.addCell(c4);

            linhaCinza = !linhaCinza;
            totalFuncionarios++;
        }

        com.lowagie.text.pdf.PdfPCell totalCell =
                new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase(
                                "TOTAL FUNCIONÁRIOS: " + totalFuncionarios,
                                headerFont
                        )
                );

        totalCell.setColspan(4);
        totalCell.setBorder(com.lowagie.text.Rectangle.TOP);

        tabela.addCell(totalCell);

        document.add(tabela);
        document.close();

        java.awt.Desktop.getDesktop().open(new java.io.File(caminho));
    }
}
    
    
    
    /*
CABECALHO PADRÃO
     */
    private com.lowagie.text.pdf.PdfPCell cabecalho(
            String texto,
            com.lowagie.text.Font fonte
    ) {

        com.lowagie.text.pdf.PdfPCell cell
                = new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase(texto, fonte)
                );

        cell.setBorder(
                com.lowagie.text.Rectangle.BOTTOM
        );

        return cell;

    }

    private com.lowagie.text.pdf.PdfPCell vazio() {

        com.lowagie.text.pdf.PdfPCell cell
                = new com.lowagie.text.pdf.PdfPCell(
                        new com.lowagie.text.Phrase(" ")
                );

        cell.setBorder(
                com.lowagie.text.Rectangle.NO_BORDER
        );

        return cell;

    }

    private void carregarEmpresas() {

        String sql = """
                 SELECT "NOME_FANTASIA"
                 FROM "Empresa"
                 WHERE "NOME_FANTASIA" IS NOT NULL
                 AND TRIM("NOME_FANTASIA") <> ''
                 ORDER BY "NOME_FANTASIA"
                 """;

        jComboBoxEmpresa.removeAllItems();

        try (
                 Connection conn = ConexaoPostgres.conectar();  PreparedStatement pst = conn.prepareStatement(sql);  ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                jComboBoxEmpresa.addItem(
                        rs.getString("NOME_FANTASIA")
                );

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao carregar empresas:\n" + e.getMessage()
            );

        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableSetores = new javax.swing.JTable();
        jTextFieldBusca = new javax.swing.JTextField();
        jButtonGerar = new javax.swing.JButton();
        jComboBoxEmpresa = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButtonNome = new javax.swing.JRadioButton();
        jRadioButtonAdmissao = new javax.swing.JRadioButton();
        jCheckBoxTodos = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jRadioButtonAdmissao2 = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Relatório Funcionários por Setor");

        jTableSetores.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTableSetores);

        jTextFieldBusca.setBackground(new java.awt.Color(255, 255, 0));

        jButtonGerar.setText("Gerar");

        jComboBoxEmpresa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Pesquisar Setor");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Empresa");

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Ordernar");

        jRadioButtonNome.setSelected(true);
        jRadioButtonNome.setText("Nome");
        jRadioButtonNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNomeActionPerformed(evt);
            }
        });

        jRadioButtonAdmissao.setText("Admissão");
        jRadioButtonAdmissao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAdmissaoActionPerformed(evt);
            }
        });

        jCheckBoxTodos.setText("Listar todos os func. do setor.");
        jCheckBoxTodos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTodosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButtonNome)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                        .addComponent(jRadioButtonAdmissao)
                        .addGap(40, 40, 40))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxTodos, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonNome)
                    .addComponent(jRadioButtonAdmissao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxTodos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setText("Data Final");

        jLabel6.setText("Data Inicial");

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Filtrar");

        jRadioButtonAdmissao2.setSelected(true);
        jRadioButtonAdmissao2.setText("Admissão");
        jRadioButtonAdmissao2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAdmissao2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonAdmissao2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jRadioButtonAdmissao2))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jDateChooser3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                        .addComponent(jDateChooser2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addGap(1, 1, 1)
                .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonGerar))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jComboBoxEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextFieldBusca)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonGerar)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBoxEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButtonNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNomeActionPerformed
        jRadioButtonAdmissao.setSelected(false);
    }//GEN-LAST:event_jRadioButtonNomeActionPerformed

    private void jRadioButtonAdmissaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAdmissaoActionPerformed
        jRadioButtonNome.setSelected(false);
    }//GEN-LAST:event_jRadioButtonAdmissaoActionPerformed

    private void jRadioButtonAdmissao2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAdmissao2ActionPerformed
       jRadioButtonAdmissao2.setSelected(true);
    }//GEN-LAST:event_jRadioButtonAdmissao2ActionPerformed

    private void jCheckBoxTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTodosActionPerformed
        if (jCheckBoxTodos.isSelected()){
            jRadioButtonAdmissao2.setEnabled(false);
            jDateChooser2.setEnabled(false);
            jDateChooser3.setEnabled(false);
           
        }else{
            jRadioButtonAdmissao2.setEnabled(true);
            jDateChooser2.setEnabled(true);
            jDateChooser3.setEnabled(true);
        }
        
        
    }//GEN-LAST:event_jCheckBoxTodosActionPerformed

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
            java.util.logging.Logger.getLogger(Relatorio002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Relatorio002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Relatorio002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Relatorio002.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Relatorio002().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonGerar;
    private javax.swing.JCheckBox jCheckBoxTodos;
    private javax.swing.JComboBox<String> jComboBoxEmpresa;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButtonAdmissao;
    private javax.swing.JRadioButton jRadioButtonAdmissao2;
    private javax.swing.JRadioButton jRadioButtonNome;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableSetores;
    private javax.swing.JTextField jTextFieldBusca;
    // End of variables declaration//GEN-END:variables
}
