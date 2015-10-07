package br.com.fences.deicdivecarfrontend.filtrocustom;

import java.util.Date;
import java.util.Map;

import org.primefaces.model.TreeNode;

import br.com.fences.fencesutils.conversor.RetirarAcentuacao;
import br.com.fences.fencesutils.filtrocustom.ChaveValor;
import br.com.fences.fencesutils.filtrocustom.TipoFiltro;
import br.com.fences.fencesutils.filtrocustom.TipoPesquisaTexto;
import br.com.fences.fencesutils.verificador.Verificador;

public class FiltroModelo {
	
	private String rotulo;
	private TipoFiltro tipo;
	private String informativo;
	private String atributoDePesquisa;
	private boolean habilitado = true;
	private boolean ativo = true;
	
	//-- TEXTO
	private String valorTexto;
	private TipoPesquisaTexto valorTextoTipo;
	
	//-- INTERVALO_DATA
	private String intervaloDataInicialMinimoFonte; 
	private String intervaloDataFinalMaximoFonte;
	private String intervaloDataInicialMinimo; 
	private String intervaloDataFinalMaximo;
	private Date intervaloDataInicial;  
	private Date intervaloDataFinal;    
	
	//-- LISTA_SELECAO_UNICA
	private Map<String, String> listaSelecaoUnica;
	private String listaSelecaoUnicaIdSelecionado;
	private String listaSelecaoUnicaDescricaoSelecionado;
	private String listaSelecaoUnicaFonte;
	private Map<String, String> listaSelecaoUnicaFonteFixa;

	//-- ARVORE
	private String arvoreFonte;
	private String arvoreTextoPesquisa;
	private TreeNode arvoreRaiz; //-- primefaces
	private TreeNode[] arvoreRaizSelecao; //
	private boolean arvoreSelecionaFilhos = true;
	
	//-- GEO em RAIO
	private String geoRaioLatitude;
	private String geoRaioLongitude;
	private String geoRaioEmMetros;
	private String geoRaioEndereco;
	
	
	//-- pesquisar na arvore
	public void pesquisarTermoNaArvore()
	{
		pesquisarTermoNaArvore(arvoreRaiz);
	}
	
	private void pesquisarTermoNaArvore(TreeNode arvore)
	{
		if (Verificador.isValorado(arvoreTextoPesquisa) && arvore != null)
		{
			if (arvore.getChildCount() > 0)
			{
				for (TreeNode elemento : arvore.getChildren())
				{
					ChaveValor<String, String> objeto = (ChaveValor<String, String>) elemento.getData();
					
					String valorEmMinusculo = objeto.getValor().toLowerCase();
					String textoPesquisaEmMinusculo = arvoreTextoPesquisa.toLowerCase();
					
					String valorEmMinusculoSemAcento = RetirarAcentuacao.converter(valorEmMinusculo);
					String textoPesquisaEmMinusculoSemAcento = RetirarAcentuacao.converter(textoPesquisaEmMinusculo);
					
					if (valorEmMinusculoSemAcento.contains(textoPesquisaEmMinusculoSemAcento))
					{
						elemento.setType("PESQUISADO");
						//elemento.getParent().setExpanded(true);
						
						TreeNode elementoRef = elemento.getParent();
						while (elementoRef != null)
						{
							elementoRef.setExpanded(true);
							elementoRef = elementoRef.getParent();
						}
						
					}
					else
					{
						elemento.setType("ORIGINAL");
						if (!elemento.isSelected())
						{
							elemento.setExpanded(false);
						}
					}
					
					//-- recursao
					pesquisarTermoNaArvore(elemento);
					
				}
			}
		}
	}

	public String getRotulo() {
		return rotulo;
	}

	public void setRotulo(String rotulo) {
		this.rotulo = rotulo;
	}

	public TipoFiltro getTipo() {
		return tipo;
	}

	public void setTipo(TipoFiltro tipo) {
		this.tipo = tipo;
	}

	public String getInformativo() {
		return informativo;
	}

