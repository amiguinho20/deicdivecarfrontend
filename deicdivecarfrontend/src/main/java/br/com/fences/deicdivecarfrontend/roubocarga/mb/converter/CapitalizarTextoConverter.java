package br.com.fences.deicdivecarfrontend.roubocarga.mb.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.fences.fencesutils.conversor.Capitalizar;
import br.com.fences.fencesutils.verificador.Verificador;

@FacesConverter("capitalizarTextoConverter")
public class CapitalizarTextoConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		
		String capitalizada = "";
		if (Verificador.isValorado(value))
		{
			capitalizada = Capitalizar.converter(value);
		}
		return capitalizada;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String capitalizada = null;
		if (value != null)
		{
			if (value instanceof String)
			{
				if (Verificador.isValorado((String) value))
				{
					capitalizada = Capitalizar.converter((String) value);
				}
			}
		}
		return capitalizada;
	}

	
}
