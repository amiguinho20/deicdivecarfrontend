package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.util.Map;

import javax.enterprise.context.RequestScoped;

import org.primefaces.model.chart.PieChartModel;

@RequestScoped
public class MontarGrafico {

	public PieChartModel pizza(Map<String, Integer> resultados, String titulo, String legendPosition, boolean showDataLabels){
	
		PieChartModel grafico = new PieChartModel();
		grafico.setTitle(titulo);
		grafico.setLegendPosition(legendPosition); 
		
		for (Map.Entry<String, Integer> entry : resultados.entrySet() )
		{
			grafico.set(entry.getKey() + " : " + entry.getValue(), entry.getValue());
		}
		
		//graficoPizzaFlagrante.setFill(false);
		grafico.setShowDataLabels(showDataLabels);
		//graficoPizzaFlagrante.setDiameter(150);
		return grafico;
	}
	
	
}