	public void setInformativo(String informativo) {
		this.informativo = informativo;
	}

	public String getAtributoDePesquisa() {
		return atributoDePesquisa;
	}

	public void setAtributoDePesquisa(String atributoDePesquisa) {
		this.atributoDePesquisa = atributoDePesquisa;
	}

	public String getValorTexto() {
		return valorTexto;
	}

	public void setValorTexto(String valorTexto) {
		this.valorTexto = valorTexto;
	}

	public Map<String, String> getListaSelecaoUnica() {
		return listaSelecaoUnica;
	}

	public void setListaSelecaoUnica(Map<String, String> listaSelecaoUnica) {
		this.listaSelecaoUnica = listaSelecaoUnica;
	}


	public String getIntervaloDataInicialMinimoFonte() {
		return intervaloDataInicialMinimoFonte;
	}

	public void setIntervaloDataInicialMinimoFonte(String intervaloDataInicialMinimoFonte) {
		this.intervaloDataInicialMinimoFonte = intervaloDataInicialMinimoFonte;
	}

	public String getIntervaloDataFinalMaximoFonte() {
		return intervaloDataFinalMaximoFonte;
	}

	public void setIntervaloDataFinalMaximoFonte(String intervaloDataFinalMaximoFonte) {
		this.intervaloDataFinalMaximoFonte = intervaloDataFinalMaximoFonte;
	}

	public String getIntervaloDataInicialMinimo() {
		return intervaloDataInicialMinimo;
	}

	public void setIntervaloDataInicialMinimo(String intervaloDataInicialMinimo) {
		this.intervaloDataInicialMinimo = intervaloDataInicialMinimo;
	}

	public String getIntervaloDataFinalMaximo() {
		return intervaloDataFinalMaximo;
	}

	public void setIntervaloDataFinalMaximo(String intervaloDataFinalMaximo) {
		this.intervaloDataFinalMaximo = intervaloDataFinalMaximo;
	}

	public Date getIntervaloDataInicial() {
		return intervaloDataInicial;
	}

	public void setIntervaloDataInicial(Date intervaloDataInicial) {
		this.intervaloDataInicial = intervaloDataInicial;
	}

	public Date getIntervaloDataFinal() {
		return intervaloDataFinal;
	}

	public void setIntervaloDataFinal(Date intervaloDataFinal) {
		this.intervaloDataFinal = intervaloDataFinal;
	}

	public String getArvoreTextoPesquisa() {
		return arvoreTextoPesquisa;
	}

	public void setArvoreTextoPesquisa(String arvoreTextoPesquisa) {
		this.arvoreTextoPesquisa = arvoreTextoPesquisa;
	}

	public TreeNode getArvoreRaiz() {
		return arvoreRaiz;
	}

	public void setArvoreRaiz(TreeNode arvoreRaiz) {
		this.arvoreRaiz = arvoreRaiz;
	}

	public TreeNode[] getArvoreRaizSelecao() {
		return arvoreRaizSelecao;
	}

	public void setArvoreRaizSelecao(TreeNode[] arvoreRaizSelecao) {
		this.arvoreRaizSelecao = arvoreRaizSelecao;
	}

	public String getListaSelecaoUnicaFonte() {
		return listaSelecaoUnicaFonte;
	}

	public void setListaSelecaoUnicaFonte(String listaSelecaoUnicaFonte) {
		this.listaSelecaoUnicaFonte = listaSelecaoUnicaFonte;
	}

	public Map<String, String> getListaSelecaoUnicaFonteFixa() {
		return listaSelecaoUnicaFonteFixa;
	}

	public void setListaSelecaoUnicaFonteFixa(Map<String, String> listaSelecaoUnicaFonteFixa) {
		this.listaSelecaoUnicaFonteFixa = listaSelecaoUnicaFonteFixa;
	}

	public String getListaSelecaoUnicaIdSelecionado() {
		return listaSelecaoUnicaIdSelecionado;
	}

