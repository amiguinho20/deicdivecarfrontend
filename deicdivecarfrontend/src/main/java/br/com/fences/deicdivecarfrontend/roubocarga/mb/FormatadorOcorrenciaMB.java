package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.enterprise.inject.Model;

import br.com.fences.fencesutils.conversor.ExcecaoParaMessageHtml;
import br.com.fences.fencesutils.formatar.FormatarData;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;

/**
 * Classe managed bean responsavel pelas formatacoes de objetos para apresentacao direta nas paginas.
 */
@Model
public class FormatadorOcorrenciaMB {

	public String formatarOcorrencia(Ocorrencia ocorrencia)
	{
		StringBuilder ocorrenciaFormatada = new StringBuilder();
		if (ocorrencia != null)
		{
			ocorrenciaFormatada.append(ocorrencia.getNumBo()); 
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getAnoBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getNomeDelegacia());
		}
		return ocorrenciaFormatada.toString();
	}
	
	public String formatarReferencia(Ocorrencia ocorrencia)
	{
		StringBuilder ocorrenciaFormatada = new StringBuilder();
		if (ocorrencia != null)
		{
			ocorrenciaFormatada.append(ocorrencia.getNumReferenciaBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getAnoReferenciaBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getDelegaciaReferencia());
		}
		return ocorrenciaFormatada.toString();
	}
	
	public String formatarEndereco(Ocorrencia ocorrencia)
	{
		String endereco = "";
		if (ocorrencia != null)
		{
			endereco = concatenarEndereco(ocorrencia.getLogradouro(),
				ocorrencia.getNumeroLogradouro(),
				ocorrencia.getBairro(), ocorrencia.getCidade(),
				ocorrencia.getIdUf());
		}
		return endereco;
	}
	
	public String concatenarEndereco(String... campos)   
	{
		String resultado = "";
		for (String campo : campos) 
		{
			if (campo != null && !campo.trim().isEmpty() && !campo.trim().equals("0"))
			{
				campo = campo.replaceAll(",", ""); //-- retirar virgulas adicionais
				if (!resultado.isEmpty())
				{
					resultado += ", ";
				} 
				resultado += campo.trim(); 					
			}
		}
		return resultado;
	}
	
	public String formatarData(String original){
		String formatacao = "";
		if (Verificador.isValorado(original))
		{
			try
			{
				if (original.length() > 10)
				{
					DateFormat dfOrigem = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados();
					DateFormat dfDestino = FormatarData.getDiaMesAnoComBarrasEHoraMinutoSegundoComDoisPontos();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
				else
				{
					DateFormat dfOrigem = FormatarData.getAnoMesDiaContatenado();
					DateFormat dfDestino = FormatarData.getDiaMesAnoComBarras();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
			}
			catch (ParseException e)
			{
				//@TODO nao tratar - temporario
				e.printStackTrace();
			}
				
		}
		return formatacao;
	}
	

	public String formatarComplementar(Ocorrencia complementar)
	{
		return formatarOcorrencia(complementar) + " - " + formatarEndereco(complementar);
	}

	public String formatarExcecaoParaMessage(Throwable e)
	{
		String retorno = ExcecaoParaMessageHtml.converter(e);
		
		return retorno;
	}
	
}
