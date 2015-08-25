package br.com.fences.deicdivecarfrontend.roubocarga.mb.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.omnifaces.util.Messages;

import br.com.fences.fencesutils.verificador.Verificador;


/**
 * Permite apenas oito digitos de 0 a 9 <br/>
 * Fonte: http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html <br/>
 */
@FacesValidator("cepRegexValidator")
public class CepRegexValidator implements Validator {
     
	final static String REGEX = "\\d{8}";
			
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException 
    {
    	String valor = value.toString();
        if (Verificador.isValorado(valor)) 
        {
        	if (!valor.matches(REGEX))
        	{
        		String msg = "Apenas oito dígitos de 0 a 9 são permitidos.";
        		throw new ValidatorException(Messages.createError(msg));
        	}
        }
    }
}