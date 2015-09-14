package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
//import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.omnifaces.util.Faces;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.chart.PieChartModel;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;

import br.com.fences.deicdivecarentidade.enderecoavulso.EnderecoAvulso;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.EnderecoAvulsoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.RdoRouboCargaReceptacaoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroMapa;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroRouboCargaReceptacao;
import br.com.fences.deicdivecarfrontend.roubocarga.util.MontarGrafico;
import br.com.fences.deicdivecarfrontend.roubocarga.util.RdoRouboCargaReceptacaoLazyDataModel;
import br.com.fences.fencesutils.formatar.FormatarData;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Natureza;



@Named
//@javax.faces.view.ViewScoped
@SessionScoped //-- por causa do mapa na segunda pagina   
public class RdoRouboCargaReceptacaoMB implements Serializable{

	private static final long serialVersionUID = 1866941789765596632L;
	
	@Inject
	private transient Logger logger;

	@Inject
	private FiltroRouboCargaReceptacao filtro;
	
	@Inject
	private RdoRouboCargaReceptacaoBO rdoRouboCargaReceptacaoBO;

	@Inject
	private EnderecoAvulsoBO enderecoAvulsoBO;
	
	@Inject
	private FiltroMapa filtroMapa;
	
	@Inject
	private MontarGrafico montarGrafico;
	
	
	private List<SelectItem> delegacias = new ArrayList<>();
	
	private List<SelectItem> tipoObjetos = new ArrayList<>();
	
	private Integer contagem;
	private LazyDataModel<Ocorrencia> ocorrenciasResultadoLazy;
	private List<Ocorrencia> ocorrenciasSelecionadas;

	private String centroMapa = "-23.538419906917593, -46.63483794999996";
	private MapModel geoModel;
	private Marker marcaSelecionada;
	
	private boolean informativoFuncionalidade;
	
	private enum TipoMarcador { COMPLEMENTAR, RECEPTACAO, ROUBO_CARGA }
	
	//--graficos
	private PieChartModel graficoPizzaFlagrante; 
	private PieChartModel graficoPizzaAno;            
	private PieChartModel graficoPizzaComplementar;   
	
	@PostConstruct
	private void init() {
		Map<String, String> mapDelegacias = rdoRouboCargaReceptacaoBO.listarDelegacias();
		mapParaSelectItem(mapDelegacias, delegacias);

		Map<String, String> mapTipoObjetos = rdoRouboCargaReceptacaoBO.listarTipoObjetos();
		mapParaSelectItem(mapTipoObjetos, tipoObjetos);
		
		String limiteDataInicial = rdoRouboCargaReceptacaoBO.pesquisarPrimeiraDataRegistro();
		filtro.setLimiteDataInicial(limiteDataInicial);
		String limiteDataFinal = rdoRouboCargaReceptacaoBO.pesquisarUltimaDataRegistro();
		filtro.setLimiteDataFinal(limiteDataFinal);

		
		//
		//setDelegacias(rdoRouboCargaReceptacaoBO.listarDelegacias());
		pesquisar();
	} 
	
	private void mapParaSelectItem(Map<String, String> mapOrigem, List<SelectItem> listSelectItemDestino)
	{
		if (Verificador.isValorado(mapOrigem))
		{
			if (listSelectItemDestino == null)
			{
				listSelectItemDestino = new ArrayList<SelectItem>();
			}
			for (Map.Entry<String, String> entry : mapOrigem.entrySet())
			{
				listSelectItemDestino.add(new SelectItem(entry.getKey(), entry.getValue()));
			}
		}
	}
	 
	public void mudarInformativoFuncionalidade(){
		informativoFuncionalidade = !informativoFuncionalidade;
	}
	
