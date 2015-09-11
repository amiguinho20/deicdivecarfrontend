package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import br.com.fences.deicdivecarentidade.indiciado.Indiciado;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.IndiciadoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroIndiciado;
import br.com.fences.deicdivecarfrontend.roubocarga.util.IndiciadoLazyDataModel;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;

@Named
//@javax.faces.view.ViewScoped
@SessionScoped
public class IndiciadoMB implements Serializable{

	private static final long serialVersionUID = 8494067495322874580L;

	@Inject 
	private transient Logger logger;
	
	@Inject
	private IndiciadoBO indiciadoBO;
	
	@Inject
	private FiltroIndiciado filtroIndiciado;

	private String formulario = "formLista";
	
	private String origem = "";
	
//	private List<Ocorrencia> ocorrencias;
	private LazyDataModel<Indiciado> indiciadosResultadoLazy;
	
	private boolean informativoFuncionalidade;

	public void mudarInformativoFuncionalidade(){
		informativoFuncionalidade = !informativoFuncionalidade;
	}

	
	@PostConstruct
	private void init() {	  
		pesquisar();
	} 
	
	/**
	 * A pesquisa lazy de fato e' feita apos o termino da execucao desse metodo, pelo primefaces.
	 * O filtro nao pode ser limpo aqui.
	 */
	public void pesquisar(){ 
		if (Verificador.isValorado(origem) && !origem.equals("ocorrencia.xhtml"))
		{
			filtroIndiciado.limpar();
		}
		setIndiciadosResultadoLazy(new IndiciadoLazyDataModel(indiciadoBO, filtroIndiciado));
	}
	
	public void pesquisar(List<Ocorrencia> ocorrencias)
	{
		filtroIndiciado.setOcorrencias(ocorrencias);
		pesquisar();
	}
	
	public void pesquisarLimpo()
	{
		filtroIndiciado.limpar();
		pesquisar();
		//return "/modulo/roubocarga/indiciado/indiciado.xhtml";
	}
	

//	public List<Ocorrencia> getOcorrencias() {
//		return ocorrencias;
//	}
//
//	public void setOcorrencias(List<Ocorrencia> ocorrencias) {
//		this.ocorrencias = ocorrencias;
//	}

	public LazyDataModel<Indiciado> getIndiciadosResultadoLazy() {
		return indiciadosResultadoLazy;
	}

	public void setIndiciadosResultadoLazy(LazyDataModel<Indiciado> indiciadosResultadoLazy) {
		this.indiciadosResultadoLazy = indiciadosResultadoLazy;
	}

	public String getFormulario() {
		return formulario;
	}

	public void setFormulario(String formulario) {
		this.formulario = formulario;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}


	public boolean isInformativoFuncionalidade() {
		return informativoFuncionalidade;
	}


	public void setInformativoFuncionalidade(boolean informativoFuncionalidade) {
		this.informativoFuncionalidade = informativoFuncionalidade;
	}
	
}
