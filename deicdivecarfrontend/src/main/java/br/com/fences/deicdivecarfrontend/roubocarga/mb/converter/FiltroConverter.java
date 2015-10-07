package br.com.fences.deicdivecarfrontend.roubocarga.mb.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import br.com.fences.deicdivecarfrontend.filtrocustom.FiltroBO;
import br.com.fences.deicdivecarfrontend.filtrocustom.FiltroModelo;


/**
 * filtrocustom
 * 
 */
@FacesConverter(forClass=FiltroModelo.class)
public class FiltroConverter  implements Converter{

	@Inject
	private FiltroBO filtroBO;
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		
		FiltroModelo filtro = filtroBO.selecionar(value);
		return filtro;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String rotulo = null;
		if (value != null)
		{
			if (value instanceof FiltroModelo)
			{
				FiltroModelo filtro = (FiltroModelo) value;
				rotulo = filtro.getRotulo();
			}
		}
		return rotulo;
	}

	
}
