package br.com.fences.deicdivecarfrontend.roubocarga.mb.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.fences.fencesutils.verificador.Verificador;

@FacesConverter("minusculoTextoConverter")
public class MinusculoTextoConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		
		String minusculo = "";
		if (Verificador.isValorado(value))
		{
			minusculo = value.toLowerCase();
		}
		return minusculo;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String minusculo = null;
		if (value != null)
		{
			if (value instanceof String)
			{
				if (Verificador.isValorado((String) value))
				{
					minusculo = ((String) value).toLowerCase();
				}
			}
		}
		return minusculo;
	}

	
}