	/**
	 * A pesquisa lazy de fato e' feita apos o termino da execucao desse metodo, pelo primefaces.
	 * O filtro nao pode ser limpo aqui.
	 */
	public void pesquisar(){ 
		setOcorrenciasResultadoLazy(new RdoRouboCargaReceptacaoLazyDataModel(rdoRouboCargaReceptacaoBO, filtro));
		setContagem(getOcorrenciasResultadoLazy().getRowCount());
		montarGraficoPizzaAno();
		montarGraficoPizzaFlagrante();
		montarGraficoPizzaComplementar();
		
		//-- 
		if (ocorrenciasSelecionadas != null)  
		{
			ocorrenciasSelecionadas.clear();
		}
		limparMapa();
	}
 
	  
	/**
	 * Botao limpar. Faz a limpeza e executa a pesquisa
	 */
	public void limpar(){
		filtro.limpar();
		pesquisar();
	}
	
	public void limparMapa(){
		filtroMapa.limpar();
		geoModel = null;
	}

	
	@Deprecated
	public String montarIdade(String dataNascimento, String dataRegistroBo)
	{
		String idade = "";
		if (Verificador.isValorado(dataNascimento, dataRegistroBo))
		{
			try
			{
				Date dtNascimento = FormatarData.getAnoMesDiaContatenado().parse(dataNascimento);
				Date dtRegistroBo = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados().parse(dataRegistroBo);
				long lgNascimento = dtNascimento.getTime();
				long lgRegistroBo = dtRegistroBo.getTime();
				long dia = 86400000;
				long anoEmDias = 365;
				long lgIdade = (lgRegistroBo - lgNascimento) / dia / anoEmDias;
				idade = lgIdade + " anos";
			}
			catch (Exception e)
			{
				//-- descartar erro
			}
		}
		return idade;
	}
		
	public void montarGraficoPizzaFlagrante()
	{
		Map<String, Integer> resultados = rdoRouboCargaReceptacaoBO.agregarPorFlagrante(filtro);
		graficoPizzaFlagrante = montarGrafico.pizza(resultados, "Flagrante", "w", true);
	}
	
	public void montarGraficoPizzaAno()
	{
		Map<String, Integer> resultados = rdoRouboCargaReceptacaoBO.agregarPorAno(filtro);
		graficoPizzaAno = montarGrafico.pizza(resultados, "Ano", "w", true);
	}

	public void montarGraficoPizzaComplementar()
	{
		Map<String, Integer> resultados = rdoRouboCargaReceptacaoBO.agregarPorComplementar(filtro);
		graficoPizzaComplementar = montarGrafico.pizza(resultados, "Ocorrências que possuem complemento de recuperação/localização", "w", true);
	}
	
