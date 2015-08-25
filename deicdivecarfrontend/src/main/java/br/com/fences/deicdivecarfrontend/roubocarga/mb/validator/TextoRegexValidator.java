package br.com.fences.deicdivecarfrontend.roubocarga.mb.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.omnifaces.util.Messages;

import br.com.fences.fencesutils.verificador.Verificador;


/**
 * Permite apenas o Latin basico e suplementar, exceto barra inversa e aspas. <br/>
 * Nao permite caracteres de controle (ctrl), tab, barra inversa e aspas.<br/>
 * Fonte: https://en.wikipedia.org/wiki/List_of_Unicode_characters <br/>
 */
@FacesValidator("textoRegexValidator")
public class TextoRegexValidator implements Validator {
     
	final static String REGEX = "\\u0020-\\u007e\\u00a0-\\u00ff|[^\\u005c|\\u0022]";
			
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException 
    {
    	String valor = value.toString();
        if (Verificador.isValorado(valor)) 
        {
        	String caracteresInvalidos = valor.replaceAll(REGEX, "");
        	if (!caracteresInvalidos.isEmpty()) 
        	{
        		String msg = "O caracter especial " + caracteresInvalidos + " não é permitido.";
        		if (caracteresInvalidos.length() > 1)
        		{
        			msg = "Os caracteres especiais " + caracteresInvalidos + " não são permitidos.";
        		}
        		throw new ValidatorException(Messages.createError(msg));
        	}
        }
    }
}