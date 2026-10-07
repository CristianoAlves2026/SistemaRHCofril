/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Visao;

/**
 *
 * @author Lavanderia
 */
public class TelaCadEmpresa extends javax.swing.JFrame {

    /**
     * Creates new form TelaCadEmpresa
     */
    public TelaCadEmpresa() {
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);
        carregarTabela();
        jTextFieldIDEmpresa.setEnabled(false);
        jTableEmpresa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                preencherCampos();
            }
        });
    }

    private void preencherCampos() {
        int linha = jTableEmpresa.getSelectedRow();

        if (linha != -1) {
            jTextFieldIDEmpresa.setText(getValorSeguro(linha, 0));
            jTextFieldFantasia.setText(getValorSeguro(linha, 1));
            jTextFieldRazaoSocial.setText(getValorSeguro(linha, 2));
            jTextFieldCNPJ.setText(getValorSeguro(linha, 3));
        }
    }

// Método auxiliar para evitar o NullPointerException
    private String getValorSeguro(int linha, int coluna) {
        Object valor = jTableEmpresa.getValueAt(linha, coluna);
        return (valor != null) ? valor.toString() : "";
    }

    private void carregarTabela() {

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar()) {

            String sql = "SELECT * FROM \"Empresa\" ORDER BY \"ID_EMPRESA\"";

            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            java.sql.ResultSet rs = pst.executeQuery();

            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                    new Object[][]{},
                    new String[]{"ID", "Empresa", "Razão Social", "CNPJ"}
            ) {
                boolean[] canEdit = new boolean[]{false, false, false, false};

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            };

            jTableEmpresa.setModel(model);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ID_EMPRESA"),
                    rs.getString("NOME_FANTASIA"),
                    rs.getString("RAZAO_SOCIAL"),
                    rs.getString("CNPJ")
                });
            }

            // 🔥 Ajuste manual das colunas
            jTableEmpresa.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

            jTableEmpresa.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
            jTableEmpresa.getColumnModel().getColumn(1).setPreferredWidth(150);  // Empresa
            jTableEmpresa.getColumnModel().getColumn(2).setPreferredWidth(220);  // Razão Social
            jTableEmpresa.getColumnModel().getColumn(3).setPreferredWidth(150);  // CNPJ

            // 🔥 Centralizar coluna ID
            javax.swing.table.DefaultTableCellRenderer centro = new javax.swing.table.DefaultTableCellRenderer();
            centro.setHorizontalAlignment(javax.swing.JLabel.CENTER);
            jTableEmpresa.getColumnModel().getColumn(0).setCellRenderer(centro);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro ao carregar tabela: " + e.getMessage());
        }
    }

    private void salvar() {

        String fantasia = jTextFieldFantasia.getText().trim();
        String razao = jTextFieldRazaoSocial.getText().trim();
        String cnpj = jTextFieldCNPJ.getText().trim();

        if (fantasia.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Informe o Nome Fantasia");
            jTextFieldFantasia.requestFocus();
            return;
        }

        if (razao.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Informe a Razão Social");
            jTextFieldRazaoSocial.requestFocus();
            return;
        }

        if (cnpj.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Informe o CNPJ");
            jTextFieldCNPJ.requestFocus();
            return;
        }

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar()) {

            if (jRadioButtonNovo.isSelected()) {

                String sql = "INSERT INTO \"Empresa\" (\"NOME_FANTASIA\", \"RAZAO_SOCIAL\", \"CNPJ\") VALUES (?, ?, ?)";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, fantasia);
                pst.setString(2, razao);
                pst.setString(3, cnpj);

                pst.executeUpdate();

                javax.swing.JOptionPane.showMessageDialog(this, "Salvo com sucesso!");

            } else if (jRadioEditar.isSelected()) {

                if (jTextFieldIDEmpresa.getText().isEmpty()) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Selecione um registro na tabela");
                    return;
                }

                String sql = "UPDATE \"Empresa\" SET \"NOME_FANTASIA\"=?, \"RAZAO_SOCIAL\"=?, \"CNPJ\"=? WHERE \"ID_EMPRESA\"=?";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, fantasia);
                pst.setString(2, razao);
                pst.setString(3, cnpj);
                pst.setInt(4, Integer.parseInt(jTextFieldIDEmpresa.getText()));

                pst.executeUpdate();

                javax.swing.JOptionPane.showMessageDialog(this, "Atualizado com sucesso!");
            }

            limparCampos();
            carregarTabela();

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void limparCampos() {
        jTextFieldIDEmpresa.setText("");
        jTextFieldFantasia.setText("");
        jTextFieldRazaoSocial.setText("");
        jTextFieldCNPJ.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldIDEmpresa = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldFantasia = new javax.swing.JTextField();
        jLabelRazaoSocial = new javax.swing.JLabel();
        jTextFieldRazaoSocial = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldCNPJ = new javax.swing.JTextField();
        jButtonSalvar = new javax.swing.JButton();
        jRadioButtonNovo = new javax.swing.JRadioButton();
        jRadioEditar = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableEmpresa = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cadastro de Empresas");

        jLabel1.setText("ID");

        jLabel2.setText("Nome Fantasia");

        jLabelRazaoSocial.setText("Razão Social");

        jLabel3.setText("CNPJ");

        jButtonSalvar.setText("Salvar");
        jButtonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButtonNovo);
        jRadioButtonNovo.setText("Novo");
        jRadioButtonNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNovoActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioEditar);
        jRadioEditar.setText("Editar");
        jRadioEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioEditarActionPerformed(evt);
            }
        });

        jTableEmpresa.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableEmpresa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableEmpresaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableEmpresa);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioButtonNovo)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioEditar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(43, 43, 43)
                                .addComponent(jLabel2)
                                .addGap(91, 91, 91)
                                .addComponent(jLabel3))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextFieldIDEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(jTextFieldFantasia, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldCNPJ, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabelRazaoSocial)
                            .addComponent(jTextFieldRazaoSocial, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 6, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel3)))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldIDEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldFantasia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextFieldCNPJ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addComponent(jLabelRazaoSocial)
                .addGap(6, 6, 6)
                .addComponent(jTextFieldRazaoSocial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRadioButtonNovo)
                        .addComponent(jRadioEditar))
                    .addComponent(jButtonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalvarActionPerformed
        if (!jRadioButtonNovo.isSelected() && !jRadioEditar.isSelected()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecione uma opção antes de salvar.");
        } else {
            salvar();
        }
    }//GEN-LAST:event_jButtonSalvarActionPerformed

    private void jRadioButtonNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNovoActionPerformed
        jRadioEditar.setSelected(false);
        limparCampos();
    }//GEN-LAST:event_jRadioButtonNovoActionPerformed

    private void jRadioEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioEditarActionPerformed
        jRadioButtonNovo.setSelected(false);

    }//GEN-LAST:event_jRadioEditarActionPerformed

    private void jTableEmpresaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableEmpresaMouseClicked
        jRadioButtonNovo.setSelected(false);
        jRadioEditar.setSelected(false);

    }//GEN-LAST:event_jTableEmpresaMouseClicked

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
            java.util.logging.Logger.getLogger(TelaCadEmpresa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaCadEmpresa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaCadEmpresa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaCadEmpresa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaCadEmpresa().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonSalvar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelRazaoSocial;
    private javax.swing.JRadioButton jRadioButtonNovo;
    private javax.swing.JRadioButton jRadioEditar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableEmpresa;
    private javax.swing.JTextField jTextFieldCNPJ;
    private javax.swing.JTextField jTextFieldFantasia;
    private javax.swing.JTextField jTextFieldIDEmpresa;
    private javax.swing.JTextField jTextFieldRazaoSocial;
    // End of variables declaration//GEN-END:variables
}
