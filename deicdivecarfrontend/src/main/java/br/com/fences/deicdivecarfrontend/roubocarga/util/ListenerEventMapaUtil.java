package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.map.GeocodeEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.event.map.PointSelectEvent;
import org.primefaces.event.map.ReverseGeocodeEvent;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.GeocodeResult;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Marker;

import br.com.fences.deicdivecarfrontend.roubocarga.mb.FormatadorOcorrenciaMB;
import br.com.fences.deicdivecarfrontend.roubocarga.mb.PesquisaMB;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;

/**
 * Classe auxiliar para o listener generico. Os managed bens acessam suas
 * propriedades para saber qual ponto no mapa foi selecionado.
 *
 */
@Named
@SessionScoped
public class ListenerEventMapaUtil implements Serializable {

	private static final long serialVersionUID = -8344920610127616345L;

	@Inject
	private transient Logger logger;

	@Inject
	private FormatadorOcorrenciaMB formatadorOcorrenciaMB;

	private Marker marcaSelecionada;

	private LatLng pontoSelecionado;
	private String pontoSelecionadoEndereco;
	
	//private MapModel geoModel;
	
	@Inject
	private PesquisaMB pesquisaMB;

	public void onMarkerSelect(OverlaySelectEvent event) {
		if (event != null && event.getOverlay() != null) {
			// -- anula o ponto, pois a marca foi selecionada
			setPontoSelecionado(null);

			if (event.getOverlay() instanceof Circle) {
				logger.debug("overlay do circulo");
			}
			if (event.getOverlay() instanceof Marker) {
				marcaSelecionada = (Marker) event.getOverlay();
				if (marcaSelecionada == null) {
					// logger.debug("A marca selecionada esta nula.");
				} else if (marcaSelecionada.getTitle() == null) {
					// logger.debug("O titulo da marca selecionada esta nulo.");
				} else if (marcaSelecionada.getData() == null) {
					// logger.debug("A informacao da marca selecionada esta
					// nula.");
				} else if (marcaSelecionada.getData() instanceof Ocorrencia) {
						Ocorrencia ocorrencia = (Ocorrencia) marcaSelecionada.getData();
						// logger.info("Marca selecionada: " +
						// formatadorOcorrenciaMB.formatarOcorrencia(ocorrencia)
						// + formatadorOcorrenciaMB.formatarEndereco(ocorrencia));
					
				}
			}
		}
	}

	public void onPointSelect(PointSelectEvent event) {

		if (event != null && event.getLatLng() != null) {
			setPontoSelecionado(event.getLatLng());
			setPontoSelecionadoEndereco("");

			// -- anula a marca, pois o ponto foi selecionado
			setMarcaSelecionada(null);
			
			String funcaoJavaScript = "geocodeReverso(" + event.getLatLng().getLat() + ", " + event.getLatLng().getLng() + ");";
			
			RequestContext.getCurrentInstance().execute(funcaoJavaScript);
		}
		logger.info("final do onPointSelect");
	}

	public void onReverseGeocode(ReverseGeocodeEvent event) {
		List<String> enderecos = event.getAddresses();
		//LatLng coord = event.getLatlng();

		if (Verificador.isValorado(enderecos))
		{
			setPontoSelecionadoEndereco(enderecos.get(0));
		}
		logger.info("Endereco: " + getPontoSelecionadoEndereco());
	}
	
	 public void onGeocode(GeocodeEvent event) {
		 List<GeocodeResult> results = event.getResults();
	         
		 if (results != null && !results.isEmpty()) 
		 {
			 LatLng center = results.get(0).getLatLng();
			 //centerGeoMap = center.getLat() + "," + center.getLng();
	             
			 for (int i = 0; i < results.size(); i++) {
				 GeocodeResult result = results.get(i);
				 pesquisaMB.setGeoModel(new DefaultMapModel());
				 pesquisaMB.getGeoModel().addOverlay(new Marker(result.getLatLng(), result.getAddress(), "", "http://maps.google.com/mapfiles/ms/micons/yellow-dot.png"));
				 pesquisaMB.setCentroMapa(center.getLat() + "," + center.getLng());
				 break;
			 }
		 }
	 }

	public Marker getMarcaSelecionada() {
		return marcaSelecionada;
	}

	public void setMarcaSelecionada(Marker marcaSelecionada) {
		this.marcaSelecionada = marcaSelecionada;
	}

	public LatLng getPontoSelecionado() {
		return pontoSelecionado;
	}

	public void setPontoSelecionado(LatLng pontoSelecionado) {
		this.pontoSelecionado = pontoSelecionado;
	}

	public String getPontoSelecionadoEndereco() {
		return pontoSelecionadoEndereco;
	}

	public void setPontoSelecionadoEndereco(String pontoSelecionadoEndereco) {
		this.pontoSelecionadoEndereco = pontoSelecionadoEndereco;
	}

//	public MapModel getGeoModel() {
//		return geoModel;
//	}
//
//	public void setGeoModel(MapModel geoModel) {
//		this.geoModel = geoModel;
//	}

}
