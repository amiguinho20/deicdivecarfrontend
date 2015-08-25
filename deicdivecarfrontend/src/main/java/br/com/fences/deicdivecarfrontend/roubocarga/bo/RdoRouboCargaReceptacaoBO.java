package br.com.fences.deicdivecarfrontend.roubocarga.bo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import br.com.fences.deicdivecarfrontend.config.AppConfig;
import br.com.fences.deicdivecarfrontend.config.Log;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroRouboCargaReceptacao;
import br.com.fences.fencesutils.conversor.converter.Converter;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;

@ApplicationScoped
public class RdoRouboCargaReceptacaoBO {     

	@Inject
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;

	@Inject
	private Converter<Ocorrencia> ocorrenciaConverter;
	
	private Gson gson = new GsonBuilder().create();
	
	private String host;
	private String port;

	
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
	
	/**
	 * @param pesquisa
	 * @return count
	 */
	@Log
	public int contar(final FiltroRouboCargaReceptacao filtro)
	{
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = gson.toJson(filtro.montarPesquisaMap(), Map.class);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"rouboCarga/contar";
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
	 * Pesquisa com <b>PAGINACAO</b>
	 * @param pesquisa
	 * @param primeiroRegistro
	 * @param registrosPorPagina
	 * @return List<Ocorrencia> paginado
	 */
	@Log
	public List<Ocorrencia> pesquisarLazy(final FiltroRouboCargaReceptacao filtro, final int primeiroRegistro, final int registrosPorPagina)
	{
		List<Ocorrencia> ocorrencias = new ArrayList<>();
		
		host = appConfig.getServerBackendHost(); 
		port = appConfig.getServerBackendPort();
		
		String json = gson.toJson(filtro.montarPesquisaMap(), Map.class);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"rouboCarga/pesquisarLazy/{primeiroRegistro}/{registrosPorPagina}"; 
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
	
	
	//////---- agregacoes
	@Log
	public Map<String, Integer> agregarPorFlagrante(final FiltroRouboCargaReceptacao filtro)
	{
		Map<String, Integer> resultado = new TreeMap<>();
		//TODO implementar grafico flagrante
		return resultado;
	}
	
	@Log
	public Map<String, Integer> agregarPorAno(final FiltroRouboCargaReceptacao filtro)
	{
		Map<String, Integer> resultado = new TreeMap<>();
		//TODO implementar grafico Ano

		return resultado;
	}
	
	@Log
	public Map<String, Integer> agregarPorComplementar(final FiltroRouboCargaReceptacao filtro)
	{
		Map<String, Integer> resultado = new TreeMap<>();
		//TODO implementar grafico Complemetar
		return resultado;
	}
	
}