	public void setListaSelecaoUnicaIdSelecionado(String listaSelecaoUnicaIdSelecionado) {
		this.listaSelecaoUnicaIdSelecionado = listaSelecaoUnicaIdSelecionado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atributoDePesquisa == null) ? 0 : atributoDePesquisa.hashCode());
		result = prime * result + ((informativo == null) ? 0 : informativo.hashCode());
		result = prime * result + ((intervaloDataFinal == null) ? 0 : intervaloDataFinal.hashCode());
		result = prime * result + ((intervaloDataFinalMaximo == null) ? 0 : intervaloDataFinalMaximo.hashCode());
		result = prime * result
				+ ((intervaloDataFinalMaximoFonte == null) ? 0 : intervaloDataFinalMaximoFonte.hashCode());
		result = prime * result + ((intervaloDataInicial == null) ? 0 : intervaloDataInicial.hashCode());
		result = prime * result + ((intervaloDataInicialMinimo == null) ? 0 : intervaloDataInicialMinimo.hashCode());
		result = prime * result
				+ ((intervaloDataInicialMinimoFonte == null) ? 0 : intervaloDataInicialMinimoFonte.hashCode());
		result = prime * result + ((listaSelecaoUnica == null) ? 0 : listaSelecaoUnica.hashCode());
		result = prime * result + ((listaSelecaoUnicaFonte == null) ? 0 : listaSelecaoUnicaFonte.hashCode());
		result = prime * result + ((listaSelecaoUnicaFonteFixa == null) ? 0 : listaSelecaoUnicaFonteFixa.hashCode());
		result = prime * result
				+ ((listaSelecaoUnicaIdSelecionado == null) ? 0 : listaSelecaoUnicaIdSelecionado.hashCode());
		result = prime * result + ((rotulo == null) ? 0 : rotulo.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		result = prime * result + ((valorTexto == null) ? 0 : valorTexto.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FiltroModelo other = (FiltroModelo) obj;
		if (atributoDePesquisa == null) {
			if (other.atributoDePesquisa != null)
				return false;
		} else if (!atributoDePesquisa.equals(other.atributoDePesquisa))
			return false;
		if (informativo == null) {
			if (other.informativo != null)
				return false;
		} else if (!informativo.equals(other.informativo))
			return false;
		if (intervaloDataFinal == null) {
			if (other.intervaloDataFinal != null)
				return false;
		} else if (!intervaloDataFinal.equals(other.intervaloDataFinal))
			return false;
		if (intervaloDataFinalMaximo == null) {
			if (other.intervaloDataFinalMaximo != null)
				return false;
		} else if (!intervaloDataFinalMaximo.equals(other.intervaloDataFinalMaximo))
			return false;
		if (intervaloDataFinalMaximoFonte == null) {
			if (other.intervaloDataFinalMaximoFonte != null)
				return false;
		} else if (!intervaloDataFinalMaximoFonte.equals(other.intervaloDataFinalMaximoFonte))
			return false;
		if (intervaloDataInicial == null) {
			if (other.intervaloDataInicial != null)
				return false;
		} else if (!intervaloDataInicial.equals(other.intervaloDataInicial))
			return false;
		if (intervaloDataInicialMinimo == null) {
			if (other.intervaloDataInicialMinimo != null)
				return false;
		} else if (!intervaloDataInicialMinimo.equals(other.intervaloDataInicialMinimo))
			return false;
		if (intervaloDataInicialMinimoFonte == null) {
			if (other.intervaloDataInicialMinimoFonte != null)
				return false;
		} else if (!intervaloDataInicialMinimoFonte.equals(other.intervaloDataInicialMinimoFonte))
			return false;
		if (listaSelecaoUnica == null) {
			if (other.listaSelecaoUnica != null)
				return false;
		} else if (!listaSelecaoUnica.equals(other.listaSelecaoUnica))
			return false;
		if (listaSelecaoUnicaFonte == null) {
			if (other.listaSelecaoUnicaFonte != null)
				return false;
		} else if (!listaSelecaoUnicaFonte.equals(other.listaSelecaoUnicaFonte))
			return false;
		if (listaSelecaoUnicaFonteFixa == null) {
			if (other.listaSelecaoUnicaFonteFixa != null)
				return false;
		} else if (!listaSelecaoUnicaFonteFixa.equals(other.listaSelecaoUnicaFonteFixa))
			return false;
		if (listaSelecaoUnicaIdSelecionado == null) {
			if (other.listaSelecaoUnicaIdSelecionado != null)
				return false;
		} else if (!listaSelecaoUnicaIdSelecionado.equals(other.listaSelecaoUnicaIdSelecionado))
			return false;
		if (rotulo == null) {
			if (other.rotulo != null)
				return false;
		} else if (!rotulo.equals(other.rotulo))
			return false;
		if (tipo != other.tipo)
			return false;
		if (valorTexto == null) {
			if (other.valorTexto != null)
				return false;
		} else if (!valorTexto.equals(other.valorTexto))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FiltroModelo [rotulo=" + rotulo + ", tipo=" + tipo + ", informativo=" + informativo
				+ ", atributoDePesquisa=" + atributoDePesquisa + ", valorTexto=" + valorTexto
				+ ", intervaloDataInicialMinimoFonte=" + intervaloDataInicialMinimoFonte
				+ ", intervaloDataFinalMaximoFonte=" + intervaloDataFinalMaximoFonte + ", intervaloDataInicialMinimo="
				+ intervaloDataInicialMinimo + ", intervaloDataFinalMaximo=" + intervaloDataFinalMaximo
				+ ", intervaloDataInicial=" + intervaloDataInicial + ", intervaloDataFinal=" + intervaloDataFinal
				+ ", listaSelecaoUnica=" + listaSelecaoUnica + ", listaSelecaoUnicaIdSelecionado="
				+ listaSelecaoUnicaIdSelecionado + ", listaSelecaoUnicaFonte=" + listaSelecaoUnicaFonte
				+ ", listaSelecaoUnicaFonteFixa=" + listaSelecaoUnicaFonteFixa + "]";
	}

	public String getListaSelecaoUnicaDescricaoSelecionado() {
		return listaSelecaoUnicaDescricaoSelecionado;
	}

	public void setListaSelecaoUnicaDescricaoSelecionado(String listaSelecaoUnicaDescricaoSelecionado) {
		this.listaSelecaoUnicaDescricaoSelecionado = listaSelecaoUnicaDescricaoSelecionado;
	}

	public String getArvoreFonte() {
		return arvoreFonte;
	}

	public void setArvoreFonte(String arvoreFonte) {
		this.arvoreFonte = arvoreFonte;
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	public boolean isArvoreSelecionaFilhos() {
		return arvoreSelecionaFilhos;
	}

	public void setArvoreSelecionaFilhos(boolean arvoreSelecionaFilhos) {
		this.arvoreSelecionaFilhos = arvoreSelecionaFilhos;
	}

	public TipoPesquisaTexto getValorTextoTipo() {
		return valorTextoTipo;
	}

	public void setValorTextoTipo(TipoPesquisaTexto valorTextoTipo) {
		this.valorTextoTipo = valorTextoTipo;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public String getGeoRaioLatitude() {
		return geoRaioLatitude;
	}

	public void setGeoRaioLatitude(String geoRaioLatitude) {
		this.geoRaioLatitude = geoRaioLatitude;
	}

	public String getGeoRaioLongitude() {
		return geoRaioLongitude;
	}

	public void setGeoRaioLongitude(String geoRaioLongitude) {
		this.geoRaioLongitude = geoRaioLongitude;
	}

	public String getGeoRaioEmMetros() {
		return geoRaioEmMetros;
	}

	public void setGeoRaioEmMetros(String geoRaioEmMetros) {
		this.geoRaioEmMetros = geoRaioEmMetros;
	}

	public String getGeoRaioEndereco() {
		return geoRaioEndereco;
	}

	public void setGeoRaioEndereco(String geoRaioEndereco) {
		this.geoRaioEndereco = geoRaioEndereco;
	}

	
	
	
}
