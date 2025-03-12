/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.essay;

import com.tm2score.util.TextProcessingUtils;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.essay.copyscape.CopyScapeUtils;
import com.tm2score.event.EventFacade;
import com.tm2score.faces.FacesUtils;
import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author Mike
 */
@Named
@RequestScoped
public class AdminEssayUtils extends FacesUtils {

    @Inject
    AdminEssayBean adminEssayBean;


    public String processSubmitTextToPlagCheck()
    {
        try
        {
            String text = adminEssayBean.getStr1();

            if( text == null || text.isEmpty() )
                throw new Exception( "Text is empty." );

            CopyScapeUtils ptu = new CopyScapeUtils();

            String result = null;
            
            Object[] out = ptu.submitEssayForWebDuplicateContentCheck(text, 0);

            if( out != null && out.length>2 )
                result = (String) out[2];

            if( result == null )
                result = "No response provided";

            this.setStringInfoMessage( "Method returned: " + result  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AdminEssayUtils.processSubmitTextToPlagCheck() " );

            setMessage( e );
        }

        return null;

    }


    public String processUpdateStatsOnUnscoredEssays()
    {
        try
        {
            DiscernFacade discernFacade = DiscernFacade.getInstance();

            List<UnscoredEssay> essays = discernFacade.getUnscoredEssaysForStatsUpdate();

            int[] vals;
            TestEvent te;
            EventFacade eventFacade=null;
            
            for( UnscoredEssay ue : essays )
            {
                if( ue.getEssay()==null || ue.getEssay().isEmpty() )
                {
                    ue.setTotalWords( 0 );
                }

                else
                {
                    if( eventFacade==null )
                        eventFacade = EventFacade.getInstance();
                    
                    te = eventFacade.getTestEvent( ue.getTestEventId(), false );
                    
                    vals = EssayScoringUtils.getWritingErrorCount(ue.getEssay(), Locale.US, te==null ? null : te.getIpCountry(), null );

                    ue.setTotalWords( vals[4] );
                    ue.setSpellingErrors( vals[1] );
                    ue.setGrammarErrors( vals[2] );
                    ue.setStyleErrors( vals[3]);
                    ue.setHasSpellingGrammarStyle( vals[7] );
                }

                discernFacade.saveUnscoredEssay(ue, false );
            }

            this.setStringInfoMessage( "Updated " + essays.size() + " records" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AdminEssayUtilsupdateStatsOnUnscoredEssays() " );

            setMessage( e );
        }

        return null;
    }


    public String processSpellCheckEssays()
    {
        try
        {
            Object[] d1;
            Object[] d2;
            Map<String,Integer> sperrs1,sperrs2;
            Locale locale = I18nUtils.getLocaleFromCompositeStr( adminEssayBean.getLocaleStr() );

            d1 = EssayScoringUtils.getWritingAnalysis(adminEssayBean.getStr1(), locale, null, null );            
            int[] count1 = (int[]) d1[0];
            sperrs1 = (Map<String,Integer>) d1[1];
            boolean valid1 = EssayScoringUtils.isValidWriting(adminEssayBean.getStr1(), locale, null, null );

            d2 = EssayScoringUtils.getWritingAnalysis(adminEssayBean.getStr2(), locale, null, null );
            int[] count2 = (int[]) d2[0];
            sperrs2 = (Map<String,Integer>) d2[1];
            
            boolean valid2 = EssayScoringUtils.isValidWriting(adminEssayBean.getStr2(), locale, null, null );

            
            
            this.setStringInfoMessage( "String 1 total errors=" + count1[0] + ", spelling=" + count1[1] + ", grammar=" + count1[2] + ", style=" + count1[3] + ", valid=" + valid1 + ", Misspelled words=" + strCountMapToString(sperrs1) );
            this.setStringInfoMessage( "String 2 total errors=" + count2[0] + ", spelling=" + count2[1] + ", grammar=" + count2[2] + ", style=" + count2[3] + ", valid=" + valid2 + ", Misspelled words=" + strCountMapToString(sperrs2) );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AdminEssayUtils.processSpellCheckEssays() " );
            setMessage( e );
        }

        return null;
    }
    
    
    public static String strCountMapToString( Map<String,Integer> l )
    {
        StringBuilder sb = new StringBuilder();
        
        Integer count;
        
        for( String s : l.keySet() )
        {
            count = l.get( s );
            
            if( sb.length()>0 )
                sb.append( ", ");
            
            sb.append( s + (count!=null ? " (" + count.toString() + ")" : "") );
        }
        
        return sb.toString();
    }

    public String processCompareEssays()
    {
        try
        {
            double[] vals = TextProcessingUtils.getTextSimilarityVals(adminEssayBean.getStr1(), adminEssayBean.getStr2() );

            this.setStringInfoMessage( "Similarity Valuies Are: Jaro=" + vals[0] + ", J-W=" + vals[1] + ", levensh=" + vals[2]  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AdminEssayUtils.processCompareEssays() " );
            setMessage( e );
        }

        return null;
    }

}
