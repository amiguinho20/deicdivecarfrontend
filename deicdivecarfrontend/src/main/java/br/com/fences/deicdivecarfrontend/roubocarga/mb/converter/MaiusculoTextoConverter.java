package br.com.fences.deicdivecarfrontend.roubocarga.mb.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.fences.fencesutils.verificador.Verificador;

@FacesConverter("maiusculoTextoConverter")
public class MaiusculoTextoConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		
		String maiusculo = "";
		if (Verificador.isValorado(value))
		{
			maiusculo = value.toUpperCase();
		}
		return maiusculo;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String maiusculo = null;
		if (value != null)
		{
			if (value instanceof String)
			{
				if (Verificador.isValorado((String) value))
				{
					maiusculo = ((String) value).toUpperCase();
				}
			}
		}
		return maiusculo;
	}

	
}
