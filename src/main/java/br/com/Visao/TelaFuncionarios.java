/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Visao;

import br.com.Classes.Relatorios.Relatorio001;
import br.com.Conexao.ConexaoPostgres;

import br.com.Utilidades.FuncionarioNotificacao;
import br.com.Utilidades.NotificacaoServices;
import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Phrase;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import br.com.Utilidades.UppercaseDocumentFilter;
import java.awt.Point;
import br.com.Visao.TelaNotificaçõesAnaliticas;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import br.com.Graficos.PainelGraficoEmpresas;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.Duration;
import java.util.Random;
//import javax.swing.JInternalFrame;

/**
 *
 * @author
 */
public class TelaFuncionarios extends javax.swing.JFrame {

    private byte[] fotoBytes = null;
    private int nivelUsuario;
    private TelaNotificacoes telaNotificacoes;
    private PainelGraficoEmpresas painelGraficoDireto;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    /**
     * Creates new form TelaFuncionarios
     */
    public TelaFuncionarios() {
        initComponents();
        jLabel24.setVisible(false);
        jLabelNivel.setVisible(false);
        jLabelBanco.setVisible(false);
        SwingUtilities.invokeLater(() -> {

            NotificacaoServices.iniciarAtualizacao();

        });
        telaNotificacoes = new TelaNotificacoes(null, false);

        aplicarMaiusculas(this);

        jCheckBoxEmProcesso.setVisible(false);

        SwingUtilities.invokeLater(() -> {

            iniciarAgendamentoBackup();

        });
        setLocationRelativeTo(null);

        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        jRadioButtonAlterar.setSelected(true);
        jTableFuncionario.setDefaultEditor(Object.class, null);

        SwingUtilities.invokeLater(() -> {

            try {

                carregarCombos();

            } catch (Exception e) {

                JOptionPane.showMessageDialog(this,
                        "Erro ao conectar com banco PostgreSQL:\n" + e.getMessage());

            }

        });

        jTextFieldNome.addActionListener(e -> {

            String texto = jTextFieldNome.getText().trim();

            if (texto.isEmpty()) {

                if (jCheckBoxEmProcesso.isSelected()) {

                    buscarPorNome(texto);

                } else {

                    JOptionPane.showMessageDialog(this, "Digite algum texto para pesquisar");
                    return;

                }

            } else {

                // CONTÉM SOMENTE NÚMEROS
                if (texto.matches("\\d+")) {

                    buscarPorCPF(texto);

                } // CONTÉM ALGUM NÚMERO E ALGUM CARACTERE INVÁLIDO
                else if (texto.matches(".*\\d.*")) {

                    JOptionPane.showMessageDialog(this,
                            "Para pesquisar CPF, digite apenas números, sem pontos, hífen ou outros caracteres.");
                    return;

                } // TEXTO
                else {

                    buscarPorNome(texto);

                }

            }

            limparFormulario();

            //tabelaBloqueada = false;
            jTableFuncionario.setEnabled(true);

            if (jRadioButtonNovo.isSelected()) {
                //habilitarFormulario(false);
            }

        });
        atualizarStatusBanco();
        

    }

    TelaFuncionarios(String usuario, String nivel) {
        this();
        jLabelUsuario.setText(usuario);
        jLabelNivel.setText(nivel);
        this.nivelUsuario = Integer.parseInt(nivel);
        
        aplicarPermissoes();

    }

    private void aplicarPermissoes() {

        switch (nivelUsuario) {

            case 0:
                // PROGRMADOR → acesso de programador
                jMenuIUsuarios.setEnabled(true);
                jLabelBanco.setVisible(true);
                break;
            case 1:
                // ADMIN → acesso total
                jMenuIUsuarios.setEnabled(true);
                break;

            case 2:
                jRadioButtonExcluir.setEnabled(false);
                break;

            case 3:
                // bloqueia novo cadastro
                jRadioButtonExcluir.setEnabled(false);
                jRadioButtonNovo.setEnabled(false);
                jCheckBoxGrafico.setEnabled(false);
                jTextAreaMotivo.setVisible(false);
                jButtonCadEmpresa.setEnabled(false);
                jButtonCadSetor.setEnabled(false);
                jButtonAfastados.setEnabled(false);
                jButtonCadFuncao.setEnabled(false);

                break;

            case 4:
                // somente consulta
                jRadioButtonExcluir.setEnabled(false);
                jRadioButtonNovo.setEnabled(false);
                jRadioButtonAlterar.setEnabled(false);
                Salvar.setEnabled(false);
                jCheckBoxGrafico.setEnabled(false);
                jTextAreaMotivo.setVisible(false);
                jButtonCadEmpresa.setEnabled(false);
                jButtonCadSetor.setEnabled(false);
                jButtonAfastados.setEnabled(false);
                jButtonCadFuncao.setEnabled(false);

                break;
            case 5:
                // somente consulta
                jRadioButtonExcluir.setEnabled(false);
                jRadioButtonNovo.setEnabled(false);
                jRadioButtonAlterar.setEnabled(false);
                Salvar.setEnabled(false);
                jCheckBoxGrafico.setEnabled(false);
                jTextAreaMotivo.setVisible(false);
                jButtonCadEmpresa.setEnabled(false);
                jButtonCadSetor.setEnabled(false);
                jButtonAfastados.setEnabled(false);
                jButtonCadFuncao.setEnabled(false);

                break;
        }
    }

    private void selecionarFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar Foto");

        int resultado = chooser.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();

            try {
                FileInputStream fis = new FileInputStream(arquivo);
                fotoBytes = fis.readAllBytes();

                ImageIcon imagem = new ImageIcon(fotoBytes);

                Image img = imagem.getImage().getScaledInstance(
                        lblFoto.getWidth(),
                        lblFoto.getHeight(),
                        Image.SCALE_SMOOTH
                );

                lblFoto.setIcon(new ImageIcon(img));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String gerarUsuarioDataHora() {

        String usuario = jLabelUsuario.getText();

        DateTimeFormatter formato
                = DateTimeFormatter.ofPattern("dd-MM-yyyy - HH:mm:ss");

        String dataHora = LocalDateTime.now().format(formato);

        return usuario + " - " + dataHora;
    }

    private void atualizarQtdEncontrados(int quantidade) {

        if (quantidade == 1) {

            jLabelQtdEncontrados.setText("1 funcionário encontrado");

        } else {

            jLabelQtdEncontrados.setText(quantidade + " colaboradores encontrados.");

        }

    }

    private void carregarCombos() {

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.Statement stmt = conn.createStatement()) {

            // limpar combos
            jComboBoxEmpresa.removeAllItems();
            jComboBoxSetor.removeAllItems();
            jComboBoxFunção.removeAllItems();
            jComboBoxStatus.removeAllItems();
            jComboBoxSexo.removeAllItems();
            jComboBoxEstadoCivil.removeAllItems();

            // primeira opção
            jComboBoxEmpresa.addItem("Selecione...");
            jComboBoxSetor.addItem("Selecione...");
            jComboBoxFunção.addItem("Selecione...");
            jComboBoxStatus.addItem("Selecione...");
            jComboBoxSexo.addItem("Selecione...");
            jComboBoxEstadoCivil.addItem("Selecione...");

            // ================= EMPRESA =================
            try ( java.sql.ResultSet rsEmpresa = stmt.executeQuery(
                    "SELECT \"NOME_FANTASIA\" FROM \"Empresa\" ORDER BY \"NOME_FANTASIA\"")) {

                while (rsEmpresa.next()) {

                    jComboBoxEmpresa.addItem(rsEmpresa.getString("NOME_FANTASIA"));

                }
            }

            // ================= SETOR =================
            try ( java.sql.ResultSet rsSetor = stmt.executeQuery(
                    "SELECT \"NOME_SETOR\" FROM \"Setor\" ORDER BY \"NOME_SETOR\"")) {

                while (rsSetor.next()) {

                    jComboBoxSetor.addItem(rsSetor.getString("NOME_SETOR"));

                }
            }

            // ================= FUNÇÃO =================
            try ( java.sql.ResultSet rsFuncao = stmt.executeQuery(
                    "SELECT \"NOME_FUNCAO\" FROM \"Funcao\" ORDER BY \"NOME_FUNCAO\"")) {

                while (rsFuncao.next()) {

                    jComboBoxFunção.addItem(rsFuncao.getString("NOME_FUNCAO"));

                }
            }

            // ================= STATUS =================
            jComboBoxStatus.addItem("ATIVO");
            jComboBoxStatus.addItem("DEMITIDO");
            jComboBoxStatus.addItem("EM PROCESSO");
            jComboBoxStatus.addItem("CANCELADO");
            jComboBoxStatus.addItem("AFASTADO");

            // ================= SEXO =================
            jComboBoxSexo.addItem("MASCULINO");
            jComboBoxSexo.addItem("FEMININO");

            // ================= ESTADO CIVIL =================
            jComboBoxEstadoCivil.addItem("CASADO(A)");
            jComboBoxEstadoCivil.addItem("DIVORCIADO(A)");
            jComboBoxEstadoCivil.addItem("SEPARADA(A)");
            jComboBoxEstadoCivil.addItem("SOLTEIRO(A)");
            jComboBoxEstadoCivil.addItem("UNIÃO ESTAVEL");
            jComboBoxEstadoCivil.addItem("UNIÃO ESTÁVEL");
            jComboBoxEstadoCivil.addItem("VIUVO(A)");

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao carregar combos: " + e.getMessage());

        }
    }

    private void preencherFormulario() {

        int linha = jTableFuncionario.getSelectedRow();

        if (linha == -1) {

            return;

        }

        int id = Integer.parseInt(
                jTableFuncionario.getValueAt(linha, 0).toString()
        );

        String sql = "SELECT "
                + "\"ID\",\"EMPRESA\",\"NOME\",\"SETOR\",\"FUNCAO\","
                + "\"STATUS\",\"SEXO\",\"ESTADO CIVIL\",\"NASCIMENTO\","
                + "\"ADMISSAO\",\"DEMISSAO\",\"MAE\",\"DDD\",\"CELULAR\","
                + "\"ENDERECO\",\"CPF\",\"RG\",\"PIS\",\"CTPS\","
                + "\"MOTIVO\",\"AQUISITIVO\",\"USUARIO\","
                + "TO_CHAR(\"CADASTRO\", 'DD/MM/YYYY HH24:MI:SS') AS \"CADASTRO\" "
                + "FROM \"Funcionarios\" "
                + "WHERE \"ID\" = ?";

        try ( java.sql.Connection conn
                = br.com.Conexao.ConexaoPostgres.conectar()) {

            // 🔧 OTIMIZAÇÃO MULTIUSUÁRIO
            conn.setAutoCommit(false);

            try ( java.sql.PreparedStatement ps
                    = conn.prepareStatement(sql)) {

                ps.setInt(1, id);

                try ( java.sql.ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        jTextFieldID.setText(rs.getString("ID"));
                        jTextFieldNomeFixo.setText(rs.getString("NOME"));
                        jLabelNomeDestaque.setText(jTextFieldNomeFixo.getText());

                        jComboBoxEmpresa.setSelectedItem(
                                rs.getString("EMPRESA"));

                        jComboBoxSetor.setSelectedItem(
                                rs.getString("SETOR"));

                        jComboBoxFunção.setSelectedItem(
                                rs.getString("FUNCAO"));

                        jComboBoxStatus.setSelectedItem(
                                rs.getString("STATUS"));
                        atualizarCorStatus();

                        jComboBoxSexo.setSelectedItem(
                                rs.getString("SEXO"));

                        jComboBoxEstadoCivil.setSelectedItem(
                                rs.getString("ESTADO CIVIL"));

                        jTextFieldNascimento.setText(
                                rs.getString("NASCIMENTO"));

                        java.text.SimpleDateFormat sdf
                                = new java.text.SimpleDateFormat("dd/MM/yyyy");

                        java.sql.Date admissao = rs.getDate("ADMISSAO");
                        jTextFieldAdmissão.setText(
                                admissao != null ? sdf.format(admissao) : "");

                        java.sql.Date demissao = rs.getDate("DEMISSAO");
                        jTextFieldDemissão.setText(
                                demissao != null ? sdf.format(demissao) : "");

                        jTextFieldMãe.setText(
                                rs.getString("MAE"));

                        jTextFieldDDD.setText(
                                rs.getString("DDD"));

                        jTextFieldCelular.setText(
                                rs.getString("CELULAR"));

                        jTextAreaEndereço.setText(
                                rs.getString("ENDERECO"));

                        jTextFieldCPF.setText(
                                rs.getString("CPF"));

                        jTextFieldRG.setText(
                                rs.getString("RG"));

                        jTextFieldPIS.setText(
                                rs.getString("PIS"));

                        jTextFieldCTPS.setText(
                                rs.getString("CTPS"));

                        jTextAreaMotivo.setText(rs.getString("MOTIVO"));

                        jTextFieldAquisitivo.setText(
                                rs.getString("AQUISITIVO"));

                        String usuario = rs.getString("USUARIO");
                        if (usuario == null || usuario.isEmpty()) {
                            jLabelModificado.setText("Funcionário sem registro de ultima alteração.");
                        } else {
                            jLabelModificado.setText("Ultima Alteração: " + usuario);
                        }
                        
                        
                        jLabelDataCadastro.setText(rs.getString("CADASTRO"));
                        
                        
                    }

                }

            }

            // 🔧 FINALIZA TRANSAÇÃO
            conn.commit();

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao preencher formulário: "
                    + e.getMessage());

        }

    }

    private void carregarFotoFuncionario() {

        if (jTextFieldID.getText().isEmpty()) {
            return;
        }

        String sql = "SELECT \"FOTO\" FROM \"Funcionarios\" WHERE \"ID\" = ?";

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(jTextFieldID.getText()));

            try ( java.sql.ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    byte[] fotoBanco = rs.getBytes("FOTO");

                    if (fotoBanco != null && fotoBanco.length > 0) {

                        fotoBytes = fotoBanco;

                        javax.swing.ImageIcon imagem = new javax.swing.ImageIcon(fotoBanco);

                        java.awt.Image img = imagem.getImage().getScaledInstance(
                                lblFoto.getWidth(),
                                lblFoto.getHeight(),
                                java.awt.Image.SCALE_SMOOTH
                        );

                        lblFoto.setIcon(new javax.swing.ImageIcon(img));

                    } else {

                        fotoBytes = null;
                        lblFoto.setIcon(null);

                        javax.swing.JOptionPane.showMessageDialog(
                                this,
                                "Funcionário sem foto cadastrada."
                        );
                    }

                }

            }

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Erro ao carregar foto: " + e.getMessage()
            );

        }
    }

    private void buscarPorNome(String nome) {

        String sql;

        // CAMPO VAZIO E CHECKBOX DESMARCADO → NÃO FAZ NADA
        if (nome.trim().isEmpty() && !jCheckBoxEmProcesso.isSelected()) {
            return;
        }

        // SQL BASE
        sql = "SELECT \"ID\",\"EMPRESA\",\"NOME\",\"SETOR\",\"ADMISSAO\",\"DEMISSAO\",\"STATUS\" "
                + "FROM \"Funcionarios\" WHERE 1=1 ";

        // FILTRO STATUS = EM PROCESSO
        if (jCheckBoxEmProcesso.isSelected()) {
            sql += " AND \"STATUS\" = 'EM PROCESSO' ";
        }

        // FILTRO POR NOME
        if (!nome.trim().isEmpty()) {
            sql += " AND \"NOME\" ILIKE ? ";
        }

        sql += " ORDER BY \"NOME\", \"STATUS\"";

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            // DEFINE PARAMETRO SOMENTE SE HOUVER NOME
            if (!nome.trim().isEmpty()) {
                ps.setString(1, "%" + nome + "%");
            }

            try ( java.sql.ResultSet rs = ps.executeQuery()) {

                javax.swing.table.DefaultTableModel modelo
                        = new javax.swing.table.DefaultTableModel();

                modelo.addColumn("ID");
                modelo.addColumn("EMPRESA");
                modelo.addColumn("NOME");
                modelo.addColumn("STATUS");
                modelo.addColumn("SETOR");
                modelo.addColumn("ADMISSAO");
                modelo.addColumn("DEMISSAO");

                int contador = 0;

                while (rs.next()) {

                    java.text.SimpleDateFormat sdf
                            = new java.text.SimpleDateFormat("dd/MM/yyyy");

                    java.sql.Date admissao = rs.getDate("ADMISSAO");
                    java.sql.Date demissao = rs.getDate("DEMISSAO");

                    modelo.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("EMPRESA"),
                        rs.getString("NOME"),
                        rs.getString("STATUS"),
                        rs.getString("SETOR"),
                        admissao != null ? sdf.format(admissao) : "",
                        demissao != null ? sdf.format(demissao) : ""
                    });

                    contador++;

                }

                jTableFuncionario.setModel(modelo);

                jTableFuncionario.setAutoCreateRowSorter(true);
                javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                        = new javax.swing.table.TableRowSorter<>(modelo);

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

