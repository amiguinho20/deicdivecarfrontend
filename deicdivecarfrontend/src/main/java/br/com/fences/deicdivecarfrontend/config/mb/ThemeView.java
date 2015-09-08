package br.com.fences.deicdivecarfrontend.config.mb;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class ThemeView implements Serializable {
    
    private String color; 
    
    @PostConstruct
    private void init(){
    	change("blue");
    }

    public String getColor() {
        return color;
    }
    
    public String getCor(){
    	if (color == null)
    	{
    		return "green";
    	}
    	else
    	{
    		return color.replace("-", "");
    	}
    }

    public void setColor(String color) {
        this.color = color;
    }
    
    public void change(String color) {
        if(color.equals("green"))
            this.color = null;
        else
            this.color = "-" + color;
    }
    
    
}
