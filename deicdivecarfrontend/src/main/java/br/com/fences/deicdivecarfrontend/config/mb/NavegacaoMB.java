package br.com.fences.deicdivecarfrontend.config.mb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class NavegacaoMB {

	private static final String HOME = "/privado/home/home.xhtml";
	private static final String LOGIN = "/login.xhtml";
	private static final String CADASTRAR_USUARIO = "/publico/cadastrousuario/cadastrarusuario.xhtml";
	private static final String CADASTRAR_USUARIO_SUCESSO = "/publico/cadastrousuario/sucesso.xhtml";
	private static final String CADASTRAR_VEICULO = "/privado/cadastroveiculo/cadastrarveiculo.xhtml";
	private static final String CADASTRAR_ALERTA = "/privado/cadastroalerta/cadastraralerta.xhtml";
	private static final String LISTAR_ALERTAS_DO_USUARIO = "/privado/listaalertas/listaralertasdousuario.xhtml";
	private static final String LISTAR_ALERTAS_GERAL = "/privado/listaalertas/listaralertasgeral.xhtml";


	public String getHome() {
		return HOME;
	}

	public String getCadastrarVeiculo() {
		return CADASTRAR_VEICULO;
	}

	public String getCadastrarUsuario() {
		return CADASTRAR_USUARIO;
	}

	public String getLogin() {
		return LOGIN;
	}

	public String getCadastrarUsuarioSucesso() {
		return CADASTRAR_USUARIO_SUCESSO;
	}

	public String getCadastrarAlerta() {
		return CADASTRAR_ALERTA;
	}

	public static String getListarAlertasDoUsuario() {
		return LISTAR_ALERTAS_DO_USUARIO;
	}

	public static String getListarAlertasGeral() {
		return LISTAR_ALERTAS_GERAL;
	}

	
}
