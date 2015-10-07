package br.com.fences.deicdivecarfrontend.filtrocustom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;


/**
 * Prove um cache para as fontes de tipos do FiltroCustom
 * 
 * Cache configurado em 6h.
 * 
 */
@ApplicationScoped
public class FiltroFonteCache implements Serializable{

	private static final long serialVersionUID = 3525719666287006566L;

	private static final Long PERIODO_CACHE = 1000L * 60 * 60 * 6; //-- 6 horas
	
	private Map<String, DataHoraJson> cache = new HashMap<>();
	
	public String getCache(String servicoFonte)
	{
		String json = null;
		DataHoraJson dataHoraJson = cache.get(servicoFonte);
		if (dataHoraJson != null)
		{
			long horarioDeCriacao = dataHoraJson.getCriacao();
			long horarioVigenciaDoCache = horarioDeCriacao + PERIODO_CACHE;
			long horarioCorrente = System.currentTimeMillis();
			
			if (horarioCorrente < horarioVigenciaDoCache)
			{
				json = dataHoraJson.getJson();
			}
		}
		return json;
	}
	
	public void addCache(String servicoFonte, String json)
	{
		DataHoraJson dataHoraJson = new DataHoraJson(System.currentTimeMillis(), json);
		cache.put(servicoFonte, dataHoraJson);
	}
	
	/**
	 * classe interna auxiliar apenas para encapsular a informacao
	 */
	private class DataHoraJson{
		private long criacao;
		private String json;
		public DataHoraJson(long criacao, String json) {
			super();
			this.criacao = criacao;
			this.json = json;
		}
		public String getJson() {
			return json;
		}
		public long getCriacao() {
			return criacao;
		}
	}
}
