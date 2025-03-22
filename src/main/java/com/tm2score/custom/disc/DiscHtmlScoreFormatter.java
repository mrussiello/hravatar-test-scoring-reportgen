/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.custom.disc;

import com.tm2score.custom.coretest2.CT2HtmlScoreFormatter;
import com.tm2score.format.ScoreFormatter;

/**
 *
 * @author miker
 */
public class DiscHtmlScoreFormatter extends CT2HtmlScoreFormatter implements ScoreFormatter {

    @Override
    public String getEmailContent(boolean tog, boolean includeTop, String topNote) throws Exception 
    {
        // the only difference is to remove the overall section.
        this.includeOverall=false;
        
        return super.getEmailContent(tog, includeTop, topNote);
    }

    
}
