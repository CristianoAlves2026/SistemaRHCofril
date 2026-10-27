/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package br.com.Visao;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import java.io.File;
import java.util.Arrays;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Lavanderia
 */
public class TelaVisualizarPDF extends javax.swing.JFrame {

    private PDDocument documento;
    private PDFRenderer renderer;
    //private final String PASTA_RAIZ = "F:\\RH\\Fichas de Funcionários";
    private final String PASTA_RAIZ = "X:\\Ronalson\\DEMITIDOS";
    private int paginaAtual = 0;
    private int totalPaginas = 0;

    private float zoom = 1.5f;
    private File[] listaArquivosPDF;

    /**
     * Creates new form TelaVisualizarPDF
     */
    public TelaVisualizarPDF() {
        initComponents();

    }

    public TelaVisualizarPDF(String cpf) {

        initComponents();

        //jLabelPath.setText("F:\\RH\\Fichas de Funcionários");
        setExtendedState(MAXIMIZED_BOTH);
        jLabelPDF.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPDF.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        carregarListaPDFs(cpf);

    }

    private File[] localizarPDFs(String cpf) {

        try {

            return java.nio.file.Files.walk(new File(PASTA_RAIZ).toPath())
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .filter(path -> {

                        String nome = path.getFileName().toString().toLowerCase();

                        // Remove a extensão .pdf
                        nome = nome.substring(0, nome.length() - 4);

                        // Remove qualquer caractere que não seja número
                        nome = nome.replaceAll("\\D", "");

                        // Remove máscara do CPF informado
                        String cpfPesquisa = cpf.replaceAll("\\D", "");

                        return nome.contains(cpfPesquisa);

                    })
                    .map(java.nio.file.Path::toFile)
                    .sorted((a, b) -> a.getAbsolutePath()
                    .compareToIgnoreCase(b.getAbsolutePath()))
                    .toArray(File[]::new);

        } catch (Exception ex) {

            ex.printStackTrace();

            return new File[0];

        }

    }

    private void abrirPDF(String caminhoPDF) {

        try {

            if (documento != null) {
                documento.close();
            }

            documento = Loader.loadPDF(new File(caminhoPDF));

            renderer = new PDFRenderer(documento);

            totalPaginas = documento.getNumberOfPages();

            paginaAtual = 0;

            mostrarPagina();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );

        }

    }

    private void mostrarPagina() {

        try {

            BufferedImage imagem
                    = renderer.renderImage(paginaAtual, zoom);

            jLabelPDF.setIcon(new ImageIcon(imagem));

            jLabelPDF.setPreferredSize(new Dimension(
                    imagem.getWidth(),
                    imagem.getHeight()));

            lblPagina.setText(
                    "Página "
                    + (paginaAtual + 1)
                    + " de "
                    + totalPaginas
            );

            lblZoom.setText((int) (zoom * 100) + "%");

            btnAnterior.setEnabled(paginaAtual > 0);

            btnProximo.setEnabled(
                    paginaAtual < totalPaginas - 1
            );

            jLabelPDF.revalidate();
            jLabelPDF.repaint();

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(this,
                    ex.getMessage());

        }

    }

    private void carregarListaPDFs(String cpf) {

        DefaultTableModel modelo
                = (DefaultTableModel) jTableArquivos.getModel();

        modelo.setRowCount(0);

        listaArquivosPDF = localizarPDFs(cpf);

        if (listaArquivosPDF.length == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Nenhum documento foi digitalizado para o CPF:\n\n" + cpf
            );

            return;

        }

        for (File arquivo : listaArquivosPDF) {

            modelo.addRow(new Object[]{
                arquivo.getName()
            });

        }

        jTableArquivos.setRowSelectionInterval(0, 0);

        abrirPDF(listaArquivosPDF[0].getAbsolutePath());

    }

    @Override
    public void dispose() {

        try {

            if (documento != null) {

                documento.close();

            }

        } catch (Exception e) {

        }

        super.dispose();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnAnterior = new javax.swing.JButton();
        btnProximo = new javax.swing.JButton();
        btnZoomMais = new javax.swing.JButton();
        btnZoomMenos = new javax.swing.JButton();
        lblZoom = new javax.swing.JLabel();
        lblPagina = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPanePDF = new javax.swing.JScrollPane();
        jLabelPDF = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableArquivos = new javax.swing.JTable();
        jLabelPath = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        btnAnterior.setText("<< Anteiror");
        btnAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnteriorActionPerformed(evt);
            }
        });

        btnProximo.setText("Próxima >>");
        btnProximo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProximoActionPerformed(evt);
            }
        });

        btnZoomMais.setText("+");
        btnZoomMais.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomMaisActionPerformed(evt);
            }
        });

        btnZoomMenos.setText("-");
        btnZoomMenos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomMenosActionPerformed(evt);
            }
        });

        lblZoom.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblZoom.setText("150%");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(323, Short.MAX_VALUE)
                .addComponent(btnAnterior)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnProximo)
                .addGap(286, 286, 286)
                .addComponent(lblZoom, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnZoomMais)
                .addGap(7, 7, 7)
                .addComponent(btnZoomMenos)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAnterior)
                    .addComponent(btnProximo)
                    .addComponent(btnZoomMais)
                    .addComponent(btnZoomMenos)
                    .addComponent(lblZoom))
                .addContainerGap())
        );

        lblPagina.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPagina.setText("Página");

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(300, 200));

        jScrollPanePDF.setViewportView(jLabelPDF);

        jSplitPane1.setRightComponent(jScrollPanePDF);

        jTableArquivos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Documento"
            }
        ));
        jTableArquivos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableArquivosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTableArquivos);

        jSplitPane1.setLeftComponent(jScrollPane2);

        jLabelPath.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabelPath, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblPagina, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPagina)
                    .addComponent(jLabelPath))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnteriorActionPerformed
        if (paginaAtual > 0) {

            paginaAtual--;

            mostrarPagina();

        }
    }//GEN-LAST:event_btnAnteriorActionPerformed

    private void btnProximoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProximoActionPerformed
        if (paginaAtual < totalPaginas - 1) {

            paginaAtual++;

            mostrarPagina();

        }
    }//GEN-LAST:event_btnProximoActionPerformed

    private void btnZoomMaisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomMaisActionPerformed
        zoom += 0.25f;

        mostrarPagina();
    }//GEN-LAST:event_btnZoomMaisActionPerformed

    private void btnZoomMenosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomMenosActionPerformed
        if (zoom > 0.50f) {

            zoom -= 0.25f;

            mostrarPagina();

        }
    }//GEN-LAST:event_btnZoomMenosActionPerformed

    private void jTableArquivosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableArquivosMouseClicked
        int linha = jTableArquivos.getSelectedRow();

        if (linha == -1) {
            return;
        }

        abrirPDF(listaArquivosPDF[linha].getAbsolutePath());

    }//GEN-LAST:event_jTableArquivosMouseClicked

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
            java.util.logging.Logger.getLogger(TelaVisualizarPDF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaVisualizarPDF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaVisualizarPDF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaVisualizarPDF.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaVisualizarPDF().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnterior;
    private javax.swing.JButton btnProximo;
    private javax.swing.JButton btnZoomMais;
    private javax.swing.JButton btnZoomMenos;
    private javax.swing.JLabel jLabelPDF;
    private javax.swing.JLabel jLabelPath;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPanePDF;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableArquivos;
    private javax.swing.JLabel lblPagina;
    private javax.swing.JLabel lblZoom;
    // End of variables declaration//GEN-END:variables
}
