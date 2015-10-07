package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;

import br.com.fences.deicdivecarentidade.enderecoavulso.EnderecoAvulso;
import br.com.fences.deicdivecarfrontend.filtrocustom.FiltroArvoreConverter;
import br.com.fences.deicdivecarfrontend.filtrocustom.FiltroBO;
import br.com.fences.deicdivecarfrontend.filtrocustom.FiltroModelo;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.EnderecoAvulsoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroMapa;
import br.com.fences.deicdivecarfrontend.roubocarga.util.FiltroLazyDataModel;
import br.com.fences.deicdivecarfrontend.roubocarga.util.MapaUtil;
import br.com.fences.fencesutils.filtrocustom.ArvoreSimples;
import br.com.fences.fencesutils.filtrocustom.FiltroCondicao;
import br.com.fences.fencesutils.filtrocustom.TipoFiltro;
import br.com.fences.fencesutils.formatar.FormatarData;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Natureza;



@Named
@SessionScoped
public class PesquisaMB implements Serializable{

	private static final long serialVersionUID = 1866941789765596632L;
	
	@Inject
	private transient Logger logger;

	@Inject
	private FiltroBO filtroBO;
	
	@Inject
	private FiltroArvoreConverter filtroArvoreConverter;
	
	@Inject
	private FiltroMapa filtroMapa;

	@Inject 
	private MapaUtil mapaUtil;
	
	@Inject
	private EnderecoAvulsoBO enderecoAvulsoBO;
	
	@Inject
	private FormatadorOcorrenciaMB formatadorOcorrenciaMB;
	
	private LazyDataModel<Ocorrencia> ocorrenciasResultadoLazy;
	private List<Ocorrencia> ocorrenciasSelecionadas;
	
	private boolean informativoFuncionalidade;
	
	private List<FiltroModelo> filtroLista;

	private FiltroModelo filtroModelo;
	
	private List<FiltroCondicao> pilhaDeFiltros;
	
	private String centroMapa = "-23.538419906917593, -46.63483794999996";
	private MapModel geoModel;
//	private Marker marcaSelecionada;
	
	
	
	@PostConstruct
	private void init() {
		setFiltroLista(filtroBO.listarFiltros());
		pesquisar();
		limparMapa();
	}    
	 
	public void mudarInformativoFuncionalidade(){
		informativoFuncionalidade = !informativoFuncionalidade;
	}
	
	public void limparMapa(){
		filtroMapa.limpar();
		geoModel = null;
	}
	
	/**
	 * A pesquisa lazy de fato e' feita apos o termino da execucao desse metodo, pelo primefaces.
	 * O filtro nao pode ser limpo aqui.
	 */
	public void pesquisar()
	{ 
		setOcorrenciasResultadoLazy(new FiltroLazyDataModel(filtroBO, pilhaDeFiltros));
		
		// -- ao pesquisar, sempre colocar na primeira pagina
		DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
				.findComponent("form:listaDeOcorrenciasLazy");
		dataTable.setFirst(0);
		
		if (ocorrenciasSelecionadas != null)  
		{
			ocorrenciasSelecionadas.clear();
		}
	}
 
