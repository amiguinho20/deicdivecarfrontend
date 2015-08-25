package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
//import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
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
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Circunstancia;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Desdobramento;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Modalidade;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Natureza;
import br.com.fences.ocorrenciaentidade.ocorrencia.pessoa.Pessoa;



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
	
	private Integer contagem;
	private LazyDataModel<Ocorrencia> ocorrenciasResultadoLazy;
	private List<Ocorrencia> ocorrenciasSelecionadas;
	private Ocorrencia ocorrenciaDetalhe;

	private String centroMapa = "-23.538419906917593, -46.63483794999996";
	private MapModel geoModel;
	private Marker marcaSelecionada;
	

	private enum TipoMarcador { COMPLEMENTAR, RECEPTACAO, ROUBO_CARGA }
	
	//--graficos
	private PieChartModel graficoPizzaFlagrante; 
	private PieChartModel graficoPizzaAno;            
	private PieChartModel graficoPizzaComplementar;   
	
	@PostConstruct
	private void init() {	  
		pesquisar();
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

	
	public String atualizarOcorrenciaDetalhe(Ocorrencia ocorrenciaParaAtualizar)
	{
		ocorrenciaDetalhe = ocorrenciaParaAtualizar;
		if (ocorrenciaDetalhe != null && Verificador.isValorado(ocorrenciaDetalhe.getId()))
		{
			ocorrenciaDetalhe = rdoRouboCargaReceptacaoBO.consultar(ocorrenciaDetalhe.getId());
		}
		return "ocorrenciadetalhe";      
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
	
	public String formatarColecaoEmUmBox(String separadorInterno, String separadorExterno, Collection colecao, String arg1)
	{
		return formatarColecaoEmUmBox(true, separadorInterno, separadorExterno, colecao, arg1);
	}
	public String formatarColecaoEmUmBox(String separadorInterno, String separadorExterno, Collection colecao, String arg1, String arg2)
	{
		return formatarColecaoEmUmBox(true, separadorInterno, separadorExterno, colecao, arg1, arg2);
	}
	public String formatarColecaoEmUmBox(String separadorInterno, String separadorExterno, Collection colecao, String arg1, String arg2, String arg3)
	{
		return formatarColecaoEmUmBox(true, separadorInterno, separadorExterno, colecao, arg1, arg2, arg3);
	}
	
	private String formatarColecaoEmUmBox(boolean x, String separadorInterno, String separadorExterno, Collection colecao, String... atributos)
	{
		StringBuilder retorno = new StringBuilder();
		if (Verificador.isValorado(colecao))
		{
			for (Object obj : colecao)
			{
				StringBuilder registro = new StringBuilder();
				for (String atributo : atributos)
				{
					String metodo = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
					String valor = null;
					try
					{
						Class<? extends Object> clazz = obj.getClass(); 
						Method method = clazz.getMethod(metodo, new Class[] {});
						valor = (String) method.invoke(obj, null);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
						logger.error("Erro na reflexao em formatarColecaoEmUmBox do atributo[" + atributo + "]:" + e.getMessage());
						logger.error(e);
						continue;
					}
					if (Verificador.isValorado(valor))
					{
						if (Verificador.isValorado(registro.toString()))
						{
							registro.append(separadorInterno);
						}
						registro.append(valor);
					}
				}
				if (Verificador.isValorado(registro.toString()))
				{
					if (Verificador.isValorado(retorno.toString()))
					{
						retorno.append(separadorExterno);
					}
					retorno.append(registro.toString());
				}
			}
		}
		return retorno.toString();
	}
	
	private String formatarNatureza(TreeNode superior, int nivel)
	{
		nivel++;
		StringBuilder arvoreFormatada = new StringBuilder();
		if (superior != null)
		{
			for (TreeNode inferior : superior.getChildren())
			{	
					if (nivel == 1)
					{
						arvoreFormatada.append("<ul style='margin-top:0px; margin-bottom:0px; padding-left:0px;'>");
					}
					else
					{
						arvoreFormatada.append("<ul>");
					}
	
					String inferiorLabel = (String) inferior.getData();
					arvoreFormatada.append("<li>" + inferiorLabel + "</li>");
	
					if (!inferior.getChildren().isEmpty())
					{
						String inferiorFormatado = formatarNatureza(inferior, nivel);
						arvoreFormatada.append("<li>" + inferiorFormatado + "</li>");
					}
					arvoreFormatada.append("</ul>");
			}
		}
		return arvoreFormatada.toString();
	}

	public String montarNatureza(Ocorrencia ocorrencia)
	{
		TreeNode treeNatureza = new DefaultTreeNode("Natureza", null);
		treeNatureza.setExpanded(true);
		TreeNode natOcorrencia = null;
		TreeNode especie = null;
		TreeNode subespecie = null;
		TreeNode condutaNatureza = null;
		TreeNode desdobramentoCircunstancia = null;
		TreeNode modalidade = null;
		
		if (ocorrencia == null)
		{
			return null;
		}
		
		for (Natureza natureza : ocorrencia.getNaturezas())
		{
			//-- ocorrencia
			boolean possui = false;
			for (TreeNode child : treeNatureza.getChildren())
			{
				if (child.getData().equals(natureza.getDescrOcorrencia()))
				{
					possui = true;
					natOcorrencia = child;
				}
			}
			if (!possui)
			{
				natOcorrencia = new DefaultTreeNode(natureza.getDescrOcorrencia(), treeNatureza);
				natOcorrencia.setExpanded(true);
			}
			
			//-- especie
			possui = false;
			for (TreeNode child : natOcorrencia.getChildren())
			{
				if (child.getData().equals(natureza.getDescrEspecie()))
				{
					possui = true;
					especie = child;
				}
			}
			if (!possui)
			{
				especie = new DefaultTreeNode(natureza.getDescrEspecie(), natOcorrencia);
				especie.setExpanded(true);
			}
			
			//-- subespecie
			possui = false;
			for (TreeNode child : especie.getChildren())
			{
				if (child.getData().equals(natureza.getDescrSubespecie()))
				{
					possui = true;
					subespecie = child;
				}
			}
			if (!possui)
			{
				subespecie = new DefaultTreeNode(natureza.getDescrSubespecie(), especie);
				subespecie.setExpanded(true);
			}

			//-- conduta/natureza
			possui = false;
			String condutaRubrica = null;
			if (Verificador.isValorado(natureza.getRubrica()))
			{
				condutaRubrica = natureza.getRubrica();
			}
			else
			{
				condutaRubrica = natureza.getDescrConduta();
			}
			if (condutaRubrica != null)
			{
				for (TreeNode child : subespecie.getChildren())
				{
					if (child.getData().equals(condutaRubrica))
					{
						possui = true;
						condutaNatureza = child;
					}
				}
				if (!possui)
				{
					condutaNatureza = new DefaultTreeNode(condutaRubrica, subespecie);
					condutaNatureza.setExpanded(true);
				}
			}
			
			//-- desdobramentoCircunstancia
			for (Desdobramento desdobramento : natureza.getDesdobramentos())
			{
				if (Verificador.isValorado(desdobramento.getDescrDesdobramento())){
					possui = false;
					for (TreeNode child : condutaNatureza.getChildren())
					{
						if (child.getData().equals(desdobramento.getDescrDesdobramento()))
						{
							possui = true;
							desdobramentoCircunstancia = child;
						}
					}
					if (!possui)
					{
						desdobramentoCircunstancia = new DefaultTreeNode(desdobramento.getDescrDesdobramento(), condutaNatureza);
						desdobramentoCircunstancia.setExpanded(true);
					}
					//-- modalidade
					for (Modalidade modalid : desdobramento.getModalidades())
					{
						possui = false;
						for (TreeNode child : desdobramentoCircunstancia.getChildren())
						{
							if (child.getData().equals(modalid.getRubrica()))
							{
								possui = true;
								modalidade = child;
							}
						}
						if (!possui)
						{
							modalidade = new DefaultTreeNode(modalid.getRubrica(), desdobramentoCircunstancia);
							modalidade.setExpanded(true);
						}
					}
				}
			}
			for (Circunstancia circunstancia : natureza.getCircunstancias())
			{
				if (Verificador.isValorado(circunstancia.getDescrCircunstancia()))
				{
					possui = false;
					for (TreeNode child : condutaNatureza.getChildren())
					{
						if (child.getData().equals(circunstancia.getDescrCircunstancia()))
						{
							possui = true;
							desdobramentoCircunstancia = child;
						}
					}
					if (!possui)
					{
						desdobramentoCircunstancia = new DefaultTreeNode(circunstancia.getDescrCircunstancia(), condutaNatureza);
						desdobramentoCircunstancia.setExpanded(true);
					}
				}
			}
		}

		String naturezaFormatada = formatarNatureza(treeNatureza, 0);
		return naturezaFormatada;
		
	}

	public String montarNaturezaPessoa(Pessoa pessoa)
	{
		TreeNode treeNatureza = new DefaultTreeNode("Natureza", null);
		treeNatureza.setExpanded(true);
		TreeNode natOcorrencia = null;
		TreeNode especie = null;
		TreeNode subespecie = null;
		TreeNode condutaNatureza = null;
		TreeNode desdobramentoCircunstancia = null;
		TreeNode modalidade = null;
		
		if (pessoa == null)
		{
			return null;
		}
		
		for (br.com.fences.ocorrenciaentidade.ocorrencia.pessoa.Natureza natureza : pessoa.getNaturezas())
		{
			//-- ocorrencia
			boolean possui = false;
			for (TreeNode child : treeNatureza.getChildren())
			{
				if (child.getData().equals(natureza.getDescrOcorrencia()))
				{
					possui = true;
					natOcorrencia = child;
				}
			}
			if (!possui)
			{
				natOcorrencia = new DefaultTreeNode(natureza.getDescrOcorrencia(), treeNatureza);
				natOcorrencia.setExpanded(true);
			}
			
			//-- especie
			possui = false;
			for (TreeNode child : natOcorrencia.getChildren())
			{
				if (child.getData().equals(natureza.getDescrEspecie()))
				{
					possui = true;
					especie = child;
				}
			}
			if (!possui)
			{
				especie = new DefaultTreeNode(natureza.getDescrEspecie(), natOcorrencia);
				especie.setExpanded(true);
			}
			
			//-- subespecie
			if (!Verificador.isValorado(natureza.getDescrSubespecie()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : especie.getChildren())
			{
				if (child.getData().equals(natureza.getDescrSubespecie()))
				{
					possui = true;
					subespecie = child;
				}
			}
			if (!possui)
			{
				subespecie = new DefaultTreeNode(natureza.getDescrSubespecie(), especie);
				subespecie.setExpanded(true);
			}

			//-- natureza
			if (!Verificador.isValorado(natureza.getRubrica()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : subespecie.getChildren())
			{
				if (child.getData().equals(natureza.getRubrica()))
				{
					possui = true;
					condutaNatureza = child;
				}
			}
			if (!possui)
			{
				condutaNatureza = new DefaultTreeNode(natureza.getRubrica(), subespecie);
				condutaNatureza.setExpanded(true);
			}

			//-- desdobramento
			if (!Verificador.isValorado(natureza.getDescrDesdobramento()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : condutaNatureza.getChildren())
			{
				if (child.getData().equals(natureza.getDescrDesdobramento()))
				{
					possui = true;
					desdobramentoCircunstancia = child;
				}
			}
			if (!possui)
			{
				desdobramentoCircunstancia = new DefaultTreeNode(natureza.getDescrDesdobramento(), condutaNatureza);
				desdobramentoCircunstancia.setExpanded(true);
			}

			//-- modalidade
			if (!Verificador.isValorado(natureza.getDescrModalidade()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : desdobramentoCircunstancia.getChildren())
			{
				if (child.getData().equals(natureza.getDescrModalidade()))
				{
					possui = true;
					modalidade = child;
				}
			}
			if (!possui)
			{
				modalidade = new DefaultTreeNode(natureza.getDescrDesdobramento(), desdobramentoCircunstancia);
				modalidade.setExpanded(true);
			}			
		}
		String naturezaFormatada = formatarNatureza(treeNatureza, 0);
		return naturezaFormatada;
		
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
								exibir = true;
								break;
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
				if (enderecoAvulso.getGeocoderStatus().equals("OK")) 
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
	
	public void listarRaio()
	{
		Double latitude = ocorrenciaDetalhe.getAuxiliar().getGeometry().getLatitude();
		Double longitude = ocorrenciaDetalhe.getAuxiliar().getGeometry().getLongitude();
		Integer raioEmMetros = 10000;
		
		filtro.setLatitude(latitude.toString());
		filtro.setLongitude(longitude.toString());
		filtro.setRaioEmMetros(raioEmMetros.toString());  

		//-- pesquisar tradicional (montar lista paginada e atualizar total)
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
        	logger.info("imprimiu o circulo: " + circulo);
        }
 
	}
	
	public void onMarkerSelect(OverlaySelectEvent event) 
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
	
	public String formatarNatureza(Natureza natureza)
	{
		StringBuilder nat = new StringBuilder();
		if (natureza != null)
		{
			List<String> naturezas = new ArrayList<>();
			if (Verificador.isValorado(natureza.getDescrOcorrencia()))
			{
				naturezas.add(natureza.getDescrOcorrencia());
			}
			if (Verificador.isValorado(natureza.getDescrOcorrencia()))
			{
				naturezas.add(natureza.getDescrEspecie());
			}
			if (Verificador.isValorado(natureza.getDescrOcorrencia()))
			{
				naturezas.add(natureza.getDescrSubespecie());
			}
			if (Verificador.isValorado(natureza.getRubrica()))
			{
				naturezas.add(natureza.getRubrica());
			}
			if (Verificador.isValorado(natureza.getDescrConduta()))
			{
				naturezas.add(natureza.getDescrConduta());
			}
			for (String aux : naturezas)
			{
				if (!nat.toString().isEmpty())
				{
					nat.append("; ");
				}
				nat.append(aux);
			}
		}
		return nat.toString();
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

	public Ocorrencia getOcorrenciaDetalhe() {
		return ocorrenciaDetalhe;
	}

	public void setOcorrenciaDetalhe(Ocorrencia ocorrenciaDetalhe) {
		this.ocorrenciaDetalhe = ocorrenciaDetalhe;
	}

	public Marker getMarcaSelecionada() {
		return marcaSelecionada;
	}

	public void setMarcaSelecionada(Marker marcaSelecionada) {
		this.marcaSelecionada = marcaSelecionada;
	}

	
}
