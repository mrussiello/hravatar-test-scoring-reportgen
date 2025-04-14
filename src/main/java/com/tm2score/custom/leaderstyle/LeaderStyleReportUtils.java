/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.leaderstyle;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.service.LogService;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author miker_000
 */
public class LeaderStyleReportUtils {

    public static final String[] LEADER_STYLE_NAMES = new String[]{"Authoritarian","Democratic","Laissez-Faire", "Transactional","Transformational"};
    public static final String[] LEADER_STYLE_STUBS = new String[]{"auth","demo",  "lais", "transact", "transform"};

    
    private final String bundleName;
    private Properties customProperties;


    public LeaderStyleReportUtils( String bundleName)
    {
        this.bundleName=bundleName;
    }




    public static String getCompetencyStub( int topTraitIndex )
    {
        return getCompetencyStubLetter( topTraitIndex );
    }


    public static String getCompetencyStubLetter( int index )
    {
        return LEADER_STYLE_STUBS[index];
    }

    public static int getTopTraitIndex( float[] leaderStyleScoreVals )
    {
        int out = -1;
        float highVal=-1;
        int idx=0;

        if( leaderStyleScoreVals==null || leaderStyleScoreVals.length<5 )
            return out;

        for( int i=0; i<5; i++ )
        {
            if(leaderStyleScoreVals[i]<=0)
                continue;
            
            if( leaderStyleScoreVals[i]>highVal )
            {
                highVal=leaderStyleScoreVals[i];
                idx=i;
            }
            
            else if( leaderStyleScoreVals[i]==highVal )
            {
                int indexToUse = getTieBreakerIndex( i, idx );
                LogService.logIt( "LeaderStyleReportUtils.getTopTraitIndexes() TOP value Tie Breaker: i=" + i + ", existing idx=" + idx +", tieBreaker index=" + indexToUse );
                idx = indexToUse;
            }
            
        }
        out=idx;

        return out;
    }
    
    public static int getTieBreakerIndex( int idx1, int idx2 )
    {
        // same - should not happen
        if( idx1==idx2 )
            return idx1;
        
        // Dominance beats all
        if( idx1==0 || idx2==0 )
            return 0;

        // influence beats all except dominance.
        if( idx1==1 || idx2==1 )
            return 1;
        
        // at this point must have 1 steadiness and 1 compliance.
        
        // compliance always beats steadiness
        return 3;
        
    }

    public static float[] getLeaderStyleScoreVals( TestEvent te )
    {
        // dauth, dem, lais, trasact, transform
        float[] out = new float[5];

        // TESTING ONLY
        if( 1==2 )
        {
            // out = new float[]{50,15,40,25};
            out = new float[]{35,50,50,5,8};
            return out;
        }

        if( te==null )
        {
            LogService.logIt( "LeaderStyleReportUtils.getLeaderStyleScoreVals() testEvent is NULL!" );
            return out;
        }

        if( te.getTestEventScoreList()==null )
        {
            try
            {
                te.setTestEventScoreList(EventFacade.getInstance().getTestEventScoresForTestEvent( te.getTestEventId(), true));
            }
            catch( Exception e )
            {
                LogService.logIt( e, "LeaderStyleReportUtils.getDiscScoreVals() testEventId=" + te.getTestEventId() );
            }
        }
        String nameEnglish;
        for( int i=0; i<5; i++ )
        {
            nameEnglish = LEADER_STYLE_NAMES[i].toLowerCase();

            for( TestEventScore tes : te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()) )
            {
                if( (tes.getName()!=null && tes.getName().toLowerCase().equals(nameEnglish )) ||
                    (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().equals(nameEnglish )) )
                {
                    out[i] = tes.getScore();
                    break;
                }
            }
        }

        return out;
    }

    public String getKey( String key )
    {
        if( customProperties==null )
            getProperties();

        try
        {
            if( customProperties==null )
            {
                LogService.logIt( "LeaderStyleReportUtils.getKey() customProperties is null. Cannot load. Returning null. key=" + key );
                return null;
            }

            String s = customProperties.getProperty( key, "KEY NOT FOUND" );

            if( s.startsWith( "KEY NOT FOUND") )
                s += " (" + key + ")";

            return s;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "LeaderStyleReportUtils.getKey() " + key );
            return null;
        }
    }

    public synchronized Properties getProperties()
    {
        if( customProperties== null )
            loadProperties();
        return customProperties;
    }

    private synchronized void loadProperties()
    {
        try
        {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream( bundleName );

            if( in!=null )
            {
                prop.load(in);
                in.close();
            }
            else
                LogService.logIt( "LeaderStyleReportUtils.loadProperties() BBB.1 Unable to load properties for Bundle=" + bundleName );

            customProperties = prop;
            // LogService.logIt( "LeaderStyleReportUtils.loadProperties() " + bundleName + ", Properties files has " + customProperties.size() + " keys. " );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "LeaderStyleReportUtils.loadProperties() " );
        }
    }

}
