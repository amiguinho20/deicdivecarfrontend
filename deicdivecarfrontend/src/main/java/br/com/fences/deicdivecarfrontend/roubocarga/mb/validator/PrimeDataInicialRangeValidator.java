package br.com.fences.deicdivecarfrontend.roubocarga.mb.validator;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.omnifaces.util.Messages;
import org.primefaces.component.calendar.Calendar;

/**
 * filtrocustom
 * 
 */
@FacesValidator("primeDataInicialRangeValidator")
public class PrimeDataInicialRangeValidator implements Validator {
     
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        Date dataFinal = (Date) value; //-- idIntervaloData

        Object dataInicialObjeto = component.getAttributes().get("dataInicial");
        Calendar calendarPrimeFaces = (Calendar) dataInicialObjeto;
        Date dataInicial = (Date) calendarPrimeFaces.getValue();
        
        if (dataInicial == null && dataFinal == null)
        {
        	throw new ValidatorException(Messages.createError("As datas não podem estar vazias. Pelo menos uma delas deve ter valor."));
        }
        
        if (dataFinal.before(dataInicial)) {
        	dataFinal = null;
        	throw new ValidatorException(Messages.createError("A data inicial não pode ser maior que a data final."));
        }
        
//    	if (dataFinal == null) {
//            return;
//        }
//
//        if (dataInicial == null) {
//            return;
//        }
//         
//        
//        if (dataFinal.before(dataInicial)) {
//        	dataFinal = null;
//        	//String msg = "A data inicial não pode ser maior que a data final";
////        	FacesMessage msg = 
////    				new FacesMessage("E-mail validation failed.", 
////    						"Invalid E-mail format.");
////    			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
//        	//Messages.addWarn("form:valorDoFiltro", msg);
//        	//throw new ValidatorException(Messages.create(msg).get());
////        	throw new ValidatorException(
////                    FacesMessageUtil.newBundledFacesMessage(FacesMessage.SEVERITY_ERROR, "", "msg.dateRange", ((Calendar)component).getLabel(), startDate));
//
////        	FacesMessage facesMessage = new FacesMessage("A data inicial não pode ser maior que a data final");
////        	facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
//    		throw new ValidatorException(Messages.createWarn("A data inicial não pode ser maior que a data final."));
//        }
    }
}