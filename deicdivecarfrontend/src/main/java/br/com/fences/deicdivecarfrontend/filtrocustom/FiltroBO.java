package br.com.fences.deicdivecarfrontend.filtrocustom;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import br.com.fences.deicdivecarfrontend.config.AppConfig;
import br.com.fences.fencesutils.conversor.converter.Converter;
import br.com.fences.fencesutils.filtrocustom.ArvoreSimples;
import br.com.fences.fencesutils.filtrocustom.ChaveValor;
import br.com.fences.fencesutils.filtrocustom.FiltroCondicao;
import br.com.fences.fencesutils.filtrocustom.TipoFiltro;
import br.com.fences.fencesutils.filtrocustom.TipoPesquisaTexto;
import br.com.fences.fencesutils.formatar.FormatarData;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;

@SessionScoped 
public class FiltroBO implements Serializable{
	
	private static final long serialVersionUID = 6343587360472833456L;
	
	@Inject
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;

	@Inject
	private Converter<Ocorrencia> ocorrenciaConverter;
	
	@Inject
	private FiltroArvoreConverter filtroArvoreConverter;

	private List<FiltroModelo> filtros;
	
	private Gson gson = new GsonBuilder().create();
	
	private String host;
	private String port;
	
	@PostConstruct
	private void init()
	{
		filtros = new ArrayList<>();
		try
		{
		
			//--- TEXTO
			{
				FiltroModelo numBo = new FiltroModelo();
				numBo.setRotulo("Número");
				numBo.setTipo(TipoFiltro.TEXTO);
				numBo.setInformativo("Número da ocorrência");
				numBo.setAtributoDePesquisa("NUM_BO");
				numBo.setValorTextoTipo(TipoPesquisaTexto.EXATO);
				filtros.add(numBo);
			}
			
			
			//--- LISTA_SELECAO_UNICA
			{
				FiltroModelo anoBo = new FiltroModelo();
				anoBo.setRotulo("Ano");
				anoBo.setTipo(TipoFiltro.LISTA_SELECAO_UNICA);
				anoBo.setInformativo("Ano da ocorrência");
				anoBo.setAtributoDePesquisa("ANO_BO");
				anoBo.setListaSelecaoUnicaFonte("/deicdivecarbackend/rest/rouboCarga/listarAnosMap");
			
				Map<String, String> anos = chamarServicoFonteParaMap(anoBo.getListaSelecaoUnicaFonte());
				anoBo.setListaSelecaoUnica(anos);
				filtros.add(anoBo);
			}
			
			//--- LISTA_SELECAO_UNICA - manual
			{
				FiltroModelo flagFlagrante = new FiltroModelo();
				flagFlagrante.setRotulo("Flagrante");
				flagFlagrante.setTipo(TipoFiltro.LISTA_SELECAO_UNICA);
				flagFlagrante.setInformativo("Indicador de flagrante");
				flagFlagrante.setAtributoDePesquisa("FLAG_FLAGRANTE");
				
				Map<String, String> mapFlagFlagrante = new LinkedHashMap<>();
				mapFlagFlagrante.put("S", "Sim");
				mapFlagFlagrante.put("N", "Não");
				flagFlagrante.setListaSelecaoUnicaFonteFixa(mapFlagFlagrante);
	
				flagFlagrante.setListaSelecaoUnica(flagFlagrante.getListaSelecaoUnicaFonteFixa());
				filtros.add(flagFlagrante);
			}
			
			//--- LISTA_SELECAO_UNICA - manual
			{
				FiltroModelo complementar = new FiltroModelo();
				complementar.setRotulo("Complementar");
				complementar.setTipo(TipoFiltro.LISTA_SELECAO_UNICA);
				complementar.setInformativo("Indicador de boletim de ocorrência complementar");
				complementar.setAtributoDePesquisa("COMPLEMENTAR");
				
				Map<String, String> mapFlagFlagrante = new LinkedHashMap<>();
				mapFlagFlagrante.put("A", "Sem complementar de localização/devolução");
				mapFlagFlagrante.put("B", "Com complementar de localização/devolução");
				mapFlagFlagrante.put("C", "Apenas complementar de localização/devolução");
				mapFlagFlagrante.put("D", "Apenas não complementar");
				mapFlagFlagrante.put("E", "Apenas complementar");
				complementar.setListaSelecaoUnicaFonteFixa(mapFlagFlagrante);
	
				complementar.setListaSelecaoUnica(complementar.getListaSelecaoUnicaFonteFixa());
				filtros.add(complementar);
			}
			
			//--- LISTA_SELECAO_UNICA - dinamica
			{
				FiltroModelo delegacia = new FiltroModelo();
				delegacia.setRotulo("Delegacia");
				delegacia.setTipo(TipoFiltro.LISTA_SELECAO_UNICA);
				delegacia.setInformativo("Delegacia de registro da ocorrência");
				delegacia.setAtributoDePesquisa("ID_DELEGACIA");
				delegacia.setListaSelecaoUnicaFonte("/deicdivecarbackend/rest/rouboCarga/listarDelegacias");
				
				Map<String, String> delegacias = chamarServicoFonteParaMap(delegacia.getListaSelecaoUnicaFonte());
				delegacia.setListaSelecaoUnica(delegacias);
				filtros.add(delegacia);
			}
			
			
			//--- INTERVALO_DATA
			{
				FiltroModelo dataRegistro = new FiltroModelo();
				dataRegistro.setRotulo("Data de registro");
				dataRegistro.setTipo(TipoFiltro.INTERVALO_DATA);
				dataRegistro.setInformativo("Data de registro da ocorrência");
				dataRegistro.setAtributoDePesquisa("DATAHORA_REGISTRO_BO");
				dataRegistro.setIntervaloDataInicialMinimoFonte("/deicdivecarbackend/rest/rouboCarga/pesquisarPrimeiraDataRegistro");
				dataRegistro.setIntervaloDataFinalMaximoFonte("/deicdivecarbackend/rest/rouboCarga/pesquisarUltimaDataRegistro");
				
				String minimoOriginal = chamarServicoFonteParaString(dataRegistro.getIntervaloDataInicialMinimoFonte());
				String maximoOriginal = chamarServicoFonteParaString(dataRegistro.getIntervaloDataFinalMaximoFonte());
				
				Date minimoFormatado = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados().parse(minimoOriginal);
				Date maximoFormatado = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados().parse(maximoOriginal);
				
				dataRegistro.setIntervaloDataInicialMinimo(FormatarData.getDiaMesAnoComBarras().format(minimoFormatado));
				dataRegistro.setIntervaloDataFinalMaximo(FormatarData.getDiaMesAnoComBarras().format(maximoFormatado));
				
				filtros.add(dataRegistro);
			}
			
			//--- ARVORE NATUREZA
			{
				FiltroModelo arvore = new FiltroModelo();
				arvore.setAtivo(false);
				arvore.setRotulo("Natureza");
				arvore.setTipo(TipoFiltro.ARVORE);
				arvore.setInformativo("Hierarquia de naturezas");
				arvore.setAtributoDePesquisa("NATUREZA");
				arvore.setArvoreFonte("/deicdivecarbackend/rest/rouboCarga/listarNaturezasArvore");
				arvore.setArvoreSelecionaFilhos(false);
				
				ArvoreSimples arvoreSimples = chamarServicoFonteParaArvoreSimples(arvore.getArvoreFonte());
				
				TreeNode arvorePrime = filtroArvoreConverter.converterArvoreSimplesParaArvorePrime(arvoreSimples, null, false, false);
				
				configurarTipoElementoArvore(arvorePrime);
				arvore.setArvoreRaiz(arvorePrime);
				
				filtros.add(arvore);
			}
			
			//--- LISTA_SELECAO_UNICA - NATUREZA
			{
				FiltroModelo natureza = new FiltroModelo();
				natureza.setRotulo("Natureza");
				natureza.setTipo(TipoFiltro.LISTA_SELECAO_UNICA);
				natureza.setInformativo("Natureza resumida");
				natureza.setAtributoDePesquisa("NATUREZA");
				
				Map<String, String> mapNatureza = new LinkedHashMap<>();
				mapNatureza.put("CARGA", "Carga");
				mapNatureza.put("RECEPTACAO", "Receptação");
				natureza.setListaSelecaoUnicaFonteFixa(mapNatureza);
	
				natureza.setListaSelecaoUnica(natureza.getListaSelecaoUnicaFonteFixa());
				filtros.add(natureza);
			}

			//--- ARVORE
			{
				FiltroModelo arvore = new FiltroModelo();
				arvore.setRotulo("Objeto > Tipo");
				arvore.setTipo(TipoFiltro.ARVORE);
				arvore.setInformativo("Hierarquia de tipo de objeto");
				arvore.setAtributoDePesquisa("OBJETO.ID_TIPO_OBJETO|OBJETO.ID_SUBTIPO_OBJETO");
				arvore.setArvoreFonte("/deicdivecarbackend/rest/rouboCarga/listarTipoObjetosArvore");
				
				ArvoreSimples arvoreSimples = chamarServicoFonteParaArvoreSimples(arvore.getArvoreFonte());
				
				TreeNode arvorePrime = filtroArvoreConverter.converterArvoreSimplesParaArvorePrime(arvoreSimples, null, false, false);
				
				configurarTipoElementoArvore(arvorePrime);
				arvore.setArvoreRaiz(arvorePrime);
				
				filtros.add(arvore);
			}			
			
			//--- TEXTO - objeto > imei
			{
				FiltroModelo imei = new FiltroModelo();
				imei.setRotulo("Objeto > IMEI");
				imei.setTipo(TipoFiltro.TEXTO);
				imei.setInformativo("IMEI");
				imei.setAtributoDePesquisa("OBJETO.IMEI");
				imei.setValorTextoTipo(TipoPesquisaTexto.REGEX);
				filtros.add(imei);
			}			
			
			//--- TEXTO - pessoa > nome
			{
				FiltroModelo numBo = new FiltroModelo();
				numBo.setRotulo("Pessoa > Nome");
				numBo.setTipo(TipoFiltro.TEXTO);
				numBo.setInformativo("Nome da pessoa");
				numBo.setAtributoDePesquisa("PESSOA.NOME_PESSOA");
				numBo.setValorTextoTipo(TipoPesquisaTexto.REGEX);
				filtros.add(numBo);
			}			
			
			//--- LISTA_SELECAO_UNICA - pessoa > tipo de pessoa
			{
				FiltroModelo tipoPessoa = new FiltroModelo();
				tipoPessoa.setRotulo("Pessoa > Tipo");
				tipoPessoa.setTipo(TipoFiltro.LISTA_SELECAO_UNICA);
				tipoPessoa.setInformativo("Tipo da pessoa");
				tipoPessoa.setAtributoDePesquisa("PESSOA.ID_TIPO_PESSOA");
				tipoPessoa.setListaSelecaoUnicaFonte("/deicdivecarbackend/rest/rouboCarga/listarTipoPessoas");
				
				Map<String, String> tipos = chamarServicoFonteParaMap(tipoPessoa.getListaSelecaoUnicaFonte());
				tipoPessoa.setListaSelecaoUnica(tipos);
				filtros.add(tipoPessoa);
			}			
			   
			//--- TEXTO - veiculo > placa
			{
				FiltroModelo numBo = new FiltroModelo();
				numBo.setRotulo("Veículo > Placa");
				numBo.setTipo(TipoFiltro.TEXTO);
				numBo.setInformativo("Placa do veículo");
				numBo.setAtributoDePesquisa("VEICULO.PLACA_VEICULO");
				numBo.setValorTextoTipo(TipoPesquisaTexto.REGEX);
				filtros.add(numBo);
			}
			
			
			
			
			//-- retirar desabilitados da lista
			for (int i = filtros.size() - 1; i >= 0; i--)
			{
				FiltroModelo elemento = filtros.get(i);
				if (!elemento.isAtivo())
				{
					filtros.remove(i);
				}
			}
			
		}
		catch(Exception e)
		{
			logger.fatal("Erro geral na montagem de filtros", e);
		}
		
		
	}


