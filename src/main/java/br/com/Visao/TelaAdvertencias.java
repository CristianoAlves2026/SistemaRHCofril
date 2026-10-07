/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Visao;

/**
 *
 * @author Lavanderia
 */
public class TelaAdvertencias extends javax.swing.JFrame {

    /**
     * Creates new form TelaAdvertencias
     */
    public TelaAdvertencias(String nome, String cpf) {

        initComponents();
        setLocationRelativeTo(null);
        jTextFieldNomeFuncionario.setText(nome);

        jTextFieldCPFFuncionario.setText(cpf);

        jTextAreaMotivo.setText("");

        carregarAdvertenciasFuncionario(nome);
    }

    private void gerarAdvertenciaWord(String nome, String cpf, String motivo) {

        try {

            String modeloArquivo = "";
            String tipoDocumento = "";

            boolean usaCPF = false;
            boolean usaMotivo = false;
            boolean usaNumero = false;

            if (jRadioButtonAdvertencia.isSelected()) {

                modeloArquivo = "AdvertenciaModelo.docx";
                tipoDocumento = "Advertencia";

                usaCPF = true;
                usaMotivo = true;
                usaNumero = true;

            } else if (jRadioButtonSuspençao.isSelected()) {

                modeloArquivo = "SuspensaoModelo.docx";
                tipoDocumento = "Suspensao";

                usaMotivo = true;
                usaNumero = true;

            } else if (jRadioButtonPedidoDemissao.isSelected()) {

                modeloArquivo = "PedidoDemissaoModelo.docx";
                tipoDocumento = "PedidoDemissao";

                usaCPF = true;

            } else if (jRadioButtonAvisoDispensa.isSelected()) {

                modeloArquivo = "AvisoPrevioDispensa.docx";
                tipoDocumento = "AvisoDispensa";

            } else if (jRadioButtonAvisoExp.isSelected()) {

                modeloArquivo = "AvisoPrevioExperiencia.docx";
                tipoDocumento = "AvisoExperiencia";

            } else {

                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Selecione um tipo de documento"
                );

                return;
            }

            String caminhoModelo = "F:\\Lavanderia\\Backup\\Advertencias\\" + modeloArquivo;

            java.io.File arquivoModelo = new java.io.File(caminhoModelo);

            if (!arquivoModelo.exists()) {

                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Modelo não encontrado:\n" + caminhoModelo
                );

                return;
            }

            org.apache.poi.xwpf.usermodel.XWPFDocument documento
                    = new org.apache.poi.xwpf.usermodel.XWPFDocument(
                            new java.io.FileInputStream(caminhoModelo)
                    );

            String dataFormatada = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String numeroSequencial = gerarNumeroSequencial();

            substituirMarcadoresSelecionados(
                    documento,
                    nome,
                    cpf,
                    motivo,
                    dataFormatada,
                    numeroSequencial,
                    usaCPF,
                    usaMotivo,
                    usaNumero
            );

            String pastaDestino = "F:\\Lavanderia\\Backup\\Advertencias\\";

            String nomeSeguro = nome.replaceAll("[^a-zA-Z0-9]", "_");

            String dataArquivo = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            String caminhoDestino = String.format(
                    "%s\\%s - %s - %s - %s.docx",
                    pastaDestino,
                    numeroSequencial.isEmpty() ? "0000" : numeroSequencial,
                    tipoDocumento,
                    dataArquivo,
                    nomeSeguro
            );

            java.io.FileOutputStream out
                    = new java.io.FileOutputStream(caminhoDestino);

            documento.write(out);

