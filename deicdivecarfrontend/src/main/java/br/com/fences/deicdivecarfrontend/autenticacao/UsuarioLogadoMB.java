package br.com.fences.deicdivecarfrontend.autenticacao;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;

import br.com.fences.autenticacaoentidade.usuario.Usuario;
import br.com.fences.deicdivecarfrontend.cadastrarusuario.bo.CadastrarUsuarioBO;
import br.com.fences.deicdivecarfrontend.config.Constantes;
import br.com.fences.fencesutils.conversor.Capitalizar;
import br.com.fences.fencesutils.verificador.Verificador;

@Named
@SessionScoped
public class UsuarioLogadoMB implements Serializable{

	private static final long serialVersionUID = 5513251990555251017L;
	
	@Inject
	private transient Logger logger;
	
	@Inject
	private CadastrarUsuarioBO cadastroUsuarioBO;
	
	@Inject
	private LogoutMB logoutMB;
	
	private Usuario usuario;
	
	@PostConstruct
	private void init()
	{
		String username = (String) SecurityUtils.getSubject().getSession().getAttribute(Constantes.USERNAME);
		if (Verificador.isValorado(username))
		{
			setUsuario(cadastroUsuarioBO.consultarUsername(username));
		}
		else
		{
			logger.error("Erro na criação da sessão autenticada, não foi possível recuperar o username.");
			try {
				logoutMB.desautenticar();
			} catch (IOException e) {
				logger.error("Erro ao desautenticar: " + e.getMessage(), e);
			}
		}
		
	}

	/**
	 * Recuperar informacoes do usuario cadastradas no banco.
	 * Usar isso apos uma atualizacao.
	 */
	public void refresh()
	{
		init();
	}
	
	public String getPrimeiroNome(){
		String primeiroNome = usuario.getUsername().split("\\s")[0];
		if (usuario.getRdoUsuario() != null && Verificador.isValorado(usuario.getRdoUsuario().getNomeUsuario()))
		{
			primeiroNome = usuario.getRdoUsuario().getNomeUsuario().split("\\s")[0];
			primeiroNome = Capitalizar.converter(primeiroNome);
		}
		return primeiroNome;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}
