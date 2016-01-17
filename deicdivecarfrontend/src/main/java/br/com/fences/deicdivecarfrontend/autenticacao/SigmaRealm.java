package br.com.fences.deicdivecarfrontend.autenticacao;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.fences.autenticacaoentidade.usuario.Usuario;
import br.com.fences.deicdivecarfrontend.config.AppConfig;
import br.com.fences.fencesutils.conversor.converter.ColecaoJsonAdapter;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;
import br.com.fences.fencesutils.verificador.Verificador;

public class SigmaRealm extends AuthorizingRealm {

	private Logger logger = LogManager.getLogger(SigmaRealm.class);
	
	private AppConfig appConfig;
	
	private VerificarErro verificarErro;
	
	private Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(Collection.class, new ColecaoJsonAdapter())
			.create();
 

	public SigmaRealm()
	{
		BeanManager bm = CDI.current().getBeanManager();
		
		//-- AppConfig
		Bean<AppConfig> beanAppConfig = (Bean<AppConfig>) bm.getBeans(AppConfig.class).iterator().next();
		CreationalContext<AppConfig> ctxAppConfig = bm.createCreationalContext(beanAppConfig);

		appConfig = (AppConfig) bm.getReference(beanAppConfig, AppConfig.class, ctxAppConfig);
		
		//-- VerificarErro
		Bean<VerificarErro> beanVerificarErro = (Bean<VerificarErro>) bm.getBeans(VerificarErro.class).iterator().next();
		CreationalContext<VerificarErro> ctxVerificarErro = bm.createCreationalContext(beanVerificarErro);

		verificarErro = (VerificarErro) bm.getReference(beanVerificarErro, VerificarErro.class, ctxVerificarErro);
		
	}
	
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {

		Set<String> roles = new HashSet<>();
		Set<Permission> permissions = new HashSet<>();
		
		for (Object tmp : pc.fromRealm(getClass().getName())) {
			
			String emailUsername = tmp.toString();
			
			Usuario usuario = consultarUsuario(emailUsername);

			if (usuario != null) {

//				if (usuario.getUsername().contains("deus") || usuario.getUsername().contains("super"))
//				{
//					roles.add("super");
//				}
				roles.add("usuario");
				
//				for (String temp : roles) { 
//
//					//Query<Role> q = ds.createQuery(Role.class);
//					//TODO recuperar permissoes
//					Role role = q.get();
//					if (role != null) {
//						permissions.addAll(role.getPermissions());
//					}
//				}
			}
		}
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setRoles(roles);
		info.setObjectPermissions(permissions);
		return info;

	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {

		String emailUsername = at.getPrincipal().toString();

		Usuario usuario = consultarUsuario(emailUsername);

		if (usuario != null) {
			return new SimpleAuthenticationInfo(usuario.getUsername(), usuario.getPassword(), getClass().getName());
		}
		throw new AuthenticationException();

	}
	
	private Usuario consultarUsuario(String email)
	{
		
		Client client = ClientBuilder.newClient();
		String servico = "http://" + appConfig.getServerBackendHost() + ":"+ appConfig.getServerBackendPort() + "/autenticacaobackend/rest/" + 
				"usuario/consultarUsername/{email}"; 
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.resolveTemplate("email", email)
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		else
		{
			if (!Verificador.isValorado(json) || json.equals("null"))
			{
				throw new UnknownAccountException();
			}
		}
		Usuario usuario = gson.fromJson(json, Usuario.class);
		if (usuario != null && Verificador.isValorado(usuario.getAtivo()))
		{
			if (usuario.getAtivo().equalsIgnoreCase("N"))
			{
				throw new CadastroPendenteDeAtivacaoException();
			}
		}
		
		return usuario;
	}


}
