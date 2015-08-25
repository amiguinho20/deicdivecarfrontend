package br.com.fences.deicdivecarfrontend.roubocarga.mb.converter;

import java.text.DateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.fences.fencesutils.formatar.FormatarData;


@FacesConverter("dataConverter")
public class DataConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		String formatacao = "";
		if (value != null)
		{
			try
			{
				String original = value.toString();
				if (original.length() > 10)
				{
					DateFormat dfOrigem = FormatarData.getDiaMesAnoComBarrasEHoraMinutoSegundoComDoisPontos();
					DateFormat dfDestino = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
				else
				{
					DateFormat dfOrigem = FormatarData.getDiaMesAnoComBarras();
					DateFormat dfDestino = FormatarData.getAnoMesDiaContatenado();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
			}
			catch (Exception e)
			{
				//-- nao trata, apenas retorna vazio.
			}
		}
		return formatacao;

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String formatacao = "";
		if (value != null)
		{
			try
			{
				String original = value.toString();
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
			catch (Exception e)
			{
				//-- nao trata, apenas retorna vazio.
			}
		}
		return formatacao;
	}

}