// ADMISSAO (coluna 5)
                sorter.setComparator(5, (o1, o2) -> {

                    try {

                        if (o1 == null || o1.toString().trim().isEmpty()) {
                            return -1;
                        }
                        if (o2 == null || o2.toString().trim().isEmpty()) {
                            return 1;
                        }

                        return sdf.parse(o1.toString()).compareTo(sdf.parse(o2.toString()));

                    } catch (Exception e) {

                        return 0;

                    }

                });

// DEMISSAO (coluna 6)
                sorter.setComparator(6, (o1, o2) -> {

                    try {

                        if (o1 == null || o1.toString().trim().isEmpty()) {
                            return -1;
                        }
                        if (o2 == null || o2.toString().trim().isEmpty()) {
                            return 1;
                        }

                        return sdf.parse(o1.toString()).compareTo(sdf.parse(o2.toString()));

                    } catch (Exception e) {

                        return 0;

                    }

                });

                jTableFuncionario.setRowSorter(sorter);

                // DESATIVA AUTO AJUSTE
                jTableFuncionario.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

                // AJUSTE MANUAL DAS COLUNAS
                jTableFuncionario.getColumnModel().getColumn(0).setPreferredWidth(40);
                jTableFuncionario.getColumnModel().getColumn(1).setPreferredWidth(100);
                jTableFuncionario.getColumnModel().getColumn(2).setPreferredWidth(280);
                jTableFuncionario.getColumnModel().getColumn(3).setPreferredWidth(110);
                jTableFuncionario.getColumnModel().getColumn(4).setPreferredWidth(200);
                jTableFuncionario.getColumnModel().getColumn(5).setPreferredWidth(100);
                jTableFuncionario.getColumnModel().getColumn(6).setPreferredWidth(100);

                atualizarQtdEncontrados(contador);

            }

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao buscar funcionários: " + e.getMessage());

        }

    }

    private void buscarPorCPF(String cpf) {

        String sql;

        sql = "SELECT \"ID\",\"EMPRESA\",\"NOME\",\"SETOR\",\"ADMISSAO\",\"DEMISSAO\",\"STATUS\" "
                + "FROM \"Funcionarios\" WHERE 1=1 ";

        // FILTRO STATUS = EM PROCESSO
        if (jCheckBoxEmProcesso.isSelected()) {
            sql += " AND \"STATUS\" = 'EM PROCESSO' ";
        }

        // PESQUISA POR CPF (permite pesquisar por partes)
        sql += " AND CAST(\"CPF\" AS TEXT) ILIKE ? ";

        sql += " ORDER BY \"NOME\", \"STATUS\"";

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + cpf + "%");

            try ( java.sql.ResultSet rs = ps.executeQuery()) {

                javax.swing.table.DefaultTableModel modelo
                        = new javax.swing.table.DefaultTableModel();

                modelo.addColumn("ID");
                modelo.addColumn("EMPRESA");
                modelo.addColumn("NOME");
                modelo.addColumn("STATUS");
                modelo.addColumn("SETOR");
                modelo.addColumn("ADMISSAO");
                modelo.addColumn("DEMISSAO");

                int contador = 0;

                while (rs.next()) {

                    java.text.SimpleDateFormat sdf
                            = new java.text.SimpleDateFormat("dd/MM/yyyy");

                    java.sql.Date admissao = rs.getDate("ADMISSAO");
                    java.sql.Date demissao = rs.getDate("DEMISSAO");

                    modelo.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("EMPRESA"),
                        rs.getString("NOME"),
                        rs.getString("STATUS"),
                        rs.getString("SETOR"),
                        admissao != null ? sdf.format(admissao) : "",
                        demissao != null ? sdf.format(demissao) : ""
                    });

                    contador++;
                }

                jTableFuncionario.setModel(modelo);

                jTableFuncionario.setAutoCreateRowSorter(true);

                javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                        = new javax.swing.table.TableRowSorter<>(modelo);

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

                // ADMISSÃO
                sorter.setComparator(5, (o1, o2) -> {

                    try {

                        if (o1 == null || o1.toString().trim().isEmpty()) {
                            return -1;
                        }
                        if (o2 == null || o2.toString().trim().isEmpty()) {
                            return 1;
                        }

                        return sdf.parse(o1.toString()).compareTo(sdf.parse(o2.toString()));

                    } catch (Exception e) {
                        return 0;
                    }

                });

                // DEMISSÃO
                sorter.setComparator(6, (o1, o2) -> {

                    try {

                        if (o1 == null || o1.toString().trim().isEmpty()) {
                            return -1;
                        }
                        if (o2 == null || o2.toString().trim().isEmpty()) {
                            return 1;
                        }

                        return sdf.parse(o1.toString()).compareTo(sdf.parse(o2.toString()));

                    } catch (Exception e) {
                        return 0;
                    }

                });

                jTableFuncionario.setRowSorter(sorter);

                // DESATIVA AUTO AJUSTE
                jTableFuncionario.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

                // AJUSTE DAS COLUNAS
                jTableFuncionario.getColumnModel().getColumn(0).setPreferredWidth(60);
                jTableFuncionario.getColumnModel().getColumn(1).setPreferredWidth(130);
                jTableFuncionario.getColumnModel().getColumn(2).setPreferredWidth(280);
                jTableFuncionario.getColumnModel().getColumn(3).setPreferredWidth(110);
                jTableFuncionario.getColumnModel().getColumn(4).setPreferredWidth(180);
                jTableFuncionario.getColumnModel().getColumn(5).setPreferredWidth(100);
                jTableFuncionario.getColumnModel().getColumn(6).setPreferredWidth(100);

                atualizarQtdEncontrados(contador);

            }

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao buscar CPF: " + e.getMessage());

        }

    }

    private void validarDatas() {

        validarCampoData(jTextFieldNascimento);
        validarCampoData(jTextFieldAdmissão);
        validarCampoData(jTextFieldDemissão);
        validarCampoData(jTextFieldAquisitivo);

    }

    private void validarCampoData(javax.swing.JTextField campo) {

        String texto = campo.getText();

        if (texto == null || texto.trim().isEmpty()) {

            campo.setForeground(java.awt.Color.BLACK);
            campo.setFont(campo.getFont().deriveFont(java.awt.Font.PLAIN));
            return;

        }

        java.text.SimpleDateFormat sdf
                = new java.text.SimpleDateFormat("dd/MM/yyyy");

        sdf.setLenient(false);

        try {

            sdf.parse(texto.trim());

            // DATA VÁLIDA
            campo.setForeground(java.awt.Color.BLACK);
            campo.setFont(campo.getFont().deriveFont(java.awt.Font.PLAIN));

        } catch (Exception e) {

            // DATA INVÁLIDA
            campo.setForeground(java.awt.Color.RED);
            campo.setFont(campo.getFont().deriveFont(java.awt.Font.BOLD));

        }

    }

    private void calcularIdade() {
        String dataTexto = jTextFieldNascimento.getText().trim();

        java.time.format.DateTimeFormatter formatter
                = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            java.time.LocalDate dataNascimento
                    = java.time.LocalDate.parse(dataTexto, formatter);

            java.time.LocalDate hoje = java.time.LocalDate.now();

            // se for data futura → inválida
            if (dataNascimento.isAfter(hoje)) {
                jLabelIdade.setText("Data inválida.");
                return;
            }

            java.time.Period periodo
                    = java.time.Period.between(dataNascimento, hoje);

            int anos = periodo.getYears();
            int meses = periodo.getMonths();

            jLabelIdade.setText(anos + " anos ");

        } catch (Exception e) {
            // QUALQUER erro de data → ignora e mostra mensagem
            jLabelIdade.setText("Data inválida.");
        }
    }

    private void calcularTempoCasa() {
        Object selected = jComboBoxStatus.getSelectedItem();

        if (selected == null) {
            return; // simplesmente ignora
        }

        String status = selected.toString();

        String admissaoTexto = jTextFieldAdmissão.getText().trim();
        String demissaoTexto = jTextFieldDemissão.getText().trim();

        java.time.format.DateTimeFormatter formatter
                = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            java.time.LocalDate dataAdmissao
                    = java.time.LocalDate.parse(admissaoTexto, formatter);

            java.time.LocalDate dataFinal;

            // 🔵 CASO ATIVO
            if (status.equalsIgnoreCase("Ativo")) {
                dataFinal = java.time.LocalDate.now();
            } // 🔴 CASO DEMITIDO
            else if (status.equalsIgnoreCase("Demitido")) {
                dataFinal = java.time.LocalDate.parse(demissaoTexto, formatter);
            } // qualquer outro status
            else {
                jLabelTempoCasa.setText("Status inválido, impossível calcular o tempo de casa.");
                return;
            }

            // validação: admissão não pode ser depois da data final
            if (dataAdmissao.isAfter(dataFinal)) {
                jLabelTempoCasa.setText("Data inválida, impossível calcular o tempo de casa.");
                return;
            }

            java.time.Period periodo
                    = java.time.Period.between(dataAdmissao, dataFinal);

            int anos = periodo.getYears();
            int meses = periodo.getMonths();
            int dias = periodo.getDays();

            jLabelTempoCasa.setText(
                    anos + " anos, " + meses + " meses e " + dias + " dias"
            );

        } catch (Exception e) {
            jLabelTempoCasa.setText("Data inválida, impossível calcular o tempo de casa.");
        }
    }

    private void validarCPF() {
        String cpf = jTextFieldCPF.getText().replaceAll("[^0-9]", ""); // remove pontos e traços

        boolean valido = isCPFValido(cpf);

        if (valido) {
            jTextFieldCPF.setForeground(java.awt.Color.BLACK);
            jTextFieldCPF.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        } else {
            jTextFieldCPF.setForeground(java.awt.Color.RED);
            jTextFieldCPF.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        }
    }

    private boolean isCPFValido(String cpf) {

        // precisa ter 11 dígitos
        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        // bloqueia CPFs tipo 11111111111
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        try {
            // cálculo do 1º dígito
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int dig1 = 11 - (soma % 11);
            if (dig1 >= 10) {
                dig1 = 0;
            }

            // cálculo do 2º dígito
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int dig2 = 11 - (soma % 11);
            if (dig2 >= 10) {
                dig2 = 0;
            }

            // valida dígitos finais
            return dig1 == (cpf.charAt(9) - '0')
                    && dig2 == (cpf.charAt(10) - '0');

        } catch (Exception e) {
            return false;
        }
    }

    private void limparFormulario() {

        // TEXTFIELDS
        jLabelDataCadastro.setText("");
        jTextFieldID.setText("");
        jTextFieldNomeFixo.setText("");
        jTextFieldNascimento.setText("");
        jTextFieldAdmissão.setText("");
        jTextFieldDemissão.setText("");
        jTextFieldMãe.setText("");
        jTextFieldDDD.setText("");
        jTextFieldCelular.setText("");
        jTextAreaEndereço.setText("");
        jTextFieldCPF.setText("");
        jTextFieldRG.setText("");
        jTextFieldPIS.setText("");
        jTextFieldCTPS.setText("");
        jTextAreaMotivo.setText("");
        jTextFieldAquisitivo.setText("");

        // COMBOBOXES (volta pro primeiro item)
        jComboBoxEmpresa.setSelectedIndex(0);
        jComboBoxSetor.setSelectedIndex(0);
        jComboBoxFunção.setSelectedIndex(0);
        jComboBoxStatus.setSelectedIndex(0);
        jComboBoxSexo.setSelectedIndex(0);
        jComboBoxEstadoCivil.setSelectedIndex(0);

        // LABELS
        jLabelStatus.setText("");
        jLabelIdade.setText("");
        jLabelTempoCasa.setText("");
        jLabelNomeDestaque.setText("");

        // RESET VISUAL (cores/fontes)
        resetarCampoVisual(jTextFieldNascimento);
        resetarCampoVisual(jTextFieldAdmissão);
        resetarCampoVisual(jTextFieldDemissão);
        resetarCampoVisual(jTextFieldAquisitivo);
        resetarCampoVisual(jTextFieldCPF);

        // LIMPAR FOTO
        lblFoto.setIcon(new ImageIcon(
                getClass().getResource("/imagens/Pessoa-256pix.png")
        ));
    }

    private void resetarCampoVisual(javax.swing.JTextField campo) {
        campo.setForeground(java.awt.Color.BLACK);
        campo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
    }

    private void habilitarFormulario(boolean habilitar) {

        jTextFieldDemissão.setEnabled(habilitar);
        jTextFieldAquisitivo.setEnabled(habilitar);
        jTextAreaMotivo.setEnabled(habilitar);
        jTextFieldNomeFixo.setEnabled(habilitar);
        jTextFieldID.setEnabled(habilitar);
        jComboBoxStatus.setEnabled(habilitar);
        jTextFieldCPF.setEnabled(habilitar);
        jTextFieldNascimento.setEnabled(habilitar);
        jComboBoxSexo.setEnabled(habilitar);
        jComboBoxEstadoCivil.setEnabled(habilitar);
        jTextFieldMãe.setEnabled(habilitar);
        jTextFieldRG.setEnabled(habilitar);
        jTextFieldPIS.setEnabled(habilitar);
        jTextFieldCTPS.setEnabled(habilitar);
    }

    private void filtrarPorDuploClique() {
        System.out.println("Duplo clique detectado");
        int linhaView = jTableFuncionario.getSelectedRow();

        if (linhaView == -1) {
            return;
        }

        // 🔥 CONVERSÃO CORRETA
        int linhaModel = jTableFuncionario.convertRowIndexToModel(linhaView);

        String nome = jTableFuncionario.getModel().getValueAt(linhaModel, 2).toString();

        jTextFieldNome.setText(nome);

        buscarPorNome(nome);
    }

    private void carregarComboEmpresa() {

        String sql = "SELECT \"NOME_FANTASIA\" FROM \"Empresa\" ORDER BY \"NOME_FANTASIA\"";

        jComboBoxEmpresa.removeAllItems();
        jComboBoxEmpresa.addItem("Selecione");

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql);  java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                jComboBoxEmpresa.addItem(rs.getString("NOME_FANTASIA"));
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao carregar empresas: " + e.getMessage());
        }
    }

    private void carregarComboFuncao() {

        String sql = "SELECT \"NOME_FUNCAO\" FROM \"Funcao\" ORDER BY \"NOME_FUNCAO\"";

        jComboBoxFunção.removeAllItems();
        jComboBoxFunção.addItem("Selecione");

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql);  java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                jComboBoxFunção.addItem(rs.getString("NOME_FUNCAO"));
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao carregar funções: " + e.getMessage());
        }
    }

    private void carregarComboSetor() {

        String sql = "SELECT \"NOME_SETOR\" FROM \"Setor\" ORDER BY \"NOME_SETOR\"";

        jComboBoxSetor.removeAllItems();
        jComboBoxSetor.addItem("Selecione");

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql);  java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                jComboBoxSetor.addItem(rs.getString("NOME_SETOR"));
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao carregar setores: " + e.getMessage());
        }
    }

    private void salvarNovoFuncionario() {

        String sql = "INSERT INTO \"Funcionarios\" ("
                + "\"EMPRESA\",\"NOME\",\"SETOR\",\"FUNCAO\","
                + "\"ADMISSAO\",\"AQUISITIVO\",\"NASCIMENTO\","
                + "\"MOTIVO\",\"DEMISSAO\",\"SEXO\",\"ESTADO CIVIL\","
                + "\"MAE\",\"CTPS\",\"CPF\",\"PIS\",\"RG\","
                + "\"ENDERECO\",\"DDD\",\"CELULAR\",\"STATUS\",\"USUARIO\""
                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try ( java.sql.Connection conn
                = br.com.Conexao.ConexaoPostgres.conectar()) {

            // 🔧 OTIMIZAÇÃO MULTIUSUÁRIO
            conn.setAutoCommit(false);

            try ( java.sql.PreparedStatement ps
                    = conn.prepareStatement(sql)) {

                ps.setString(1, jComboBoxEmpresa.getSelectedItem().toString());
                ps.setString(2, jTextFieldNomeFixo.getText());
                ps.setString(3, jComboBoxSetor.getSelectedItem().toString());
                ps.setString(4, jComboBoxFunção.getSelectedItem().toString());
                java.text.SimpleDateFormat sdf
                        = new java.text.SimpleDateFormat("dd/MM/yyyy");

                sdf.setLenient(false);

                // ADMISSAO
                if (!jTextFieldAdmissão.getText().trim().isEmpty()) {
                    java.util.Date dataAdm
                            = sdf.parse(jTextFieldAdmissão.getText().trim());
                    ps.setDate(5, new java.sql.Date(dataAdm.getTime()));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }

                ps.setString(6, jTextFieldAquisitivo.getText());
                ps.setString(7, jTextFieldNascimento.getText());
                ps.setString(8, jTextAreaMotivo.getText());

                // DEMISSAO
                if (!jTextFieldDemissão.getText().trim().isEmpty()) {
                    java.util.Date dataDem
                            = sdf.parse(jTextFieldDemissão.getText().trim());
                    ps.setDate(9, new java.sql.Date(dataDem.getTime()));
                } else {
                    ps.setNull(9, java.sql.Types.DATE);
                }
                ps.setString(10, jComboBoxSexo.getSelectedItem().toString());
                ps.setString(11, jComboBoxEstadoCivil.getSelectedItem().toString());
                ps.setString(12, jTextFieldMãe.getText());
                ps.setString(13, jTextFieldCTPS.getText());
                ps.setString(14, jTextFieldCPF.getText());
                ps.setString(15, jTextFieldPIS.getText());
                ps.setString(16, jTextFieldRG.getText());
                ps.setString(17, jTextAreaEndereço.getText());
                ps.setString(18, jTextFieldDDD.getText());
                ps.setString(19, jTextFieldCelular.getText());
                ps.setString(20, jComboBoxStatus.getSelectedItem().toString());

                String usuarioLog = gerarUsuarioDataHora();

                ps.setString(21, usuarioLog);

                ps.executeUpdate();

            }

            // 🔧 COMMIT CONTROLADO
            conn.commit();

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Funcionário salvo com sucesso.");
            jTextFieldNome.setText(jTextFieldNomeFixo.getText());
            limparFormulario();
            jRadioButtonNovo.setSelected(false);
            habilitarFormulario(true);
            jTableFuncionario.setEnabled(true);
            buscarPorNome(jTextFieldNome.getText());

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Erro ao salvar funcionário: " + e.getMessage());

        }

    }

    private void atualizarFuncionario() {

        String sql = "UPDATE \"Funcionarios\" SET "
                + "\"EMPRESA\"=?,"
                + "\"NOME\"=?,"
                + "\"SETOR\"=?,"
                + "\"FUNCAO\"=?,"
                + "\"STATUS\"=?,"
                + "\"SEXO\"=?,"
                + "\"NASCIMENTO\"=?,"
                + "\"ADMISSAO\"=?,"
                + "\"DEMISSAO\"=?,"
                + "\"ESTADO CIVIL\"=?,"
                + "\"MAE\"=?,"
                + "\"DDD\"=?,"
                + "\"CELULAR\"=?,"
                + "\"ENDERECO\"=?,"
                + "\"RG\"=?,"
                + "\"CPF\"=?,"
                + "\"PIS\"=?,"
                + "\"CTPS\"=?,"
                + "\"MOTIVO\"=?,"
                + "\"AQUISITIVO\"=?,"
                + "\"FOTO\"=? ,"
                + "\"USUARIO\"=? "
                + "WHERE \"ID\"=?";

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jComboBoxEmpresa.getSelectedItem().toString());
            ps.setString(2, jTextFieldNomeFixo.getText());
            ps.setString(3, jComboBoxSetor.getSelectedItem().toString());
            ps.setString(4, jComboBoxFunção.getSelectedItem().toString());
            ps.setString(5, jComboBoxStatus.getSelectedItem().toString());
            ps.setString(6, jComboBoxSexo.getSelectedItem().toString());
            ps.setString(7, jTextFieldNascimento.getText());

            java.text.SimpleDateFormat sdf
                    = new java.text.SimpleDateFormat("dd/MM/yyyy");

            sdf.setLenient(false);

            //ADMISSAO
            if (!jTextFieldAdmissão.getText().trim().isEmpty()) {

                java.util.Date data
                        = sdf.parse(jTextFieldAdmissão.getText().trim());

                ps.setDate(8, new java.sql.Date(data.getTime()));

            } else {

                ps.setNull(8, java.sql.Types.DATE);

            }

            //DEMISSAO
            if (!jTextFieldDemissão.getText().trim().isEmpty()) {

                java.util.Date data
                        = sdf.parse(jTextFieldDemissão.getText().trim());

                ps.setDate(9, new java.sql.Date(data.getTime()));

            } else {

                ps.setNull(9, java.sql.Types.DATE);

            }

            ps.setString(10, jComboBoxEstadoCivil.getSelectedItem().toString());
            ps.setString(11, jTextFieldMãe.getText());
            ps.setString(12, jTextFieldDDD.getText());
            ps.setString(13, jTextFieldCelular.getText());
            ps.setString(14, jTextAreaEndereço.getText());
            ps.setString(15, jTextFieldRG.getText());
            ps.setString(16, jTextFieldCPF.getText());
            ps.setString(17, jTextFieldPIS.getText());
            ps.setString(18, jTextFieldCTPS.getText());
            ps.setString(19, jTextAreaMotivo.getText());

            if (jTextFieldAquisitivo.getText().trim().isEmpty()) {
                ps.setNull(20, java.sql.Types.VARCHAR);
            } else {
                ps.setString(20, jTextFieldAquisitivo.getText());
            }

            // FOTO
            if (fotoBytes != null) {
                ps.setBytes(21, fotoBytes);
            } else {
                ps.setNull(21, java.sql.Types.BINARY);
            }

            String usuarioLog = gerarUsuarioDataHora();

            ps.setString(22, usuarioLog);
            ps.setInt(23, Integer.parseInt(jTextFieldID.getText()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Funcionário atualizado com sucesso!");

            //****ATAULIZANDO SESMT
            // 📌 CAPTURA OS DADOS ANTES DE LIMPAR
            // 📌 Captura dados
            int id = Integer.parseInt(jTextFieldID.getText());
            String empresa = jComboBoxEmpresa.getSelectedItem().toString();
            String nome = jTextFieldNomeFixo.getText();
            String setor = jComboBoxSetor.getSelectedItem().toString();
            String funcao = jComboBoxFunção.getSelectedItem().toString();
            String status = jComboBoxStatus.getSelectedItem().toString();

            // 🔄 Atualiza SESMT (Armarios)
            javax.swing.JDialog dialog = criarDialogoAtualizando();

            new Thread(() -> {

                try {

                    javax.swing.SwingUtilities.invokeLater(() -> dialog.setVisible(true));

                    atualizarArmarioSESMT(id, empresa, nome, setor, funcao, status);

                } finally {

                    javax.swing.SwingUtilities.invokeLater(() -> dialog.dispose());
                }

            }).start();
            //***FIM DE ATUALIZANDO SEMST

            limparFormulario();
            buscarPorNome(jTextFieldNome.getText());
            jTableFuncionario.setEnabled(true);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + e.getMessage());

        }
    }

    private void excluirFuncionario() {

        try {

            if (jTextFieldID.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Nenhum funcionário selecionado.");

                return;
            }

            int id = Integer.parseInt(jTextFieldID.getText());

            String nome = jTextFieldNomeFixo.getText();
            String empresa = jComboBoxEmpresa.getSelectedItem().toString();
            String setor = jComboBoxSetor.getSelectedItem().toString();

            int resposta1 = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente EXCLUIR o funcionário "
                    + nome
                    + " número de ID "
                    + id
                    + " empresa "
                    + empresa
                    + " setor "
                    + setor
                    + " ?",
                    "Confirmação",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (resposta1 != JOptionPane.YES_OPTION) {
                return;
            }

            int resposta2 = JOptionPane.showConfirmDialog(
                    this,
                    "Atenção, esta operação não poderá ser desfeita. Confirma?",
                    "Confirmação Final",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (resposta2 != JOptionPane.YES_OPTION) {
                return;
            }

            String sql = "DELETE FROM \"Funcionarios\" WHERE \"ID\" = ?";

            try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);

                int registros = ps.executeUpdate();

                if (registros > 0) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Exclusão concluída com sucesso.");
                    jRadioButtonAlterar.setSelected(false);
                    jRadioButtonExcluir.setForeground(new java.awt.Color(0, 0, 0));

                    limparFormulario();

                    buscarPorNome(jTextFieldNome.getText());

                    jTableFuncionario.setEnabled(true);

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Funcionário não encontrado.");

                }
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao excluir funcionário: " + e.getMessage());

        }
    }

    //*******ATUALIZANDO SESMT ***************************
    private boolean atualizarArmarioSESMT(
            int id,
            String empresa,
            String nome,
            String setor,
            String funcao,
            String status) {

        String sqlUpdate = "UPDATE \"Armarios\" SET "
                + "\"EMPRESA\"=?, "
                + "\"NOME\"=?, "
                + "\"SETOR\"=?, "
                + "\"FUNCAO\"=?, "
                + "\"STATUS_FUNC\"=? "
                + "WHERE \"ID_FUNC\"=?";

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgreSESMT.conectar();  java.sql.PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {

            System.out.println("🔎 Atualizando Armários (SESMT) ID: " + id);

            ps.setString(1, empresa);
            ps.setString(2, nome);
            ps.setString(3, setor);
            ps.setString(4, funcao);
            ps.setString(5, status);
            ps.setInt(6, id);

            int linhas = ps.executeUpdate();

            if (linhas > 0) {
                System.out.println("✅ Atualizado com sucesso. Linhas: " + linhas);
                return true;
            } else {
                // NÃO encontrou → não faz nada (regra que você definiu)
                System.out.println("⚠ ID não encontrado nos Armários. Nenhuma ação.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private javax.swing.JDialog criarDialogoAtualizando() {

        javax.swing.JDialog dialog = new javax.swing.JDialog();
        dialog.setTitle("Aguarde");
        dialog.setModal(false);
        dialog.setSize(320, 100);
        dialog.setLocationRelativeTo(null);

        javax.swing.JLabel label = new javax.swing.JLabel(
                "Atualizando dados para SESMT...",
                javax.swing.SwingConstants.CENTER
        );

        dialog.add(label);

        return dialog;
    }
//************ FIM DE ATUALIZANDO SESMT ***************************

    private boolean validarData(javax.swing.JTextField campo, String nomeCampo, boolean obrigatorio) {

        String texto = campo.getText().trim();

        // 🔴 obrigatório vazio
        if (obrigatorio && texto.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    nomeCampo + " é obrigatório!");
            campo.requestFocus();
            return false;
        }

        // 🟡 opcional vazio → OK
        if (!obrigatorio && texto.isEmpty()) {
            return true;
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);

        try {
            sdf.parse(texto);
            return true; // válido
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    nomeCampo + " inválida! Use dd/MM/yyyy");
            campo.requestFocus();
            return false;
        }
    }

    public void backupBancoRemotoPostgres() {

        javax.swing.JDialog loading = new javax.swing.JDialog();
        loading.setTitle("Backup Banco Neon");
        loading.setModal(false);
        loading.setSize(320, 100);
        loading.setLocationRelativeTo(null);

        javax.swing.JLabel label = new javax.swing.JLabel(
                "Gerando backup do banco de dados...",
                javax.swing.SwingConstants.CENTER
        );

        loading.add(label);
        loading.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        loading.setVisible(true);

        new javax.swing.SwingWorker<Void, Void>() {

            String erroBackup = null;

            @Override
            protected Void doInBackground() {

                try {

                    // CONFIGURAÇÕES DO NEON
                    String host = "ep-blue-frost-ad6pf00o-pooler.c-2.us-east-1.aws.neon.tech";
                    String porta = "5432";
                    String banco = "bdcofril";
                    String usuario = "neondb_owner";
                    String senha = "npg_z8fDyvuC5ScI";

                    String pastaBackup = "F:\\Lavanderia\\Backup\\BackupTabelas ProjetoCofrilApp\\";
                    java.io.File dir = new java.io.File(pastaBackup);

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    String dataHora = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm")
                            .format(new java.util.Date());

                    String arquivoBackup = pastaBackup
                            + "bdcofril_" + dataHora + ".backup";

                    String pgDump = System.getProperty("user.dir")
                            + "\\postgres\\pg_dump.exe";

                    java.io.File arquivoPgDump = new java.io.File(pgDump);

                    if (!arquivoPgDump.exists()) {
                        throw new Exception(
                                "Arquivo pg_dump.exe não encontrado:\n" + pgDump
                                + "\n\nVerifique a pasta postgres da aplicação."
                        );
                    }

                    ProcessBuilder pb = new ProcessBuilder(
                            pgDump,
                            "-h", host,
                            "-p", porta,
                            "-U", usuario,
                            "-d", banco,
                            "-F", "c",
                            "-b",
                            "-v",
                            "--no-owner",
                            "--no-privileges",
                            "-f", arquivoBackup
                    );

                    // VARIÁVEIS DE AMBIENTE DO NEON
                    pb.environment().put("PGPASSWORD", senha);
                    pb.environment().put("PGSSLMODE", "require");
                    pb.environment().put("PGCHANNELBINDING", "require");
                    pb.environment().put("PGCLIENTENCODING", "UTF8");

                    pb.redirectErrorStream(true);

                    Process process = pb.start();

                    StringBuilder saida = new StringBuilder();

                    try ( java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream()))) {

                        String linha;
                        while ((linha = reader.readLine()) != null) {
                            saida.append(linha).append("\n");
                        }
                    }

                    int exitCode = process.waitFor();

                    if (exitCode != 0) {
                        erroBackup = saida.toString();
                    }

                } catch (Exception e) {
                    erroBackup = e.getMessage();
                }

                return null;
            }

            @Override
            protected void done() {

                loading.dispose();

                if (erroBackup == null) {

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Backup Banco de Dados gerado com sucesso!"
                    );
                    backupFuncionariosExcel();

                } else {

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Erro no backup:\n\n" + erroBackup
                    );
                }
            }

        }.execute();
    }

    public void backupFuncionariosExcel() {

        javax.swing.JDialog loading = new javax.swing.JDialog();
        loading.setTitle("Backup");
        loading.setModal(false);
        loading.setSize(300, 100);
        loading.setLocationRelativeTo(null);

        javax.swing.JLabel label = new javax.swing.JLabel(
                "Backup nos formatos CSV e XLSX em andamento. Aguarde...",
                javax.swing.SwingConstants.CENTER
        );

        loading.add(label);
        loading.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        loading.setVisible(true);

        new javax.swing.SwingWorker<Void, Void>() {

            private Exception erro = null;

            @Override
            protected Void doInBackground() {

                String pastaBackup
                        = "F:\\Lavanderia\\Backup\\BackupTabelas ProjetoCofrilApp\\";

                try {

                    java.io.File diretorio = new java.io.File(pastaBackup);

                    if (!diretorio.exists()) {
                        diretorio.mkdirs();
                    }

                    String dataHora = new java.text.SimpleDateFormat(
                            "dd-MM-yyyy_HH-mm"
                    ).format(new java.util.Date());

                    backupTabelaExcel("Funcionarios", pastaBackup, dataHora);
                    backupTabelaCSV("Afastados", pastaBackup, dataHora);
                    backupTabelaCSV("Documentos", pastaBackup, dataHora);
                    backupTabelaCSV("Empresa", pastaBackup, dataHora);
                    backupTabelaCSV("Funcao", pastaBackup, dataHora);
                    backupTabelaCSV("Setor", pastaBackup, dataHora);
                    backupTabelaCSV("Usuario", pastaBackup, dataHora);

                } catch (Exception e) {

                    erro = e;

                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void done() {

                loading.dispose();

                if (erro == null) {

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "Backup concluído com sucesso."
                    );

                } else {

                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);

                    erro.printStackTrace(pw);

                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            sw.toString(),
                            "Erro no Backup",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

// ================= BACKUP EXCEL =================
    private void backupTabelaExcel(String tabela, String pastaBackup, String dataHora) throws Exception {

        String sql = "SELECT * FROM \"" + tabela + "\" ORDER BY 1";

        String caminhoArquivo
                = pastaBackup + "Backup" + tabela + "_" + dataHora + ".xlsx";

        try (
                 java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement stmt = conn.prepareStatement(sql);  java.sql.ResultSet rs = stmt.executeQuery();  org.apache.poi.xssf.usermodel.XSSFWorkbook workbook
                = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.xssf.usermodel.XSSFSheet sheet
                    = workbook.createSheet(tabela);

            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // ESTILO DE DATA
            org.apache.poi.ss.usermodel.CellStyle estiloData
                    = workbook.createCellStyle();

            org.apache.poi.ss.usermodel.CreationHelper createHelper
                    = workbook.getCreationHelper();

            estiloData.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

            for (int i = 1; i <= columnCount; i++) {

                headerRow.createCell(i - 1)
                        .setCellValue(metaData.getColumnName(i));
            }

            int rowIndex = 1;

            while (rs.next()) {

                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);

                for (int i = 1; i <= columnCount; i++) {

                    String nomeColuna = metaData.getColumnName(i).toUpperCase();

                    org.apache.poi.ss.usermodel.Cell cell
                            = row.createCell(i - 1);

                    Object valor = rs.getObject(i);

                    // COLUNAS DE DATA
                    if (valor != null
                            && (nomeColuna.equals("ADMISSAO")
                            || nomeColuna.equals("AQUISITVO")
                            || nomeColuna.equals("DEMISSAO")
                            || nomeColuna.equals("NASCIMENTO"))) {

                        if (valor instanceof java.sql.Date) {

                            cell.setCellValue((java.sql.Date) valor);
                            cell.setCellStyle(estiloData);

                        } else if (valor instanceof java.sql.Timestamp) {

                            cell.setCellValue(
                                    new java.util.Date(
                                            ((java.sql.Timestamp) valor).getTime()));

                            cell.setCellStyle(estiloData);

                        } else {

                            cell.setCellValue(valor.toString());
                        }

                    } else {

                        cell.setCellValue(valor != null
                                ? valor.toString()
                                : "");
                    }
                }
            }

            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }

            try ( java.io.FileOutputStream fileOut
                    = new java.io.FileOutputStream(caminhoArquivo)) {

                workbook.write(fileOut);
            }
        }
    }

// ================= BACKUP CSV =================
    private void backupTabelaCSV(String tabela, String pastaBackup, String dataHora) throws Exception {

        String sql = "SELECT * FROM \"" + tabela + "\"";

        String caminhoArquivo
                = pastaBackup + "Backup" + tabela + "_" + dataHora + ".csv";

        try (
                 java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement stmt = conn.prepareStatement(sql);  java.sql.ResultSet rs = stmt.executeQuery();  java.io.FileWriter writer = new java.io.FileWriter(caminhoArquivo)) {

            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Cabeçalhos
            for (int i = 1; i <= columnCount; i++) {
                writer.append(metaData.getColumnName(i));
                if (i < columnCount) {
                    writer.append(";");
                }
            }
            writer.append("\n");

            // Dados
            while (rs.next()) {

                for (int i = 1; i <= columnCount; i++) {

                    Object valor = rs.getObject(i);
                    String texto = valor != null ? valor.toString() : "";

                    // Evita quebrar CSV
                    texto = texto.replace(";", ",");

                    writer.append(texto);

                    if (i < columnCount) {
                        writer.append(";");
                    }
                }

                writer.append("\n");
            }

            writer.flush();
        }
    }

    private void iniciarAgendamentoBackup() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        LocalDateTime agora = LocalDateTime.now();
        // Define o horário alvo para hoje às 16:00
        LocalDateTime proximaExecucao = agora.withHour(16).withMinute(30).withSecond(0).withNano(0);

        // Se abrir o sistema após as 16h, executa a verificação imediata
        if (agora.isAfter(proximaExecucao)) {
            solicitarBackup(); // Pergunta agora
            // Ajusta a próxima execução agendada para as 16h de amanhã
            proximaExecucao = proximaExecucao.plusDays(1);
        }

        long atrasoInicial = Duration.between(agora, proximaExecucao).toSeconds();

        // Agenda as execuções automáticas diárias
        scheduler.scheduleAtFixedRate(() -> {
            solicitarBackup();
        }, atrasoInicial, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

// Método auxiliar para a interface gráfica
    private void solicitarBackup() {
        SwingUtilities.invokeLater(() -> {
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Está na hora de fazer o bakcup.",
                    "Backup do Sistema",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (resposta == JOptionPane.YES_OPTION) {
                backupBancoRemotoPostgres();
                //backupFuncionariosExcel();
            }
        });
    }

    private void gerarRelatorioTotalizaEmpresa() {

        javax.swing.JDialog loading = new javax.swing.JDialog();

        loading.setTitle("Aguarde");

        javax.swing.JLabel mensagem
                = new javax.swing.JLabel(
                        "Gerando relatório...",
                        javax.swing.SwingConstants.CENTER
                );

        loading.add(mensagem);

        loading.setSize(250, 100);

        loading.setLocationRelativeTo(null);

        loading.setModal(false);

        loading.setVisible(true);

        new Thread(() -> {

            try {

                String pastaDestino
                        = "F:\\Lavanderia\\Backup\\Relatorios";

                File pasta = new File(pastaDestino);

                if (!pasta.exists()) {

                    pasta.mkdirs();

                }

                java.text.SimpleDateFormat sdfArquivo
                        = new java.text.SimpleDateFormat("HHmmss");

                String nomeArquivo
                        = "TotalizaEmpresa_"
                        + sdfArquivo.format(new java.util.Date())
                        + ".pdf";

                File arquivo
                        = new File(pastaDestino + "\\" + nomeArquivo);

                Document documento
                        = new Document(PageSize.A4);

                try ( FileOutputStream fos
                        = new FileOutputStream(arquivo)) {

                    PdfWriter.getInstance(documento, fos);

                    documento.open();

                    Font fonteTitulo
                            = new Font(Font.HELVETICA, 16, Font.BOLD);

                    Font fonteDados
                            = new Font(Font.HELVETICA, 10, Font.NORMAL);

                    Font fonteEmpresa
                            = new Font(Font.HELVETICA, 10, Font.BOLD);

                    Font fonteCabecalho
                            = new Font(Font.HELVETICA, 11, Font.BOLD);

                    Font fonteTotalGeral
                            = new Font(Font.HELVETICA, 12, Font.BOLD);

                    Paragraph titulo
                            = new Paragraph(
                                    "Total de Funcionários por Empresa / Setor",
                                    fonteTitulo
                            );

                    titulo.setAlignment(Element.ALIGN_CENTER);

                    titulo.setSpacingAfter(15);

                    documento.add(titulo);

                    PdfPTable tabela
                            = new PdfPTable(3);

                    tabela.setWidthPercentage(100);

                    tabela.setWidths(new float[]{3, 7, 2});

                    // CABEÇALHO
                    PdfPCell cab1
                            = new PdfPCell(new Phrase("EMPRESA", fonteCabecalho));

                    PdfPCell cab2
                            = new PdfPCell(new Phrase("SETOR", fonteCabecalho));

                    PdfPCell cab3
                            = new PdfPCell(new Phrase("QTD", fonteCabecalho));

                    cab1.setBorder(PdfPCell.BOTTOM);
                    cab2.setBorder(PdfPCell.BOTTOM);
                    cab3.setBorder(PdfPCell.BOTTOM);

                    cab3.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    tabela.addCell(cab1);
                    tabela.addCell(cab2);
                    tabela.addCell(cab3);

                    String sql
                            = "SELECT \"EMPRESA\", \"SETOR\", COUNT(*) AS TOTAL, "
                            + "SUM(COUNT(*)) OVER (PARTITION BY \"EMPRESA\") AS TOTAL_EMPRESA "
                            + "FROM \"Funcionarios\" "
                            + "WHERE \"STATUS\" = 'ATIVO' "
                            + "GROUP BY \"EMPRESA\", \"SETOR\" "
                            + "ORDER BY \"EMPRESA\", \"SETOR\"";

                    try (
                             java.sql.Connection conn
                            = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst
                            = conn.prepareStatement(sql);  java.sql.ResultSet rs
                            = pst.executeQuery();) {

                        int totalGeral = 0;

                        String empresaAtual = "";

                        boolean linhaCinza = false;

                        while (rs.next()) {

                            String empresa
                                    = rs.getString("EMPRESA");

                            String setor
                                    = rs.getString("SETOR");

                            int total
                                    = rs.getInt("TOTAL");

                            int totalEmpresa
                                    = rs.getInt("TOTAL_EMPRESA");

                            if (!empresa.equals(empresaAtual)) {

                                if (!empresaAtual.isEmpty()) {

                                    // pula duas linhas antes da próxima empresa
                                    PdfPCell espaco1
                                            = new PdfPCell(new Phrase(" "));
                                    espaco1.setColspan(3);
                                    espaco1.setBorder(PdfPCell.NO_BORDER);

                                    PdfPCell espaco2
                                            = new PdfPCell(new Phrase(" "));
                                    espaco2.setColspan(3);
                                    espaco2.setBorder(PdfPCell.NO_BORDER);

                                    tabela.addCell(espaco1);
                                    tabela.addCell(espaco2);

                                }

                                PdfPCell cellEmpresa
                                        = new PdfPCell(
                                                new Phrase(
                                                        empresa
                                                        + " ("
                                                        + totalEmpresa
                                                        + ")",
                                                        fonteEmpresa
                                                )
                                        );

                                cellEmpresa.setBorder(PdfPCell.NO_BORDER);

                                tabela.addCell(cellEmpresa);

                                empresaAtual = empresa;

                            } else {

                                PdfPCell vazio
                                        = new PdfPCell(new Phrase(""));

                                vazio.setBorder(PdfPCell.NO_BORDER);

                                tabela.addCell(vazio);

                            }

                            PdfPCell cellSetor
                                    = new PdfPCell(
                                            new Phrase(setor, fonteDados)
                                    );

                            cellSetor.setBorder(PdfPCell.NO_BORDER);

                            cellSetor.setNoWrap(true);

                            PdfPCell cellTotal
                                    = new PdfPCell(
                                            new Phrase(
                                                    String.valueOf(total),
                                                    fonteDados
                                            )
                                    );

                            cellTotal.setBorder(PdfPCell.NO_BORDER);

                            cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);

                            // alternar cinza
                            if (linhaCinza) {

                                java.awt.Color cinza
                                        = new java.awt.Color(230, 230, 230);

                                cellSetor.setBackgroundColor(cinza);

                                cellTotal.setBackgroundColor(cinza);

                            }

                            tabela.addCell(cellSetor);

                            tabela.addCell(cellTotal);

                            linhaCinza = !linhaCinza;

                            totalGeral += total;

                        }

                        documento.add(tabela);

                        Paragraph rodape
                                = new Paragraph(
                                        "\nTOTAL GERAL: "
                                        + totalGeral,
                                        fonteTotalGeral
                                );

                        rodape.setAlignment(Element.ALIGN_RIGHT);

                        documento.add(rodape);

                    }

                    documento.close();

                }

                java.awt.Desktop
                        .getDesktop()
                        .open(arquivo);

            } catch (Exception e) {

                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "Erro ao gerar relatório:\n"
                        + e.getMessage()
                );

                e.printStackTrace();

            } finally {

                javax.swing.SwingUtilities.invokeLater(
                        loading::dispose
                );

            }

        }).start();

    }

    private void atualizarCorStatus() {

        String status = (String) jComboBoxStatus.getSelectedItem();

        if (status == null) {
            return;
        }

        switch (status) {

            case "DEMITIDO":
                jLabelStatus.setText((String) jComboBoxStatus.getSelectedItem());
                jLabelStatus.setForeground(new java.awt.Color(139, 0, 0));
                break;

            case "ATIVO":
                jLabelStatus.setText((String) jComboBoxStatus.getSelectedItem());
                jLabelStatus.setForeground(new java.awt.Color(0, 128, 0)); // verde
                break;

            default:
                jLabelStatus.setText((String) jComboBoxStatus.getSelectedItem());
                jLabelStatus.setForeground(java.awt.Color.BLUE);
                break;
        }
    }

    private void aplicarMaiusculas(Container container) {

        for (Component c : container.getComponents()) {

            if (c instanceof JTextField campo) {

                ((AbstractDocument) campo.getDocument())
                        .setDocumentFilter(new UppercaseDocumentFilter());

            } else if (c instanceof Container cont) {

                aplicarMaiusculas(cont);

            }
        }

    }

    
    private void exibirGraficoNoDesktopPane() {
        if (painelGraficoDireto == null) {
            // Instancia o painel existente no pacote br.com.Graficos
            painelGraficoDireto = new PainelGraficoEmpresas();

            // Faz o gráfico preencher toda a área disponível no jDesktopPane1
            painelGraficoDireto.setBounds(0, 0, jDesktopPane1.getWidth(), jDesktopPane1.getHeight());

            // Adiciona o painel do gráfico diretamente na camada superior do jDesktopPane1
            jDesktopPane1.add(painelGraficoDireto, javax.swing.JLayeredPane.DEFAULT_LAYER);
        }

        painelGraficoDireto.setVisible(true);
        jDesktopPane1.revalidate();
        jDesktopPane1.repaint();
    }

    /**
     * Método responsável por ocultar e remover o painel do gráfico do
     * jDesktopPane1.
     */
    private void removerGraficoDoDesktopPane() {
        if (painelGraficoDireto != null) {
            jDesktopPane1.remove(painelGraficoDireto);
            painelGraficoDireto = null; // Libera a referência para recarregar atualizado na próxima vez
            jDesktopPane1.revalidate();
            jDesktopPane1.repaint();
        }
    }

    /**
     * Método de controle acionado pelo jCheckBoxGrafico.
     */
    private void controlarExibicaoGraficoDireto() {
        if (jCheckBoxGrafico.isSelected()) {
            exibirGraficoNoDesktopPane();
        } else {
            removerGraficoDoDesktopPane();
        }
    }
    
    // Adicione a palavra 'final' aqui
public final void atualizarStatusBanco() {
    String ambiente = ConexaoPostgres.getTipoBanco();
    
    java.awt.EventQueue.invokeLater(() -> {
        jLabelBanco.setText(ambiente);
        
        if ("Banco de Teste".equals(ambiente)) {
            jLabelBanco.setForeground(new java.awt.Color(204, 102, 0)); // Laranja
        } else {
            jLabelBanco.setForeground(new java.awt.Color(255,255,0));   // Verde
        }
    });
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupFuncao = new javax.swing.ButtonGroup();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jInternalFrameTabela = new javax.swing.JInternalFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFuncionario = new javax.swing.JTable();
        jInternalFrameColaborador = new javax.swing.JInternalFrame();
        jPanel1 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldID = new javax.swing.JTextField();
        jTextFieldNomeFixo = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jComboBoxSexo = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jTextFieldNascimento = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jComboBoxEstadoCivil = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jTextFieldMãe = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldDDD = new javax.swing.JTextField();
        jTextFieldCelular = new javax.swing.JTextField();
        lblFoto = new javax.swing.JLabel();
        btnSelecionarFoto = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextAreaEndereço = new javax.swing.JTextArea();
        jLabelStatus = new javax.swing.JLabel();
        jLabelDataCadastro = new javax.swing.JLabel();
        jInternalFrameDocPessoais = new javax.swing.JInternalFrame();
        jPanel2 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldRG = new javax.swing.JTextField();
        jTextFieldCPF = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldPIS = new javax.swing.JTextField();
        jTextFieldCTPS = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jInternalFrameEmpresa = new javax.swing.JInternalFrame();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxEmpresa = new javax.swing.JComboBox<>();
        jButtonCadEmpresa = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxSetor = new javax.swing.JComboBox<>();
        jButtonCadSetor = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldAdmissão = new javax.swing.JTextField();
        jTextFieldAquisitivo = new javax.swing.JTextField();
        jTextFieldDemissão = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaMotivo = new javax.swing.JTextArea();
        jLabel22 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButtonAfastados = new javax.swing.JButton();
        jComboBoxStatus = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jComboBoxFunção = new javax.swing.JComboBox<>();
        jLabelTempoCasa = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jButtonCadFuncao = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jRadioButtonNovo = new javax.swing.JRadioButton();
        jRadioButtonAlterar = new javax.swing.JRadioButton();
        jRadioButtonExcluir = new javax.swing.JRadioButton();
        Salvar = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jTextFieldNome = new javax.swing.JTextField();
        jToggleButtonEmProcesso = new javax.swing.JToggleButton();
        jLabelSino = new javax.swing.JLabel();
        jLabelIdade = new javax.swing.JLabel();
        jCheckBoxEmProcesso = new javax.swing.JCheckBox();
        jLabelNomeDestaque = new javax.swing.JLabel();
        jLabelBanco = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabelUsuario = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabelNivel = new javax.swing.JLabel();
        jLabelModificado = new javax.swing.JLabel();
        jLabelQtdEncontrados = new javax.swing.JLabel();
        jCheckBoxFrameColaborador = new javax.swing.JCheckBox();
        jCheckBoxFrameDocPessoais = new javax.swing.JCheckBox();
        jCheckBoxFrameEmpresa = new javax.swing.JCheckBox();
        jCheckBoxFrameTabela = new javax.swing.JCheckBox();
        jCheckBoxNomeDestaque = new javax.swing.JCheckBox();
        jCheckBoxGrafico = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuCadastros = new javax.swing.JMenu();
        jMenuIUsuarios = new javax.swing.JMenuItem();
        jMenuDocumentos = new javax.swing.JMenu();
        jMenuItemPastaFunc = new javax.swing.JMenuItem();
        jMenuIRelaDocs = new javax.swing.JMenuItem();
        jMenuItemAdvertencia = new javax.swing.JMenuItem();
        jMenuUtilitarios = new javax.swing.JMenu();
        jMenuItemAniversariantes = new javax.swing.JMenuItem();
        jMenuItemExperiencia = new javax.swing.JMenuItem();
        jMenuItemEmProcesso = new javax.swing.JMenuItem();
        jMenuItemBackUp = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Controle de RH - ABAV - Versão 1.0.0");
        setResizable(false);

        jInternalFrameTabela.setClosable(true);
        jInternalFrameTabela.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        jInternalFrameTabela.setIconifiable(true);
        jInternalFrameTabela.setResizable(true);
        jInternalFrameTabela.setTitle("Colaboradores");
        jInternalFrameTabela.setVisible(true);
        jInternalFrameTabela.addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                jInternalFrameTabelaInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jTableFuncionario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTableFuncionario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableFuncionarioMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableFuncionario);

        jInternalFrameTabela.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jDesktopPane1.add(jInternalFrameTabela);
        jInternalFrameTabela.setBounds(390, 400, 950, 210);

        jInternalFrameColaborador.setBackground(new java.awt.Color(255, 255, 255));
        jInternalFrameColaborador.setClosable(true);
        jInternalFrameColaborador.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        jInternalFrameColaborador.setIconifiable(true);
        jInternalFrameColaborador.setTitle("Dados do Colaborador");
        jInternalFrameColaborador.setVisible(true);
        jInternalFrameColaborador.addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                jInternalFrameColaboradorInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setForeground(new java.awt.Color(0, 51, 51));
        jLabel21.setText("ID");

        jTextFieldID.setEnabled(false);

        jLabel1.setForeground(new java.awt.Color(0, 51, 51));
        jLabel1.setText("Colaborador");

        jLabel9.setForeground(new java.awt.Color(0, 51, 51));
        jLabel9.setText("Sexo");

        jComboBoxSexo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione", "MASCULINO", "FEMININO", " " }));

        jLabel7.setForeground(new java.awt.Color(0, 51, 51));
        jLabel7.setText("Data Nascimento");

        jTextFieldNascimento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldNascimentoFocusLost(evt);
            }
        });

        jLabel11.setText("Estado Civil");

        jComboBoxEstadoCivil.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel12.setText("Nome da Mãe");

        jTextFieldMãe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMãeActionPerformed(evt);
            }
        });

        jLabel18.setText("Endereço");

        jLabel19.setText("Celular");

        lblFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Pessoa-256pix.png"))); // NOI18N
        lblFoto.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        lblFoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFotoMouseClicked(evt);
            }
        });

        btnSelecionarFoto.setText("Pesquisar");
        btnSelecionarFoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecionarFotoActionPerformed(evt);
            }
        });

        jTextAreaEndereço.setColumns(20);
        jTextAreaEndereço.setLineWrap(true);
        jTextAreaEndereço.setRows(5);
        jTextAreaEndereço.setWrapStyleWord(true);
        jScrollPane3.setViewportView(jTextAreaEndereço);

        jLabelStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelStatus.setText("...");
        jLabelStatus.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabelDataCadastro.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jLabelDataCadastro.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelDataCadastro.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabelDataCadastro.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldNomeFixo, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextFieldMãe)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextFieldDDD, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jTextFieldCelular))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(55, 55, 55)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(1, 1, 1)
                                    .addComponent(jComboBoxEstadoCivil, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jComboBoxSexo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldNascimento))
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(btnSelecionarFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblFoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelDataCadastro, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(180, 180, 180)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel21)
                    .addComponent(jLabelStatus))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldNomeFixo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldNascimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxSexo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addGap(6, 6, 6)
                        .addComponent(jComboBoxEstadoCivil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSelecionarFoto)
                            .addComponent(lblFoto, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(jLabelDataCadastro, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(jTextFieldMãe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldDDD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCelular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        javax.swing.GroupLayout jInternalFrameColaboradorLayout = new javax.swing.GroupLayout(jInternalFrameColaborador.getContentPane());
        jInternalFrameColaborador.getContentPane().setLayout(jInternalFrameColaboradorLayout);
        jInternalFrameColaboradorLayout.setHorizontalGroup(
            jInternalFrameColaboradorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jInternalFrameColaboradorLayout.setVerticalGroup(
            jInternalFrameColaboradorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jDesktopPane1.add(jInternalFrameColaborador);
        jInternalFrameColaborador.setBounds(10, 70, 370, 540);

        jInternalFrameDocPessoais.setClosable(true);
        jInternalFrameDocPessoais.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        jInternalFrameDocPessoais.setIconifiable(true);
        jInternalFrameDocPessoais.setTitle("Documentos Pessoais");
        jInternalFrameDocPessoais.setVisible(true);
        jInternalFrameDocPessoais.addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                jInternalFrameDocPessoaisInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel14.setText("Identidade/RG");

        jTextFieldRG.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        jTextFieldCPF.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextFieldCPF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldCPFFocusLost(evt);
            }
        });

        jLabel15.setText("CPF");

        jLabel16.setText("PIS");

        jTextFieldPIS.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        jTextFieldCTPS.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        jLabel17.setText("CTPS/Série/UF..:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldRG, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCPF, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldPIS, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCTPS, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldRG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldCPF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldPIS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldCTPS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
        );

        javax.swing.GroupLayout jInternalFrameDocPessoaisLayout = new javax.swing.GroupLayout(jInternalFrameDocPessoais.getContentPane());
        jInternalFrameDocPessoais.getContentPane().setLayout(jInternalFrameDocPessoaisLayout);
        jInternalFrameDocPessoaisLayout.setHorizontalGroup(
            jInternalFrameDocPessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameDocPessoaisLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jInternalFrameDocPessoaisLayout.setVerticalGroup(
            jInternalFrameDocPessoaisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameDocPessoaisLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jDesktopPane1.add(jInternalFrameDocPessoais);
        jInternalFrameDocPessoais.setBounds(390, 120, 220, 270);

        jInternalFrameEmpresa.setClosable(true);
        jInternalFrameEmpresa.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        jInternalFrameEmpresa.setIconifiable(true);
        jInternalFrameEmpresa.setTitle("Empresa");
        jInternalFrameEmpresa.setVisible(true);
        jInternalFrameEmpresa.addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                jInternalFrameEmpresaInternalFrameClosing(evt);
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setForeground(new java.awt.Color(0, 51, 51));
        jLabel5.setText("Empresa");

        jComboBoxEmpresa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButtonCadEmpresa.setText("+");
        jButtonCadEmpresa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCadEmpresaActionPerformed(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(0, 51, 51));
        jLabel3.setText("Setor");

        jComboBoxSetor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButtonCadSetor.setText("+");
        jButtonCadSetor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCadSetorActionPerformed(evt);
            }
        });

        jLabel2.setText("Admissão");

        jTextFieldAdmissão.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldAdmissãoFocusLost(evt);
            }
        });

        jTextFieldAquisitivo.setToolTipText("Período Aquisitivo");
        jTextFieldAquisitivo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldAquisitivoFocusLost(evt);
            }
        });

        jTextFieldDemissão.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldDemissãoFocusLost(evt);
            }
        });

        jLabel8.setText("Data Demissão");

        jTextAreaMotivo.setColumns(20);
        jTextAreaMotivo.setRows(5);
        jTextAreaMotivo.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextAreaMotivo);

        jLabel22.setText("Motivo Demissão");

        jLabel6.setForeground(new java.awt.Color(0, 51, 51));
        jLabel6.setText("Status");

        jButtonAfastados.setText("+");
        jButtonAfastados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAfastadosActionPerformed(evt);
            }
        });

        jComboBoxStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione", "ATIVO", "DEMITIDO ", "EM PROCESSO", "AFASTADO" }));
        jComboBoxStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxStatusActionPerformed(evt);
            }
        });

        jLabel10.setForeground(new java.awt.Color(0, 51, 51));
        jLabel10.setText("Função");

        jComboBoxFunção.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabelTempoCasa.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelTempoCasa.setText("Tempo de Casa");

        jLabel25.setText("Tempo de Empresa");

        jButtonCadFuncao.setText("+");
        jButtonCadFuncao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCadFuncaoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldDemissão, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAquisitivo, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(76, 76, 76)
                                .addComponent(jButtonCadEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBoxEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(91, 91, 91)
                                .addComponent(jButtonAfastados, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldAdmissão, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTempoCasa, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonCadSetor, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBoxSetor, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonCadFuncao, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBoxFunção, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(9, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButtonCadEmpresa)
                            .addComponent(jLabel5))
                        .addGap(6, 6, 6)
                        .addComponent(jComboBoxEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonAfastados)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25)
                                .addComponent(jLabel2))
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextFieldAdmissão, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTempoCasa))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonCadSetor)
                            .addComponent(jLabel10))
                        .addComponent(jLabel3))
                    .addComponent(jButtonCadFuncao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxSetor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxFunção, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(6, 6, 6)
                        .addComponent(jTextFieldDemissão, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldAquisitivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jInternalFrameEmpresaLayout = new javax.swing.GroupLayout(jInternalFrameEmpresa.getContentPane());
        jInternalFrameEmpresa.getContentPane().setLayout(jInternalFrameEmpresaLayout);
        jInternalFrameEmpresaLayout.setHorizontalGroup(
            jInternalFrameEmpresaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameEmpresaLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jInternalFrameEmpresaLayout.setVerticalGroup(
            jInternalFrameEmpresaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameEmpresaLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jDesktopPane1.add(jInternalFrameEmpresa);
        jInternalFrameEmpresa.setBounds(630, 120, 710, 270);

        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        buttonGroupFuncao.add(jRadioButtonNovo);
        jRadioButtonNovo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jRadioButtonNovo.setText("Novo");
        jRadioButtonNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNovoActionPerformed(evt);
            }
        });

        buttonGroupFuncao.add(jRadioButtonAlterar);
        jRadioButtonAlterar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jRadioButtonAlterar.setText("Editar");
        jRadioButtonAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAlterarActionPerformed(evt);
            }
        });

        buttonGroupFuncao.add(jRadioButtonExcluir);
        jRadioButtonExcluir.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jRadioButtonExcluir.setText("Excluir");
        jRadioButtonExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonExcluirActionPerformed(evt);
            }
        });

        Salvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/SalvarPequeno.png"))); // NOI18N
        Salvar.setText("Salvar");
        Salvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SalvarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jRadioButtonNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonExcluir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                .addComponent(Salvar, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonAlterar)
                    .addComponent(jRadioButtonNovo)
                    .addComponent(jRadioButtonExcluir)
                    .addComponent(Salvar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        jDesktopPane1.add(jPanel5);
        jPanel5.setBounds(880, 0, 470, 50);

        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jTextFieldNome.setBackground(new java.awt.Color(255, 255, 102));
        jTextFieldNome.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextFieldNome.setMaximumSize(new java.awt.Dimension(280, 24));
        jTextFieldNome.setMinimumSize(new java.awt.Dimension(280, 24));
        jTextFieldNome.setPreferredSize(new java.awt.Dimension(280, 24));

        jToggleButtonEmProcesso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/Lupa Pequena.png"))); // NOI18N
        jToggleButtonEmProcesso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonEmProcessoActionPerformed(evt);
            }
        });

        jLabelSino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sino.png"))); // NOI18N
        jLabelSino.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelSinoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelSinoMouseExited(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButtonEmProcesso, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextFieldNome, javax.swing.GroupLayout.PREFERRED_SIZE, 278, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelSino)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelSino, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jTextFieldNome, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButtonEmProcesso, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jDesktopPane1.add(jPanel6);
        jPanel6.setBounds(0, 0, 380, 50);

        jLabelIdade.setFont(new java.awt.Font("Segoe UI Black", 0, 18)); // NOI18N
        jLabelIdade.setForeground(new java.awt.Color(255, 255, 255));
        jLabelIdade.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelIdade.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jDesktopPane1.add(jLabelIdade);
        jLabelIdade.setBounds(1180, 70, 140, 40);

        jCheckBoxEmProcesso.setText("Em Processo de Admissão");
        jCheckBoxEmProcesso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxEmProcessoActionPerformed(evt);
            }
        });
        jDesktopPane1.add(jCheckBoxEmProcesso);
        jCheckBoxEmProcesso.setBounds(0, 50, 161, 20);

        jLabelNomeDestaque.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabelNomeDestaque.setForeground(new java.awt.Color(255, 255, 255));
        jDesktopPane1.add(jLabelNomeDestaque);
        jLabelNomeDestaque.setBounds(400, 70, 760, 40);

        jLabelBanco.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabelBanco.setForeground(new java.awt.Color(255, 255, 0));
        jDesktopPane1.add(jLabelBanco);
        jLabelBanco.setBounds(500, 20, 210, 30);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel4.setMinimumSize(new java.awt.Dimension(0, 5));
        jPanel4.setLayout(null);

        jLabelUsuario.setText("Usuario");
        jPanel4.add(jLabelUsuario);
        jLabelUsuario.setBounds(10, 6, 150, 20);

        jLabel24.setText("Nível de Acesso:");
        jPanel4.add(jLabel24);
        jLabel24.setBounds(177, 8, 100, 16);

        jLabelNivel.setText("Nível");
        jPanel4.add(jLabelNivel);
        jLabelNivel.setBounds(283, 8, 27, 16);

        jLabelModificado.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        jLabelModificado.setText("Modificado");
        jPanel4.add(jLabelModificado);
        jLabelModificado.setBounds(411, 8, 357, 16);

        jLabelQtdEncontrados.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelQtdEncontrados.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelQtdEncontrados.setText(" 0 colaboradores encontrados.");
        jPanel4.add(jLabelQtdEncontrados);
        jLabelQtdEncontrados.setBounds(1137, 8, 185, 16);

        jCheckBoxFrameColaborador.setSelected(true);
        jCheckBoxFrameColaborador.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFrameColaboradorActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBoxFrameColaborador);
        jCheckBoxFrameColaborador.setBounds(790, 0, 20, 19);

        jCheckBoxFrameDocPessoais.setSelected(true);
        jCheckBoxFrameDocPessoais.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFrameDocPessoaisActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBoxFrameDocPessoais);
        jCheckBoxFrameDocPessoais.setBounds(810, 0, 20, 19);

        jCheckBoxFrameEmpresa.setSelected(true);
        jCheckBoxFrameEmpresa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFrameEmpresaActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBoxFrameEmpresa);
        jCheckBoxFrameEmpresa.setBounds(830, 0, 20, 19);

        jCheckBoxFrameTabela.setSelected(true);
        jCheckBoxFrameTabela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxFrameTabelaActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBoxFrameTabela);
        jCheckBoxFrameTabela.setBounds(850, 0, 20, 19);

        jCheckBoxNomeDestaque.setSelected(true);
        jCheckBoxNomeDestaque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNomeDestaqueActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBoxNomeDestaque);
        jCheckBoxNomeDestaque.setBounds(870, 0, 20, 19);

        jCheckBoxGrafico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxGraficoActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBoxGrafico);
        jCheckBoxGrafico.setBounds(890, 0, 20, 19);

        jMenuCadastros.setText("Cadastros");

        jMenuIUsuarios.setText("Usuários - Casdastro/Manutenção");
        jMenuIUsuarios.setEnabled(false);
        jMenuIUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuIUsuariosActionPerformed(evt);
            }
        });
        jMenuCadastros.add(jMenuIUsuarios);

        jMenuBar1.add(jMenuCadastros);

        jMenuDocumentos.setText("Documentos");

        jMenuItemPastaFunc.setText("Pasta do funcionário");
        jMenuItemPastaFunc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPastaFuncActionPerformed(evt);
            }
        });
        jMenuDocumentos.add(jMenuItemPastaFunc);

        jMenuIRelaDocs.setText("Entrega de documentos admissão");
        jMenuIRelaDocs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuIRelaDocsActionPerformed(evt);
            }
        });
        jMenuDocumentos.add(jMenuIRelaDocs);

        jMenuItemAdvertencia.setText("Emissão/Impressão de documentos");
        jMenuItemAdvertencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAdvertenciaActionPerformed(evt);
            }
        });
        jMenuDocumentos.add(jMenuItemAdvertencia);

        jMenuBar1.add(jMenuDocumentos);

        jMenuUtilitarios.setText("Utilitários");

        jMenuItemAniversariantes.setText("Aniversariantes do dia");
        jMenuItemAniversariantes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAniversariantesActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemAniversariantes);

        jMenuItemExperiencia.setText("Colaborades em experiência");
        jMenuItemExperiencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExperienciaActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemExperiencia);

        jMenuItemEmProcesso.setText("Colaboradores em processo de admissão");
        jMenuItemEmProcesso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemEmProcessoActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemEmProcesso);

        jMenuItemBackUp.setText("BackUp");
        jMenuItemBackUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBackUpActionPerformed(evt);
            }
        });
        jMenuUtilitarios.add(jMenuItemBackUp);

        jMenuBar1.add(jMenuUtilitarios);

        jMenu1.setText("Relatórios");

        jMenuItem1.setText("Relatório funcionários");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem3.setText("Relatório funcionarios ativos por setor");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuItem2.setText("Total funcionários por empresa");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem4.setText("Relatório por empresas");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jDesktopPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1346, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jDesktopPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldNascimentoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNascimentoFocusLost
        validarCampoData(jTextFieldNascimento);
        calcularIdade();
    }//GEN-LAST:event_jTextFieldNascimentoFocusLost

    private void jTextFieldMãeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMãeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMãeActionPerformed

    private void jTextFieldCPFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldCPFFocusLost
        validarCPF();
    }//GEN-LAST:event_jTextFieldCPFFocusLost

    private void jTableFuncionarioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableFuncionarioMouseClicked
