package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroMapa;
import br.com.fences.deicdivecarfrontend.roubocarga.mb.FormatadorOcorrenciaMB;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Natureza;

/**
 * Classe auxiliar para montagem de mapa
 *
 */
@ApplicationScoped
public class MapaUtil {
	
	@Inject
	private FormatadorOcorrenciaMB formatadorOcorrenciaMB;

	private enum TipoMarcador { COMPLEMENTAR, RECEPTACAO, ROUBO_CARGA }
	
	/**
	 * Verifica se a ocorrencia possui filho(complemento) com natureza de recuperacao
	 * e se ambas possuem geocode para desenho de uma linha.
	 */
	public boolean verificarExibicaoDeLinha(Ocorrencia ocorrencia)
	{
		boolean exibir = false;
		for (Ocorrencia complementar : ocorrencia.getAuxiliar().getFilhos())
		{
			boolean nat = false;
			for (Natureza natureza : complementar.getNaturezas())
			{
				if (natureza.getIdOcorrencia().equals("40") && natureza.getIdEspecie().equals("40"))
				{
					nat = true;
					break;
				}
			}
			if (nat)
			{
				LatLng latLngPai = new LatLng(ocorrencia.getAuxiliar().getGeometry().getLatitude(), ocorrencia.getAuxiliar().getGeometry().getLongitude());
				LatLng latLngFilho = new LatLng(complementar.getAuxiliar().getGeometry().getLatitude(), complementar.getAuxiliar().getGeometry().getLongitude());
				
				if (latLngPai != null && latLngFilho != null)
				{
					if (latLngPai.getLat() != latLngFilho.getLat() && latLngPai.getLng() != latLngFilho.getLng() )
					{
						exibir = true;
					}
				}
			}
		}
		return exibir;
	}

	public void exibirNoMapa(Ocorrencia ocorrencia, MapModel mapModel, FiltroMapa filtroMapa)
	{
		LatLng latLng = new LatLng(ocorrencia.getAuxiliar().getGeometry().getLatitude(), ocorrencia.getAuxiliar().getGeometry().getLongitude());
		if (latLng != null)
		{
			String ocorrenciaFormatada = formatadorOcorrenciaMB.formatarOcorrencia(ocorrencia);
			String enderecoFormatado = formatadorOcorrenciaMB.formatarEndereco(ocorrencia);
			
			Enum<TipoMarcador> tipoMarcador = identificarMarcador(ocorrencia);
			String iconeMarcador = recuperarMarcador(tipoMarcador);
			
			if (	(!filtroMapa.isExibirComplementar() && tipoMarcador == TipoMarcador.COMPLEMENTAR)	||
					(!filtroMapa.isExibirReceptacao() && tipoMarcador == TipoMarcador.RECEPTACAO) ||
					(!filtroMapa.isExibirRoubo() && tipoMarcador == TipoMarcador.ROUBO_CARGA) 
				)
			{
				return;
			}
			else
			{
				Marker marcaNoMapa = new Marker(latLng, ocorrenciaFormatada + " - " + enderecoFormatado, ocorrencia, iconeMarcador);
				
				mapModel.addOverlay(marcaNoMapa);
			}
		}	
	}
	
	/**
	 * Identifica o tipo de marcador para a ocorrencia.
	 * Podendo ser Roubo de carga, e a partir desse ser Complementar ou Receptacao. 
	 */
	private Enum<TipoMarcador> identificarMarcador(Ocorrencia ocorrencia)
	{
		TipoMarcador tipoMarcador = TipoMarcador.ROUBO_CARGA;
		if (ocorrencia != null)
		{
			if (ocorrencia.getAnoReferenciaBo() != null && !ocorrencia.getAnoReferenciaBo().trim().isEmpty())
			{
				tipoMarcador = TipoMarcador.COMPLEMENTAR;
			}
			else
			{
				for (Natureza natureza : ocorrencia.getNaturezas())
				{
					List<String> naturezasReceptacao = Arrays.asList("180A", "180B", "180C");
					if (naturezasReceptacao.contains(natureza.getIdNatureza()))
					{
						tipoMarcador = TipoMarcador.RECEPTACAO;
						break;
					}
				}
			}
		}
		return tipoMarcador;
	}  

	private String recuperarMarcador(Enum<TipoMarcador> tipoMarcador)
	{
		String urlMarcador = "http://maps.google.com/mapfiles/ms/micons/red-dot.png"; //-- ROUBO_CARGA
		if (tipoMarcador == TipoMarcador.COMPLEMENTAR)
		{
			urlMarcador = "http://maps.google.com/mapfiles/ms/micons/green-dot.png";
		}
		else if (tipoMarcador == TipoMarcador.RECEPTACAO)
		{
			urlMarcador = "http://maps.google.com/mapfiles/ms/micons/blue-dot.png";
		}
		return urlMarcador;
	}
	
}