	public void exibirRegistrosSelecionadosNoMapa() 
	{
		geoModel = new DefaultMapModel();  	

/*
		//-- processar geocode dos Avulsos...
		if (filtroMapa.isExibirAvulsoDeposito() || filtroMapa.isExibirAvulsoDesmanche() || 
				filtroMapa.isExibirAvulsoGalpao() || filtroMapa.isExibirAvulsoMercado())
		{
			List<String> tipos = new ArrayList<>();
			if (filtroMapa.isExibirAvulsoDeposito()) tipos.add("Depósito");
			if (filtroMapa.isExibirAvulsoDesmanche()) tipos.add("Desmanche");
			if (filtroMapa.isExibirAvulsoGalpao()) tipos.add("Galpão");
			if (filtroMapa.isExibirAvulsoMercado()) tipos.add("Mercado");
			
			List<EnderecoAvulso> enderecosAvulsos = enderecoAvulsoBO.pesquisarAtivoPorTipo(tipos);
			
			for (EnderecoAvulso enderecoAvulso : enderecosAvulsos)
			{
				//-- nao processar geocode previo com ZERO_RESULTS
				if (Verificador.isValorado(enderecoAvulso.getGoogleGeocoderStatus()))
				{
					if (enderecoAvulso.getGoogleGeocoderStatus().equals("ZERO_RESULTS"))
					{
						continue;
					}
				}
				
				//-- verifica existencia previa de geoCode
				LatLng latLng = EnderecoGeocodeUtil
						.verificarExistenciaPreviaDeGeocode(
								enderecoAvulso.getGoogleLatitude(),
								enderecoAvulso.getGoogleLongitude());
				if (latLng == null)
				{
					String enderecoFormatado = EnderecoGeocodeUtil
							.concatenarEndereco(
									enderecoAvulso.getLogradouro(),
									enderecoAvulso.getNumero(),
									enderecoAvulso.getBairro(),
									enderecoAvulso.getCidade(),
									enderecoAvulso.getUf());
					try 
					{
						//-- consulta geoCode no google
						latLng = EnderecoGeocodeUtil.converterEnderecoEmLatLng(enderecoFormatado);
					}
					catch (GoogleLimiteAtingidoRuntimeException e)
					{
						Messages.create("Erro na pesquisa do Geocode no google").warn().detail(e.getMessage()).add();
						return;
					}
					catch (GoogleZeroResultsRuntimeException e)
					{
						enderecoAvulso.setGoogleGeocoderStatus("ZERO_RESULTS");
						//-- atualiza no banco
						enderecoAvulsoDAO.substituir(enderecoAvulso);
						//logger.info("atualizado como ZERO_RESULTS [" + enderecoFormatado + "]");
					}
					if (latLng != null)
					{
						enderecoAvulso.setGoogleLatitude(Double.toString(latLng.getLat()));
						enderecoAvulso.setGoogleLongitude(Double.toString(latLng.getLng()));
						enderecoAvulso.setGoogleGeocoderStatus("OK");
						//-- atualiza no banco
						enderecoAvulsoDAO.substituir(enderecoAvulso);
						//logger.info("atualizado como OK [" + enderecoFormatado + "]");
					}
				}
			}
		}
*/		
		
 
		//-- exibe no mapa apenas ocorrencias que contem geoCode pre-processado
		if (ocorrenciasSelecionadas != null)
		{
			for (Ocorrencia ocorrencia : ocorrenciasSelecionadas) 
			{
				if (filtroMapa.isExibirApenasLinhas())
				{
					if (!verificarExibicaoDeLinha(ocorrencia))
					{
						continue;
					}
				}
				
				exibirNoMapa(ocorrencia);
				
				for (Ocorrencia complementar : ocorrencia.getAuxiliar().getFilhos())
				{
					
					//--- add linha entre pai e filho
					if (filtroMapa.isExibirComplementar())
					{
						boolean exibir = false;
						for (Natureza natureza : complementar.getNaturezas())
						{
							if (natureza.getIdOcorrencia().equals("40") && natureza.getIdEspecie().equals("40"))
							{
								if (complementar.getAuxiliar().getGeocoderStatus().equals("OK"))
								{
									exibir = true;
									break;
								}
							}
						}
						if (exibir)
						{
							exibirNoMapa(complementar);
							
							LatLng latLngPai = new LatLng(ocorrencia.getAuxiliar().getGeometry().getLatitude(), ocorrencia.getAuxiliar().getGeometry().getLongitude());
							LatLng latLngFilho = new LatLng(complementar.getAuxiliar().getGeometry().getLatitude(), complementar.getAuxiliar().getGeometry().getLongitude());
							
							if (latLngPai != null && latLngFilho != null)
							{
								Polyline polyline = new Polyline();
								polyline.getPaths().add(latLngPai);
								polyline.getPaths().add(latLngFilho);
								
								polyline.setStrokeWeight(5);
								polyline.setStrokeColor("#FF0000");
							    polyline.setStrokeOpacity(0.4);
							    
							    if (geoModel != null)
							    {
							    	geoModel.addOverlay(polyline);
							    }
							}
						}
					}
				}
			}
		}
		
		
		//-- exibe endereco avulso previamente processado geocode
		if (filtroMapa.isExibirAvulsoDeposito() || filtroMapa.isExibirAvulsoDesmanche() || 
				filtroMapa.isExibirAvulsoGalpao() || filtroMapa.isExibirAvulsoMercado() || 
				filtroMapa.isExibirAvulsoCentroDeDistribuicao())
		{
			List<String> tipos = new ArrayList<>();
			if (filtroMapa.isExibirAvulsoDeposito()) tipos.add("Depósito");
			if (filtroMapa.isExibirAvulsoDesmanche()) tipos.add("Desmanche");
			if (filtroMapa.isExibirAvulsoGalpao()) tipos.add("Galpão");
			if (filtroMapa.isExibirAvulsoMercado()) tipos.add("Mercado");
			if (filtroMapa.isExibirAvulsoCentroDeDistribuicao()) tipos.add("Centro de distribuição");
			
			List<EnderecoAvulso> enderecosAvulsos = enderecoAvulsoBO.pesquisarAtivoPorTipo(tipos);
			
			for (EnderecoAvulso enderecoAvulso : enderecosAvulsos)
			{
				if (Verificador.isValorado(enderecoAvulso.getGeocoderStatus()) && enderecoAvulso.getGeocoderStatus().equals("OK")) 
				{
					String urlMarcador = "http://maps.google.com/mapfiles/ms/micons/yellow-dot.png";
					
					if (enderecoAvulso.getTipo().equalsIgnoreCase("Mercado"))
					{
						urlMarcador = Faces.getRequestContextPath() + "/resources/fences/images/iconeMapa/mercado.png";
					}
					if (enderecoAvulso.getTipo().equalsIgnoreCase("Depósito"))
					{
						urlMarcador = Faces.getRequestContextPath() + "/resources/fences/images/iconeMapa/deposito.png";
					}
					if (enderecoAvulso.getTipo().equalsIgnoreCase("Galpão"))
					{
						urlMarcador = Faces.getRequestContextPath() + "/resources/fences/images/iconeMapa/galpao.png";
					}
					if (enderecoAvulso.getTipo().equalsIgnoreCase("Desmanche"))
					{
						urlMarcador = Faces.getRequestContextPath() + "/resources/fences/images/iconeMapa/desmanche.png";
					}
					if (enderecoAvulso.getTipo().equalsIgnoreCase("Centro de distribuição"))
					{
						urlMarcador = Faces.getRequestContextPath() + "/resources/fences/images/iconeMapa/centrodedistribuicao.png";
					}

					
					String enderecoFormatado = concatenarEndereco(
							enderecoAvulso.getLogradouro(),
							enderecoAvulso.getNumero(),
							enderecoAvulso.getBairro(),
							enderecoAvulso.getCidade(),
							enderecoAvulso.getUf());
					
					String titulo = enderecoAvulso.getTipo() + " - " + enderecoFormatado;
					
					LatLng latLng = new LatLng(enderecoAvulso.getGeometry().getLatitude(), enderecoAvulso.getGeometry().getLongitude());
					
					Marker marcaNoMapa = new Marker(latLng, titulo, null, urlMarcador);
					
					geoModel.addOverlay(marcaNoMapa);
					
				}
			}
		}
	}
	
