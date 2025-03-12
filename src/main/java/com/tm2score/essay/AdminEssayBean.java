/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.essay;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 *
 * @author Mike
 */
@Named
@SessionScoped
public class AdminEssayBean implements Serializable {

    private String str1;
    private String str2;

    private String localeStr = "en_US";

    public static AdminEssayBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (AdminEssayBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "adminEssayBean" );
    }


    public String getStr1() {
        return str1;
    }

    public void setStr1(String str1) {
        this.str1 = str1;
    }

    public String getStr2() {
        return str2;
    }

    public void setStr2(String str2) {
        this.str2 = str2;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }




}