// 🔒 BLOQUEIO REAL
        if (!jTableFuncionario.isEnabled()) {
            return;
        }
        lblFoto.setIcon(new ImageIcon(getClass().getResource("/imagens/Pessoa-256pix.png")));
        // 🔴 DUPLO CLIQUE (AGORA É QUEM MANDA NAS REGRAS)
        if (evt.getClickCount() == 2 && jRadioButtonNovo.isSelected()) {

            filtrarPorDuploClique();
            //System.out.println("Duplo clique detectado");
            //jTableFuncionario.setEnabled(true);
            //************jTableFuncionario.setEnabled(false);

            jComboBoxStatus.setSelectedItem("EM PROCESSO");
            jLabelStatus.setText("");

            jComboBoxEmpresa.setSelectedItem("Selecione...");
            jComboBoxSetor.setSelectedItem("Selecione...");
            jComboBoxFunção.setSelectedItem("Selecione...");

            // Mantém dados já preenchidos e limpa só o necessário
            jTextFieldID.setText("");
            jTextFieldAdmissão.setText("");
            jTextFieldDemissão.setText("");
            jTextFieldAquisitivo.setText("");
            jTextAreaMotivo.setText("");

            // Habilita formulário
            habilitarFormulario(false);

            // Bloqueia a tabela 🔥
            jTableFuncionario.setEnabled(false);

            return;
        }

        // 🟡 CLIQUE SIMPLES (NOVO)
        if (evt.getClickCount() == 1 && jRadioButtonNovo.isSelected()) {

            // 🔥 Apenas preenche tudo (sem interferência)
            preencherFormulario();
            calcularIdade();

            return;
        }

        // 🟢 MODO ALTERAR (não mexi)
        preencherFormulario();
        validarDatas();
        calcularIdade();
        calcularTempoCasa();
        validarCPF();
    }//GEN-LAST:event_jTableFuncionarioMouseClicked

    private void jButtonCadEmpresaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCadEmpresaActionPerformed
        TelaCadEmpresa tela = new TelaCadEmpresa();

        // 🔥 ESCUTA O FECHAMENTO
        tela.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                carregarComboEmpresa(); // atualiza quando fechar
            }
        });

        tela.setVisible(true);
    }//GEN-LAST:event_jButtonCadEmpresaActionPerformed

    private void jButtonCadSetorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCadSetorActionPerformed
        TelaCadSetor tela = new TelaCadSetor();

        // 🔥 ESCUTA quando a tela fechar
        tela.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                carregarComboSetor(); // atualiza combo
            }
        });

        tela.setVisible(true);
    }//GEN-LAST:event_jButtonCadSetorActionPerformed

    private void jTextFieldAdmissãoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldAdmissãoFocusLost
        validarCampoData(jTextFieldAdmissão);
    }//GEN-LAST:event_jTextFieldAdmissãoFocusLost

    private void jTextFieldAquisitivoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldAquisitivoFocusLost
        validarCampoData(jTextFieldAquisitivo);
    }//GEN-LAST:event_jTextFieldAquisitivoFocusLost

    private void jTextFieldDemissãoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDemissãoFocusLost
        validarCampoData(jTextFieldDemissão);
    }//GEN-LAST:event_jTextFieldDemissãoFocusLost

    private void jRadioButtonNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNovoActionPerformed
        jRadioButtonExcluir.setForeground(new java.awt.Color(0, 0, 0));
        jRadioButtonAlterar.setSelected(false);
        jRadioButtonExcluir.setSelected(false);
        limparFormulario();
        jTextFieldNomeFixo.requestFocus();
        habilitarFormulario(true);
        jTextFieldID.setEnabled(false);
        jTextFieldDemissão.setEnabled(false);

        // Status
        jComboBoxStatus.removeAllItems();
        jComboBoxStatus.addItem("Selecione");
        jComboBoxStatus.addItem("ATIVO");
        jComboBoxStatus.addItem("DEMITIDO");
        jComboBoxStatus.addItem("EM PROCESSO");
        jComboBoxStatus.addItem("CANCELADO");
        jComboBoxStatus.addItem("AFASTADO");

        jComboBoxStatus.setSelectedItem("EM PROCESSO");

        jComboBoxStatus.setEnabled(false);

        jTextAreaMotivo.setEnabled(false);
        jTableFuncionario.setEnabled(true);
        jPanel1.setBackground(new Color(255, 255, 255));
        jLabelNomeDestaque.setForeground(new Color(255, 255, 255));
    }//GEN-LAST:event_jRadioButtonNovoActionPerformed

    private void jRadioButtonAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAlterarActionPerformed
        jRadioButtonExcluir.setForeground(new java.awt.Color(0, 0, 0));
        jTableFuncionario.setEnabled(true);
        jRadioButtonNovo.setSelected(false);
        jRadioButtonExcluir.setSelected(false);
        jTextFieldDemissão.setEnabled(true);
        jTextAreaMotivo.setEnabled(false);
        limparFormulario();
        //limparTabelaFuncionario();
        habilitarFormulario(true);
        jTextFieldID.setEnabled(false);
        jPanel1.setBackground(new Color(255, 255, 255));
        jLabelNomeDestaque.setForeground(new Color(255, 255, 255));

    }//GEN-LAST:event_jRadioButtonAlterarActionPerformed

    private void jRadioButtonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonExcluirActionPerformed
        jRadioButtonExcluir.setForeground(new java.awt.Color(255, 0, 0));
        jRadioButtonNovo.setSelected(false);
        jRadioButtonAlterar.setSelected(false);
        jPanel1.setBackground(new Color(255, 153, 153));
        jLabelNomeDestaque.setForeground(new Color(255, 153, 153));
    }//GEN-LAST:event_jRadioButtonExcluirActionPerformed

    private void SalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SalvarActionPerformed
        if (jRadioButtonNovo.isSelected()) {

            // 🔥 SE DER ERRO → PARA TUDO
            if (!validacampos()) {
                return;
            }

            salvarNovoFuncionario();

        } else if (jRadioButtonAlterar.isSelected()) {

            // 🔥 SE DER ERRO → PARA TUDO
            if (!validacampos()) {
                return;
            }

            jTextFieldNome.setText(jTextFieldNomeFixo.getText());
            atualizarFuncionario();

        } else if (jRadioButtonExcluir.isSelected()) {

            // 🔥 SE DER ERRO → PARA TUDO
            if (!validacampos()) {
                return;
            }

            excluirFuncionario();

        } else {

            JOptionPane.showMessageDialog(this,
                    "Selecione NOVO ou ALTERAR.");

        }


    }//GEN-LAST:event_SalvarActionPerformed

    private boolean validacampos() {

        // 🔴 VALIDA DATAS
        if (!validarData(jTextFieldNascimento, "Data de Nascimento", true)) {
            return false;
        }
        if (!validarData(jTextFieldAdmissão, "Data de Admissão", false)) {
            return false;
        }
        if (!validarData(jTextFieldDemissão, "Data de Demissão", false)) {
            return false;
        }
        if (!validarData(jTextFieldAquisitivo, "Período Aquisitivo", false)) {
            return false;
        }

        // 🔴 CAMPOS OBRIGATÓRIOS
        if (jComboBoxEmpresa.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione a Empresa");
            jComboBoxEmpresa.requestFocus();
            return false;
        }

        if (jTextFieldNomeFixo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o Nome");
            jTextFieldNomeFixo.requestFocus();
            return false;
        }

        if (jComboBoxSetor.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione o Setor");
            jComboBoxSetor.requestFocus();
            return false;
        }

        if (jComboBoxFunção.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione a Função");
            jComboBoxFunção.requestFocus();
            return false;
        }

        if (jComboBoxStatus.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione o Status");
            jComboBoxStatus.requestFocus();
            return false;
        }

        if (jComboBoxSexo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione o Sexo");
            jComboBoxSexo.requestFocus();
            return false;
        }

        if (jTextFieldNascimento.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a Data de Nascimento");
            jTextFieldNascimento.requestFocus();
            return false;
        }

        if (jComboBoxEstadoCivil.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione o Estado Civil");
            jComboBoxEstadoCivil.requestFocus();
            return false;
        }

        if (jTextFieldMãe.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o Nome da Mãe");
            jTextFieldMãe.requestFocus();
            return false;
        }

        if (jTextFieldCPF.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o CPF");
            jTextFieldCPF.requestFocus();
            return false;
        }

        // 🔴 CPF
        String cpf = jTextFieldCPF.getText().replaceAll("[^0-9]", "");

        if (!isCPFValido(cpf)) {
            JOptionPane.showMessageDialog(this, "CPF inválido!");
            jTextFieldCPF.requestFocus();
            return false;
        }

        return true; // ✅ tudo válido
    }

    private void jButtonAfastadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAfastadosActionPerformed
        if (jTextFieldID.getText().isEmpty()) {

            JOptionPane.showMessageDialog(null,
                    "Selecione um funcionário primeiro.");

            return;

        }

        int idFuncionario = Integer.parseInt(jTextFieldID.getText());

        String nomeFuncionario = jTextFieldNomeFixo.getText();

        new TelaAfastamento(idFuncionario, nomeFuncionario).setVisible(true);
    }//GEN-LAST:event_jButtonAfastadosActionPerformed

    private void jComboBoxStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxStatusActionPerformed
        calcularTempoCasa();

    }//GEN-LAST:event_jComboBoxStatusActionPerformed

    private void lblFotoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFotoMouseClicked
        carregarFotoFuncionario();
    }//GEN-LAST:event_lblFotoMouseClicked

    private void btnSelecionarFotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarFotoActionPerformed
        selecionarFoto();
    }//GEN-LAST:event_btnSelecionarFotoActionPerformed

    private void jMenuItemPastaFuncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPastaFuncActionPerformed
        String cpf = jTextFieldCPF.getText().trim();

        TelaVisualizarPDF tela = new TelaVisualizarPDF(cpf);

        tela.setVisible(true);
    }//GEN-LAST:event_jMenuItemPastaFuncActionPerformed

    private void jMenuIRelaDocsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuIRelaDocsActionPerformed
        if (jTextFieldID.getText().isEmpty()) {

            JOptionPane.showMessageDialog(null,
                    "Selecione um funcionário primeiro!");

            return;
        }

        int idFuncionario = Integer.parseInt(jTextFieldID.getText());

        TelaDocumentos tela = new TelaDocumentos(idFuncionario);

        tela.setVisible(true);
    }//GEN-LAST:event_jMenuIRelaDocsActionPerformed

    private void jMenuItemAdvertenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAdvertenciaActionPerformed
        String nomeFuncionario = jTextFieldNomeFixo.getText();
        String cpfFuncionario = jTextFieldCPF.getText();

        if (nomeFuncionario.isEmpty() || cpfFuncionario.isEmpty()) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Selecione um funcionário primeiro."
            );

            return;
        }

        new TelaAdvertencias(nomeFuncionario, cpfFuncionario)
                .setVisible(true);
    }//GEN-LAST:event_jMenuItemAdvertenciaActionPerformed

    private void jMenuItemBackUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBackUpActionPerformed

        backupBancoRemotoPostgres();
    }//GEN-LAST:event_jMenuItemBackUpActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        new Relatorio001(nivelUsuario).setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        new br.com.Classes.Relatorios.Relatorio002().setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        gerarRelatorioTotalizaEmpresa();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jCheckBoxEmProcessoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxEmProcessoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxEmProcessoActionPerformed

    private void jToggleButtonEmProcessoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonEmProcessoActionPerformed
        if (jToggleButtonEmProcesso.isSelected()) {
            jCheckBoxEmProcesso.setSelected(true);
            jTextFieldNome.setBackground(Color.GREEN);
        } else {
            jCheckBoxEmProcesso.setSelected(false);
            jTextFieldNome.setBackground(Color.yellow);
        }
        jTextFieldNome.requestFocus();
    }//GEN-LAST:event_jToggleButtonEmProcessoActionPerformed

    private void jCheckBoxFrameColaboradorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFrameColaboradorActionPerformed
        if (jCheckBoxFrameColaborador.isSelected()) {
            jInternalFrameColaborador.setVisible(true);
        } else {
            jInternalFrameColaborador.setVisible(false);
        }

    }//GEN-LAST:event_jCheckBoxFrameColaboradorActionPerformed

    private void jInternalFrameColaboradorInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_jInternalFrameColaboradorInternalFrameClosing
        jCheckBoxFrameColaborador.setSelected(false);
    }//GEN-LAST:event_jInternalFrameColaboradorInternalFrameClosing

    private void jCheckBoxFrameDocPessoaisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFrameDocPessoaisActionPerformed
        if (jCheckBoxFrameDocPessoais.isSelected()) {
            jInternalFrameDocPessoais.setVisible(true);
        } else {
            jInternalFrameDocPessoais.setVisible(false);
        }
    }//GEN-LAST:event_jCheckBoxFrameDocPessoaisActionPerformed

    private void jInternalFrameDocPessoaisInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_jInternalFrameDocPessoaisInternalFrameClosing
        jCheckBoxFrameDocPessoais.setSelected(false);
    }//GEN-LAST:event_jInternalFrameDocPessoaisInternalFrameClosing

    private void jCheckBoxFrameEmpresaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFrameEmpresaActionPerformed
        if (jCheckBoxFrameEmpresa.isSelected()) {
            jInternalFrameEmpresa.setVisible(true);
        } else {
            jInternalFrameEmpresa.setVisible(false);

        }
    }//GEN-LAST:event_jCheckBoxFrameEmpresaActionPerformed

    private void jInternalFrameEmpresaInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_jInternalFrameEmpresaInternalFrameClosing
        jCheckBoxFrameEmpresa.setSelected(false);
    }//GEN-LAST:event_jInternalFrameEmpresaInternalFrameClosing

    private void jCheckBoxFrameTabelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxFrameTabelaActionPerformed
        if (jCheckBoxFrameTabela.isSelected()) {
            jInternalFrameTabela.setVisible(true);
        } else {
            jInternalFrameTabela.setVisible(false);
        }
    }//GEN-LAST:event_jCheckBoxFrameTabelaActionPerformed

    private void jInternalFrameTabelaInternalFrameClosing(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_jInternalFrameTabelaInternalFrameClosing
        jCheckBoxFrameTabela.setSelected(false);
    }//GEN-LAST:event_jInternalFrameTabelaInternalFrameClosing

    private void jCheckBoxNomeDestaqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNomeDestaqueActionPerformed
        if (jCheckBoxNomeDestaque.isSelected()) {
            jLabelNomeDestaque.setVisible(true);
            jLabelIdade.setVisible(true);
        } else {
            jLabelNomeDestaque.setVisible(false);
            jLabelIdade.setVisible(false);
        }
    }//GEN-LAST:event_jCheckBoxNomeDestaqueActionPerformed

    private void jLabelSinoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelSinoMouseEntered
        Point p = jLabelSino.getLocationOnScreen();

        telaNotificacoes.setLocation(
                p.x - 250,
                p.y + jLabelSino.getHeight()
        );

        telaNotificacoes.setVisible(true);
    }//GEN-LAST:event_jLabelSinoMouseEntered

    private void jLabelSinoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelSinoMouseExited
        telaNotificacoes.setVisible(false);
    }//GEN-LAST:event_jLabelSinoMouseExited

    private void jMenuItemAniversariantesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAniversariantesActionPerformed
        java.util.List<br.com.Utilidades.FuncionarioNotificacao> lista
                = br.com.Utilidades.NotificacaoServices.obterAniversariantesDoDia();

        TelaNotificaçõesAnaliticas tela
                = new TelaNotificaçõesAnaliticas(this, true);

        tela.preencherComAniversariantes(lista);

        tela.setLocationRelativeTo(this);
        tela.setVisible(true);
    }//GEN-LAST:event_jMenuItemAniversariantesActionPerformed

    private void jMenuItemExperienciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExperienciaActionPerformed
        java.util.List<FuncionarioNotificacao> lista
                = NotificacaoServices.obterFuncionariosEmExperiencia();

        TelaNotificaçõesAnaliticas tela
                = new TelaNotificaçõesAnaliticas(this, true);

        tela.preencherComExperiencia(lista);

        tela.setLocationRelativeTo(this);

        tela.setVisible(true);
    }//GEN-LAST:event_jMenuItemExperienciaActionPerformed

    private void jMenuItemEmProcessoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEmProcessoActionPerformed
        java.util.List<FuncionarioNotificacao> lista
                = NotificacaoServices.obterFuncionariosEmProcesso();

        TelaNotificaçõesAnaliticas tela
                = new TelaNotificaçõesAnaliticas(this, true);

        tela.preencherComFuncionariosEmProcesso(lista);

        tela.setLocationRelativeTo(this);

        tela.setVisible(true);
    }//GEN-LAST:event_jMenuItemEmProcessoActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        new br.com.Classes.Relatorios.Relatório003().setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jCheckBoxGraficoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGraficoActionPerformed
        if (jCheckBoxGrafico.isSelected()) {
            jInternalFrameColaborador.setVisible(false);
            jInternalFrameDocPessoais.setVisible(false);
            jInternalFrameEmpresa.setVisible(false);
            jInternalFrameTabela.setVisible(false);
            jLabelNomeDestaque.setVisible(false);
            jCheckBoxFrameEmpresa.setSelected(false);
            jCheckBoxFrameColaborador.setSelected(false);
            jCheckBoxNomeDestaque.setSelected(false);
            jCheckBoxFrameTabela.setSelected(false);
            jCheckBoxFrameDocPessoais.setSelected(false);
            jPanel6.setVisible(false);
            jPanel5.setVisible(false);
            jLabelIdade.setVisible(false);
            
        } else {
            jInternalFrameColaborador.setVisible(true);
            jInternalFrameDocPessoais.setVisible(true);
            jInternalFrameEmpresa.setVisible(true);
            jInternalFrameTabela.setVisible(true);
            jLabelNomeDestaque.setVisible(true);
            jCheckBoxFrameEmpresa.setSelected(true);
            jCheckBoxFrameColaborador.setSelected(true);
            jCheckBoxNomeDestaque.setSelected(true);
            jCheckBoxFrameTabela.setSelected(true);
            jCheckBoxFrameDocPessoais.setSelected(true);
            jPanel6.setVisible(true);
            jPanel5.setVisible(true);
            jLabelIdade.setVisible(true);
            
        }