	public void listarRaio(Integer raioEmMetros)
	{
		if (marcaSelecionada == null || marcaSelecionada.getData() == null)
		{
			return;
		}
		
		Ocorrencia ocorrencia = (Ocorrencia) marcaSelecionada.getData();
		
		Double latitude = ocorrencia.getAuxiliar().getGeometry().getLatitude();
		Double longitude = ocorrencia.getAuxiliar().getGeometry().getLongitude();
		//Integer raioEmMetros = 10000;
		
		filtro.setLatitude(latitude.toString());
		filtro.setLongitude(longitude.toString());
		filtro.setRaioEmMetros(raioEmMetros.toString());  

		//-- pesquisar tradicional (montar lista paginada e atualizar total) com os filtros latitude, longitude e raioEmMetros
		pesquisar();
		
		//-- pesquisar no raio (limitado a 100 registros)
		List<Ocorrencia> ocorrenciasRetornadas = rdoRouboCargaReceptacaoBO.pesquisarLazy(filtro, 0, 100);
		
		//-- selecionar todos do resultado
		ocorrenciasSelecionadas.addAll(ocorrenciasRetornadas);

		//-- montar mapa
		exibirRegistrosSelecionadosNoMapa();
		
		//-- exibe circulo
		LatLng latLng = new LatLng(latitude, longitude);
		
        Circle circulo = new Circle(latLng, raioEmMetros);
        circulo.setStrokeColor("#d93c3c");
        circulo.setFillColor("#d93c3c");
        circulo.setFillOpacity(0.5);
        
        if (geoModel != null)
        {
        	geoModel.addOverlay(circulo);
        	//logger.info("imprimiu o circulo: " + circulo);
        }
	}
	