	public List<FiltroModelo> listarFiltros()
	{
		return filtros;
	}
	
	//-- recursivo
	private void configurarTipoElementoArvore(TreeNode arvore)
	{
		String ORIGINAL = "ORIGINAL";
		arvore.setType(ORIGINAL);
		
		for (TreeNode elemento : arvore.getChildren())
		{
			configurarTipoElementoArvore(elemento);
		}
	}
	
	
	public FiltroModelo selecionar(String rotulo)
	{
		FiltroModelo filtro = null;
		for (FiltroModelo aux : filtros)
		{
			if (aux.getRotulo().equals(rotulo))
			{
				filtro = aux;
			}
		}
		return filtro;
	}
	
	//--
	public List<Ocorrencia> pesquisarLazy(final List<FiltroCondicao> filtroCondicoes, final int primeiroRegistro, final int registrosPorPagina)
	{
		List<Ocorrencia> ocorrencias = new ArrayList<>();
		
		host = appConfig.getServerBackendHost(); 
		port = appConfig.getServerBackendPort();
		
		String json = gson.toJson(filtroCondicoes);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"rouboCarga/pesquisarDinamicoLazy/{primeiroRegistro}/{registrosPorPagina}"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("primeiroRegistro", primeiroRegistro)
				.resolveTemplate("registrosPorPagina", registrosPorPagina)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.json(json));
		json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
		Type collectionType = new TypeToken<List<Ocorrencia>>(){}.getType();
		ocorrencias = (List<Ocorrencia>) ocorrenciaConverter.paraObjeto(json, collectionType); 
	    return ocorrencias;
	}   
	
	public int contar(final List<FiltroCondicao> filtroCondicoes)
	{
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		Type collectionType = new TypeToken<List<FiltroCondicao>>(){}.getType();
		String json = gson.toJson(filtroCondicoes, collectionType);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"rouboCarga/contarDinamico";
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.json(json));
		json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
	    int quantidade = Integer.parseInt(json);
	    return quantidade;
	}	
	
	/**
	 * Consulta pelo id (identificador unico), o "_id"
	 * @param id
	 */
	public Ocorrencia consultar(final String id)
	{
	    Ocorrencia ocorrencia = null;
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"rouboCarga/consultar/{id}";
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("id", id)
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
		ocorrencia = ocorrenciaConverter.paraObjeto(json, Ocorrencia.class);
		return ocorrencia;
	}
	
	
	public String chamarServicoFonteParaString(String servicoFonte)
	{
		String retorno = chamarServicoFonte(servicoFonte);
		return retorno;
	}
	
	public Map<String, String> chamarServicoFonteParaMap(String servicoFonte)
	{
		String json = chamarServicoFonte(servicoFonte);
		
		Map<String, String> mapRetorno = new LinkedHashMap<String, String>();
		Type collectionType = new TypeToken<Map<String, String>>(){}.getType();
		mapRetorno = (Map<String, String>) gson.fromJson(json, collectionType);
	    return mapRetorno;
	}
	
	public ArvoreSimples chamarServicoFonteParaArvoreSimples(String servicoFonte)
	{
		String json = chamarServicoFonte(servicoFonte);
		ArvoreSimples arvore = gson.fromJson(json, ArvoreSimples.class);
		return arvore;
	}
	
	private String chamarServicoFonte(String servicoFonte)
	{
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + servicoFonte;
		WebTarget webTarget = client.target(servico);
		
		Response response = webTarget
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
		return json;
	}
			
}