// Chama o método para exibir ou esconder a janela interna
        controlarExibicaoGraficoDireto();
    }//GEN-LAST:event_jCheckBoxGraficoActionPerformed

    private void jButtonCadFuncaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCadFuncaoActionPerformed
        TelaCadFuncao telaFun = new TelaCadFuncao();

        // 🔥 ESCUTA O FECHAMENTO
        telaFun.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                carregarComboFuncao(); // atualiza quando fechar
            }
        });

        telaFun.setVisible(true);
    }//GEN-LAST:event_jButtonCadFuncaoActionPerformed

    private void jMenuIUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuIUsuariosActionPerformed
        TelaCadUsusarios telaUsu = new TelaCadUsusarios();
        telaUsu.setVisible(true);
    }//GEN-LAST:event_jMenuIUsuariosActionPerformed

    /*  /**
     * /**
     * /
     *
     **
    /**
     * /**
     * /
     *
     **
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
            java.util.logging.Logger.getLogger(TelaFuncionarios.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaFuncionarios.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaFuncionarios.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaFuncionarios.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaFuncionarios().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Salvar;
    private javax.swing.JButton btnSelecionarFoto;
    private javax.swing.ButtonGroup buttonGroupFuncao;
    private javax.swing.JButton jButtonAfastados;
    private javax.swing.JButton jButtonCadEmpresa;
    private javax.swing.JButton jButtonCadFuncao;
    private javax.swing.JButton jButtonCadSetor;
    private javax.swing.JCheckBox jCheckBoxEmProcesso;
    private javax.swing.JCheckBox jCheckBoxFrameColaborador;
    private javax.swing.JCheckBox jCheckBoxFrameDocPessoais;
    private javax.swing.JCheckBox jCheckBoxFrameEmpresa;
    private javax.swing.JCheckBox jCheckBoxFrameTabela;
    private javax.swing.JCheckBox jCheckBoxGrafico;
    private javax.swing.JCheckBox jCheckBoxNomeDestaque;
    private javax.swing.JComboBox<String> jComboBoxEmpresa;
    private javax.swing.JComboBox<String> jComboBoxEstadoCivil;
    private javax.swing.JComboBox<String> jComboBoxFunção;
    private javax.swing.JComboBox<String> jComboBoxSetor;
    private javax.swing.JComboBox<String> jComboBoxSexo;
    private javax.swing.JComboBox<String> jComboBoxStatus;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JInternalFrame jInternalFrameColaborador;
    private javax.swing.JInternalFrame jInternalFrameDocPessoais;
    private javax.swing.JInternalFrame jInternalFrameEmpresa;
    private javax.swing.JInternalFrame jInternalFrameTabela;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelBanco;
    private javax.swing.JLabel jLabelDataCadastro;
    private javax.swing.JLabel jLabelIdade;
    private javax.swing.JLabel jLabelModificado;
    private javax.swing.JLabel jLabelNivel;
    private javax.swing.JLabel jLabelNomeDestaque;
    private javax.swing.JLabel jLabelQtdEncontrados;
    private javax.swing.JLabel jLabelSino;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelTempoCasa;
    private javax.swing.JLabel jLabelUsuario;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuCadastros;
    private javax.swing.JMenu jMenuDocumentos;
    private javax.swing.JMenuItem jMenuIRelaDocs;
    private javax.swing.JMenuItem jMenuIUsuarios;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItemAdvertencia;
    private javax.swing.JMenuItem jMenuItemAniversariantes;
    private javax.swing.JMenuItem jMenuItemBackUp;
    private javax.swing.JMenuItem jMenuItemEmProcesso;
    private javax.swing.JMenuItem jMenuItemExperiencia;
    private javax.swing.JMenuItem jMenuItemPastaFunc;
    private javax.swing.JMenu jMenuUtilitarios;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButton jRadioButtonAlterar;
    private javax.swing.JRadioButton jRadioButtonExcluir;
    private javax.swing.JRadioButton jRadioButtonNovo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTableFuncionario;
    private javax.swing.JTextArea jTextAreaEndereço;
    private javax.swing.JTextArea jTextAreaMotivo;
    private javax.swing.JTextField jTextFieldAdmissão;
    private javax.swing.JTextField jTextFieldAquisitivo;
    private javax.swing.JTextField jTextFieldCPF;
    private javax.swing.JTextField jTextFieldCTPS;
    private javax.swing.JTextField jTextFieldCelular;
    private javax.swing.JTextField jTextFieldDDD;
    private javax.swing.JTextField jTextFieldDemissão;
    private javax.swing.JTextField jTextFieldID;
    private javax.swing.JTextField jTextFieldMãe;
    private javax.swing.JTextField jTextFieldNascimento;
    private javax.swing.JTextField jTextFieldNome;
    private javax.swing.JTextField jTextFieldNomeFixo;
    private javax.swing.JTextField jTextFieldPIS;
    private javax.swing.JTextField jTextFieldRG;
    private javax.swing.JToggleButton jToggleButtonEmProcesso;
    private javax.swing.JLabel lblFoto;
    // End of variables declaration//GEN-END:variables
}
