/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.user;

import com.tm2score.entity.user.Resume;
import com.tm2score.util.MessageFactory;
import java.util.Locale;

/**
 *
 * @author miker
 */
public class ResumeUtils 
{
        public static String getPlainTextResumeForAi( Resume jd )
    {
        StringBuilder sb = new StringBuilder();
        Locale locale = jd.getLocale();
        
        sb.append( MessageFactory.getStringMessage(locale, "g.Resume" ) + "\n");

        
        if( jd==null )
            return sb.toString();
        
        if( !jd.getHasAnyFormData() )
        {
            if( jd.getUploadedText()!=null && !jd.getUploadedText().isBlank() )
            {
                sb.append( jd.getUploadedText() );                
                return  sb.toString();
            }
        }
        
        if( jd.getSummary()!=null && !jd.getSummary().isBlank() )
            sb.append( MessageFactory.getStringMessage(locale, "g.Summary" ) + ": " + jd.getSummary() + "\n\n");
                
        return sb.toString();
    }

}
