package br.com.fences.deicdivecarfrontend.cadastrarusuario.mb;

import java.io.Serializable;

import javax.enterprise.inject.New;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import br.com.fences.autenticacaoentidade.usuario.Usuario;
import br.com.fences.deicdivecarfrontend.cadastrarusuario.bo.CadastrarUsuarioBO;
import br.com.fences.deicdivecarfrontend.config.mb.NavegacaoMB;
import br.com.fences.fencesutils.conversor.Capitalizar;
import br.com.fences.fencesutils.conversor.ExcecaoParaMessageHtml;

@Named
@ViewScoped
public class CadastrarUsuarioMB implements Serializable{

	private static final long serialVersionUID = 7020066849012425486L;
	
	@Inject
	private transient Logger logger;
	
	@Inject
	private CadastrarUsuarioBO cadastrarUsuarioBO;
	
	@Inject @New
	private Usuario usuario;
	
	@Inject
	private NavegacaoMB navegacaoMB;
	
	private String emailConferencia;
	

	public String cadastrarSe()
	{
		
		String redirecionamento = "";
		if (!usuario.getUsername().equals(emailConferencia))
		{
			 Messages.create("O email deve coincidir com a email de conferência.").error().add("formCadastro:email1");
		}
		else
		{
			if (usuario.getRg().contains("00000"))
			{
				Messages.create("O RG não deve conter zeros sequenciais.").error().add("formCadastro:rg");
			}
			else
			{
				try
				{
					boolean existeEmail = cadastrarUsuarioBO.existeUsername(usuario);
					if (existeEmail)
					{
						Messages.create("O email informado já possui cadastro prévio.").error().add("formCadastro:email1");
					}
					else
					{
						cadastrarUsuarioBO.popularInformacoesDoRdo(usuario);
						if (usuario.getRdoUsuario() == null)
						{
							Messages.create("O RG " + usuario.getRg() + " informado nao possui cadastro no RDO.").error().add("formCadastro:rg");
						}
						else
						{
							//-- encripta a senha do usuario, a autenticacao da senha eh configurada no shiro.ini
							//usuario.setPassword(new Sha256Hash(usuario.getPassword()).toHex());
							logger.warn("Encriptacao do password inibida, e tambem no shiro.ini.");
							
							cadastrarUsuarioBO.adicionar(usuario);
							
							String nomes[] = usuario.getRdoUsuario().getNomeUsuario().split("\\s");
							String primeiroNome = nomes[0];
							primeiroNome = Capitalizar.converter(primeiroNome);
							Faces.getFlash().put("nome", primeiroNome);
							redirecionamento = navegacaoMB.getCadastrarUsuarioSucesso();
	
						}
						
					}
				}
				catch (Exception e)
				{
					String excecao = ExcecaoParaMessageHtml.converter(e);
					Messages.create("Erro no cadastrarSe.  {0}", excecao).error().add();
				}
			}
		}
			
		
		return redirecionamento;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getEmailConferencia() {
		return emailConferencia;
	}

	public void setEmailConferencia(String emailConferencia) {
		this.emailConferencia = emailConferencia;
	}

}
