/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Classes.Relatorios;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import java.io.FileOutputStream;
import java.io.File;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 *
 * @author Lavanderia
 */
public class Relatorio001 extends javax.swing.JFrame {

    private int nivelUsuario;

    /**
     * Creates new form Relatorio001
     */
    public Relatorio001() {

    }

    public Relatorio001(int nivelUsuario) {
        initComponents();
        setLocationRelativeTo(null);
        carregarCombos();
        this.nivelUsuario = nivelUsuario;
        aplicarPermissoes();

    }

    private void carregarCombos() {

        jComboBoxEmpresa.removeAllItems();
        jComboBoxStatus.removeAllItems();

        jComboBoxEmpresa.addItem("Selecione...");
        jComboBoxEmpresa.addItem("TODAS");
        jComboBoxStatus.addItem("Selecione...");

        jComboBoxStatus.addItem("ATIVO");
        jComboBoxStatus.addItem("DEMITIDO");
        jComboBoxStatus.addItem("EM PROCESSO");
        jComboBoxStatus.addItem("CANCELADO");
        jComboBoxStatus.addItem("AFASTADO");

        String sql = "SELECT \"NOME_FANTASIA\" FROM \"Empresa\" ORDER BY \"NOME_FANTASIA\"";

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst = conn.prepareStatement(sql);  java.sql.ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                jComboBoxEmpresa.addItem(rs.getString("NOME_FANTASIA"));

            }

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Erro ao carregar empresas:\n" + e.getMessage()
            );

        }
    }

    private boolean validarCampos() {

        if (jComboBoxEmpresa.getSelectedIndex() == 0) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Selecione uma empresa."
            );

            jComboBoxEmpresa.requestFocus();
            return false;

        }

        if (jComboBoxStatus.getSelectedIndex() == 0) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Selecione um status."
            );

            jComboBoxStatus.requestFocus();
            return false;

        }

        if (jDateChooserDataInicial.getDate() == null) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Selecione a data inicial."
            );

            jDateChooserDataInicial.requestFocus();
            return false;

        }

        if (jDateChooserDataFinal.getDate() == null) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Selecione a data final."
            );

            jDateChooserDataFinal.requestFocus();
            return false;

        }

        if (jDateChooserDataInicial.getDate().after(jDateChooserDataFinal.getDate())) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "A data inicial não pode ser maior que a data final."
            );

            jDateChooserDataInicial.requestFocus();
            return false;

        }

        return true;

    }

    private void gerarRelatorioModificado() {

        try {

            String campoData;

            if (jRadioButtonAdmissao.isSelected()) {

                campoData = "ADMISSAO";

            } else if (jRadioButtonDemissao.isSelected()) {

                campoData = "DEMISSAO";

                if (jComboBoxStatus.getSelectedItem().toString().equals("ATIVO")) {

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Funcionários ATIVOS não possuem data de demissão."
                    );

                    return;

                }

            } else {

                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Selecione Admissão ou Demissão."
                );

                return;

            }

            String pastaDestino = "F:\\Lavanderia\\Backup\\Relatorios";

            File pasta = new File(pastaDestino);

            if (!pasta.exists()) {

                pasta.mkdirs();

            }

            java.text.SimpleDateFormat sdfArquivo
                    = new java.text.SimpleDateFormat("HHmmss");

            String nomeArquivo
                    = "Relatorio_funcionarios_"
                    + sdfArquivo.format(new java.util.Date())
                    + ".pdf";

            File arquivo
                    = new File(pastaDestino + "\\" + nomeArquivo);

            Document documento = new Document(PageSize.A4);

            try ( FileOutputStream fos = new FileOutputStream(arquivo)) {

                PdfWriter.getInstance(documento, fos);

                documento.open();

                Font fonteTitulo
                        = new Font(Font.HELVETICA, 16, Font.BOLD);

                Font fonteCabecalho
                        = new Font(Font.HELVETICA, 10, Font.BOLD);

                Font fonteDados
                        = new Font(Font.HELVETICA, 9, Font.NORMAL);

                String empresa
                        = jComboBoxEmpresa.getSelectedItem().toString();

                boolean todasEmpresas
                        = empresa.equals("TODAS");

                String empresaTitulo
                        = todasEmpresas
                                ? "TODAS"
                                : empresa;

                String status
                        = jComboBoxStatus.getSelectedItem().toString();

                java.sql.Date dataInicial
                        = new java.sql.Date(
                                jDateChooserDataInicial.getDate().getTime()
                        );

                java.sql.Date dataFinal
                        = new java.sql.Date(
                                jDateChooserDataFinal.getDate().getTime()
                        );

                java.text.SimpleDateFormat sdfBR
                        = new java.text.SimpleDateFormat("dd/MM/yyyy");

                String tipoRelatorio
                        = campoData.equals("ADMISSAO")
                        ? "Admissões"
                        : "Demissões";

                Paragraph titulo
                        = new Paragraph(
                                "Relatório de " + tipoRelatorio
                                + "\nEmpresa: "
                                + empresaTitulo
                                + " | Status: "
                                + status
                                + "\nPeríodo: "
                                + sdfBR.format(dataInicial)
                                + " até "
                                + sdfBR.format(dataFinal),
                                fonteTitulo
                        );

                titulo.setAlignment(Element.ALIGN_CENTER);

                titulo.setSpacingAfter(15);

                documento.add(titulo);

                PdfPTable tabela
                        = new PdfPTable(
                                todasEmpresas ? 5 : 4
                        );

                tabela.setWidthPercentage(100);

                if (todasEmpresas) {

                    tabela.setWidths(
                            new float[]{1, 4, 3, 2, 2}
                    );

                } else {

                    tabela.setWidths(
                            new float[]{1, 5, 2, 2}
                    );

                }

                tabela.setHeaderRows(1);

                String[] colunas;

                if (todasEmpresas) {

                    colunas = new String[]{
                        "ID",
                        "NOME",
                        "EMPRESA",
                        campoData,
                        "STATUS"

                    };

                } else {

                    colunas = new String[]{
                        "ID",
                        "NOME",
                        campoData,
                        "STATUS"

                    };

                }

                for (String coluna : colunas) {

                    PdfPCell cell
                            = new PdfPCell(
                                    new Phrase(coluna, fonteCabecalho)
                            );

                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);

                    cell.setBorder(PdfPCell.BOTTOM);

                    tabela.addCell(cell);

                }

                String sql;

                if (todasEmpresas) {

                    sql
                            = "SELECT \"ID\", \"NOME\", \"EMPRESA\", \"" + campoData + "\", \"STATUS\" "
                            + "FROM \"Funcionarios\" f "
                            + "WHERE \"STATUS\" = ? "
                            + "AND \"" + campoData + "\" BETWEEN ? AND ? "
                            + "AND ( ? <> 'DEMITIDO' OR NOT EXISTS ( "
                            + "    SELECT 1 FROM \"Funcionarios\" f2 "
                            + "    WHERE f2.\"NOME\" = f.\"NOME\" "
                            + "    AND f2.\"STATUS\" = 'ATIVO' "
                            + ") ) "
                            + "ORDER BY \"EMPRESA\", \"" + campoData + "\", \"NOME\"";

                } else {

                    sql
                            = "SELECT \"ID\", \"NOME\", \"EMPRESA\", \"" + campoData + "\", \"STATUS\" "
                            + "FROM \"Funcionarios\" f "
                            + "WHERE \"EMPRESA\" = ? "
                            + "AND \"STATUS\" = ? "
                            + "AND \"" + campoData + "\" BETWEEN ? AND ? "
                            + "AND ( ? <> 'DEMITIDO' OR NOT EXISTS ( "
                            + "    SELECT 1 FROM \"Funcionarios\" f2 "
                            + "    WHERE f2.\"NOME\" = f.\"NOME\" "
                            + "    AND f2.\"STATUS\" = 'ATIVO' "
                            + ") ) "
                            + "ORDER BY \"" + campoData + "\", \"NOME\"";

                }

                try (
                         java.sql.Connection conn
                        = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst
                        = conn.prepareStatement(sql)) {

                    if (todasEmpresas) {

                        pst.setString(1, status);
                        pst.setDate(2, dataInicial);
                        pst.setDate(3, dataFinal);
                        pst.setString(4, status);

                    } else {

                        pst.setString(1, empresa);
                        pst.setString(2, status);
                        pst.setDate(3, dataInicial);
                        pst.setDate(4, dataFinal);
                        pst.setString(5, status);

                    }

                    try ( java.sql.ResultSet rs = pst.executeQuery()) {

                        int total = 0;

                        // CRIA CSV UTF-8
                        java.io.OutputStreamWriter csvWriter
                                = new java.io.OutputStreamWriter(
                                        new java.io.FileOutputStream(
                                                pastaDestino + "\\import.csv"
                                        ),
                                        java.nio.charset.StandardCharsets.UTF_8
                                );

                        // CABEÇALHO CSV
                        if (todasEmpresas) {

                            csvWriter.write(
                                    "ID;NOME;EMPRESA;"
                                    + campoData
                                    + ";STATUS\n"
                            );

                        } else {

                            csvWriter.write(
                                    "ID;NOME;"
                                    + campoData
                                    + ";STATUS\n"
                            );

                        }

                        while (rs.next()) {

                            PdfPCell cellID
                                    = new PdfPCell(
                                            new Phrase(
                                                    String.valueOf(
                                                            rs.getInt("ID")
                                                    ),
                                                    fonteDados
                                            )
                                    );

                            cellID.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellID);

                            PdfPCell cellNome
                                    = new PdfPCell(
                                            new Phrase(
                                                    rs.getString("NOME"),
                                                    fonteDados
                                            )
                                    );

                            cellNome.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellNome);
                            if (todasEmpresas) {

                                PdfPCell cellEmpresa
                                        = new PdfPCell(
                                                new Phrase(
                                                        rs.getString("EMPRESA"),
                                                        fonteDados
                                                )
                                        );

                                cellEmpresa.setBorder(PdfPCell.NO_BORDER);

                                tabela.addCell(cellEmpresa);

                            }

                            PdfPCell cellData
                                    = new PdfPCell(
                                            new Phrase(
                                                    sdfBR.format(
                                                            rs.getDate(campoData)
                                                    ),
                                                    fonteDados
                                            )
                                    );

                            cellData.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellData);

                            PdfPCell cellStatus
                                    = new PdfPCell(
                                            new Phrase(
                                                    rs.getString("STATUS"),
                                                    fonteDados
                                            )
                                    );

                            cellStatus.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellStatus);

                            // ESCREVE CSV
                            if (todasEmpresas) {

                                csvWriter.write(
                                        rs.getInt("ID") + ";"
                                        + rs.getString("NOME") + ";"
                                        + rs.getString("EMPRESA") + ";"
                                        + sdfBR.format(rs.getDate(campoData)) + ";"
                                        + rs.getString("STATUS") + "\n"
                                );

                            } else {

                                csvWriter.write(
                                        rs.getInt("ID") + ";"
                                        + rs.getString("NOME") + ";"
                                        + sdfBR.format(rs.getDate(campoData)) + ";"
                                        + rs.getString("STATUS") + "\n"
                                );

                            }

                            total++;

                        }

                        csvWriter.flush();
                        csvWriter.close();

                        documento.add(tabela);

                        Paragraph rodape
                                = new Paragraph(
                                        "\nTotal de registros: "
                                        + total,
                                        fonteCabecalho
                                );

                        rodape.setAlignment(Element.ALIGN_RIGHT);

                        documento.add(rodape);

                    }

                }

                documento.close();

            }

            java.awt.Desktop.getDesktop().open(arquivo);

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Erro ao gerar relatório:\n"
                    + e.getMessage()
            );

            e.printStackTrace();

        }

    }

    private void gerarRelatorio() {

        try {

            String campoData;

            if (jRadioButtonAdmissao.isSelected()) {

                campoData = "ADMISSAO";

            } else if (jRadioButtonDemissao.isSelected()) {

                campoData = "DEMISSAO";

                if (jComboBoxStatus.getSelectedItem().toString().equals("ATIVO")) {

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Funcionários ATIVOS não possuem data de demissão."
                    );

                    return;

                }

            } else {

                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Selecione Admissão ou Demissão."
                );

                return;

            }

            String pastaDestino = "F:\\Lavanderia\\Backup\\Relatorios";

            File pasta = new File(pastaDestino);

            if (!pasta.exists()) {

                pasta.mkdirs();

            }

            java.text.SimpleDateFormat sdfArquivo
                    = new java.text.SimpleDateFormat("HHmmss");

            String nomeArquivo
                    = "Relatorio_funcionarios_"
                    + sdfArquivo.format(new java.util.Date())
                    + ".pdf";

            File arquivo
                    = new File(pastaDestino + "\\" + nomeArquivo);

            Document documento = new Document(PageSize.A4);

            try ( FileOutputStream fos = new FileOutputStream(arquivo)) {

                PdfWriter.getInstance(documento, fos);

                documento.open();

                Font fonteTitulo
                        = new Font(Font.HELVETICA, 16, Font.BOLD);

                Font fonteCabecalho
                        = new Font(Font.HELVETICA, 10, Font.BOLD);

                Font fonteDados
                        = new Font(Font.HELVETICA, 9, Font.NORMAL);

                String empresa
                        = jComboBoxEmpresa.getSelectedItem().toString();

                boolean todasEmpresas
                        = empresa.equals("TODAS");

                String empresaTitulo
                        = todasEmpresas
                                ? "TODAS"
                                : empresa;

                String status
                        = jComboBoxStatus.getSelectedItem().toString();

                java.sql.Date dataInicial
                        = new java.sql.Date(
                                jDateChooserDataInicial.getDate().getTime()
                        );

                java.sql.Date dataFinal
                        = new java.sql.Date(
                                jDateChooserDataFinal.getDate().getTime()
                        );

                java.text.SimpleDateFormat sdfBR
                        = new java.text.SimpleDateFormat("dd/MM/yyyy");

                String tipoRelatorio
                        = campoData.equals("ADMISSAO")
                        ? "Admissões"
                        : "Demissões";

                Paragraph titulo
                        = new Paragraph(
                                "Relatório de " + tipoRelatorio
                                + "\nEmpresa: "
                                + empresaTitulo
                                + " | Status: "
                                + status
                                + "\nPeríodo: "
                                + sdfBR.format(dataInicial)
                                + " até "
                                + sdfBR.format(dataFinal),
                                fonteTitulo
                        );

                titulo.setAlignment(Element.ALIGN_CENTER);

                titulo.setSpacingAfter(15);

                documento.add(titulo);

                PdfPTable tabela
                        = new PdfPTable(
                                todasEmpresas ? 5 : 4
                        );

                tabela.setWidthPercentage(100);

                if (todasEmpresas) {

                    tabela.setWidths(
                            new float[]{1, 4, 3, 2, 2}
                    );

                } else {

                    tabela.setWidths(
                            new float[]{1, 5, 2, 2}
                    );

                }

                tabela.setHeaderRows(1);

                String[] colunas;

                if (todasEmpresas) {

                    colunas = new String[]{
                        "ID",
                        "NOME",
                        "EMPRESA",
                        campoData,
                        "STATUS"
                    };

                } else {

                    colunas = new String[]{
                        "ID",
                        "NOME",
                        campoData,
                        "STATUS"
                    };

                }

                for (String coluna : colunas) {

                    PdfPCell cell
                            = new PdfPCell(
                                    new Phrase(coluna, fonteCabecalho)
                            );

                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);

                    cell.setBorder(PdfPCell.BOTTOM);

                    tabela.addCell(cell);

                }

                String sql;

                if (todasEmpresas) {

                    sql
                            = "SELECT \"ID\", \"NOME\", \"EMPRESA\", \"" + campoData + "\", \"STATUS\" "
                            + "FROM \"Funcionarios\" f "
                            + "WHERE \"STATUS\" = ? "
                            + "AND \"" + campoData + "\" BETWEEN ? AND ? "
                            + "ORDER BY \"EMPRESA\", \"" + campoData + "\", \"NOME\"";

                } else {

                    sql
                            = "SELECT \"ID\", \"NOME\", \"EMPRESA\", \"" + campoData + "\", \"STATUS\" "
                            + "FROM \"Funcionarios\" f "
                            + "WHERE \"EMPRESA\" = ? "
                            + "AND \"STATUS\" = ? "
                            + "AND \"" + campoData + "\" BETWEEN ? AND ? "
                            + "ORDER BY \"" + campoData + "\", \"NOME\"";

                }

                try (
                         java.sql.Connection conn
                        = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst
                        = conn.prepareStatement(sql)) {

                    if (todasEmpresas) {

                        pst.setString(1, status);
                        pst.setDate(2, dataInicial);
                        pst.setDate(3, dataFinal);

                    } else {

                        pst.setString(1, empresa);
                        pst.setString(2, status);
                        pst.setDate(3, dataInicial);
                        pst.setDate(4, dataFinal);

                    }

                    try ( java.sql.ResultSet rs = pst.executeQuery()) {

                        int total = 0;

                        // CRIA CSV UTF-8
                        java.io.OutputStreamWriter csvWriter
                                = new java.io.OutputStreamWriter(
                                        new java.io.FileOutputStream(
                                                pastaDestino + "\\import.csv"
                                        ),
                                        java.nio.charset.StandardCharsets.UTF_8
                                );

                        // CABEÇALHO CSV
                        if (todasEmpresas) {

                            csvWriter.write(
                                    "ID;NOME;EMPRESA;"
                                    + campoData
                                    + ";STATUS\n"
                            );

                        } else {

                            csvWriter.write(
                                    "ID;NOME;"
                                    + campoData
                                    + ";STATUS\n"
                            );

                        }

                        while (rs.next()) {

                            PdfPCell cellID
                                    = new PdfPCell(
                                            new Phrase(
                                                    String.valueOf(
                                                            rs.getInt("ID")
                                                    ),
                                                    fonteDados
                                            )
                                    );

                            cellID.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellID);

                            PdfPCell cellNome
                                    = new PdfPCell(
                                            new Phrase(
                                                    rs.getString("NOME"),
                                                    fonteDados
                                            )
                                    );

                            cellNome.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellNome);

                            if (todasEmpresas) {

                                PdfPCell cellEmpresa
                                        = new PdfPCell(
                                                new Phrase(
                                                        rs.getString("EMPRESA"),
                                                        fonteDados
                                                )
                                        );

                                cellEmpresa.setBorder(PdfPCell.NO_BORDER);

                                tabela.addCell(cellEmpresa);

                            }

                            PdfPCell cellData
                                    = new PdfPCell(
                                            new Phrase(
                                                    sdfBR.format(
                                                            rs.getDate(campoData)
                                                    ),
                                                    fonteDados
                                            )
                                    );

                            cellData.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellData);

                            PdfPCell cellStatus
                                    = new PdfPCell(
                                            new Phrase(
                                                    rs.getString("STATUS"),
                                                    fonteDados
                                            )
                                    );

                            cellStatus.setBorder(PdfPCell.NO_BORDER);

                            tabela.addCell(cellStatus);

                            // ESCREVE CSV
                            if (todasEmpresas) {

                                csvWriter.write(
                                        rs.getInt("ID") + ";"
                                        + rs.getString("NOME") + ";"
                                        + rs.getString("EMPRESA") + ";"
                                        + sdfBR.format(rs.getDate(campoData)) + ";"
                                        + rs.getString("STATUS") + "\n"
                                );

                            } else {

                                csvWriter.write(
                                        rs.getInt("ID") + ";"
                                        + rs.getString("NOME") + ";"
                                        + sdfBR.format(rs.getDate(campoData)) + ";"
                                        + rs.getString("STATUS") + "\n"
                                );

                            }

                            total++;

                        }

                        csvWriter.flush();
                        csvWriter.close();

                        documento.add(tabela);

                        Paragraph rodape
                                = new Paragraph(
                                        "\nTotal de registros: "
                                        + total,
                                        fonteCabecalho
                                );

                        rodape.setAlignment(Element.ALIGN_RIGHT);

                        documento.add(rodape);

                    }

                }

                documento.close();

            }

            java.awt.Desktop.getDesktop().open(arquivo);

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Erro ao gerar relatório:\n"
                    + e.getMessage()
            );

            e.printStackTrace();

        }

    }

    private void aplicarPermissoes() {

        if (nivelUsuario != 0) {

            jButtonRelatEspecial.setVisible(false);

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

        jDateChooserDataFinal = new com.toedter.calendar.JDateChooser();
        jDateChooserDataInicial = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxEmpresa = new javax.swing.JComboBox<>();
        jComboBoxStatus = new javax.swing.JComboBox<>();
        jButtonGerarRelat = new javax.swing.JButton();
        jRadioButtonAdmissao = new javax.swing.JRadioButton();
        jRadioButtonDemissao = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButtonRelatEspecial = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Relatorio de Funcionários");

        jLabel1.setText("Data Final");

        jLabel2.setText("Data Inicial");

        jComboBoxEmpresa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBoxStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButtonGerarRelat.setText("Gerar");
        jButtonGerarRelat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGerarRelatActionPerformed(evt);
            }
        });

        jRadioButtonAdmissao.setText("Admissao");
        jRadioButtonAdmissao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAdmissaoActionPerformed(evt);
            }
        });

        jRadioButtonDemissao.setText("Demissão");
        jRadioButtonDemissao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonDemissaoActionPerformed(evt);
            }
        });

        jLabel3.setText("Status");

        jLabel4.setText("Empresa");

        jButtonRelatEspecial.setText("Relat. Especial");
        jButtonRelatEspecial.setToolTipText("Relatorio Auxiliar para Lavanderia - ignora funcionario se ATIVO e DEMITDO no periodo selecionado (independe de empresa.) ");
        jButtonRelatEspecial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRelatEspecialActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonRelatEspecial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonGerarRelat, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBoxEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jRadioButtonAdmissao)
                                .addGap(35, 35, 35)
                                .addComponent(jRadioButtonDemissao))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jDateChooserDataInicial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jDateChooserDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(0, 20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonAdmissao)
                    .addComponent(jRadioButtonDemissao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBoxEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDateChooserDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDateChooserDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonGerarRelat, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonRelatEspecial))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonGerarRelatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGerarRelatActionPerformed
        if (!validarCampos()) {
            return;
        }
        gerarRelatorio();

    }//GEN-LAST:event_jButtonGerarRelatActionPerformed

    private void jRadioButtonAdmissaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAdmissaoActionPerformed
        jRadioButtonDemissao.setSelected(false);
    }//GEN-LAST:event_jRadioButtonAdmissaoActionPerformed

    private void jRadioButtonDemissaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonDemissaoActionPerformed
        jRadioButtonAdmissao.setSelected(false);
    }//GEN-LAST:event_jRadioButtonDemissaoActionPerformed

    private void jButtonRelatEspecialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRelatEspecialActionPerformed
        if (!validarCampos()) {
            return;
        }
        gerarRelatorioModificado();
    }//GEN-LAST:event_jButtonRelatEspecialActionPerformed

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
            java.util.logging.Logger.getLogger(Relatorio001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Relatorio001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Relatorio001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Relatorio001.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Relatorio001().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonGerarRelat;
    private javax.swing.JButton jButtonRelatEspecial;
    private javax.swing.JComboBox<String> jComboBoxEmpresa;
    private javax.swing.JComboBox<String> jComboBoxStatus;
    private com.toedter.calendar.JDateChooser jDateChooserDataFinal;
    private com.toedter.calendar.JDateChooser jDateChooserDataInicial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButtonAdmissao;
    private javax.swing.JRadioButton jRadioButtonDemissao;
    // End of variables declaration//GEN-END:variables
}