	public void onMarkerSelect(OverlaySelectEvent event) 
	{
		if (event != null && event.getOverlay() != null)
		{
			if (event.getOverlay() instanceof Circle)
			{
				logger.info("overlay do circulo");
			}
			if (event.getOverlay() instanceof Marker)
			{
				marcaSelecionada = (Marker) event.getOverlay();
				if (marcaSelecionada == null)
				{
					logger.debug("A marca selecionada esta nula.");
				}
				else if (marcaSelecionada.getTitle() == null)
				{
					logger.debug("O titulo da marca selecionada esta nulo.");
				}
				else if (marcaSelecionada.getData() == null)
				{
					logger.debug("A informacao da marca selecionada esta nula.");
				}
				else
				{
					Ocorrencia ocorrencia = (Ocorrencia) marcaSelecionada.getData();
					logger.info("Marca selecionada: " + formatarOcorrencia(ocorrencia) + formatarEndereco(ocorrencia));
				}
			}
		}
	}
	
	private boolean verificarExibicaoDeLinha(Ocorrencia ocorrencia)
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
	
	private void exibirNoMapa(Ocorrencia ocorrencia)
	{
		LatLng latLng = new LatLng(ocorrencia.getAuxiliar().getGeometry().getLatitude(), ocorrencia.getAuxiliar().getGeometry().getLongitude());
		if (latLng != null)
		{
			String ocorrenciaFormatada = formatarOcorrencia(ocorrencia);
			String enderecoFormatado = formatarEndereco(ocorrencia);
			
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
				
				geoModel.addOverlay(marcaNoMapa);
			}
		}	
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
	

