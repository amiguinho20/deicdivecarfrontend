package br.com.fences.deicdivecarfrontend.cadastrarusuario.bo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
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

import br.com.fences.autenticacaoentidade.rdo.usuario.RdoUsuario;
import br.com.fences.autenticacaoentidade.usuario.Usuario;
import br.com.fences.deicdivecarfrontend.autenticacao.UsuarioLogadoMB;
import br.com.fences.deicdivecarfrontend.config.AppConfig;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;
import br.com.fences.fencesutils.verificador.Verificador;


@RequestScoped
public class CadastrarUsuarioBO {
	
	
	@Inject
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private UsuarioLogadoMB usuarioLogadoMB;
	
	@Inject
	private VerificarErro verificarErro;
	
	private Gson gson = new GsonBuilder().create();
	
	private String host;
	private String port;
	
	@PostConstruct
	private void init()
	{
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();	
	}

	public void adicionar(Usuario usuario)
	{
		
		String json = gson.toJson(usuario);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/autenticacaobackend/rest/" + 
				"usuario/adicionar"; 
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
	
//	public void atualizar(Usuario usuario)
//	{
//		String json = gson.toJson(usuario);
//		
//		Client client = ClientBuilder.newClient();
//		String servico = "http://" + host + ":"+ port + "/vigilantebackend/rest/" + 
//				"usuario/atualizar"; 
//		WebTarget webTarget = client
//				.target(servico);
//		Response response = webTarget
//				.request(MediaType.APPLICATION_JSON)
//				.post(Entity.json(json));
//		json = response.readEntity(String.class);
//		if (verificarErro.contemErro(response, json))
//		{
//			String msg = verificarErro.criarMensagem(response, json, servico);
//			logger.error(msg);
//			throw new RuntimeException(msg);
//		}	
//		
//		//---- ATUALIZAR INFORMACOES DO USUARIO NA SESSAO
//		usuarioLogadoMB.refresh();
//		
//	}	

	public boolean existeUsername(Usuario usuario)
	{
		
		String json = gson.toJson(usuario);
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/autenticacaobackend/rest/" + 
				"usuario/consultarUsername/{username}"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("username", usuario.getUsername())
				.request(MediaType.APPLICATION_JSON)
				.get();
		json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
		
		Usuario usuarioRetorno = gson.fromJson(json, Usuario.class);
		boolean retorno = false;
		if (usuarioRetorno != null && Verificador.isValorado(usuarioRetorno.getUsername()))
		{
			retorno = true;
		}
		return retorno;
	}	
	
	public Usuario consultarUsername(String email)
	{
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/autenticacaobackend/rest/" + 
				"usuario/consultarUsername/{username}"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("username", email)
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
		
		Usuario usuarioRetorno = gson.fromJson(json, Usuario.class);
		return usuarioRetorno;
	}

	public void popularInformacoesDoRdo(Usuario usuario) {
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/autenticacaobackend/rest/" + 
				"rdoUsuario/consultarRg/{rg}"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("rg", usuario.getRg())
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}	
		
		RdoUsuario rdoUsuario = gson.fromJson(json, RdoUsuario.class);
		if (rdoUsuario != null && Verificador.isValorado(rdoUsuario.getId()))
		{
			usuario.setRdoUsuario(rdoUsuario);
		}

	}
	

}
