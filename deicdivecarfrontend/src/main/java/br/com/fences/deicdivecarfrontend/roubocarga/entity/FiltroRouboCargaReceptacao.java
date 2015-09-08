package br.com.fences.deicdivecarfrontend.roubocarga.entity;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.mongodb.BasicDBObject;

import br.com.fences.fencesutils.formatar.FormatarData;
import br.com.fences.fencesutils.verificador.Verificador;


@Named
//@javax.faces.view.ViewScoped
@SessionScoped
public class FiltroRouboCargaReceptacao implements Serializable{ 

	private static final long serialVersionUID = -7701810214532312594L;
	
	private DateFormat df = FormatarData.getAnoMesDiaContatenado();
	
	
	private Date dataInicial;
	private Date dataFinal;
	private String flagFlagrante;
	private String complemento;
	private String natureza;
	private String numBo;
	private String anoBo;
	private String idDelegacia;
	
	//-- limites para montagem e validacao do calendario (nao devem ser limpos)
	private String limiteDataInicial;
	private String limiteDataFinal;
	
	//-- filtros para pesquisa geo
	private String latitude;
	private String longitude;
	private String raioEmMetros;
	
	public void limpar()
	{
		setDataInicial(null);
		setDataFinal(null);
		setFlagFlagrante("");
		setComplemento("");
		setNatureza("");
		setLatitude("");
		setLongitude("");
		setRaioEmMetros("");
		setNumBo("");
		setAnoBo("");
		setIdDelegacia("");
	}
	
	public String getDataInicialString(){
		String dataFormatada = "";
		if (dataInicial != null)
		{ 
			dataFormatada = df.format(dataInicial) + "000000";
		}
		return dataFormatada;
	}
	
	public String getDataFinalString(){
		String dataFormatada = "";
		if (dataFinal != null)
		{
			dataFormatada = df.format(dataFinal) + "999999";
		}
		return dataFormatada;
	}
	
	public HashMap<String, String> montarPesquisaMap()
	{
		HashMap<String, String> map = new HashMap<>();
		if (Verificador.isValorado(getDataInicialString()))
		{
			map.put("dataInicial", getDataInicialString());
		}
		if (Verificador.isValorado(getDataFinalString()))
		{
			map.put("dataFinal", getDataFinalString());
		}
		if (Verificador.isValorado(getFlagFlagrante()))
		{
			map.put("flagFlagrante", getFlagFlagrante());
		}
		if (Verificador.isValorado(getComplemento()))
		{
			map.put("complemento", getComplemento());
		}
		if (Verificador.isValorado(getNatureza()))
		{
			map.put("natureza", getNatureza());
		}
		if (Verificador.isValorado(getLatitude()))
		{
			map.put("latitude", getLatitude());
		}
		if (Verificador.isValorado(getLongitude()))
		{
			map.put("longitude", getLongitude());
		}
		if (Verificador.isValorado(getRaioEmMetros()))
		{
			map.put("raioEmMetros", getRaioEmMetros());
		}
		if (Verificador.isValorado(getNumBo()))
		{
			map.put("numBo", getNumBo());
		}
		if (Verificador.isValorado(getAnoBo()))
		{
			map.put("anoBo", getAnoBo());
		}
		if (Verificador.isValorado(getIdDelegacia()))
		{
			map.put("idDelegacia", getIdDelegacia());
		}

		
		return map;
	}

	public BasicDBObject montarPesquisa()
	{
		BasicDBObject pesquisa = new BasicDBObject();
		
		if (dataInicial != null || dataFinal != null)
		{
			BasicDBObject periodo = new BasicDBObject();
			if (dataInicial != null)
			{
				periodo.put("$gt", getDataInicialString());
			}
			if (dataFinal != null)
			{
				periodo.put("$lt", getDataFinalString());
			}
			pesquisa.put("DATAHORA_REGISTRO_BO", periodo);
		}
		if (Verificador.isValorado(flagFlagrante))
		{
			pesquisa.put("FLAG_FLAGRANTE", flagFlagrante);
		}
		if (Verificador.isValorado(complemento))
		{
			if (complemento.equalsIgnoreCase("A"))
			{	//-- ocorrencias que nao possuem complementares
				pesquisa.put("CUSTOM_COMPLEMENTAR_LOCALIZACAO", new BasicDBObject("$exists", false));
			}
			else if (complemento.equalsIgnoreCase("B"))
			{	//-- ocorrencias que possuem complementares
				pesquisa.put("CUSTOM_COMPLEMENTAR_LOCALIZACAO", new BasicDBObject("$exists", true));
			}
			else if (complemento.equalsIgnoreCase("C"))
			{	//-- apenas ocorrencias complementares 
				pesquisa.put("ANO_REFERENCIA_BO", new BasicDBObject("$exists", true));
				pesquisa.put("NATUREZA.ID_OCORRENCIA", "40");
				pesquisa.put("NATUREZA.ID_ESPECIE", "40");
			}
		}		
		if (Verificador.isValorado(natureza))
		{
			if (natureza.equalsIgnoreCase("C"))
			{	
				pesquisa.put("NATUREZA.ID_NATUREZA", 
					new BasicDBObject("$nin", 
						Arrays.asList("180A", "180B", "180C") ));
			}
			else if (natureza.equalsIgnoreCase("R"))
			{	
				pesquisa.put("NATUREZA.ID_NATUREZA", 
					new BasicDBObject("$in", 
							Arrays.asList("180A", "180B", "180C") ));
			}
		}		
		return pesquisa;
	}
	
	
	
	@Override
	public String toString() {
		return montarPesquisa().toString();
	}

	public Date getDataInicial() {
		return dataInicial;
	}
	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}
	public Date getDataFinal() {
		return dataFinal;
	}
	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}
	

	public String getComplemento() {
		return complemento;
	}
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public String getFlagFlagrante() {
		return flagFlagrante;
	}

	public void setFlagFlagrante(String flagFlagrante) {
		this.flagFlagrante = flagFlagrante;
	}

	public String getNatureza() {
		return natureza;
	}

	public void setNatureza(String natureza) {
		this.natureza = natureza;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getRaioEmMetros() {
		return raioEmMetros;
	}

	public void setRaioEmMetros(String raioEmMetros) {
		this.raioEmMetros = raioEmMetros;
	}

	public String getNumBo() {
		return numBo;
	}

	public void setNumBo(String numBo) {
		this.numBo = numBo;
	}

	public String getAnoBo() {
		return anoBo;
	}

	public void setAnoBo(String anoBo) {
		this.anoBo = anoBo;
	}

	public String getIdDelegacia() {
		return idDelegacia;
	}

	public void setIdDelegacia(String idDelegacia) {
		this.idDelegacia = idDelegacia;
	}

	public String getLimiteDataInicial() {
		return limiteDataInicial;
	}

	public void setLimiteDataInicial(String limiteDataInicial) {
		this.limiteDataInicial = limiteDataInicial;
	}

	public String getLimiteDataFinal() {
		return limiteDataFinal;
	}

	public void setLimiteDataFinal(String limiteDataFinal) {
		this.limiteDataFinal = limiteDataFinal;
	}


	
}