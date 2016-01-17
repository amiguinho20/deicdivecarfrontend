package br.com.fences.deicdivecarfrontend.autenticacao;

import java.io.IOException;

import javax.enterprise.inject.Model;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import br.com.fences.deicdivecarfrontend.config.Constantes;

@Model
public class LoginMB {
	
	private String username = "";
	private String password = "";
	private boolean remember;

	public String autenticar() throws IOException {
		try {
			UsernamePasswordToken upt = new UsernamePasswordToken(username, password, remember);
			SecurityUtils.getSubject().login(upt);
			HttpServletRequest request = Faces.getRequest();
			SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
			if (savedRequest == null)
			{
				Faces.redirect("/login.xhtml");
			}
			else
			{
				String requestUrl = savedRequest.getRequestUrl();
				SecurityUtils.getSubject().getSession().setAttribute(Constantes.USERNAME, username);
				Faces.redirect(requestUrl);
			}
		} catch ( UnknownAccountException uae ) {
			//Messages.addGlobalError("Usuário desconhecido.");
			Messages.create("Usuário desconhecido.").error().add("login:username");
		} catch ( CadastroPendenteDeAtivacaoException cda ) {
			Messages.create("Cadastro pendente de ativação. Por gentileza, verificar o email de ativação.").error().add("login:username");
		} catch ( IncorrectCredentialsException ice ) {
			//Messages.addGlobalError("Senha inválida.");
			Messages.create("Senha inválida.").error().add("login:password");
		} catch ( LockedAccountException lae ) {
			//Messages.addGlobalError("Conta bloqueada.");
			Messages.create("Usuário bloqueado/desativado.").error().add("login:username");
		} catch ( AuthenticationException ae ) {
			Messages.addGlobalError("Problemas na autenticação.");
			ae.printStackTrace(); 
		}
		return "";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRemember() {
		return remember;
	}

	public void setRemember(boolean remember) {
		this.remember = remember;
	}
	
	
}