            out.close();

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    tipoDocumento + " gerado com sucesso!"
            );

            java.awt.Desktop.getDesktop().open(
                    new java.io.File(caminhoDestino)
            );

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Erro ao gerar documento:\n" + e.getMessage()
            );
        }
    }

    private void substituirMarcadoresSelecionados(
            org.apache.poi.xwpf.usermodel.XWPFDocument doc,
            String nome,
            String cpf,
            String motivo,
            String data,
            String numero,
            boolean usaCPF,
            boolean usaMotivo,
            boolean usaNumero) {

        for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragrafo : doc.getParagraphs()) {

            substituirMarcador(paragrafo, "${NOME}", nome);
            substituirMarcador(paragrafo, "${DATA}", data);

            if (usaCPF) {
                substituirMarcador(paragrafo, "${CPF}", cpf);
            }

            if (usaMotivo) {
                substituirMarcador(paragrafo, "${MOTIVO}", motivo);
            }

            if (usaNumero) {
                substituirMarcador(paragrafo, "${NUM}", numero);
            }
        }

        for (org.apache.poi.xwpf.usermodel.XWPFTable tabela : doc.getTables()) {

            for (org.apache.poi.xwpf.usermodel.XWPFTableRow linha : tabela.getRows()) {

                for (org.apache.poi.xwpf.usermodel.XWPFTableCell celula : linha.getTableCells()) {

                    for (org.apache.poi.xwpf.usermodel.XWPFParagraph paragrafo : celula.getParagraphs()) {

                        substituirMarcador(paragrafo, "${NOME}", nome);
                        substituirMarcador(paragrafo, "${DATA}", data);

                        if (usaCPF) {
                            substituirMarcador(paragrafo, "${CPF}", cpf);
                        }

                        if (usaMotivo) {
                            substituirMarcador(paragrafo, "${MOTIVO}", motivo);
                        }

                        if (usaNumero) {
                            substituirMarcador(paragrafo, "${NUM}", numero);
                        }
                    }
                }
            }
        }
    }

    private void carregarAdvertenciasFuncionario(String nomeFuncionario) {

        //String pasta = "C:\\Advertencias";
        String pasta = "F:\\Lavanderia\\Backup\\Advertencias";

        java.io.File diretorio = new java.io.File(pasta);

        if (!diretorio.exists()) {
            return;
        }

        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modelo.addColumn("Número");

        modelo.addColumn("Tipo");

        modelo.addColumn("Data");

        modelo.addColumn("Arquivo");

        String nomeBusca = nomeFuncionario.replaceAll("[^a-zA-Z0-9]", "_");

        for (java.io.File arquivo : diretorio.listFiles()) {

            String nomeArquivo = arquivo.getName();

            if (nomeArquivo.contains(nomeBusca)) {

                String[] partes = nomeArquivo.split(" - ");

                if (partes.length >= 4) {

                    modelo.addRow(new Object[]{
                        partes[0],
                        partes[1],
                        partes[2],
                        nomeArquivo
                    });
                }
            }
        }

        jTableDocumentos.setModel(modelo);
    }

    private void substituirMarcador(
            org.apache.poi.xwpf.usermodel.XWPFParagraph paragrafo,
            String marcador,
            String valor) {

        java.util.List<org.apache.poi.xwpf.usermodel.XWPFRun> runs = paragrafo.getRuns();

        if (runs == null || runs.isEmpty()) {
            return;
        }

        for (int i = 0; i < runs.size(); i++) {

            org.apache.poi.xwpf.usermodel.XWPFRun run = runs.get(i);

            String texto = run.getText(0);

            if (texto == null) {
                continue;
            }

            if (texto.contains(marcador)) {

                texto = texto.replace(marcador, valor);

                run.setText(texto, 0);

                continue;
            }

            if (texto.contains("$")) {

                StringBuilder marcadorCompleto = new StringBuilder(texto);

                int j = i + 1;

                while (j < runs.size()) {

                    String proximoTexto = runs.get(j).getText(0);

                    if (proximoTexto != null) {

                        marcadorCompleto.append(proximoTexto);

                        if (marcadorCompleto.toString().contains(marcador)) {

                            String novoTexto = marcadorCompleto
                                    .toString()
                                    .replace(marcador, valor);

                            run.setText(novoTexto, 0);

                            for (int k = j; k > i; k--) {
                                paragrafo.removeRun(k);
                            }

                            break;
                        }
                    }

                    j++;
                }
            }
        }
    }

    private String gerarNumeroSequencial() {

        try {

            
            String caminho = "F:\\Lavanderia\\Backup\\Advertencias\\contador.txt";
            java.io.File arquivo = new java.io.File(caminho);

            int numero = 1;

            if (arquivo.exists()) {

                java.io.BufferedReader reader
                        = new java.io.BufferedReader(
                                new java.io.FileReader(arquivo)
                        );

                String linha = reader.readLine();

                reader.close();

                if (linha != null && !linha.isEmpty()) {

                    numero = Integer.parseInt(linha) + 1;
                }
            }

            java.io.BufferedWriter writer
                    = new java.io.BufferedWriter(
                            new java.io.FileWriter(arquivo)
                    );

            writer.write(String.valueOf(numero));

            writer.close();

            return String.format("%04d", numero);

        } catch (Exception e) {

            return "0001";
        }
    }

    private void abrirDocumentoSelecionado() {

        try {

            int linhaSelecionada = jTableDocumentos.getSelectedRow();

            if (linhaSelecionada == -1) {
                return;
            }

            String nomeArquivo = jTableDocumentos
                    .getValueAt(linhaSelecionada, 3)
                    .toString();

            String caminhoCompleto
                    = "F:\\Lavanderia\\Backup\\Advertencias\\" + nomeArquivo;

            java.io.File arquivo = new java.io.File(caminhoCompleto);

            if (!arquivo.exists()) {

                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Arquivo não encontrado:\n" + caminhoCompleto
                );

                return;
            }

            java.awt.Desktop.getDesktop().open(arquivo);

        } catch (Exception e) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Erro ao abrir documento:\n" + e.getMessage()
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableDocumentos = new javax.swing.JTable();
        jLabelTextoDoc = new javax.swing.JLabel();
        jTextFieldNomeFuncionario = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldCPFFuncionario = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jRadioButtonAdvertencia = new javax.swing.JRadioButton();
        jRadioButtonSuspençao = new javax.swing.JRadioButton();
        jButtonGerar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaMotivo = new javax.swing.JTextArea();
        jRadioButtonAvisoDispensa = new javax.swing.JRadioButton();
        jRadioButtonAvisoExp = new javax.swing.JRadioButton();
        jRadioButtonPedidoDemissao = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Documentos do Funcionário");

        jTableDocumentos.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableDocumentos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableDocumentosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableDocumentos);

        jLabelTextoDoc.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelTextoDoc.setText("advertido por");

        jTextFieldNomeFuncionario.setEnabled(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Fica o funcionário(a), ");

        jTextFieldCPFFuncionario.setEnabled(false);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("portador do CPF: ");

        jRadioButtonAdvertencia.setText("Advertência");
        jRadioButtonAdvertencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAdvertenciaActionPerformed(evt);
            }
        });

        jRadioButtonSuspençao.setText("Suspensão");
        jRadioButtonSuspençao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSuspençaoActionPerformed(evt);
            }
        });

        jButtonGerar.setText("Gerar");
        jButtonGerar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGerarActionPerformed(evt);
            }
        });

        jTextAreaMotivo.setColumns(20);
        jTextAreaMotivo.setRows(5);
        jScrollPane2.setViewportView(jTextAreaMotivo);

        jRadioButtonAvisoDispensa.setText("Aviso Dispensa");
        jRadioButtonAvisoDispensa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAvisoDispensaActionPerformed(evt);
            }
        });

        jRadioButtonAvisoExp.setText("Aviso Experiência");
        jRadioButtonAvisoExp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAvisoExpActionPerformed(evt);
            }
        });

        jRadioButtonPedidoDemissao.setText("Pedido Demissão");
        jRadioButtonPedidoDemissao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonPedidoDemissaoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 715, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jRadioButtonAdvertencia)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jRadioButtonSuspençao)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jRadioButtonPedidoDemissao, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jRadioButtonAvisoDispensa, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jRadioButtonAvisoExp, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextFieldNomeFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel3)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextFieldCPFFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButtonGerar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabelTextoDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRadioButtonSuspençao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonGerar)
                        .addComponent(jRadioButtonAvisoDispensa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jRadioButtonAvisoExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jRadioButtonPedidoDemissao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jRadioButtonAdvertencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNomeFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCPFFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelTextoDoc)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonGerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGerarActionPerformed
        String nome = jTextFieldNomeFuncionario.getText();

        String cpf = jTextFieldCPFFuncionario.getText();

        String motivo = jTextAreaMotivo.getText();

        if (nome.isEmpty() || cpf.isEmpty() || motivo.isEmpty()) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Preencha o motivo."
            );

            return;
        }

        gerarAdvertenciaWord(nome, cpf, motivo);

        carregarAdvertenciasFuncionario(nome);
    }//GEN-LAST:event_jButtonGerarActionPerformed

    private void jRadioButtonAdvertenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAdvertenciaActionPerformed
        jLabelTextoDoc.setText("advertido por");
        jTextAreaMotivo.setEnabled(true);
        jRadioButtonSuspençao.setSelected(false);
        jRadioButtonPedidoDemissao.setSelected(false);
        jRadioButtonAvisoDispensa.setSelected(false);
        jRadioButtonAvisoExp.setSelected(false);


    }//GEN-LAST:event_jRadioButtonAdvertenciaActionPerformed

    private void jRadioButtonSuspençaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSuspençaoActionPerformed
        jLabelTextoDoc.setText("suspenso de suas atividades por");
        jTextAreaMotivo.setEnabled(true);
        jRadioButtonAdvertencia.setSelected(false);
        jRadioButtonPedidoDemissao.setSelected(false);
        jRadioButtonAvisoDispensa.setSelected(false);
        jRadioButtonAvisoExp.setSelected(false);
    }//GEN-LAST:event_jRadioButtonSuspençaoActionPerformed

    private void jRadioButtonPedidoDemissaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonPedidoDemissaoActionPerformed
        jLabelTextoDoc.setText("");
        jTextAreaMotivo.setText("Descreva o motivo...");
        jTextAreaMotivo.setEnabled(false);
        jRadioButtonSuspençao.setSelected(false);
        jRadioButtonAdvertencia.setSelected(false);
        jRadioButtonAvisoDispensa.setSelected(false);
        jRadioButtonAvisoExp.setSelected(false);
    }//GEN-LAST:event_jRadioButtonPedidoDemissaoActionPerformed

    private void jRadioButtonAvisoDispensaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAvisoDispensaActionPerformed
        jLabelTextoDoc.setText("");
        jTextAreaMotivo.setText("Descreva o motivo...");
        jTextAreaMotivo.setEnabled(false);
        jRadioButtonSuspençao.setSelected(false);
        jRadioButtonPedidoDemissao.setSelected(false);
        jRadioButtonAdvertencia.setSelected(false);
        jRadioButtonAvisoExp.setSelected(false);
    }//GEN-LAST:event_jRadioButtonAvisoDispensaActionPerformed

    private void jRadioButtonAvisoExpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAvisoExpActionPerformed
        jLabelTextoDoc.setText("");
        jTextAreaMotivo.setText("Descreva o motivo...");
        jTextAreaMotivo.setEnabled(false);
        jRadioButtonSuspençao.setSelected(false);
        jRadioButtonPedidoDemissao.setSelected(false);
        jRadioButtonAvisoDispensa.setSelected(false);
        jRadioButtonAdvertencia.setSelected(false);
    }//GEN-LAST:event_jRadioButtonAvisoExpActionPerformed

    private void jTableDocumentosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableDocumentosMouseClicked
        if (evt.getClickCount() == 2) {

            abrirDocumentoSelecionado();

        }
    }//GEN-LAST:event_jTableDocumentosMouseClicked

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
            java.util.logging.Logger.getLogger(TelaAdvertencias.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaAdvertencias.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaAdvertencias.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAdvertencias.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new TelaAdvertencias(nomeFuncionario, cpfFuncionario).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonGerar;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelTextoDoc;
    private javax.swing.JRadioButton jRadioButtonAdvertencia;
    private javax.swing.JRadioButton jRadioButtonAvisoDispensa;
    private javax.swing.JRadioButton jRadioButtonAvisoExp;
    private javax.swing.JRadioButton jRadioButtonPedidoDemissao;
    private javax.swing.JRadioButton jRadioButtonSuspençao;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableDocumentos;
    private javax.swing.JTextArea jTextAreaMotivo;
    private javax.swing.JTextField jTextFieldCPFFuncionario;
    private javax.swing.JTextField jTextFieldNomeFuncionario;
    // End of variables declaration//GEN-END:variables
}