	public void adicionarFiltro()
	{
		try
		{
			if (pilhaDeFiltros == null)
			{
				pilhaDeFiltros = new ArrayList<>();
			}
			
			FiltroCondicao filtroCondicao = new FiltroCondicao();
			filtroCondicao.setRotulo(filtroModelo.getRotulo());
			filtroCondicao.setTipo(filtroModelo.getTipo());
			filtroCondicao.setAtributoDePesquisa(filtroModelo.getAtributoDePesquisa());
			
			if (filtroModelo.getTipo().equals(TipoFiltro.TEXTO))
			{  
				filtroCondicao.setValorTextoTipo(filtroModelo.getValorTextoTipo());
				filtroCondicao.setValorTexto(filtroModelo.getValorTexto());
				filtroModelo.setValorTexto("");
			}
			if (filtroModelo.getTipo().equals(TipoFiltro.LISTA_SELECAO_UNICA))
			{
				String id = filtroModelo.getListaSelecaoUnicaIdSelecionado();
				String descricao = filtroModelo.getListaSelecaoUnica().get(id);
	
				filtroCondicao.setValorListaSelecaoUnicaIdSelecionado(id);
				filtroCondicao.setValorListaSelecaoUnicaDescricaoSelecionado(descricao);
				
				filtroModelo.setListaSelecaoUnicaIdSelecionado("");
			}
			  
			if (filtroModelo.getTipo().equals(TipoFiltro.INTERVALO_DATA))
			{
				//--transferido para o validador
	//			if (possuiErroIntervaloDeData(filtroModelo.getIntervaloDataInicial(), filtroModelo.getIntervaloDataFinal()))
	//			{
	//				Messages.create("A data inicial não pode ser maior que a data final").warn().add("form:valorDoFiltro");
	//				return;
	//			}
				
				if (filtroModelo.getIntervaloDataInicial() != null)
				{
					Date inicial = filtroModelo.getIntervaloDataInicial();
					String dataInicial = FormatarData.getAnoMesDiaContatenado().format(inicial);
					filtroCondicao.setValorIntervaloDataInicial(dataInicial);
					filtroModelo.setIntervaloDataInicial(null);
				}
				
				if (filtroModelo.getIntervaloDataFinal() != null)
				{  
					Date dFinal = filtroModelo.getIntervaloDataFinal();
					String dataFinal = FormatarData.getAnoMesDiaContatenado().format(dFinal);
					filtroCondicao.setValorIntervaloDataFinal(dataFinal);
					filtroModelo.setIntervaloDataFinal(null);
				}
			}
			
			if (filtroModelo.getTipo().equals(TipoFiltro.ARVORE))      
			{
				if (filtroModelo.getArvoreRaizSelecao() != null && filtroModelo.getArvoreRaizSelecao().length == 0)
				{
					//-- nao eh possivel colocar como 'requered', pois a pesquisa na arvore precisa da submissao mesmo com ela vazia.
					Messages.create("É necessário selecionar pelo menos uma opção.").error().add("form:idArvore");
					return;
				}
				
				if (filtroModelo.getArvoreRaizSelecao() != null && filtroModelo.getArvoreRaizSelecao().length > 0)
				{
					TreeNode arvorePrime = filtroModelo.getArvoreRaiz();
					ArvoreSimples arvoreSimples = filtroArvoreConverter.converterArvorePrimeParaArvoreSimples(arvorePrime, null, true);
							
					filtroCondicao.setArvoreSelecao(arvoreSimples);
					
					desselecionarElementoArvore(filtroModelo.getArvoreRaiz());
					filtroModelo.setArvoreTextoPesquisa("");
					filtroModelo.setArvoreRaizSelecao(null);
				}
			}
			
			if (filtroModelo.getTipo().equals(TipoFiltro.GEO_RAIO))
			{
				//-- descarta eventual filtro GEO_RAIO antes de criar outro.
				//-- passa de uma lista para outra, pois a remocao apresentou erro de uso concorrente.
				List<FiltroCondicao> listaAux = new ArrayList<>();
				for (FiltroCondicao filtroCondicaoAux : pilhaDeFiltros)
				{
					if (!filtroCondicaoAux.getTipo().equals(TipoFiltro.GEO_RAIO))
					{
						listaAux.add(filtroCondicaoAux);
					}
				}
				pilhaDeFiltros = listaAux;
				
				filtroCondicao.setValorGeoRaioLatitude(filtroModelo.getGeoRaioLatitude());
				filtroCondicao.setValorGeoRaioLongitude(filtroModelo.getGeoRaioLongitude());
				filtroCondicao.setValorGeoRaioEmMetros(filtroModelo.getGeoRaioEmMetros());
				filtroCondicao.setValorGeoRaioEndereco(filtroModelo.getGeoRaioEndereco());
			}
			
			pilhaDeFiltros.add(filtroCondicao);
	
			//-- desabilita o filtro modelo na lista de filtro
			filtroModelo.setHabilitado(false);
			
			filtroModelo = null;
			
			pesquisar();
		}
		catch (Exception e)
		{
			String excecao = formatadorOcorrenciaMB.formatarExcecaoParaMessage(e);
			Messages.create("Erro na adição de filtro.  {0}", excecao).error().add();
		}
	}
	
	//-- recursivo
	private void desselecionarElementoArvore(TreeNode arvore)
	{
		arvore.setSelected(false);
		arvore.setExpanded(false);
		String ORIGINAL = "ORIGINAL";
		arvore.setType(ORIGINAL);
		
		for (TreeNode elemento : arvore.getChildren())
		{
			desselecionarElementoArvore(elemento);
		}
	}
	
	public void excluirFiltro(FiltroCondicao filtroCondicao)
	{
		try
		{
			//-- habilita o modelo do filtro na lista de filtro
			for (FiltroModelo modelo : filtroLista)
			{
				if (modelo.getRotulo().equals(filtroCondicao.getRotulo()) &&
					modelo.getTipo().equals(filtroCondicao.getTipo()) &&
					modelo.getAtributoDePesquisa().equals(filtroCondicao.getAtributoDePesquisa()))
				{
					modelo.setHabilitado(true);
				}
			}
			
			pilhaDeFiltros.remove(filtroCondicao);
			limparMapa();
			pesquisar();
		}
		catch (Exception e)
		{
			String excecao = formatadorOcorrenciaMB.formatarExcecaoParaMessage(e);
			Messages.create("Erro na exclusão de filtro.  {0}", excecao).error().add();
		}
	}
	  
