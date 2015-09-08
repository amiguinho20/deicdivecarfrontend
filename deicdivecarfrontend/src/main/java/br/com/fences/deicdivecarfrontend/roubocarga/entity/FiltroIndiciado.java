package br.com.fences.deicdivecarfrontend.roubocarga.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.mongodb.BasicDBObject;

import br.com.fences.fencesutils.conversor.AcentuacaoParaRegex;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;
import br.com.fences.ocorrenciaentidade.ocorrencia.pessoa.Pessoa;

 
@Named
//@javax.faces.view.ViewScoped
@SessionScoped
public class FiltroIndiciado implements Serializable{ 
  
	private static final long serialVersionUID = -7701810214532312594L;
	
	private Map<String, Object> primeMapFiltro;
	
	private List<Ocorrencia> ocorrencias;
	
	public BasicDBObject montarPesquisa()
	{
		BasicDBObject pesquisa = new BasicDBObject();
		if (primeMapFiltro != null && !primeMapFiltro.isEmpty())
		{
			for (Map.Entry<String, Object> filtro : primeMapFiltro.entrySet())
			{
				if (filtro.getValue() != null)
				{
					String valor = filtro.getValue().toString().trim();
					if (Verificador.isValorado(valor))
					{
						String convertido = AcentuacaoParaRegex.converter(valor);
						pesquisa.append(filtro.getKey(), new BasicDBObject("$regex", convertido).append("$options", "i"));
					}
				}
			}
		}
		if (Verificador.isValorado(ocorrencias))
		{
			Set<String> codigos = new LinkedHashSet<>();
			for (Ocorrencia ocorrencia : ocorrencias)
			{
				if (ocorrencia.getFlagFlagrante().equalsIgnoreCase("S"))
				{
					if (Verificador.isValorado(ocorrencia.getPessoas()))
					{
						for (Pessoa pessoa : ocorrencia.getPessoas())
						{
							if (pessoa.getIdTipoPessoa().equals("1")) //-- indiciado
							{
								codigos.add(ocorrencia.getId());
							}
						}
					}
				}
			}
			if (!codigos.isEmpty())
			{
				pesquisa.append("ocorrencias.id", new BasicDBObject("$in", codigos));
			}
		}
		return pesquisa;
	}
	
	public Map<String, Object> montarPesquisaMap()
	{
		Map<String, Object> filtros = new HashMap<>();

		if (primeMapFiltro != null)
		{
			filtros.putAll(primeMapFiltro);
		}
		if (Verificador.isValorado(ocorrencias))
		{
			Set<String> codigos = new LinkedHashSet<>();
			for (Ocorrencia ocorrencia : ocorrencias)
			{
//				if (ocorrencia.getFlagFlagrante().equalsIgnoreCase("S"))
//				{
//					if (Verificador.isValorado(ocorrencia.getPessoas()))
//					{
//						for (Pessoa pessoa : ocorrencia.getPessoas())
//						{
//							if (pessoa.getIdTipoPessoa().equals("1")) //-- indiciado
//							{
								codigos.add(ocorrencia.getId());
//							}
//						}
//					}
//				}
			}
			if (!codigos.isEmpty())
			{	
				StringBuilder concatenado = new StringBuilder();
				for (String codigo : codigos)
				{
					if (concatenado.length() > 0)
					{
						concatenado.append(";");
					}
					concatenado.append(codigo);
				}
				filtros.put("ocorrencias.id", concatenado.toString());
			}
		}

		return filtros;
	}
	
	public void limpar()
	{
		if (Verificador.isValorado(ocorrencias))
		{
			ocorrencias.clear();
		}
	}

	public Map<String, Object> getPrimeMapFiltro() {
		return primeMapFiltro;
	}

	public void setPrimeMapFiltro(Map<String, Object> primeMapFiltro) {
		this.primeMapFiltro = primeMapFiltro;
	}

	public List<Ocorrencia> getOcorrencias() {
		return ocorrencias;
	}

	public void setOcorrencias(List<Ocorrencia> ocorrencias) {
		this.ocorrencias = ocorrencias;
	}

	@Override
	public String toString() {
		return "FiltroIndiciado [primeMapFiltro=" + primeMapFiltro + ", ocorrencias=" + ocorrencias + "]";
	}
	
	
	
}