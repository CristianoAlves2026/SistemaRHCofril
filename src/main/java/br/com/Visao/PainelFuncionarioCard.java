package br.com.Visao;

public class PainelFuncionarioCard extends javax.swing.JPanel {

    private javax.swing.JLabel jLabelNome;
    private javax.swing.JLabel jLabelSetor;
    private javax.swing.JLabel jLabelStatus;
    private int targetY;
    
public void setNome(String nome) {
    jLabelNome.setText(nome);
}

public void setSetor(String setor) {
    jLabelSetor.setText(setor);
}

public void setStatus(String status) {
    jLabelStatus.setText(status);
}
    
    public PainelFuncionarioCard() {

        setLayout(null);
        setSize(300, 80);
        setPreferredSize(new java.awt.Dimension(300, 80));
        setMinimumSize(new java.awt.Dimension(300, 80));
        setMaximumSize(new java.awt.Dimension(300, 80));
        setBackground(new java.awt.Color(45, 45, 45));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(70, 70, 70)));

        setBackground(new java.awt.Color(50, 50, 50));

        jLabelNome = new javax.swing.JLabel();
        jLabelSetor = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();

        jLabelNome.setBounds(10, 10, 280, 20);
        jLabelSetor.setBounds(10, 30, 280, 20);
        jLabelStatus.setBounds(10, 50, 280, 20);

        jLabelNome.setForeground(java.awt.Color.WHITE);
        jLabelSetor.setForeground(java.awt.Color.LIGHT_GRAY);
        jLabelStatus.setForeground(java.awt.Color.GREEN);

        add(jLabelNome);
        add(jLabelSetor);
        add(jLabelStatus);

    }
    
    public void animarEntrada() {

    int startY = getY() + 20;
    targetY = getY();

    setLocation(getX(), startY);

    javax.swing.Timer timer = new javax.swing.Timer(10, null);

    timer.addActionListener(e -> {

        int y = getY();

        if (y <= targetY) {
            setLocation(getX(), targetY);
            timer.stop();
        } else {
            setLocation(getX(), y - 2);
        }

    });

    timer.start();
}

    
}
