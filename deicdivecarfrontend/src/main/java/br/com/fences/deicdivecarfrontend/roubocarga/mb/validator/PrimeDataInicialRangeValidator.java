package br.com.fences.deicdivecarfrontend.roubocarga.mb.validator;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("primeDataInicialRangeValidator")
public class PrimeDataInicialRangeValidator implements Validator {
     
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
         
        //Leave the null handling of startDate to required="true"
        Object startDateValue = component.getAttributes().get("dataInicial");
        if (startDateValue==null) {
            return;
        }
         
        Date startDate = (Date)startDateValue;
        Date endDate = (Date)value; 
        if (endDate.before(startDate)) {
            //throw new ValidatorException(
                    //FacesMessageUtil.newBundledFacesMessage(FacesMessage.SEVERITY_ERROR, "", "msg.dateRange", ((Calendar)component).getLabel(), startDate));
        }
    }
}