	/**
	 * Botao limpar. Faz a limpeza e executa a pesquisa
	 */
	public void limpar(){
		//filtro.limpar();
		pesquisar();
	}

	
	public void exibirRegistrosSelecionadosNoMapa() 
	{
		try
		{
			geoModel = new DefaultMapModel();  	
	
			//-- exibe no mapa apenas ocorrencias que contem geoCode pre-processado
			if (ocorrenciasSelecionadas != null)
			{
				for (Ocorrencia ocorrencia : ocorrenciasSelecionadas) 
				{
					if (filtroMapa.isExibirApenasLinhas())
					{
						if (!mapaUtil.verificarExibicaoDeLinha(ocorrencia))
						{
							continue;
						}
					}
					
					mapaUtil.exibirNoMapa(ocorrencia, geoModel, filtroMapa);
					
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
								mapaUtil.exibirNoMapa(complementar, geoModel, filtroMapa);
								
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
	
						
						String enderecoFormatado = formatadorOcorrenciaMB.concatenarEndereco(
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
		catch (Exception e)
		{
			String excecao = formatadorOcorrenciaMB.formatarExcecaoParaMessage(e);
			Messages.create("Erro exibição de registros selecionados no mapa.  {0}", excecao).error().add();
		}
	}
	
	public void listarRaio(Integer raioEmMetros, Ocorrencia ocorrencia)
	{
		try
		{
			if (ocorrencia != null)
			{
				Double latitude = ocorrencia.getAuxiliar().getGeometry().getLatitude();
				Double longitude = ocorrencia.getAuxiliar().getGeometry().getLongitude();
				
				filtroModelo = new FiltroModelo();
				filtroModelo.setRotulo("Pesquisa de ponto e raio no mapa");
				filtroModelo.setTipo(TipoFiltro.GEO_RAIO);
				filtroModelo.setAtributoDePesquisa("");
				
				filtroModelo.setGeoRaioLatitude(latitude.toString());
				filtroModelo.setGeoRaioLongitude(longitude.toString());
				filtroModelo.setGeoRaioEmMetros(raioEmMetros.toString());
				filtroModelo.setGeoRaioEndereco(formatadorOcorrenciaMB.formatarEndereco(ocorrencia));
					
				//-- pesquisar tradicional (montar lista paginada e atualizar total) com os filtros latitude, longitude e raioEmMetros
				adicionarFiltro();
				
				//-- pesquisar no raio (limitado a 100 registros)
				List<Ocorrencia> ocorrenciasRetornadas = filtroBO.pesquisarLazy(pilhaDeFiltros, 0, 100);
				
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
		        }
			}
		}
		catch (Exception e)
		{
			String excecao = formatadorOcorrenciaMB.formatarExcecaoParaMessage(e);
			Messages.create("Erro na montagem do raio.  {0}", excecao).error().add();
		}
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

	public FiltroModelo getFiltroModelo() {
		return filtroModelo;
	}

	public void setFiltroModelo(FiltroModelo filtroModelo) {
		this.filtroModelo = filtroModelo;
	}


	public boolean isInformativoFuncionalidade() {
		return informativoFuncionalidade;
	}

	public void setInformativoFuncionalidade(boolean informativoFuncionalidade) {
		this.informativoFuncionalidade = informativoFuncionalidade;
	}

	public List<FiltroModelo> getFiltroLista() {
		return filtroLista;
	}

	public void setFiltroLista(List<FiltroModelo> filtroLista) {
		this.filtroLista = filtroLista;
	}

	public List<FiltroCondicao> getPilhaDeFiltros() {
		return pilhaDeFiltros;
	}

	public void setPilhaDeFiltros(List<FiltroCondicao> pilhaDeFiltros) {
		this.pilhaDeFiltros = pilhaDeFiltros;
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

//	public Marker getMarcaSelecionada() {
//		return marcaSelecionada;
//	}
//
//	public void setMarcaSelecionada(Marker marcaSelecionada) {
//		this.marcaSelecionada = marcaSelecionada;
//	}

	public FiltroMapa getFiltroMapa() {
		return filtroMapa;
	}

	public void setFiltroMapa(FiltroMapa filtroMapa) {
		this.filtroMapa = filtroMapa;
	}



}
