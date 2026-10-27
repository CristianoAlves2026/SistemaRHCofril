package br.com.Graficos;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;


import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import org.jfree.chart.util.Rotation;

public class PainelGraficoEmpresas extends JPanel {

    /**
     * Construtor da classe responsável por montar a interface gráfica do gráfico.
     */
    public PainelGraficoEmpresas() {
        setLayout(new BorderLayout());

        // 1. Busca os dados no banco usando o GraficoDAO
        GraficoDAO dao = new GraficoDAO();
        DefaultPieDataset dataset = dao.obterDadosAtivosPorEmpresa();

        // 2. Monta o gráfico de Pizza 3D
        JFreeChart chart = ChartFactory.createPieChart3D(
                "Funcionários Ativos por Empresa", // Título principal
                dataset,                           // Dataset com os dados
                true,                              // Exibir legenda
                true,                              // Exibir tooltips
                false                              // URLs
        );

        // 3. Estiliza os parâmetros 3D e transparência
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.65f);            // Transparência
        plot.setDepthFactor(0.12);                 // Espessura do efeito 3D
        
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));

        // 4. Encapsula o gráfico em um ChartPanel e adiciona ao JPanel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        add(chartPanel, BorderLayout.CENTER);
    }
}