	public String formatarOcorrencia(Ocorrencia ocorrencia)
	{
		StringBuilder ocorrenciaFormatada = new StringBuilder();
		if (ocorrencia != null)
		{
			ocorrenciaFormatada.append(ocorrencia.getNumBo()); 
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getAnoBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getNomeDelegacia());
		}
		return ocorrenciaFormatada.toString();
	}
	
	public String formatarReferencia(Ocorrencia ocorrencia)
	{
		StringBuilder ocorrenciaFormatada = new StringBuilder();
		if (ocorrencia != null)
		{
			ocorrenciaFormatada.append(ocorrencia.getNumReferenciaBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getAnoReferenciaBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getDelegaciaReferencia());
		}
		return ocorrenciaFormatada.toString();
	}
	
	public String formatarEndereco(Ocorrencia ocorrencia)
	{
		String endereco = concatenarEndereco(ocorrencia.getLogradouro(),
				ocorrencia.getNumeroLogradouro(),
				ocorrencia.getBairro(), ocorrencia.getCidade(),
				ocorrencia.getIdUf());
		return endereco;
	}
	
	private String concatenarEndereco(String... campos) 
	{
		String resultado = "";
		for (String campo : campos) 
		{
			if (campo != null && !campo.trim().isEmpty() && !campo.trim().equals("0"))
			{
				campo = campo.replaceAll(",", ""); //-- retirar virgulas adicionais
				if (!resultado.isEmpty())
				{
					resultado += ", ";
				} 
				resultado += campo.trim(); 					
			}
		}
		return resultado;
	}
	
	public String formatarData(String original){
		String formatacao = "";
		if (Verificador.isValorado(original))
		{
			try
			{
				if (original.length() > 10)
				{
					DateFormat dfOrigem = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados();
					DateFormat dfDestino = FormatarData.getDiaMesAnoComBarrasEHoraMinutoSegundoComDoisPontos();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
				else
				{
					DateFormat dfOrigem = FormatarData.getAnoMesDiaContatenado();
					DateFormat dfDestino = FormatarData.getDiaMesAnoComBarras();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
			}
			catch (ParseException e)
			{
				//@TODO nao tratar - temporario
				e.printStackTrace();
			}
				
		}
		return formatacao;
	}
	
	public String formatarGeocode(Ocorrencia ocorrencia)
	{
		String formatado = "";
		//logger.warn("INIBIDO formatarGeocode");
		/*
		LatLng latLng = EnderecoGeocodeUtil.converter(ocorrencia);
		if (latLng != null)
		{
			formatado = latLng.getLat() + ", " + latLng.getLng();
		}
		*/
		return formatado;
	}
	
	public String formatarComplementar(Ocorrencia complementar)
	{
		return formatarOcorrencia(complementar) + " - " + formatarEndereco(complementar);
	}
	
	public Integer getContagem() {
		return contagem;
	}

	public void setContagem(Integer contagem) {
		this.contagem = contagem;
	}

	public LazyDataModel<Ocorrencia> getOcorrenciasResultadoLazy() {
		return ocorrenciasResultadoLazy;
	}

	public void setOcorrenciasResultadoLazy(
			LazyDataModel<Ocorrencia> ocorrenciasResultadoLazy) {
		this.ocorrenciasResultadoLazy = ocorrenciasResultadoLazy;
	}

	public List<Ocorrencia> getOcorrenciasSelecionadas() {
		return ocorrenciasSelecionadas;
	}

	public void setOcorrenciasSelecionadas(List<Ocorrencia> ocorrenciasSelecionadas) {
		this.ocorrenciasSelecionadas = ocorrenciasSelecionadas;
	}

	public String getCentroMapa() {
		return centroMapa;
	}

	public void setCentroMapa(String centroMapa) {
		this.centroMapa = centroMapa;
	}

	public MapModel getGeoModel() {
		return geoModel;
	}

	public void setGeoModel(MapModel geoModel) {
		this.geoModel = geoModel;
	}

	public FiltroRouboCargaReceptacao getFiltro() {
		return filtro;
	}

	public void setFiltro(FiltroRouboCargaReceptacao filtro) {
		this.filtro = filtro;
	}

	public FiltroMapa getFiltroMapa() {
		return filtroMapa;
	}

	public void setFiltroMapa(FiltroMapa filtroMapa) {
		this.filtroMapa = filtroMapa;
	}

	public PieChartModel getGraficoPizzaFlagrante() {
		return graficoPizzaFlagrante;
	}

	public void setGraficoPizzaFlagrante(PieChartModel graficoPizzaFlagrante) {
		this.graficoPizzaFlagrante = graficoPizzaFlagrante;
	}

	public PieChartModel getGraficoPizzaAno() {
		return graficoPizzaAno;
	}

	public void setGraficoPizzaAno(PieChartModel graficoPizzaAno) {
		this.graficoPizzaAno = graficoPizzaAno;
	}

	public PieChartModel getGraficoPizzaComplementar() {
		return graficoPizzaComplementar;
	}

	public void setGraficoPizzaComplementar(PieChartModel graficoPizzaComplementar) {
		this.graficoPizzaComplementar = graficoPizzaComplementar;
	}

	public Marker getMarcaSelecionada() {
		return marcaSelecionada;
	}

	public void setMarcaSelecionada(Marker marcaSelecionada) {
		this.marcaSelecionada = marcaSelecionada;
	}

	public boolean isInformativoFuncionalidade() {
		return informativoFuncionalidade;
	}

	public void setInformativoFuncionalidade(boolean informativoFuncionalidade) {
		this.informativoFuncionalidade = informativoFuncionalidade;
	}

	public List<SelectItem> getDelegacias() {
		return delegacias;
	}

	public void setDelegacias(List<SelectItem> delegacias) {
		this.delegacias = delegacias;
	}

	public List<SelectItem> getTipoObjetos() {
		return tipoObjetos;
	}

	public void setTipoObjetos(List<SelectItem> tipoObjetos) {
		this.tipoObjetos = tipoObjetos;
	}

}
