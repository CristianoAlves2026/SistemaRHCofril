/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Visao;

import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author Lavanderia
 */
public class TelaDocumentos extends javax.swing.JFrame {

    private final int idFuncionario;
    private Map<javax.swing.JCheckBox, String> mapaDocumentos;
    private javax.swing.JDialog dialogProcessando;

    public TelaDocumentos() {
        //initComponents();
        this.idFuncionario = 0;
    }

    public TelaDocumentos(int idFuncionario) {
        initComponents();

        setLocationRelativeTo(null);
        this.idFuncionario = idFuncionario;
        inicializarMapaDocumentos();
        carregarDocumentosFuncionario();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {

                fecharComProcessamento();

            }
        });
    }

    private void fecharComProcessamento() {

        mostrarProcessando();

        new Thread(() -> {

            sincronizarDocumentos();

            fecharProcessando();

            dispose();

        }).start();
    }

    private void mostrarProcessando() {

        dialogProcessando = new javax.swing.JDialog(this, "Aguarde", true);

        javax.swing.JLabel label = new javax.swing.JLabel("Processando documentos...");

        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));

        dialogProcessando.add(label);

        dialogProcessando.pack();

        dialogProcessando.setLocationRelativeTo(this);

        new Thread(() -> dialogProcessando.setVisible(true)).start();
    }
    
    private void fecharProcessando() {

    if (dialogProcessando != null) {

        dialogProcessando.dispose();
    }
}

    private void inicializarMapaDocumentos() {

        mapaDocumentos = new HashMap<>();

        mapaDocumentos.put(jCheckBoxFoto, "jCheckBoxFoto");
        mapaDocumentos.put(jCheckBoxCTPS, "jCheckBoxCTPS");
        mapaDocumentos.put(jCheckBoxCriminais, "jCheckBoxCriminais");
        mapaDocumentos.put(jCheckBoxRG, "jCheckBoxRG");
        mapaDocumentos.put(jCheckBoxCPF, "jCheckBoxCPF");
        mapaDocumentos.put(jCheckBoxResidencia, "jCheckBoxResidencia");
        mapaDocumentos.put(jCheckBoxCTPSFoto, "jCheckBoxCTPSFoto");
        mapaDocumentos.put(jCheckBoxCasamento, "jCheckBoxCasamento");
        mapaDocumentos.put(jCheckBoxCPFConjuge, "jCheckBoxCPFConjuge");
        mapaDocumentos.put(jCheckBoxCertidaoFilhos, "jCheckBoxCertidaoFilhos");
        mapaDocumentos.put(jCheckBoxVacinação, "jCheckBoxVacinação");
        mapaDocumentos.put(jCheckBoxDecEscolar, "jCheckBoxDecEscolar");
        mapaDocumentos.put(jCheckBoxCPFFilhos, "jCheckBoxCPFFilhos");
    }

    private void carregarDocumentosFuncionario() {

    String sql = """
        SELECT "TIPO_DOC", "DATA"
        FROM "Documentos"
        WHERE "ID_FUNCIONARIO" = ?
    """;

    try (java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();
         java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {

        pst.setInt(1, idFuncionario);

        try (java.sql.ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                String tipoDoc = rs.getString("TIPO_DOC");

                java.sql.Date data = rs.getDate("DATA");

                marcarCheckBoxComData(tipoDoc, data);

            }
        }

    } catch (Exception e) {

        e.printStackTrace();
    }
}
    
    private void marcarCheckBoxComData(String tipoDoc, java.sql.Date data) {

    for (java.util.Map.Entry<javax.swing.JCheckBox, String> entry : mapaDocumentos.entrySet()) {

        if (entry.getValue().equals(tipoDoc)) {

            javax.swing.JCheckBox checkBox = entry.getKey();

            checkBox.setSelected(true);

            aplicarEstiloDocumentoConfirmado(checkBox, data);
        }
    }
}
    
    private void aplicarEstiloDocumentoConfirmado(javax.swing.JCheckBox checkBox,
                                              java.sql.Date data) {

    java.text.SimpleDateFormat formato =
            new java.text.SimpleDateFormat("dd/MM/yyyy");

    String textoOriginal = checkBox.getText();

    String dataFormatada = formato.format(data);

    checkBox.setForeground(new java.awt.Color(0, 128, 0));

    checkBox.setText(textoOriginal + " - " + dataFormatada);
}

    private void sincronizarDocumentos() {

        processarCheckBox(jCheckBoxFoto);
        processarCheckBox(jCheckBoxCTPS);
        processarCheckBox(jCheckBoxCriminais);
        processarCheckBox(jCheckBoxRG);
        processarCheckBox(jCheckBoxCPF);
        processarCheckBox(jCheckBoxResidencia);
        processarCheckBox(jCheckBoxCTPSFoto);
        processarCheckBox(jCheckBoxCasamento);
        processarCheckBox(jCheckBoxCPFConjuge);
        processarCheckBox(jCheckBoxCertidaoFilhos);
        processarCheckBox(jCheckBoxVacinação);
        processarCheckBox(jCheckBoxDecEscolar);
        processarCheckBox(jCheckBoxCPFFilhos);

    }

    private void processarCheckBox(javax.swing.JCheckBox checkBox) {

        String tipoDoc = mapaDocumentos.get(checkBox);

        boolean existe = documentoExiste(tipoDoc);

        if (checkBox.isSelected() && !existe) {

            inserirDocumento(tipoDoc);

        } else if (!checkBox.isSelected() && existe) {

            excluirDocumento(tipoDoc);
        }
    }

    private boolean documentoExiste(String tipoDoc) {

        String sql = """
        SELECT 1
        FROM "Documentos"
        WHERE "ID_FUNCIONARIO" = ?
        AND "TIPO_DOC" = ?
        """;

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, idFuncionario);
            pst.setString(2, tipoDoc);

            java.sql.ResultSet rs = pst.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    private void inserirDocumento(String tipoDoc) {

        String sql = """
        INSERT INTO "Documentos"
        ("ID_FUNCIONARIO","TIPO_DOC","DATA")
        VALUES (?, ?, CURRENT_DATE)
        """;

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, idFuncionario);
            pst.setString(2, tipoDoc);

            pst.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void excluirDocumento(String tipoDoc) {

        String sql = """
        DELETE FROM "Documentos"
        WHERE "ID_FUNCIONARIO" = ?
        AND "TIPO_DOC" = ?
        """;

        try ( java.sql.Connection conn = br.com.Conexao.ConexaoPostgres.conectar();  java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, idFuncionario);
            pst.setString(2, tipoDoc);

            pst.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void marcarCheckBox(String tipoDoc) {

        for (Map.Entry<javax.swing.JCheckBox, String> entry : mapaDocumentos.entrySet()) {

            if (entry.getValue().equals(tipoDoc)) {

                entry.getKey().setSelected(true);
            }
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

        jCheckBoxResidencia = new javax.swing.JCheckBox();
        jCheckBoxCriminais = new javax.swing.JCheckBox();
        jCheckBoxRG = new javax.swing.JCheckBox();
        jCheckBoxCTPSFoto = new javax.swing.JCheckBox();
        jCheckBoxVacinação = new javax.swing.JCheckBox();
        jCheckBoxCasamento = new javax.swing.JCheckBox();
        jCheckBoxFoto = new javax.swing.JCheckBox();
        jCheckBoxCertidaoFilhos = new javax.swing.JCheckBox();
        jCheckBoxCPFConjuge = new javax.swing.JCheckBox();
        jCheckBoxCTPS = new javax.swing.JCheckBox();
        jCheckBoxCPF = new javax.swing.JCheckBox();
        jCheckBoxCPFFilhos = new javax.swing.JCheckBox();
        jCheckBoxDecEscolar = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ducumentos para Admissão");

        jCheckBoxResidencia.setText("Comprovante Residência");

        jCheckBoxCriminais.setText("Certidão de antecedentes criminais");

        jCheckBoxRG.setText("RG");

        jCheckBoxCTPSFoto.setText("CTPS - Página Foto (frente e verso)");

        jCheckBoxVacinação.setText("Carteira e vacinação filhos de 0 a 14 anos");

        jCheckBoxCasamento.setText("Ceridão de Casamento");

        jCheckBoxFoto.setText("2 Fotos 3x4");

        jCheckBoxCertidaoFilhos.setText("Certidão Nasc. filhos menores de 14 anos");

        jCheckBoxCPFConjuge.setText("CPF do Cônjuge");

        jCheckBoxCTPS.setText("CTPS - Carteira de Trabalho");

        jCheckBoxCPF.setText("CPF");

        jCheckBoxCPFFilhos.setText("CPF dos filhos");

        jCheckBoxDecEscolar.setText("Declaração escolar dos filhos de 07 a 14 anos");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Documentos Originais");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Cópias");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBoxVacinação, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCertidaoFilhos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCPFFilhos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxDecEscolar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCPFConjuge, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCasamento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxCTPSFoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxResidencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCPF, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxRG, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCriminais, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxCTPS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxFoto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxFoto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCTPS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCriminais)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRG)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCPF)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxResidencia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCTPSFoto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCasamento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCPFConjuge)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCertidaoFilhos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxVacinação)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxDecEscolar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxCPFFilhos)
                .addGap(17, 17, 17))
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
            java.util.logging.Logger.getLogger(TelaDocumentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaDocumentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaDocumentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaDocumentos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaDocumentos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxCPF;
    private javax.swing.JCheckBox jCheckBoxCPFConjuge;
    private javax.swing.JCheckBox jCheckBoxCPFFilhos;
    private javax.swing.JCheckBox jCheckBoxCTPS;
    private javax.swing.JCheckBox jCheckBoxCTPSFoto;
    private javax.swing.JCheckBox jCheckBoxCasamento;
    private javax.swing.JCheckBox jCheckBoxCertidaoFilhos;
    private javax.swing.JCheckBox jCheckBoxCriminais;
    private javax.swing.JCheckBox jCheckBoxDecEscolar;
    private javax.swing.JCheckBox jCheckBoxFoto;
    private javax.swing.JCheckBox jCheckBoxRG;
    private javax.swing.JCheckBox jCheckBoxResidencia;
    private javax.swing.JCheckBox jCheckBoxVacinação;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
