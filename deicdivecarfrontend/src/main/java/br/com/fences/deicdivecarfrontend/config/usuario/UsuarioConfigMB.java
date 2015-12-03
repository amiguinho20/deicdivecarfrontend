package br.com.fences.deicdivecarfrontend.config.usuario;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped //-- por causa do mapa na segunda pagina   
public class UsuarioConfigMB implements Serializable{

	private static final long serialVersionUID = -2669364155677039988L;

	private boolean abrirDetalheOcorrenciaEmJanelaSeparada;
	private boolean informativoFuncionalidade;
	private String alturaDoMapa = "400px";

	
	
	public void mudarInformativoFuncionalidade(){
		informativoFuncionalidade = !informativoFuncionalidade;
	}

	
	public boolean isAbrirDetalheOcorrenciaEmJanelaSeparada() {
		return abrirDetalheOcorrenciaEmJanelaSeparada;
	}

	public void setAbrirDetalheOcorrenciaEmJanelaSeparada(boolean abrirDetalheOcorrenciaEmJanelaSeparada) {
		this.abrirDetalheOcorrenciaEmJanelaSeparada = abrirDetalheOcorrenciaEmJanelaSeparada;
	}


	public boolean isInformativoFuncionalidade() {
		return informativoFuncionalidade;
	}


	public void setInformativoFuncionalidade(boolean informativoFuncionalidade) {
		this.informativoFuncionalidade = informativoFuncionalidade;
	}


	public String getAlturaDoMapa() {
		return alturaDoMapa;
	}


	public void setAlturaDoMapa(String alturaDoMapa) {
		this.alturaDoMapa = alturaDoMapa;
	}
	
	
	
	
}
