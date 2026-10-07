package br.com.Visao;

import br.com.Conexao.ConexaoPostgres;
import br.com.Utilidades.Criptografia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaCadUsusarios extends javax.swing.JFrame {

    public TelaCadUsusarios() {
        initComponents();

        // Configurações iniciais de tela
        this.setLocationRelativeTo(null);
        jTextFieldID.setEnabled(false); // ID autoincremento, bloqueado para edição
        jCheckBoxAlterarSenha.setVisible(false);
        preencherComboNivel();
        preencherTabela();
        limparCampos();
        
        configurarEstadoCampos();
    }

    // =========================================================================
    // MÉTODOS DE BANCO DE DADOS (PostgreSQL + HikariCP)
    // =========================================================================
    private void preencherTabela() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Nível"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT \"ID\", \"NOME\", \"NIVEL\" "
                   + "FROM \"Usuario\" "
                   + "ORDER BY UPPER(\"NOME\") ASC";

        try (Connection conn = ConexaoPostgres.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ID"),
                    rs.getString("NOME"),
                    rs.getInt("NIVEL")
                });
            }
            jTableUsuarios.setModel(model);
            ajustarColunasUsuarios(jTableUsuarios);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar tabela: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean existeUsuarioComMesmoNome(String nome, String idAtual) {
        String sql = "SELECT \"ID\" "
                + "FROM \"Usuario\" "
                + "WHERE UPPER(\"NOME\") = UPPER(?)";

        if (idAtual != null && !idAtual.trim().isEmpty()) {
            sql += " AND \"ID\" <> ?";
        }

        try ( Connection conn = ConexaoPostgres.conectar();  PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome.trim());
            if (idAtual != null && !idAtual.trim().isEmpty()) {
                ps.setInt(2, Integer.parseInt(idAtual));
            }

            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao verificar duplicidade: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return true;
        }
    }

    private void salvarNovoUsuario() {
        String nome = jTextFieldUsuario.getText().trim();
        String senha1 = new String(jPasswordFieldSenha1.getPassword());
        String senha2 = new String(jPasswordFieldSenha2.getPassword());

        // 1. Validação primeiro (antes de tentar converter para int)
        if (nome.isEmpty() || jComboBoxNivel.getSelectedIndex() == 0 || senha1.isEmpty() || senha2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatórios!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Agora que sabemos que o índice é > 0, fazemos o parse com segurança
        int nivel = Integer.parseInt(jComboBoxNivel.getSelectedItem().toString());

        if (!senha1.equals(senha2)) {
            JOptionPane.showMessageDialog(this, "As senhas digitadas não conferem!", "Atenção", JOptionPane.WARNING_MESSAGE);
            jPasswordFieldSenha1.setText("");
            jPasswordFieldSenha2.setText("");
            jPasswordFieldSenha1.requestFocus();
            return;
        }

        if (existeUsuarioComMesmoNome(nome, null)) {
            JOptionPane.showMessageDialog(this, "Já existe um usuário cadastrado com este nome!", "Atenção", JOptionPane.WARNING_MESSAGE);
            jTextFieldUsuario.requestFocus();
            return;
        }

        String senhaCriptografada = Criptografia.criptografarSenha(senha1);

        String sql = "INSERT INTO \"Usuario\" (\"NOME\", \"SENHA\", \"NIVEL\") "
                   + "VALUES (?, ?, ?)";

        try (Connection conn = ConexaoPostgres.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, senhaCriptografada);
            ps.setInt(3, nivel);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            limparCampos();
            preencherTabela();
            jTableUsuarios.setEnabled(true);
            jTextFieldUsuario.requestFocus();
            buttonGroupFuncao.clearSelection();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarUsuario() {
        String idStr = jTextFieldID.getText().trim();
        String nome = jTextFieldUsuario.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para editar!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nome.isEmpty() || jComboBoxNivel.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Preencha os campos Nome e Nível!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int nivel = Integer.parseInt(jComboBoxNivel.getSelectedItem().toString());

        if (existeUsuarioComMesmoNome(nome, idStr)) {
            JOptionPane.showMessageDialog(this, "Já existe outro usuário com este nome!", "Atenção", JOptionPane.WARNING_MESSAGE);
            jTextFieldUsuario.requestFocus();
            return;
        }

        boolean zerarSenha = jCheckBoxAlterarSenha.isSelected();

        // Se zerarSenha for true, atualiza SENHA para NULL. Se false, não mexe no campo SENHA.
        String sql;
        if (zerarSenha) {
            sql = "UPDATE \"Usuario\" SET \"NOME\" = ?, \"NIVEL\" = ?, \"SENHA\" = NULL WHERE \"ID\" = ?";
        } else {
            sql = "UPDATE \"Usuario\" SET \"NOME\" = ?, \"NIVEL\" = ? WHERE \"ID\" = ?";
        }

        try (Connection conn = ConexaoPostgres.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setInt(2, nivel);
            ps.setInt(3, Integer.parseInt(idStr));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuário atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            limparCampos();
            preencherTabela();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirUsuario() {
        String idStr = jTextFieldID.getText().trim();
        String nome = jTextFieldUsuario.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para excluir!", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opcao = JOptionPane.showConfirmDialog(
                this,
                "Confirms exclusão do usuario(a) " + nome + " ?",
                "Confirmação de Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcao == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM \"Usuario\" "
                    + "WHERE \"ID\" = ?";

            try ( Connection conn = ConexaoPostgres.conectar();  PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, Integer.parseInt(idStr));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Usuário excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                limparCampos();
                preencherTabela();
                buttonGroupFuncao.clearSelection();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE TELA
    // =========================================================================
    private void preencherComboNivel() {
        jComboBoxNivel.removeAllItems();
        jComboBoxNivel.addItem("Selecione...");
        jComboBoxNivel.addItem("1");
        jComboBoxNivel.addItem("2");
        jComboBoxNivel.addItem("3");
        jComboBoxNivel.addItem("4");
        jComboBoxNivel.addItem("5");
    }

    private void limparCampos() {
        jTextFieldID.setText("");
        jTextFieldUsuario.setText("");
        jPasswordFieldSenha1.setText("");
        jPasswordFieldSenha2.setText("");
        jComboBoxNivel.setSelectedIndex(0);
        jTableUsuarios.clearSelection();
        jCheckBoxAlterarSenha.setSelected(false);
    }

    private void configurarEstadoCampos() {
        if (jRadioButtonNovo.isSelected()) {
            jTextFieldUsuario.setEnabled(true);
            jComboBoxNivel.setEnabled(true);
            jPasswordFieldSenha1.setEnabled(true);
            jPasswordFieldSenha2.setEnabled(true);
            jButtonSalvar.setEnabled(true);
            limparCampos();
            jTextFieldUsuario.requestFocus();

        } else if (jRadioButtonEditar.isSelected()) {
            jTextFieldUsuario.setEnabled(true);
            jComboBoxNivel.setEnabled(true);
            jPasswordFieldSenha1.setEnabled(false);
            jPasswordFieldSenha2.setEnabled(false);
            jButtonSalvar.setEnabled(true);

        } else if (jRadioButtonExcluir.isSelected()) {
            jTextFieldUsuario.setEnabled(false);
            jComboBoxNivel.setEnabled(false);
            jPasswordFieldSenha1.setEnabled(false);
            jPasswordFieldSenha2.setEnabled(false);
            jButtonSalvar.setEnabled(true); // Confirma a exclusão do registro selecionado
        }
    }

    private void selecionarLinhaTabela() {
        int linha = jTableUsuarios.getSelectedRow();
        if (linha != -1) {
            jTextFieldID.setText(jTableUsuarios.getValueAt(linha, 0).toString());
            jTextFieldUsuario.setText(jTableUsuarios.getValueAt(linha, 1).toString());
            jComboBoxNivel.setSelectedItem(jTableUsuarios.getValueAt(linha, 2).toString());

            jPasswordFieldSenha1.setText("");
            jPasswordFieldSenha2.setText("");
        }
    }

    private void ajustarColunasUsuarios(javax.swing.JTable tabela) {
        // Permite que as colunas se ajustem até preencher o tamanho total da JTable
        tabela.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Coluna 0: ID (Tamanho fixo/curto)
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(0).setMaxWidth(80);

        // Coluna 1: Nome (Sem limite máximo -> vai esticar até o final da tela)
        tabela.getColumnModel().getColumn(1).setPreferredWidth(300);

        // Coluna 2: Nível (Tamanho fixo/curto)
        tabela.getColumnModel().getColumn(2).setPreferredWidth(60);
        tabela.getColumnModel().getColumn(2).setMaxWidth(100);
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
        jTextFieldID = new javax.swing.JTextField();
        jTextFieldUsuario = new javax.swing.JTextField();
        jPasswordFieldSenha1 = new javax.swing.JPasswordField();
        jComboBoxNivel = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableUsuarios = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jRadioButtonNovo = new javax.swing.JRadioButton();
        jRadioButtonEditar = new javax.swing.JRadioButton();
        jRadioButtonExcluir = new javax.swing.JRadioButton();
        jPasswordFieldSenha2 = new javax.swing.JPasswordField();
        jButtonSalvar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxAlterarSenha = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manutenção de Usuários");

        jTextFieldID.setEnabled(false);

        jPasswordFieldSenha1.setBackground(new java.awt.Color(255, 255, 51));
        jPasswordFieldSenha1.setText("jPasswordField1");
        jPasswordFieldSenha1.setEnabled(false);

        jComboBoxNivel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jTableUsuarios.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableUsuariosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableUsuarios);

        jLabel1.setText("ID");

        jLabel2.setText("Usuario");

        jLabel3.setText("Nivel de Acesso");

        jLabel4.setText("Senha");

        buttonGroupFuncao.add(jRadioButtonNovo);
        jRadioButtonNovo.setText("Novo");
        jRadioButtonNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonNovoActionPerformed(evt);
            }
        });

        buttonGroupFuncao.add(jRadioButtonEditar);
        jRadioButtonEditar.setText("Editar");
        jRadioButtonEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonEditarActionPerformed(evt);
            }
        });

        buttonGroupFuncao.add(jRadioButtonExcluir);
        jRadioButtonExcluir.setText("Excluir");
        jRadioButtonExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonExcluirActionPerformed(evt);
            }
        });

        jPasswordFieldSenha2.setBackground(new java.awt.Color(255, 255, 51));
        jPasswordFieldSenha2.setText("jPasswordField1");
        jPasswordFieldSenha2.setEnabled(false);

        jButtonSalvar.setText("Salvar");
        jButtonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSalvarActionPerformed(evt);
            }
        });

        jLabel5.setText("Confimar Senha");

        jCheckBoxAlterarSenha.setText("Redefinir Senha.");
        jCheckBoxAlterarSenha.setToolTipText("Marque para cadastrar uma nova senha ao acessar o sistema.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBoxNivel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jRadioButtonNovo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButtonEditar)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jRadioButtonExcluir)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(39, 39, 39))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jPasswordFieldSenha1, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                                        .addComponent(jPasswordFieldSenha2))
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxAlterarSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButtonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(14, 14, 14))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPasswordFieldSenha1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxNivel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonNovo)
                    .addComponent(jRadioButtonEditar)
                    .addComponent(jRadioButtonExcluir)
                    .addComponent(jPasswordFieldSenha2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxAlterarSenha))
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSalvarActionPerformed
        if (jTextFieldUsuario.getText().equals("CRISTIANO")){
            JOptionPane.showMessageDialog(this, "Este usuario não pode ser alterado ou excluído.", "Atenção", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (jRadioButtonNovo.isSelected()) {
            salvarNovoUsuario();
        } else if (jRadioButtonEditar.isSelected()) {
            editarUsuario();
        } else if (jRadioButtonExcluir.isSelected()) {
            excluirUsuario();
        }else{
            JOptionPane.showMessageDialog(this, "Selecione Novo, Editar, ou Excluir.", "Atenção", JOptionPane.INFORMATION_MESSAGE);
        }
        jCheckBoxAlterarSenha.setSelected(false);
        
        
        
        
    }//GEN-LAST:event_jButtonSalvarActionPerformed

    private void jRadioButtonNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonNovoActionPerformed
        configurarEstadoCampos();
        jTableUsuarios.setEnabled(false);
        jCheckBoxAlterarSenha.setSelected(false);
        jCheckBoxAlterarSenha.setVisible(false);

    }//GEN-LAST:event_jRadioButtonNovoActionPerformed

    private void jRadioButtonEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonEditarActionPerformed
        configurarEstadoCampos();
        jTableUsuarios.setEnabled(true);
        jCheckBoxAlterarSenha.setVisible(true);
    }//GEN-LAST:event_jRadioButtonEditarActionPerformed

    private void jRadioButtonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonExcluirActionPerformed
        configurarEstadoCampos();
        jTableUsuarios.setEnabled(true);
        jCheckBoxAlterarSenha.setSelected(false);
        jCheckBoxAlterarSenha.setVisible(false);
    }//GEN-LAST:event_jRadioButtonExcluirActionPerformed

    private void jTableUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableUsuariosMouseClicked
        selecionarLinhaTabela();
        
        
    }//GEN-LAST:event_jTableUsuariosMouseClicked

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
            java.util.logging.Logger.getLogger(TelaCadUsusarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaCadUsusarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaCadUsusarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaCadUsusarios.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaCadUsusarios().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupFuncao;
    private javax.swing.JButton jButtonSalvar;
    private javax.swing.JCheckBox jCheckBoxAlterarSenha;
    private javax.swing.JComboBox<String> jComboBoxNivel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPasswordField jPasswordFieldSenha1;
    private javax.swing.JPasswordField jPasswordFieldSenha2;
    private javax.swing.JRadioButton jRadioButtonEditar;
    private javax.swing.JRadioButton jRadioButtonExcluir;
    private javax.swing.JRadioButton jRadioButtonNovo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableUsuarios;
    private javax.swing.JTextField jTextFieldID;
    private javax.swing.JTextField jTextFieldUsuario;
    // End of variables declaration//GEN-END:variables
}
