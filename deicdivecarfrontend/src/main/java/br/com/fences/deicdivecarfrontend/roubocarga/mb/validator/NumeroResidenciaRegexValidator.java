package br.com.fences.deicdivecarfrontend.roubocarga.mb.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.omnifaces.util.Messages;

import br.com.fences.fencesutils.verificador.Verificador;


/**
 * Permite apenas de 1 a 5 digitos, e o numero deve estar entre 1 e 50000 <br/>
 */
@FacesValidator("numeroResidencialRegexValidator")
public class NumeroResidenciaRegexValidator implements Validator {
     
	final static String REGEX = "\\d{1,5}";
			
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException 
    {
    	String valor = value.toString();
        if (Verificador.isValorado(valor)) 
        {
        	if (valor.matches(REGEX))
        	{
        		int numero = Integer.parseInt(valor);
        		if (numero < 0 || numero > 50000)
        		{
        			String msg = "Apenas números entre 0 e 50000 são permitidos.";
            		throw new ValidatorException(Messages.createError(msg));
        		}
        	}
        	else
        	{
        		String msg = "Apenas de 1 a 5 dígitos permitidos.";
        		throw new ValidatorException(Messages.createError(msg));
        	}
        }
    }
}