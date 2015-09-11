package br.com.fences.deicdivecarfrontend.roubocarga.bo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import br.com.fences.deicdivecarentidade.enderecoavulso.EnderecoAvulso;
import br.com.fences.deicdivecarfrontend.config.AppConfig;
import br.com.fences.deicdivecarfrontend.config.Log;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroEnderecoAvulso;
import br.com.fences.fencesutils.conversor.converter.Converter;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;

@ApplicationScoped
public class EnderecoAvulsoBO {     

	@Inject
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;

	@Inject
	private Converter<EnderecoAvulso> enderecoAvulsoConverter;
	
	private Gson gson = new GsonBuilder().create();
	
	private String host;
	private String port;

	
	/**
	 * Consulta pelo id (identificador unico), o "_id"
	 * @param id
	 */
	public EnderecoAvulso consultar(final String id)
	{
	    EnderecoAvulso enderecoAvulso = null;
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/consultar/{id}";
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
		enderecoAvulso = enderecoAvulsoConverter.paraObjeto(json, EnderecoAvulso.class);
		return enderecoAvulso;
	}
	
	/**
	 * @param pesquisa
	 * @return count
	 */
	@Log
	public int contar(final FiltroEnderecoAvulso filtro)
	{
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = gson.toJson(filtro.getPrimeMapFiltro(), Map.class);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/contar";
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
	public List<EnderecoAvulso> pesquisarLazy(final FiltroEnderecoAvulso filtro, final int primeiroRegistro, final int registrosPorPagina)
	{
		List<EnderecoAvulso> enderecosAvulsos = new ArrayList<>();
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = gson.toJson(filtro.getPrimeMapFiltro(), Map.class);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/pesquisarLazy/{primeiroRegistro}/{registrosPorPagina}"; 
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
		Type collectionType = new TypeToken<List<EnderecoAvulso>>(){}.getType();
		enderecosAvulsos = (List<EnderecoAvulso>) enderecoAvulsoConverter.paraObjeto(json, collectionType); 
	    return enderecosAvulsos;
	}	

	@Log
	public List<EnderecoAvulso> pesquisarAtivoPorTipo(List<String> tipos)
	{
		List<EnderecoAvulso> enderecosAvulsos = new ArrayList<>();
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = gson.toJson(tipos, List.class);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/pesquisarAtivoPorTipo"; 
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
		Type collectionType = new TypeToken<List<EnderecoAvulso>>(){}.getType();
		enderecosAvulsos = (List<EnderecoAvulso>) enderecoAvulsoConverter.paraObjeto(json, collectionType); 
	    return enderecosAvulsos;
	}	
	
	@Log
	public void adicionar(EnderecoAvulso enderecoAvulso)
	{
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = enderecoAvulsoConverter.paraJson(enderecoAvulso);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/adicionar"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.json(json));
		json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
	}	
	
	public void adicionar(List<EnderecoAvulso> enderecosAvulsos)
	{
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = enderecoAvulsoConverter.paraJson(enderecosAvulsos);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/adicionarLote"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.request(MediaType.APPLICATION_JSON)
				.put(Entity.json(json));
		json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
	}	
	
	@Log
	public void substituir(EnderecoAvulso enderecoAvulso)
	{
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = enderecoAvulsoConverter.paraJson(enderecoAvulso);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/substituir"; 
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
	}	
	
	@Log
	public void remover(EnderecoAvulso enderecoAvulso)
	{
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		String json = enderecoAvulsoConverter.paraJson(enderecoAvulso);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/deicdivecarbackend/rest/" + 
				"enderecoAvulso/remover/{id}"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("id", enderecoAvulso.getId())
				.request(MediaType.APPLICATION_JSON)
				.delete();
		json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
	}	


}
