/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.sports;

import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class SpData {
    
    
    public static String REPORT_BUNDLE = "com.tm2score.custom.sports.SPMessages";
    
    static String[] COMPETENCIES = new String[] { "Competitiveness","Composure","Concentration","Confidence","Curiosity","Flexibility","GoalOriented","Integrity","Leadership","PositiveImagery","Resiliency","SelfMotivation", "TeamPlayer" }; 
            
        
    
    static synchronized void init()
    {
    }
    
    
    public String getNameKey( String nameStub )
    {
        return "sp.comp." + nameStub;
    }

    public String getName( Locale loc, String nameStub )
    {
        return bmsg( loc, getNameKey( nameStub ) );
    }
    
    public String getDescriptionKey( String nameStub )
    {
        return getNameKey( nameStub ) + ".descrip";
    }

    public String getDescription( Locale loc, String nameStub )
    {
        return bmsg( loc, getDescriptionKey( nameStub ) );
    }

    public String getImportantKey( String nameStub )
    {
        return getNameKey( nameStub ) + ".impt";
    }

    
    public String getImportant( Locale loc, String nameStub )
    {
        return bmsg( loc, getImportantKey( nameStub ) );
    }

    public String getScoreCategoryKey( int scrCode, boolean extra)
    {
        String k = extra ? "g.ScoreCatTextExtra." : "g.ScoreCatText.";
        if( scrCode==1 )
            k += "low";
        if( scrCode==2 )
            k += "med";
        if( scrCode==3 )
            k += "high";
        return k;
    }

    public String getScoreCategoryText( Locale loc, int scrCode, boolean extra)
    {
        return bmsg(loc, getScoreCategoryKey(scrCode, extra ));
    }
    
    
    public List<String> getQuestionList( Locale loc, String nameStub, int scrCode )
    {
        String stub = getNameKey( nameStub );
        if( scrCode==1 )
            stub += ".qlow.";
        if( scrCode==2 )
            stub += ".qmed.";
        if( scrCode==3 )
            stub += ".qhigh.";
        
        List<String> out = new ArrayList<>();
        
        String val;
        for( int i=1;i<10;i++ )
        {
            val = bmsg( loc, stub+i );
            if( val==null || val.isEmpty() )
                break;
            
            out.add( val );
                
        }
        
        return out;
    }
    

    public String getCompetencyScoreTextKey(String nameStub, int scrCode)
    {
        String o = getNameKey( nameStub ) + ".";
        
        if( scrCode==1 )
            o += "low";
        if( scrCode==2 )
            o += "med";
        if( scrCode==3 )
            o += "high";
        
        return o;
    }
    
    public String getCompetencyScoreText( Locale loc, String nameStub, int scrCode)
    {
        return bmsg( loc, getCompetencyScoreTextKey( nameStub, scrCode ));
    }
    
    public int getScoreCode( float score )
    {
        int c = 1;
        if( score>= 3.32f )
            c=2;
        if( score>=6.66f)
            c=3;
        return c;
    }
    
    
    public String getBundleName()
    {
        return REPORT_BUNDLE;
    }
    
    public String[] getCompetencies()
    {
        init();
        return COMPETENCIES;
    }

    
    public String bmsg( Locale loc, String key )
    {
        if( loc==null )
            loc = Locale.US;
        
        return MessageFactory.getStringMessage( getBundleName(), loc , key, null );
    }


    public String bmsg( Locale loc, String key, String[] prms )
    {        
        if( loc==null )
            loc = Locale.US;
        
        return MessageFactory.getStringMessage( getBundleName(), loc , key, prms );
    }
    
    
    
}
