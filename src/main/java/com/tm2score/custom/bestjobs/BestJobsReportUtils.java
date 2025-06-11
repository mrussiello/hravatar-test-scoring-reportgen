/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.bestjobs;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.onet.Soc;
import com.tm2score.entity.profile.Profile;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.onet.OnetElementType;
import com.tm2score.onet.OnetFacade;
import com.tm2score.onet.OnetInterestElementType;
import com.tm2score.onet.OnetJobZoneType;
import com.tm2score.onet.OnetTrainingExperienceElementType;
import com.tm2score.onet.OnetTrainingExperienceElementTypeCareerScoutV2;
import com.tm2score.profile.ProfileFacade;
import com.tm2score.profile.ProfileFloat1Comparator;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class BestJobsReportUtils {
    
    public static int MIN_COMPETENCIES_TO_MATCH = 3;
    public static int MAX_SOC5_FREQ = 1;
    public static int MAX_SOC4_FREQ = 2;
    public static int MAX_SOC3_FREQ = 3;
    
    public static float RIASEC_WEIGHT = 1f;
    public static float SCORE_WEIGHT = .5f;
    public static float EDUCEXP_WEIGHT = .33f;
    public static float JOBZONE_WEIGHT = .33f;
    
    // These values are set by analyzing existing data to set to the the mean of 1st ranked item minus 1 avg standard deviations.
    public static float MIN_JOBSPEC_SCORE = 41.07f;
    public static float MIN_RIASEC_MATCH = 52f; // 53.93f;
    public static float MIN_COMBINED_MATCH = 59.41f;
    
    // This one seemed too restrictive so lowered to a gross error limit for now.
    public static float MIN_EDUCEXP_MATCH = 50; // 65; // 79.69f; // 82.34f;
    
    public static String[] RIASEC_COMPETENCIES = new String[] { "Realistic","Investigative","Artistic", "Social","Enterprising", "Conventional", "Education","Experience","Training"}; 

    private ProfileFacade profileFacade;
    
    private OnetFacade onetFacade;
    
    

    public static List<Long> avReportIds = null;
    public static List<Long> ct2ReportIds = null;
    public static List<Long> uminnReportIds = null;
    public static List<Long> sportsReportIds = null;    
    public static List<Integer> careerScoutProductIds = null;
    
    public static List<Long> jobMatchReportIds = null;    
    
    public List<Profile> currentProfileList;
    String currentLocaleStr;
    ProfileUsageType currentProfileUsageType;
    OnetJobZoneType currentMinOnetJobZoneType; 
    OnetJobZoneType currentMaxOnetJobZoneType;
    boolean careerScoutV2 = false;
    
    /*
     Map of SOC Top 5  and frequency (number of occurrences in currentProfileList
        SOC Top 5 is the first 5 digits plus dash in a soc. So the first 6 characters.
        11-1031.00    Soc Top 5 = 11-103
    */
    private Map<String,Integer> soc5FreqMap;

    /*
     Map of SOC Top 4  and frequency (number of occurrences in currentProfileList
        SOC Top 4 is the first 4 digits plus dash in a soc. So the first 5 characters.
        11-1031.00    Soc Top 4 = 11-10
    */
    private Map<String,Integer> soc4FreqMap;

    /*
     Map of SOC Top 3  and frequency (number of occurrences in currentProfileList
        SOC Top 3 is the first 3 digits plus dash in a soc. So the first 4 characters.
        11-1031.00    Soc Top 3 = 11-1
    */
    private Map<String,Integer> soc3FreqMap;
    
    
    public BestJobsReportUtils()
    {}
    
    
    public BestJobsReportUtils( boolean careerScoutV2 )
    {
        this.careerScoutV2=careerScoutV2;
    }
    
    
    /**
     * socDataCode - indicates what data to include in the p.Soc 
     *               0 = none
     *               1 = all 
     *               10 = Report 2 Full = tasks, workstyles, skills, knowledges, abilities, bls, altitles, related
     *               11 = Report 2 Lite = bls, altitles, related
     * 
     * 
     * @param maxMatches
     * @param riasecTe
     * @param jobSpecTe
     * @param baseJobZoneToUse
     * @param maxJobZoneGap
     * @param includeEducTrngExp
     * @param socDataCode
     * @return
     * @throws Exception 
     */
    public List<Profile> getTopCT3ProfileMatches( int maxMatches, TestEvent riasecTe, TestEvent jobSpecTe, OnetJobZoneType baseJobZoneToUse, int maxJobZoneGap, int includeEducTrngExpCode, int socDataCode, boolean useMatchLimits, List<EeoMatch> eeoMatchList) throws Exception
    {
        // LogService.logIt("BestJobReportUtils.getTopCT3ProfileMatches() START baseJobZoneToUse=" + (baseJobZoneToUse==null ? "null" : baseJobZoneToUse.getJobZoneId()) );
        
        List<Profile> pl;        
        List<Profile> lastPl=new ArrayList<>();        
        int lastCount = 0;
                
        if( maxJobZoneGap<0 )
            maxJobZoneGap = 1;
        
        boolean abilities = false;
        boolean knowledge = false;
        boolean skills = false;
        boolean workAct = false;
        boolean workSty = false;
        boolean workCont = false;
        boolean tasks = false;
        boolean altTitles = false;
        boolean related = false;
        boolean bls = false;
        
        switch (socDataCode) {
            case 1:
                abilities = true;
                knowledge = true;
                skills = true;
                workAct = true;
                workSty = true;
                workCont = true;
                tasks = true;
                altTitles = true;
                related = true;
                bls = true;
                break;
            case 10:
                abilities = true;
                knowledge = true;
                skills = true;
                workSty = true;
                tasks = true;
                altTitles = true;
                related = true;
                bls = true;
                break;
            case 11:
                altTitles = true;
                related = true;
                bls = true;
                break;
            default:
                break;
        }
        
        soc5FreqMap=null;
        soc4FreqMap=null;
        soc3FreqMap=null;
        
        // maxSearchGap is 25, so 4 rounds of limitReduc=0, 5, 10, 15, 20, 25
        for( int limitReduc=0; limitReduc<=RuntimeConstants.getIntValue( "BestJobsReport_MaxSearchGap" ); limitReduc+=5 )   // maxSearchGap=15  0,5,10,15
        { 
            // LogService.logIt("BestJobReportUtils.getTopCT3ProfileMatches() AAA.11 maxMatches=" + maxMatches + ", limitReduc=" + limitReduc + ", lastCount=" + lastCount );
            
            if( lastCount>=10 )
                break;
            // Try with lower limits.

            // if MaxZoneGap is 1, will do 0 and 1
            for( int zoneGapLimit=0; zoneGapLimit<=maxJobZoneGap; zoneGapLimit++ )   // 0 (one cycle)
            {
                pl = getHighProfileMatches(lastPl, maxMatches, riasecTe, jobSpecTe, zoneGapLimit, useMatchLimits ? limitReduc : 100, baseJobZoneToUse, includeEducTrngExpCode, eeoMatchList );

                // LogService.logIt( "BestJobReportUtils.getTopCT3ProfileMatches() AAA.12 zoneGapLimit=" + zoneGapLimit + ", pl.size=" + pl.size() + ", limitReduc=" + limitReduc + ", lastCount=" + lastCount );
                
                for( Profile p : pl )
                {
                    if( lastPl.contains(p) )
                        continue;
                    
                    if( hasSameSoc( lastPl, p.getStrParam4() ) )
                    {
                        swapHighestMatch( lastPl, p );
                        continue;
                    }

                    lastPl.add(p);

                    // Found enough.
                    if( lastPl.size()>=maxMatches )
                    {
                        populateSocDataIfNeeded( lastPl, abilities,  knowledge, skills,  workAct,  workSty,  workCont, tasks,  altTitles,  related,  bls );
                        return lastPl;
                    }
                }

                // Had some but no improvement in last iteration.
                //if( lastCount>0 && lastCount>=lastPl.size() )
                //    return lastPl;

                
                lastCount = lastPl.size();
            }  
            
            if( !useMatchLimits )
                break;
            
            //if( lastCount >= 3 )
             //   break;            
        }
        
        // LogService.logIt("BestJobReportUtils.getTopCT3ProfileMatches() BBB.1 lastPl=" + lastPl.size() );

        populateSocDataIfNeeded( lastPl, abilities,  knowledge, skills,  workAct,  workSty,  workCont, tasks,  altTitles,  related,  bls );
          
        return lastPl;
    }

    private void addSoc5( String soc )
    {
        String soc5 = getSocTop5( soc );
        
        if( soc5==null )
            return;
        
        if( soc5FreqMap==null )
            soc5FreqMap = new HashMap<>();
        
        Integer i = soc5FreqMap.get(soc5 );
        
        if( i==null )
            i=0;
        
        soc5FreqMap.put(soc5, i+1 );
    }
    
    private void removeSoc5( String soc )
    {
        String soc5 = getSocTop5( soc );
        
        if( soc5==null )
            return;
        
        if( soc5FreqMap==null )
            return;
        
        Integer i = soc5FreqMap.get(soc5 );
        
        if( i==null || i==0 )
            return;
        
        soc5FreqMap.put(soc5, i-1 );        
    }
    
    private int getSoc5Freq( String soc )
    {
        String soc5 = getSocTop5( soc );
        
        if( soc5==null )
            return -1;
        
        if( soc5FreqMap==null )
            soc5FreqMap = new HashMap<>();
        
        Integer i = soc5FreqMap.get(soc5 );
        
        return i==null ? 0 : i;
    }
    
    public static String getSocTop5( String soc )
    {
        if( soc==null )
            return null;
        if( soc.length()<6 )
            return null;
        return soc.substring(0, 6 );
    }

    private void addSoc4( String soc )
    {
        String soc4 = getSocTop4( soc );
        
        if( soc4==null )
            return;
        
        if( soc4FreqMap==null )
            soc4FreqMap = new HashMap<>();
        
        Integer i = soc4FreqMap.get(soc4 );
        
        if( i==null )
            i=0;
        
        soc4FreqMap.put(soc4, i+1 );
    }

    private void removeSoc4( String soc )
    {
        String soc5 = getSocTop4( soc );
        
        if( soc5==null )
            return;
        
        if( soc4FreqMap==null )
            return;
        
        Integer i = soc4FreqMap.get(soc5 );
        
        if( i==null || i==0 )
            return;
        
        soc4FreqMap.put(soc5, i-1 );        
    }

    
    private int getSoc4Freq( String soc )
    {
        String soc4 = getSocTop4( soc );
        
        if( soc4==null )
            return -1;
        
        if( soc4FreqMap==null )
            soc4FreqMap = new HashMap<>();
        
        Integer i = soc4FreqMap.get(soc4 );
        
        return i==null ? 0 : i;
    }
    
    public static String getSocTop4( String soc )
    {
        if( soc==null )
            return null;
        if( soc.length()<5 )
            return null;
        return soc.substring(0, 5 );
    }

    private void addSoc3( String soc )
    {
        String soc3 = getSocTop3( soc );
        
        if( soc3==null )
            return;
        
        if( soc3FreqMap==null )
            soc3FreqMap = new HashMap<>();
        
        Integer i = soc3FreqMap.get(soc3 );
        
        if( i==null )
            i=0;
        
        soc3FreqMap.put(soc3, i+1 );
    }

    private void removeSoc3( String soc )
    {
        String soc5 = getSocTop3( soc );
        
        if( soc5==null )
            return;
        
        if( soc3FreqMap==null )
            return;
        
        Integer i = soc3FreqMap.get(soc5 );
        
        if( i==null || i==0 )
            return;
        
        soc3FreqMap.put(soc5, i-1 );        
    }

    
    private int getSoc3Freq( String soc )
    {
        String soc3 = getSocTop3( soc );
        
        if( soc3==null )
            return -1;
        
        if( soc3FreqMap==null )
            soc3FreqMap = new HashMap<>();
        
        Integer i = soc3FreqMap.get(soc3 );
        
        return i==null ? 0 : i;
    }
    
    public static String getSocTop3( String soc )
    {
        if( soc==null )
            return null;
        if( soc.length()<4 )
            return null;
        return soc.substring(0, 4 );
    }


    
    private EeoMatch getEeoMatchForEeoCategoryTypeId( int eeoJobCategoryId, List<EeoMatch> eeoMatchList )
    {
        if( eeoMatchList==null )
            return null;
        
        for( EeoMatch em : eeoMatchList )
        {
            if( em.getEeoJobCategoryId()==eeoJobCategoryId )
                return em;
        }
        
        EeoMatch em = new EeoMatch( EeoJobCategoryType.getValue(eeoJobCategoryId).getName(Locale.US), eeoJobCategoryId );
        eeoMatchList.add(em);
        return em;
    }
    
    private void swapHighestMatch( List<Profile> lastPl, Profile pNew ) throws Exception
    {
        // same profile.
        if( lastPl.contains(pNew) || pNew.getStrParam4()==null || pNew.getStrParam4().isBlank() )
            return;
                    
        // matches on profileId  and then soc
        if( !hasSameSoc(lastPl, pNew.getStrParam4() ) )
            throw new Exception( "lastPl does not contain profile " + pNew.getName() + ", profileId=" + pNew.getProfileId() );
        
        Profile p2 = null;
        
        for( Profile pp : lastPl )
        {
            if( pp.getStrParam4()!=null && pp.getStrParam4().equalsIgnoreCase(pNew.getStrParam4() ) )
            {
                p2 = pp;
                break;
            };
        }
        
        if( p2==null )
            return;
        
        // higher combined match already in the list. 
        if( p2.getFloatParam1()>pNew.getFloatParam1() )
            return;
        
        // Szwap
        lastPl.remove(p2);
        lastPl.add(pNew);
    }
    
    protected boolean hasSameSoc( List<Profile> pl, String socCode )
    {
        if( socCode==null || socCode.isEmpty() )
            return true;
        
        for( Profile p : pl )
        {
            if( p.getStrParam4()!=null && p.getStrParam4().equalsIgnoreCase(socCode) )
                return true;
        }
        
        return false;
    }
    
    public static void setRiasecCompetencyScoresForSampleReport( List<TestEventScore> tesl )
    {
        Map<String,Float> scoreMap = getRiasecSampleReportScores();
        
        Float val = null;
        
        for( TestEventScore tes : tesl )
        {
            val = scoreMap.get( tes.getName() );
            
            if( val==null && tes.getNameEnglish()!=null )
                val = scoreMap.get( tes.getNameEnglish() );
            
            if( val==null )
                continue;
            
            tes.setScore(val);
            tes.setRawScore(val);
        }
    }
    
    
    public static Map<String,Float> getRiasecSampleReportScores()
    {
         // "Realistic","Investigative","Artistic", "Social","Enterprising", "Conventional", "Education","Experience","Training" 

        // get a number 1 - 4
        int random = 1 + (int) (Math.floor( Math.random()*4 ) );
        
        float[] vals;
        
        // The detective
        vals = switch (random) {
            case 1 -> new float[] { 60,90,40,30,50,30,8,8,8};
            case 2 -> new float[] { 70,10,10,40,20,90,2,5,4};
            case 4 -> new float[] { 60,60,10,90,40,80,5,6,7};
            default -> new float[] { 90,60,10,10,20,85,8,8,11};
        }; // The clerk
        // The customer service rep
        // the accountant
        
        LogService.logIt( "BestJobsReportUtils.getRiasecSampleReportScores() random=" + random );
        
        Map<String,Float> out = new HashMap<>();
        
        for( int i=0;i<BestJobsReportUtils.RIASEC_COMPETENCIES.length;i++ )
        {
            out.put(BestJobsReportUtils.RIASEC_COMPETENCIES[i], vals[i]);
        }
        return out;        
    }
    

    public static boolean isValidJobMatchReportId( long reportId )
    {
        if( jobMatchReportIds == null )
        {
            String rids = RuntimeConstants.getStringValue( "jobMatchValidReportIds" );

            jobMatchReportIds = new ArrayList<>();

            String[] ridz = rids.split(";");

            for( String r : ridz )
            {
                 try
                 {
                     if( r==null || r.trim().isEmpty() )
                         continue;

                     jobMatchReportIds.add(Long.valueOf(r));
                 }
                 catch( NumberFormatException e )
                 {}                     
            }
        }
            
        for( Long rid : jobMatchReportIds )
        {
            if( rid==reportId )
                return true;
        }
        
        return false;
    }
        
    
    public static boolean isValidCareerScoutProductId( int productId )
    {
        if( careerScoutProductIds == null )
        {
            String rids = RuntimeConstants.getStringValue( "careerScoutProductIds" );

            careerScoutProductIds = new ArrayList<>();

            String[] ridz = rids.split(";");

            for( String r : ridz )
            {
                 try
                 {
                     if( r==null || r.trim().isEmpty() )
                         continue;

                     careerScoutProductIds.add( Integer.valueOf(r.trim() ) );
                 }
                 catch( NumberFormatException e )
                 {}                     
            }
        }
            
        for( Integer rid : careerScoutProductIds )
        {
            if( rid==productId )
                return true;
        }
        
        return false;        
    }
    
    
    
    public static boolean isValidUminnReportId( long reportId )
    {
        if( uminnReportIds == null )
        {
            uminnReportIds = new ArrayList<>();
            
            String rids = RuntimeConstants.getStringValue( "uminnValidReportIds" );

            // ct2ReportIds = new ArrayList<>();

            String[] ridz = rids.split(";");

            for( String r : ridz )
            {
                 try
                 {
                     if( r==null || r.trim().isEmpty() )
                         continue;

                     uminnReportIds.add(Long.valueOf(r));
                 }
                 catch( NumberFormatException e )
                 {}                     
            }
        }
            
        for( Long rid : uminnReportIds )
        {
            if( rid==reportId )
                return true;
        }
        
        return false;        
    }
    
    
    public static boolean isValidSportsReportId( long reportId )
    {
        if( sportsReportIds == null )
        {
            String rids = RuntimeConstants.getStringValue( "sportsValidReportIds" );

            sportsReportIds = new ArrayList<>();

            String[] ridz = rids.split(";");

            for( String r : ridz )
            {
                 try
                 {
                     if( r==null || r.trim().isEmpty() )
                         continue;

                     sportsReportIds.add(Long.valueOf(r) );
                 }
                 catch( NumberFormatException e )
                 {}                     
            }
        }
            
        for( Long rid : sportsReportIds )
        {
            if( rid==reportId )
                return true;
        }
        
        return false;        
    }
    
    public static boolean isValidCT2ReportId( long reportId )
    {
        if( ct2ReportIds == null )
        {
            String rids = RuntimeConstants.getStringValue( "ct2ValidReportIds" );

            ct2ReportIds = new ArrayList<>();

            String[] ridz = rids.split(";");

            for( String r : ridz )
            {
                 try
                 {
                     if( r==null || r.trim().isEmpty() )
                         continue;

                     ct2ReportIds.add(Long.valueOf(r) );
                 }
                 catch( NumberFormatException e )
                 {}                     
            }
        }
            
        for( Long rid : ct2ReportIds )
        {
            if( rid==reportId )
                return true;
        }
        
        return false;
    }


    public static boolean isValidAvReportId( long reportId )
    {
        if( avReportIds == null )
        {
            String rids = RuntimeConstants.getStringValue( "avValidReportIds" );

            avReportIds = new ArrayList<>();

            String[] ridz = rids.split(";");

            for( String r : ridz )
            {
                 try
                 {
                     if( r==null || r.trim().isEmpty() )
                         continue;

                     avReportIds.add(Long.valueOf(r));
                 }
                 catch( NumberFormatException e )
                 {}                     
            }
        }
            
        for( Long rid : avReportIds )
        {
            if( rid==reportId )
                return true;
        }
        
        return false;
    }

    
    public void setSocsInProfiles( List<Profile> pl ) throws Exception
    {
        if( pl==null )
            return;
        
        for( Profile p : pl )
        {
            if( p.getSoc()!=null  || p.getProfileId()==0 )
                continue;
            
            if( onetFacade == null )
                onetFacade = OnetFacade.getInstance();
            
            p.setSoc( onetFacade.getSoc(p.getStrParam4()));
        }
    }

    public static boolean hasRiasecCompetencies(TestEvent te) 
    {
        Map<String, Float> mp = BestJobsReportUtils.getScoreMap(te);
        int ct = 0;
        for (String c : BestJobsReportUtils.RIASEC_COMPETENCIES) 
        {
            if (mp.get(c) != null && mp.get(c) > 0)
            {
                ct++;
            }
        }
        return ct >= 6;
    }
    

    public String modifyCompactDataForSampleReport( String compactInfoStr, List<Soc> socList ) throws Exception
    {
        String riasecData = StringUtils.getBracketedArtifactFromString(compactInfoStr, Constants.RIASEC_COMPACT_INFO_KEY);
        
        if( riasecData==null || riasecData.isBlank() )
            return compactInfoStr;

        // use this without change
        String eeoData = StringUtils.getBracketedArtifactFromString(compactInfoStr, Constants.EEOCAT_COMPACT_INFO_KEY);
                
        // change data for reasec
        List<Profile> pl = getBestProfilesListWithData(riasecData, ",", 0 );
        
        Profile p;
        Soc s;
        
        for( int i=0; i<pl.size() && i<socList.size(); i++ )
        {
           p = pl.get(i);
           s=socList.get(i);
           
           if( p==null || s==null )
               continue;
           
           // LogService.logIt( "BestJobsReportUtils.modifyCompactDataForSampleReport() changing from p.getString")
           p.setName( s.getTitleSingular() );
           p.setStrParam1( s.getTitleSingular());
           p.setStrParam4( s.getSocCode() );
        }
        
        String out =  createCompactInfoString(pl, null );
        
        if( eeoData!=null && !eeoData.isBlank() )
            out += eeoData;
        
        return out;
    }
    
    
    /*
        "rank,title,eeocattypeid,percentmatch|...";        
    */
    public static List<EeoMatch> getEeoCategoryListWithData(String compactInfoStr, String delim ) 
    {
        List<EeoMatch> out = new ArrayList<>();

        try
        {
            if( compactInfoStr==null || compactInfoStr.isEmpty() )
                return out;

            String[] pStrs = compactInfoStr.split("\\|" );

            // ProfileFacade profileFacade = ProfileFacade.getInstance();

            String[] parts;

            int rank;
            String title;
            int typeId;
            float pct=0;
            EeoMatch m;

            for( String pStr : pStrs )
            {
                if( pStr==null || pStr.isEmpty() || !pStr.contains(delim) )
                    continue;
                pStr=pStr.trim();

                parts = pStr.split( delim );

                if( parts.length<4 )
                {
                    LogService.logIt("BestJobsReportUtils.getEeoCategoryListWithData() Skipping segment because not enough pieces. Segment=" + pStr + ", full compactInfoStr=" + compactInfoStr );
                    continue;
                }
                
                // skip header row.
                if( parts[0]!=null && parts[0].equalsIgnoreCase("rank"))
                    continue;

                rank = Integer.parseInt( parts[0]); 
                title = URLDecoder.decode(parts[1],"UTF8"); 
                typeId = Integer.parseInt( parts[2]); 
                pct = Float.parseFloat( parts[3]); 

                if( title==null || title.isEmpty() )
                {
                    LogService.logIt("BestJobsReportUtils.getEeoCategoryListWithData() Skipping segment because No name. Segment=" + pStr + ", full compactInfoStr=" + compactInfoStr );
                    continue;                
                }

                m = new EeoMatch(rank, title,typeId); // profileFacade.getProfile( profileId );
                m.setAveragePercentMatch(pct);
                out.add( m );
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BestJobsReportUtils.getEeoCategoryListWithData() " + e.getMessage() + ", full compactInfoStr=" + compactInfoStr );
        }
        
        return out;        
    }
    
    
    public List<Profile> getBestProfilesListWithData( String compactInfoStr, String delim, int socDataCode) 
    {
        List<Profile> out = new ArrayList<>();

        try
        {
            if( compactInfoStr==null || compactInfoStr.isEmpty() )
                return out;

            String[] pStrs = compactInfoStr.split("\\|" );

            // ProfileFacade profileFacade = ProfileFacade.getInstance();

            String[] parts;

            int rank;
            int profileId;
            String socCode;
            String name;
            float f1=0;
            float f2=0;
            float f3=0;
            float f4=0;
            
            Profile p;

            for( String pStr : pStrs )
            {
                if( pStr==null || pStr.isEmpty() || pStr.indexOf(delim )<0 )
                    continue;
                pStr=pStr.trim();

                parts = pStr.split( delim );


                if( parts.length<=7 )
                {
                    // LogService.logIt("BestJobsReportUtils.getBestProfilesListWithData() Skipping segment because not enough pieces. Segment=" + pStr + ", full compactInfoStr=" + compactInfoStr );
                    continue;
                }
                
                // skip header row if present.
                if( parts[0]!=null && parts[0].equalsIgnoreCase("rank"))
                    continue;

                rank = Integer.parseInt( parts[0]); 
                profileId = Integer.parseInt( parts[1]); 
                name = URLDecoder.decode(parts[2],"UTF8"); 
                f1 = Float.parseFloat( parts[3]);  // combined match
                f2 = Float.parseFloat( parts[4]); // riasec match
                f3 = Float.parseFloat( parts[5]); // educ exper match
                f4 = Float.parseFloat( parts[6]);  // alt score
                socCode = parts[7];

                if( name==null || name.isEmpty() )
                {
                    LogService.logIt("BestJobsReportUtils.getBestProfilesListWithData() Skipping segment because No name. Segment=" + pStr + ", full compactInfoStr=" + compactInfoStr );
                    continue;                
                }

                p = new Profile(); // profileFacade.getProfile( profileId );

                //if( p==null )
                 //   throw new Exception( "Profile not found for ProfileId=" + profileId + ", Segment=" + pStr  );

                // Clone it
               // p = (Profile)p.clone();

                
                p.setIntParam1(rank);
                p.setName(name);
                p.setStrParam1(name);  // name
                p.setFloatParam1(f1);
                p.setFloatParam2(f2);
                p.setFloatParam3(f3);
                p.setFloatParam4(f4);
                p.setStrParam4(socCode);  // soc code

                out.add( p );
            }
            
            boolean abilities = false;
            boolean knowledge = false;
            boolean skills = false;
            boolean workAct = false;
            boolean workSty = false;
            boolean workCont = false;
            boolean tasks = false;
            boolean altTitles = false;
            boolean related = false;
            boolean bls = false;

            if( socDataCode==1 )
            {
                abilities = true;
                knowledge = true;
                skills = true;
                workAct = true;
                workSty = true;
                workCont = true;
                tasks = true;
                altTitles = true;
                related = true;
                bls = true;
            }
            else if( socDataCode==10 )
            {
                abilities = true;
                knowledge = true;
                skills = true;
                workSty = true;
                tasks = true;
                altTitles = true;
                related = true;
                bls = true;
            }
            else if( socDataCode==11 )
            {
                altTitles = true;
                related = true;
                bls = true;
            }

            populateSocDataIfNeeded( out, abilities,  knowledge, skills,  workAct,  workSty,  workCont, tasks,  altTitles,  related,  bls );
            

        }
        catch( Exception e )
        {
            LogService.logIt(e, "BestJobsReportUtils.getBestProfilesListWithData() " + e.getMessage() + ", full compactInfoStr=" + compactInfoStr );
        }
        
        return out;
    }
    
    public static String createCompactInfoString( List<Profile> bestProfilesList, List<EeoMatch> eeoMatchList)
    {    
            StringBuilder sb = new StringBuilder();
            
            int count;

            if( bestProfilesList!=null && !bestProfilesList.isEmpty() )
            {
                sb.append( "[" + Constants.RIASEC_COMPACT_INFO_KEY + "]" );
                
                count = 0;

                for( Profile p : bestProfilesList )
                {
                    count++;

                    if( count>1 )
                        sb.append( "|" );

                    sb.append( getCompactProfileInfo( count, p ) );                
                }
            }

            if( eeoMatchList!=null && !eeoMatchList.isEmpty() )
            {
                sb.append( "[" + Constants.EEOCAT_COMPACT_INFO_KEY + "]" );
                
                count = 0;

                for( EeoMatch p : eeoMatchList )
                {
                    count++;

                    if( count>1 )
                        sb.append( "|" );

                    sb.append( getCompactEeocInfo( count, p ) );                
                }
            }
            
            
            return sb.toString();
    }
            
    
    private static String getCompactProfileInfo( int rank, Profile p )
    {
        return rank + "," + p.getProfileId() + "," + StringUtils.getUrlEncodedValue(p.getStrParam1()) + "," + p.getFloatParam1() + "," + p.getFloatParam2() + "," + p.getFloatParam3() + "," + p.getFloatParam4() + "," + p.getStrParam4();
    }
    

    private static String getCompactEeocInfo( int rank, EeoMatch p )
    {
        return rank + "," + StringUtils.getUrlEncodedValue(p.getEeoTitle()) + "," + p.getEeoJobCategoryId()+ "," + p.getAveragePercentMatch();
        // return rank + ";" + p.getProfileId() + ";" + URLEncoder.encode(p.getStrParam1(),"UTF8") + ";" + p.getFloatParam1() + ";" + p.getFloatParam2() + ";" + p.getFloatParam3() + ";" + p.getFloatParam4() + ";" + p.getStrParam4();
    }
    


/*
    includeEducTrngExpCode=0 do not include
                           1 = include
                           2 = include but assume one level higher
    */    
    public List<Profile> getHighProfileMatches( List<Profile> previouslyFoundPl, int maxMatches, TestEvent riasecTe, TestEvent jobSpecTe, int jobZoneGapLimit, int reduceMatchLimitsBy, OnetJobZoneType baseJobZoneToUse, int includeEducTrngExpCode, List<EeoMatch> eeoMatchList) throws Exception
    {
        try
        {
            // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() START jobZoneGapLimit=" + jobZoneGapLimit + ", reduceMatchLimitsBy=" + reduceMatchLimitsBy + ", baseJobZoneToUse=" + (baseJobZoneToUse==null ? "null" : baseJobZoneToUse.getJobZoneId() ) + ", includeEducTrngExpCode=" + includeEducTrngExpCode );
            
            List<Profile> out = new ArrayList<>();
            
            if( riasecTe==null || riasecTe.getTestEventScoreList()==null || riasecTe.getTestEventScoreList().isEmpty() )
                return out;

            if( profileFacade == null )
                profileFacade = ProfileFacade.getInstance();
            
            String localeStr = riasecTe.getLocaleStr();
            
            if( localeStr == null || localeStr.isBlank() || localeStr.equalsIgnoreCase("en") )
                localeStr = "en_US";

            int jobSpecTestJobZoneId = 0;            
            if( jobSpecTe!=null && jobSpecTe.getSimDescriptor()!=null && jobSpecTe.getSimDescriptor().getOnetSoc()!=null && !jobSpecTe.getSimDescriptor().getOnetSoc().isEmpty() )
            {
                if( onetFacade==null )
                    onetFacade = OnetFacade.getInstance();
                
                OnetJobZoneType ojzt = onetFacade.getOnetJobZoneType(jobSpecTe.getSimDescriptor().getOnetSoc());
                
                if( ojzt==null )
                    LogService.logIt("BestJobsReportUtils.getHighProfileMatches() BBB.1 OneJobZoneType is null for Soc=" + jobSpecTe.getSimDescriptor().getOnetSoc() + ", testEventId=" + jobSpecTe.getTestEventId() );

                jobSpecTestJobZoneId = ojzt==null ? 0 : ojzt.getJobZoneId();
            }
            
            OnetJobZoneType minOnetJobZoneType = baseJobZoneToUse!=null ? baseJobZoneToUse : getCandidateMinOnetJobZone(riasecTe, jobSpecTe, jobSpecTestJobZoneId ); 
            OnetJobZoneType maxOnetJobZoneType = baseJobZoneToUse!=null ? baseJobZoneToUse : getCandidateMaxOnetJobJone(riasecTe, jobSpecTe, jobSpecTestJobZoneId );  
            
            if( minOnetJobZoneType.getOnetJobZoneTypeId()>maxOnetJobZoneType.getOnetJobZoneTypeId() )
                minOnetJobZoneType = maxOnetJobZoneType;

            if( jobZoneGapLimit>0 )
            {
                int jzId = Math.max(1, minOnetJobZoneType.getJobZoneId()-jobZoneGapLimit);
                minOnetJobZoneType = OnetJobZoneType.getValueForZoneId(jzId);
                
                jzId = Math.min(5, maxOnetJobZoneType.getJobZoneId()+jobZoneGapLimit);
                maxOnetJobZoneType = OnetJobZoneType.getValueForZoneId(jzId);                
            }
            
            // ProfileUsageType profileUsageType = jobSpecTe!=null ? ProfileUsageType.CT3_ALTERNATE_OVERALL_CALC : ProfileUsageType.CT3_INTERESTS_MATCH_CALC;
            ProfileUsageType profileUsageType = ProfileUsageType.CT3_INTERESTS_MATCH_CALC;
            
            List<Profile> pl = null;
            
            if( currentProfileList!=null && 
                currentLocaleStr!=null && currentLocaleStr.equals(localeStr) && 
                currentProfileUsageType!=null && currentProfileUsageType.equals(profileUsageType) && 
                currentMinOnetJobZoneType!=null && currentMinOnetJobZoneType.equals(minOnetJobZoneType) && 
                currentMaxOnetJobZoneType!=null && currentMaxOnetJobZoneType.equals(maxOnetJobZoneType))
            {
                // LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() AAA.00 Using existing ProfilesList.size=" + currentProfileList.size() );
                pl = currentProfileList;
            }
            
            else
            {

                pl = profileFacade.getMatchingProfileListForProfileUsageTypeIdAndStrParam3( profileUsageType.getProfileUsageTypeId(), localeStr, minOnetJobZoneType, maxOnetJobZoneType );

                // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() AAA.01 minJobZoneId=" + minOnetJobZoneType.getJobZoneId() + ", maxJobZoneId=" + maxOnetJobZoneType.getJobZoneId() + ", jobZoneGapLimit=" + jobZoneGapLimit + ", reduceMatchLimitsBy=" + reduceMatchLimitsBy + ", localeStr=" + localeStr + ", found " + pl.size() + " eligible profiles." );

                if( pl.size()<20 && !localeStr.equalsIgnoreCase( "en_US" )  )
                    pl.addAll( profileFacade.getMatchingProfileListForProfileUsageTypeIdAndStrParam3( profileUsageType.getProfileUsageTypeId(), "en_US", minOnetJobZoneType, maxOnetJobZoneType ) );

                currentProfileList = pl;
                currentLocaleStr=localeStr;
                currentProfileUsageType=profileUsageType;
                currentMinOnetJobZoneType=minOnetJobZoneType;
                currentMaxOnetJobZoneType=maxOnetJobZoneType;                
                // LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() BBB.1 Loaded new Profiles. localeStr=" + localeStr + ", found " + pl.size() + " total eligible profiles. profileUsageType.getProfileUsageTypeId()=" + profileUsageType.getProfileUsageTypeId() + ", minOnetJobZoneType=" + minOnetJobZoneType.getName() + ", maxOnetJobZoneType=" + maxOnetJobZoneType.getName() );
            }
            
            float altScore=0;
            float riasecMatch;
            float educExpMatch;
            
            // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() CCC pl.size=" + (pl==null ? "null" : pl.size()) + ", minJobZoneTypeId=" + minOnetJobZoneType.getJobZoneId() + 
            //        ", maxJobZone=" + maxOnetJobZoneType.getJobZoneId() + 
            //        ", jobZoneGapLimit=" + jobZoneGapLimit + 
            //        ", reduceMatchLimitsBy=" + reduceMatchLimitsBy );
            
            float combinedMatch = 0;
            
            // don't want to show the same SOC twice. 
            List<String> usedSocList = new ArrayList<>();

            // int socxFreq;
            
            if( onetFacade==null )
                onetFacade = OnetFacade.getInstance();
                        
            // OnetJobZoneType profileOnetJobZoneType = null;
            
            ProfileFloat1Comparator cptr = new ProfileFloat1Comparator();
            Profile minP = null;
            EeoJobCategoryType eeoJobCategory;
            EeoMatch eeoMatch;
            
            boolean addIt;
            
            for( Profile p : pl )
            {
                if( previouslyFoundPl!=null && previouslyFoundPl.contains(p) )
                    continue;
                
                // Must have a SOC
                if( p.getStrParam4()==null || p.getStrParam4().isEmpty() )
                {
                    // LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.1 Skipping because Profile has no SOC" );
                    continue;
                }

                // skip any soc already covered.
                if( usedSocList.contains(p.getStrParam4()))
                {
                    // LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.2 Skipping because SOC is already processed." );
                    continue;
                }

                usedSocList.add( p.getStrParam4() );
                
                if( p.getSoc()==null )
                {
                    // Get the SOC
                    p.setSoc( onetFacade.getSoc( p.getStrParam4() ));                

                    if( p.getSoc() == null )
                    {
                        // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() DDD.2b testEventId=" + riasecTe.getTestEventId() + ", skipping profile " + p.getStrParam1() + " because no SOC found for onetsoc=" + p.getStrParam4() );
                        continue;
                    }
                }
                
                // profileOnetJobZoneType = getProfileOnetJobZoneType(p);
                
                //if( profileOnetJobZoneType!=null && !profileOnetJobZoneType.isWithinLimits( minOnetJobZoneType, maxOnetJobZoneType, jobZoneGapLimit ) )
                //{
                    // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() DDD.3 Skipping because not within limits (" + minOnetJobZoneType.getJobZoneId() + "-" +maxOnetJobZoneType.getJobZoneId() + ", Gap limit=" + jobZoneGapLimit  + ", profileOnetJobZoneType=" + profileOnetJobZoneType.getJobZoneId() );
                //    continue;
                //}
                                
                riasecMatch = calculateCt3RiasecMatch(p, riasecTe); // 0 - 100
                                
                educExpMatch = includeEducTrngExpCode>0 ? calculateCt3TrainingExperienceMatch( p, riasecTe, includeEducTrngExpCode ) : 0; // 0 - 100
                
                altScore=0;
                int combinedCount = 1;  // riasec
                                
                // Calculate.
                if( jobSpecTe!=null )
                {
                    altScore = calculateCt3OverallScore(p, jobSpecTe ); // 0 - 100 
                    
                    if( includeEducTrngExpCode>0 )
                    {
                        combinedMatch = ( RIASEC_WEIGHT*riasecMatch + SCORE_WEIGHT*altScore + EDUCEXP_WEIGHT*educExpMatch )/(RIASEC_WEIGHT + SCORE_WEIGHT + EDUCEXP_WEIGHT);
                        combinedCount = 3;
                    }
                    else
                    {
                        combinedMatch = ( RIASEC_WEIGHT*riasecMatch + SCORE_WEIGHT*altScore)/(RIASEC_WEIGHT + SCORE_WEIGHT);
                        combinedCount = 2;
                    }                        
                }
                
                else if( includeEducTrngExpCode>0 )
                {
                    combinedMatch = ( RIASEC_WEIGHT*riasecMatch + EDUCEXP_WEIGHT*educExpMatch )/(RIASEC_WEIGHT + EDUCEXP_WEIGHT);
                    combinedCount = 2;
                }                
                
                else
                {
                    combinedMatch = riasecMatch;
                    combinedCount = 1;
                }
                
                // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() DDD.4 testEventId=" + riasecTe.getTestEventId() + ", Score for SOC " + p.getStrParam4() + ", soc.name=" + (p.getSoc()==null ? "null" : p.getSoc().getTitle()) + " is " + altScore + ", riasecMatch is " + riasecMatch + ", educExpMatch=" + educExpMatch + ", combinedMatch=" + combinedMatch + ", combinedCount=" + combinedCount );
                                
                if( eeoMatchList!=null && combinedMatch>0 )
                {
                    eeoJobCategory = onetFacade.getEeoJobCategoryTypeForSoc(p.getStrParam4() );
                    
                    if( eeoJobCategory!=null && !eeoJobCategory.equals(EeoJobCategoryType.NONE) )
                    {
                        eeoMatch = getEeoMatchForEeoCategoryTypeId(eeoJobCategory.getEeoJobCategoryTypeId(), eeoMatchList);
                        eeoMatch.addPercentMatch(combinedMatch);
                    }
                }
                
                if( altScore>0 && altScore<MIN_JOBSPEC_SCORE - reduceMatchLimitsBy )
                {
                    //LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.5 Skipping JobSpecific Score is just too low. " + altScore + " (cutoff is " + (MIN_JOBSPEC_SCORE - reduceMatchLimitsBy) + ")" );
                    continue;                    
                }

                if( riasecMatch>0 && riasecMatch<MIN_RIASEC_MATCH - reduceMatchLimitsBy )
                {
                    //LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.6 Skipping RIASEC MATCH Score is just too low. " + riasecMatch + " (cutoff is " + (MIN_RIASEC_MATCH - reduceMatchLimitsBy) + ")" );
                    continue;                    
                }

                if( includeEducTrngExpCode>0 && educExpMatch>0 && educExpMatch<MIN_EDUCEXP_MATCH - reduceMatchLimitsBy )
                {
                    //LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.7 Skipping EducExp MATCH Score is just too low. " + educExpMatch + " (cutoff is " + (MIN_EDUCEXP_MATCH - reduceMatchLimitsBy) + ")" );
                    continue;                    
                }

                if( combinedMatch>0 && combinedCount>1 && combinedMatch<MIN_COMBINED_MATCH - reduceMatchLimitsBy )
                {
                    //LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.8 Skipping Combined MATCH Score is just too low. " + combinedMatch + " (cutoff is " + (MIN_COMBINED_MATCH  - reduceMatchLimitsBy)+ ")" );
                    continue;                    
                }
                                
                if( combinedMatch<=0 )
                {
                    // LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.9 Skipping Combined MATCH Score negative " + combinedMatch );
                    continue;
                }
                
                
                // already full
                if( out.size()>=maxMatches )
                {
                    Collections.sort( out, cptr );

                    minP = out.get(0);
                    if( minP.getFloatParam1() > combinedMatch )
                    {
                        //LogService.logIt( "BestJobsReportUtils.getHighProfileMatches() DDD.10 Skipping because combinedmatch already too low.  Lowest=" + minP.getFloatParam1() + ", combinedMatch=" + combinedMatch );
                        continue;                        
                    }                    
                }
                
                
                p = (Profile) p.clone();                
                p.setFloatParam1( combinedMatch );
                p.setFloatParam2( riasecMatch );
                p.setFloatParam3( educExpMatch );
                p.setFloatParam4( altScore );
                
                // Get the SOC
                if( p.getSoc()==null )
                    p.setSoc( onetFacade.getSoc( p.getStrParam4() ));                
                
                if( p.getSoc()==null )
                {
                    // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() DDD.10 testEventId=" + riasecTe.getTestEventId() + ", skipping profile " + p.getStrParam1() + " because no SOC found for onetsoc=" + p.getStrParam4() );
                    continue;
                }
                
                if( p.getSoc().getJobZoneType()==null && p.getIntParam1()>0 )
                    p.getSoc().setJobZoneType( OnetJobZoneType.getValueForZoneId(p.getIntParam1()));
                
                // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() FFF.2 Checking for too many SOC freq occurences. soc=" + p.getStrParam4() + ", soc5 freq=" + getSoc5Freq( p.getStrParam4() ) + ", soc4 freq=" + getSoc4Freq( p.getStrParam4()) + ", soc3 freq=" + getSoc3Freq( p.getStrParam4()) );
                
                addIt = true;
                
                if( out.contains(p) )
                    addIt=false;
                
                // already have enough for this Soc5, swap if this profile has a higher match
                if( addIt && getSoc5Freq( p.getStrParam4() ) >= BestJobsReportUtils.MAX_SOC5_FREQ )
                {
                    swapOrSkipSocTopXProfiles( p, 5, out );
                    addIt=false;
                }

                // already have enough for this Soc4, swap if this profile has a higher match
                if( addIt && getSoc4Freq( p.getStrParam4() ) >= BestJobsReportUtils.MAX_SOC4_FREQ )
                {
                    swapOrSkipSocTopXProfiles( p, 4, out );
                    addIt=false;
                }

                // already have enough for this Soc3, swap if this profile has a higher match
                if( addIt && getSoc3Freq( p.getStrParam4() ) >= BestJobsReportUtils.MAX_SOC3_FREQ )
                {
                    swapOrSkipSocTopXProfiles( p, 3, out );
                    addIt=false;
                }
                
                // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() FFF.3 AddIt=" + addIt + ", soc5 freq=" + getSoc5Freq( p.getStrParam4() ) + ", soc4 freq=" + getSoc4Freq( p.getStrParam4()) + ", soc3 freq=" + getSoc3Freq( p.getStrParam4()) );
                
                // do not have too many yet.
                if( addIt )
                {
                    out.add( p );
                    addSoc5( p.getStrParam4() );
                    addSoc4( p.getStrParam4() );
                    addSoc3( p.getStrParam4() );
                }                
                
                // remove extra
                if( out.size()>maxMatches )
                {
                    Collections.sort( out, cptr );
                    Collections.reverse(out);

                    // correct freq map for removed profiles
                    List<Profile> plx = out.subList(maxMatches, out.size());
                    for( Profile xp : plx )
                    {
                        removeSoc5( p.getStrParam4() );
                        removeSoc4( p.getStrParam4() );
                        removeSoc3( p.getStrParam4() );
                    }
                    out = out.subList(0, maxMatches);
                }
            }
            
            
            Collections.sort( out, cptr );
            Collections.reverse(out);
                        
            if( out.size()> maxMatches )
                out = out.subList(0, maxMatches);

            // LogService.logIt("BestJobsReportUtils.getHighProfileMatches() FFF testEventId=" + riasecTe.getTestEventId() + ", match list.size=" + out.size() );
            
            /*
            for( Profile px : out )
            {
                if( px.getSoc()==null )
                   px.setSoc( onetFacade.getSoc( px.getStrParam4() ));
               
                if( px.getSoc()!=null && !px.getSoc().hasData() )
                {
                    LogService.logIt("BestJobsReportUtils.getHighProfileMatches() GGG.1 Soc does not contain data. " );
                    addDataToSoc( px.getSoc() );
                }
                else if( px.getSoc()!=null && px.getSoc().hasData() )
                    LogService.logIt("BestJobsReportUtils.getHighProfileMatches() GGG Soc.2 DOES contain data. " );
            }
            */
            
            return out;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "BestJobsReportUtils.getHighProfileMatches " + (riasecTe==null ? "TestEvent is null" : riasecTe.toString() ) );
            
            throw new STException(e);
        }
    }
    


    private void swapOrSkipSocTopXProfiles( Profile p2, int topX, List<Profile> pl )
    {
        List<Profile> existingPl = new ArrayList<>();
        String socx = topX==5 ? BestJobsReportUtils.getSocTop5( p2.getStrParam4() ) : ( topX==4 ? BestJobsReportUtils.getSocTop4( p2.getStrParam4() )  : BestJobsReportUtils.getSocTop3( p2.getStrParam4() ) );

        // LogService.logIt( "BestJobsReportUtils.swapOrSkipSocTopXProfiles() p2.soc=" + p2.getStrParam4() + ", socx=" + socx + ", pl.size=" + pl.size() + ", top5=" + top5 );
        
        for( Profile p : pl )
        {
            if( topX==5 && BestJobsReportUtils.getSocTop5( p.getStrParam4() ).equals(socx) )
                existingPl.add(p);

            if( topX==4 && BestJobsReportUtils.getSocTop4( p.getStrParam4() ).equals(socx) )
                existingPl.add(p);

            if( topX==3 && BestJobsReportUtils.getSocTop3( p.getStrParam4() ).equals(socx) )
                existingPl.add(p);
        }

        // LogService.logIt( "BestJobsReportUtils.swapOrSkipSocTopXProfiles() top5=" + top5 + ", existingPl.size=" + existingPl.size());
        
        // no matching soc5 found - should not happen
        if( existingPl.isEmpty() )
        {
            LogService.logIt("BestJobsReportUtils.swapOrSkipSocTopXProfiles() p2.soc=" + p2.getStrParam4() + ", socx=" + socx + ", topX=" + topX + " no matching profiles found in currentProfileList. something is WRONG. pl.size=" + pl.size());
            pl.add(p2);
            addSoc5( p2.getStrParam4() );
            addSoc4( p2.getStrParam4() );
            addSoc3( p2.getStrParam4() );
            return;
        }
        
        Collections.sort(existingPl, new ProfileFloat1Comparator() );
        Profile lowP = existingPl.get(0);
        // LogService.logIt( "BestJobsReportUtils.swapOrSkipSocTopXProfiles() lowP=" +  lowP.getStrParam4() + ", combined=" + lowP.getFloatParam1() +", p2.combined=" + p2.getFloatParam1());
        
        // lowest current p is higher than the new one.
        if( lowP.getFloatParam1()>=p2.getFloatParam1() )
            return;

        
        // else remove the low p and add the new one to the list. Will sort above.
        pl.remove( lowP );
        pl.add( p2 );        

        // LogService.logIt( "BestJobsReportUtils.swapOrSkipSocTopXProfiles() swapping. New pl.size()=" + pl.size() );
    }

    
    private void populateSocDataIfNeeded( List<Profile> pl, boolean abilities, boolean knowledge, 
            boolean skills, boolean workAct, boolean workSty, boolean workCont, 
            boolean tasks, boolean altTitles, boolean related, boolean bls ) throws Exception
    {
       
        if(pl==null )
            return;
        
        for( Profile px : pl )
        {
            // this is the soc code.
            if( px.getStrParam4()==null || px.getStrParam4().isBlank() )
                continue;
            
            if( onetFacade==null )
                onetFacade=OnetFacade.getInstance();
            
            if( px.getSoc()==null )
               px.setSoc( onetFacade.getSoc( px.getStrParam4() ));

            if( px.getSoc()!=null && px.getSoc().getJobZoneType()==null && px.getIntParam1()>0 )
                px.getSoc().setJobZoneType( OnetJobZoneType.getValueForZoneId( px.getIntParam1() ));
            
            if( px.getSoc()!=null && !px.getSoc().hasAllData() )
            {
                // LogService.logIt("BestJobsReportUtils.populateSocDataIfNeeded() GGG.1 Soc does not contain data. " );
                addDataToSoc( px.getSoc(), abilities,  knowledge, skills,  workAct,  workSty,  workCont, tasks,  altTitles,  related,  bls );
            }
            // else if( px.getSoc()!=null && px.getSoc().hasAllData() )
            //     LogService.logIt("BestJobsReportUtils.populateSocDataIfNeeded() GGG Soc.2 DOES contain data. " );
        }
    }
    
    private void addDataToSoc( Soc soc, boolean abilities, boolean knowledge, 
            boolean skills, boolean workAct, boolean workSty, boolean workCont, 
            boolean tasks, boolean altTitles, boolean related, boolean bls ) throws Exception 
    {
        // if( soc.hasData() )
        //     return;
        
        if( onetFacade==null )
            onetFacade = OnetFacade.getInstance();

        int COUNT = 10;

        if( soc.getJobZoneType()==null )
            soc.setJobZoneType(onetFacade.getOnetJobZoneType( soc.getSocCode() ));
            
        if( abilities && soc.getAbilities()==null )
            soc.setAbilities( onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.ABILITY ));
        
        if( knowledge && soc.getKnowledge()==null )
            soc.setKnowledge(onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.KNOWLEDGE ));
        
        if( skills && soc.getSkills()==null )
            soc.setSkills(onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.SKILL ));
    
        if( workAct && soc.getWorkActivities()==null )
            soc.setWorkActivities(onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.WK_ACTIVITY ));
        
        if( workSty && soc.getWorkStyles()==null )
            soc.setWorkStyles(onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.WK_STYLE ));
    
        if( workCont && soc.getWorkContexts()==null )
            soc.setWorkContexts(onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.WK_CONTEXT ));

        if( tasks && soc.getTasks()==null )
            soc.setTasks(onetFacade.getKSAsForSoc(soc.getSocCode(), COUNT, OnetElementType.TASK ));
        
        if( altTitles && soc.getAlternateTitlesList()==null )
            soc.setAlternateTitlesList( onetFacade.getAlternateTitlesList( soc.getSocCode() ) );   
        
        // soc.setGreenJob( onetFacade.getIsGreenJob( soc.getSocCode() ) );
        
        if( related && soc.getRelatedJobs()==null )
            soc.setRelatedJobs( onetFacade.getRelatedSocList( soc.getSocCode() ));
        
        if( bls && soc.getBlsEmployment()<=0 )
        {
            int[] blsData = onetFacade.getBlsEmploymentData( soc.getSocCode() );

            soc.setBlsEmployment(blsData[0]);

            soc.setBlsAverageAnnualSalary( blsData[1]);
        }
        
    }
    
    
    public static Map<String,Float> getScoreMap( TestEvent te )
    {
        Map<String,Float> pm = new HashMap<>();
        
        if( te==null || te.getTestEventScoreList()==null )
            return pm;
        
        for( TestEventScore tes : te.getTestEventScoreList() )
        {
            if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                continue;
            
            pm.put( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() ? tes.getNameEnglish() : tes.getName(), (float)( tes.getScore() ) );            
        }   
        
        return pm;
    }
    
    /**
     * Returns a value 0 - 100
     * 
     * @param p
     * @param te
     * @return
     * @throws Exception 
     */
    public float calculateCt3RiasecMatch( Profile p, TestEvent te) throws Exception
    {
        if( p==null || p.getStrParam5()==null || p.getStrParam5().isEmpty() || (p.getProfileUsageTypeId()!=ProfileUsageType.CT3_ALTERNATE_OVERALL_CALC.getProfileUsageTypeId() && p.getProfileUsageTypeId()!= ProfileUsageType.CT3_INTERESTS_MATCH_CALC.getProfileUsageTypeId()) )
            return 0;
        
        if( te==null || te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty() )
            return 0;
        
        // this is a map of the 5 riasec ratings where max rating is 100 and min is 0.
        Map<String,Float> pm = getProfileRiasecScoreMap( p );
        
        //Map<String,Integer> educm = getProfileTrainingExperienceCategoryMap( p );
        
        int count = 0;
        
        float total = 0;
        float totalWeights = 0;
        
        Float pMatch;
        //Float educMatch;
        
        for( TestEventScore tes : te.getTestEventScoreList() )
        {
            if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                continue;
            
            pMatch = pm.get( tes.getName() );
            
            if( pMatch==null && tes.getNameEnglish()!=null && !tes.getNameEnglish().isBlank() )
                pMatch = pm.get( tes.getNameEnglish());
            
            if( pMatch==null )
                continue;
            
            if( pMatch==0 )
                continue;
            
            count++;
            
            total += pMatch*tes.getScore();
            
            totalWeights += pMatch;
        }
        
        if( count<3 || totalWeights<=0 )
            return 0;
        
        return total / totalWeights;        
    }

    public OnetJobZoneType getCandidateMaxOnetJobJone( TestEvent riasecTe, TestEvent jobSpecTe, int jobSpecTestJobZoneId) throws Exception
    {
        float educCat = 0;
        float expCat = 0;
        float trngCat = 0;
        
        float cogScore = -1;
        float needsStrucScore = -1;
        
        if( jobSpecTe !=null )
        {
            for( TestEventScore tes : jobSpecTe.getTestEventScoreList() )
            {
                if( needsStrucScore<0 && tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) && ( tes.getName().equalsIgnoreCase("Needs Structure") || ( tes.getNameEnglish()!=null && tes.getNameEnglish().equalsIgnoreCase("Needs Structure") ) ) )
                    needsStrucScore = tes.getScore();
                
                else if( cogScore<0 && tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCYGROUP ) && tes.getName().equalsIgnoreCase( "Abilities" ) )
                    cogScore = tes.getScore();
            }
        }
        
        // Get candidate Categories
        for( TestEventScore tes : riasecTe.getTestEventScoreList() )
        {
            if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                continue;

            // Match
            if( tes.getName().equals( OnetTrainingExperienceElementType.EDUCATION_CAT.getName() ) ||  (tes.getNameEnglish()!=null && tes.getNameEnglish().equals( OnetTrainingExperienceElementType.EDUCATION_CAT.getName() )) )
                educCat = tes.getTotalUsed(); // tes.getRawScore();

            if( tes.getName().equals( OnetTrainingExperienceElementType.EXPERIENCE_CAT.getName() ) ||  (tes.getNameEnglish()!=null && tes.getNameEnglish().equals( OnetTrainingExperienceElementType.EXPERIENCE_CAT.getName() )) )
                expCat = tes.getTotalUsed(); // tes.getRawScore();

            if( tes.getName().equals( OnetTrainingExperienceElementType.TRAINING_CAT.getName() ) ||  (tes.getNameEnglish()!=null && tes.getNameEnglish().equals( OnetTrainingExperienceElementType.TRAINING_CAT.getName() )) )
                trngCat = tes.getTotalUsed();  // tes.getRawScore();
        }
        
        int maxZoneId = 5;

        // HS Only
        if( educCat <= 3 )
        {
            // year of training or experience
            if( trngCat>=6 || expCat>=6 )
                maxZoneId = Math.min( maxZoneId, 3);
            else    
                maxZoneId = Math.min( maxZoneId, 2);
        }
        
        // best some college
        if( educCat > 3 && educCat<6 )
        {
            // 4 years of training or experience
            if( trngCat>=8 || expCat>=8 )
                maxZoneId = Math.min( maxZoneId, 4);
            // one year
            else if( trngCat>=6 || expCat>=6 )
                maxZoneId = Math.min( maxZoneId, 3);
            
            // No training
            else
                maxZoneId = Math.min( maxZoneId, 2);
        }
        
        // Bach degree 
        if( educCat >= 6 && educCat < 8 )
        {
            // 4+ years training or 6 years exp
            if( trngCat>=8 || expCat>=9 )
                maxZoneId = 5;
            
            else if( trngCat>=6 || expCat>7 )
                maxZoneId = 4;
            
            else    
                maxZoneId = 3;
        }
        
        // grad dgree
        if( educCat >= 8 )
            maxZoneId = 5;
           
        // Hig Cog scores and low max zone, raise by 1
        if( cogScore > 80 && maxZoneId<=2 )
            maxZoneId++;

        // High structure score and high zone, lower by one.
        if( needsStrucScore >80 && maxZoneId>3 )
            maxZoneId--;
            
        if( jobSpecTestJobZoneId>0 && jobSpecTestJobZoneId > maxZoneId )
            maxZoneId++;
        
        else if( jobSpecTestJobZoneId>0 && jobSpecTestJobZoneId < maxZoneId - 2 )
            maxZoneId--;
        
        return OnetJobZoneType.getValueForZoneId(maxZoneId);        
    }


    public OnetJobZoneType getCandidateMinOnetJobZone( TestEvent riasecTe, TestEvent jobSpecTe, int jobSpecTestJobZoneId) throws Exception
    {
        float educCat = 0;
        float expCat = 0;
        float trngCat = 0;
        
        float cogScore = -1;
        float needsStrucScore = -1;
                
        if( jobSpecTe !=null )
        {            
            for( TestEventScore tes : jobSpecTe.getTestEventScoreList() )
            {
                // if( needsStrucScore<0 && tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) && ( tes.getName().equalsIgnoreCase("Needs Structure") || ( tes.getNameEnglish()!=null && tes.getNameEnglish().equalsIgnoreCase("Needs Structure") ) ) )
               //      needsStrucScore = tes.getScore();
                
                if( cogScore<0 && tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCYGROUP ) && tes.getName().equalsIgnoreCase( "Abilities" ) )
                    cogScore = tes.getScore();
            }
        }
        
        // Get candidate Categories
        for( TestEventScore tes : riasecTe.getTestEventScoreList() )
        {
            if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                continue;

            // Match
            if( tes.getName().equals( OnetTrainingExperienceElementType.EDUCATION_CAT.getName() ) ||  (tes.getNameEnglish()!=null && tes.getNameEnglish().equals( OnetTrainingExperienceElementType.EDUCATION_CAT.getName() )) )
                educCat = tes.getTotalUsed(); //  tes.getRawScore();

            if( tes.getName().equals( OnetTrainingExperienceElementType.EXPERIENCE_CAT.getName() ) ||  (tes.getNameEnglish()!=null && tes.getNameEnglish().equals( OnetTrainingExperienceElementType.EXPERIENCE_CAT.getName() )) )
                expCat = tes.getTotalUsed(); // tes.getRawScore();

            if( tes.getName().equals( OnetTrainingExperienceElementType.TRAINING_CAT.getName() ) ||  (tes.getNameEnglish()!=null && tes.getNameEnglish().equals( OnetTrainingExperienceElementType.TRAINING_CAT.getName() )) )
                trngCat = tes.getTotalUsed(); // tes.getRawScore();
        }
        
        int minZoneId = 1;

        // HS 
        if( educCat <= 3 )
        {
            // 4 yrs exp or a month or training
            if( expCat>=8 || trngCat>=3 )
                minZoneId = Math.max( minZoneId, 2 );

            // 8+ yers exp or 1+year training
            if( expCat>=10 || trngCat>=6 )
                minZoneId = Math.max( minZoneId, 3 );
        }

        // Some collect or higher only
        else if( educCat>3 && educCat<6 )
        {
            // 2 yrs exp or a year training
            if( expCat>=7 || trngCat>=6 )
                minZoneId = Math.max( minZoneId, 3 );

            else
               minZoneId = Math.max( minZoneId, 2 ); 
        }

        // Bachelors
        else if( educCat>= 6 && educCat<8 )
        {
            // 2 yrs exp or a year training
            if( expCat>=7 || trngCat>=6 )
                minZoneId = Math.max( minZoneId, 4 );

            else
               minZoneId = Math.max( minZoneId, 3 ); 
        }
        
        else
            minZoneId = Math.max( minZoneId, 4 );
                
        // High Cog scores and low min zone, raise by 1
        if( cogScore>80 && minZoneId<=2 )
            minZoneId++;

        // High structure score and high min zone, lower by one.
        if( needsStrucScore>80 && minZoneId>3 )
            minZoneId--;
                
        if( jobSpecTestJobZoneId>0 && jobSpecTestJobZoneId<minZoneId )
            minZoneId--;

         if( jobSpecTestJobZoneId>0 && jobSpecTestJobZoneId > minZoneId+2 )
            minZoneId++;
        
        return OnetJobZoneType.getValueForZoneId(minZoneId);        
    }
    
    public OnetJobZoneType getProfileOnetJobZoneType( Profile p) throws Exception
    {
        if( p==null || (p.getProfileUsageTypeId()!=ProfileUsageType.CT3_ALTERNATE_OVERALL_CALC.getProfileUsageTypeId() && p.getProfileUsageTypeId()!= ProfileUsageType.CT3_INTERESTS_MATCH_CALC.getProfileUsageTypeId()) )
            return null;

        if( p.getIntParam2()>0 )
            return OnetJobZoneType.getValue(p.getIntParam2());
        
        if( p.getIntParam1()>0 )
            return OnetJobZoneType.getValueForZoneId(p.getIntParam1());
        
        if( p.getStrParam5()==null || p.getStrParam5().isEmpty() )
            return null;

        String [] da = p.getStrParam5().split(";");
                
        String n,v;
        
        for( int i=0;i<da.length-1; i+=2 )
        {
            n=da[i];
            v=da[i+1];
            
            if( n==null || !n.equals("JobZoneTypeId") )
                continue;
            
            return OnetJobZoneType.getValue( Integer.parseInt( v ) );
        }
        
        return null;
    }
    
    /**
     * Returns 0 - 100
     * 
     * @param p
     * @param te
     * @return
     * @throws Exception 
     */
    public float calculateCt3TrainingExperienceMatch( Profile p, TestEvent te, int includeEducTrngExpCode ) throws Exception
    {
        
        if( p==null || p.getStrParam5()==null || p.getStrParam5().isEmpty() || (p.getProfileUsageTypeId()!= ProfileUsageType.CT3_ALTERNATE_OVERALL_CALC.getProfileUsageTypeId() && p.getProfileUsageTypeId()!= ProfileUsageType.CT3_INTERESTS_MATCH_CALC.getProfileUsageTypeId()) )
            return 0;
        
        if( te==null || te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty() )
            return 0;
        
        if( this.careerScoutV2 )
            return calculateCt3TrainingExperienceMatchCareerScoutV2( p, te, includeEducTrngExpCode );
        
        
        Map<OnetTrainingExperienceElementType,Integer> targetEducMp = getProfileTrainingExperienceCategoryMap( p );
        
        // OnetTrainingExperienceElementType onetTrainingExperienceElementType;
        int count = 0;
        
        float total = 0;
        float totalWeights = 0;
        
        // this is the target category number for the provided profile.
        Integer profileTgtCategory;
        int scoreCategory;
        
        float scoreGap;
        
        float scoreMatch ;
        //Float educMatch;

        TestEventScore tes;
        
        for( OnetTrainingExperienceElementType oeit : OnetTrainingExperienceElementType.values() )
        {
            // get the Profile Category
            profileTgtCategory = targetEducMp.get( oeit );
            
            if( profileTgtCategory == null )
                continue;
            
            // Next, get the Score
            tes = null;
            
            for( TestEventScore tesx : te.getTestEventScoreList() )
            {
                if( !tesx.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                    continue;

                // Match
                if( tesx.getName().equals( oeit.getName() ) ||  (tesx.getNameEnglish()!=null && tesx.getNameEnglish().equals( oeit.getName() )) )
                {
                    tes = tesx;
                    break;
                }
            }
            
            if( tes==null )
                continue;

            scoreCategory = (int) tes.getTotalUsed(); //  tes.getScore();  
            
            scoreGap = (float) (scoreCategory - profileTgtCategory);
            
            if( scoreGap == 0 )
                scoreMatch = 100f;
            
            // if score gap is 'low'  (below profile value)
            else if( scoreGap < 0 )
                scoreMatch = 100f + scoreGap*oeit.getGapCostLow();
                        
            // if score gap is 'high'  (above profile value)
            else 
                scoreMatch = 100f - scoreGap*oeit.getGapCostHigh();
            
            count++;
            
            total += scoreMatch*oeit.getCalcWeight();
            
            totalWeights += oeit.getCalcWeight();
        }
        
        // no data found.
        if( count <=0 || totalWeights<=0 )
            return 0;
        
        
        return total / totalWeights;        
    }



    /**
     * Returns 0 - 100
     * 
     * @param p
     * @param te
     * @return
     * @throws Exception 
     */
    public float calculateCt3TrainingExperienceMatchCareerScoutV2( Profile p, TestEvent te, int includeEducTrngExpCode ) throws Exception
    {
        
        if( p==null || p.getStrParam5()==null || p.getStrParam5().isEmpty() || (p.getProfileUsageTypeId()!= ProfileUsageType.CT3_ALTERNATE_OVERALL_CALC.getProfileUsageTypeId() && p.getProfileUsageTypeId()!= ProfileUsageType.CT3_INTERESTS_MATCH_CALC.getProfileUsageTypeId()) )
            return 0;
        
        if( te==null || te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty() )
            return 0;
        
        if( !careerScoutV2 )
            return calculateCt3TrainingExperienceMatch( p, te, includeEducTrngExpCode );
                
        Map<OnetTrainingExperienceElementType,Integer> targetEducMp = getProfileTrainingExperienceCategoryMap( p );
        
        
        int education = targetEducMp.containsKey(OnetTrainingExperienceElementType.EDUCATION_CAT) ? targetEducMp.get(OnetTrainingExperienceElementType.EDUCATION_CAT) : 0;        
        int experience = targetEducMp.containsKey(OnetTrainingExperienceElementType.EXPERIENCE_CAT) ? targetEducMp.get(OnetTrainingExperienceElementType.EXPERIENCE_CAT) : 0;
        int training = targetEducMp.containsKey(OnetTrainingExperienceElementType.TRAINING_CAT) ? targetEducMp.get(OnetTrainingExperienceElementType.TRAINING_CAT) : 0;
        
        // now convert to 0 - 100 scores. 
        
        // education, range 1 - 12
        float educTgt = education<=0 ? -1 : 100f*(education-1f)/12f;
        float expTgt = experience<=0 ? -1 : 100f*(experience-1f)/11f;
        float trainTgt = training<=0 ? -1 : 100f*(training-1f)/9f;
        
        float exptrainTgt = -1;
        if( expTgt>0 && trainTgt>0 )
            exptrainTgt = (expTgt + trainTgt)/2f;
        else if( expTgt>0 )
            exptrainTgt = expTgt;
        else if( trainTgt>0 )
            exptrainTgt = trainTgt;
        
        // LogService.logIt( "BestJobsReportUtils.calculateCt3TrainingExperienceMatchCareerScoutV2() p.soc=" + p.getStrParam1() + ", educTgt=" + educTgt + ", expTgt=" + expTgt + ", trainTgt=" + trainTgt + ", exptrainTgt=" + exptrainTgt );
        
        // OK, now have a target value for educ and exptrain that are 0-100.
        TestEventScore tes;
        float scoreGap;        
        float scoreMatch;
        int count = 0;
        
        float total = 0;
        float totalWeights = 0;
        
        if( educTgt>=0 )
        {
            tes = getMatchingTestEventScore( TestEventScoreType.COMPETENCY, OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getName(), te.getTestEventScoreList() );
            if( tes!=null )
            {
                // this is on a 0-100 scale 
                scoreGap = tes.getScore() - educTgt;
                  
                if( includeEducTrngExpCode==2 )
                    scoreMatch=100f;
                
                else if( scoreGap==0 )
                    scoreMatch = 100f;
            
                // if score is above target
                else if( scoreGap>0 )
                    scoreMatch=100f - OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getGapCostHigh()*scoreGap;
                
                // if actual score is below target
                else
                {
                    scoreMatch=100f + OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getGapCostLow()*scoreGap;
                    if( scoreMatch<0 )
                        scoreMatch=0;
                }

                // LogService.logIt( "BestJobsReportUtils.calculateCt3TrainingExperienceMatchCareerScoutV2() Education: tes.getScore()=" + tes.getScore() + ", scoreMatch=" + scoreMatch + ", scoreGap=" + scoreGap );
                
                count++;            
                total += scoreMatch*OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getCalcWeight();            
                totalWeights += OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getCalcWeight();                
            }
            else if( includeEducTrngExpCode==2 )
            {
                count++;            
                total += 100f*OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getCalcWeight();            
                totalWeights += OnetTrainingExperienceElementTypeCareerScoutV2.EDUCATION_CAT.getCalcWeight();                
            }
        }
        
        if( exptrainTgt>=0 )
        {
            tes = getMatchingTestEventScore( TestEventScoreType.COMPETENCY, OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getName(), te.getTestEventScoreList() );
            if( tes!=null )
            {
                // this is on a 0-100 scale 
                scoreGap = tes.getScore() - exptrainTgt;
                                
                if( includeEducTrngExpCode==2 )
                    scoreMatch=100f;
                
                else if( scoreGap==0 )
                    scoreMatch = 100f;
            
                // if score is above target
                else if( scoreGap>0 )
                    scoreMatch=100f - OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getGapCostHigh()*scoreGap;
                
                // if actual score is below target
                else
                {
                    scoreMatch=100f + OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getGapCostLow()*scoreGap;
                    if( scoreMatch<0 )
                        scoreMatch=0;
                }

                // LogService.logIt( "BestJobsReportUtils.calculateCt3TrainingExperienceMatchCareerScoutV2() Experience and Training: tes.getScore()=" + tes.getScore() + ", scoreMatch=" + scoreMatch + ", scoreGap=" + scoreGap );
                
                count++;            
                total += scoreMatch*OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getCalcWeight();            
                totalWeights += OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getCalcWeight();                
            }
            else if( includeEducTrngExpCode==2 )
            {
                count++;            
                total += 100f*OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getCalcWeight();            
                totalWeights += OnetTrainingExperienceElementTypeCareerScoutV2.EXPERIENCE_AND_TRAINING_CAT.getCalcWeight();                
            }
        }

        // no data found.
        if( count <=0 || totalWeights<=0 )
            return 0;
                
        return total / totalWeights;        
    }


    private TestEventScore getMatchingTestEventScore( TestEventScoreType type, String name, List<TestEventScore> tesl )
    {
        for( TestEventScore tesx : tesl )
        {
            if( !tesx.getTestEventScoreType().equals( type ) )
                continue;

            // Match
            if( tesx.getName().equals( name ) ||  (tesx.getNameEnglish()!=null && tesx.getNameEnglish().equals( name )) )
                return tesx;
        }
        return null;
    }
    
    
    public float calculateCt3OverallScore( Profile p, TestEvent te ) throws Exception
    {
        if( p==null || p.getTextParam1()==null || p.getTextParam1().isEmpty() || (p.getProfileUsageTypeId()!=ProfileUsageType.CT3_ALTERNATE_OVERALL_CALC.getProfileUsageTypeId() && p.getProfileUsageTypeId()!= ProfileUsageType.CT3_INTERESTS_MATCH_CALC.getProfileUsageTypeId()) )
            return 0;
        
        if( te==null || te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty() )
            return 0;
        
        Map<String,Float> pm = getProfileScoreMap( p );
        
        int count = 0;
        
        float total = 0;
        float totalWeights = 0;
        
        Float pMatch;
        
        for( TestEventScore tes : te.getTestEventScoreList() )
        {
            if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                continue;
            
            if( tes.getHide()!=0 && tes.getHide()!=2 )
                continue;
            
            pMatch = pm.get( tes.getName() );
            
            if( pMatch==null && tes.getNameEnglish()!=null && !tes.getNameEnglish().isBlank() )
                pMatch = pm.get( tes.getNameEnglish());
            
            
            if( pMatch==null )
                continue;
            
            if( pMatch==0 )
                continue;
            
            count++;
            
            total += pMatch*tes.getScore();
            
            totalWeights += pMatch;
        }
        
        if( count<3 || totalWeights<=0 )
            return 0;
        
        return total / totalWeights;
    }
    
    
    
    public Map<String,Float> getProfileRiasecScoreMap( Profile p ) throws Exception
    {
        if( p==null  )
            return new HashMap<>();

        if( p.getProfileRiasecScoreMap()!=null )
            return p.getProfileRiasecScoreMap();
        
        Map<String,Float> out = new HashMap<>();
        
        if( p.getStrParam5()==null || p.getStrParam5().isEmpty() )
        {
            p.setProfileRiasecScoreMap(out);
            return out;
        }
        
        String [] da = p.getStrParam5().split(";");
        
        int onetInterestElementTypeId;
        
        OnetInterestElementType oiet;
        
        String n,v;
        
        for( int i=0;i<da.length-1; i+=2 )
        {
            n=da[i];
            v=da[i+1];
            
            if( n==null )
                continue;
            
            try
            {
                onetInterestElementTypeId = Integer.parseInt( n );
            }
            catch( NumberFormatException ee )
            {
                // All are not numbers. 
                continue;
            }

            oiet = OnetInterestElementType.getValue( onetInterestElementTypeId );
            
            if( oiet==null )
                continue;
            
            // All are not OnetInterestElementType
            out.put(oiet.getName(), Float.parseFloat(v) );
        }
        
        p.setProfileRiasecScoreMap(out);
        
        return out;
    }

    public Map<OnetTrainingExperienceElementType,Integer> getProfileTrainingExperienceCategoryMap( Profile p ) throws Exception
    {
        if( p==null )
            return new HashMap<>();
        
        if( p.getProfileTrainingExperienceCategoryMap()!=null )
            return p.getProfileTrainingExperienceCategoryMap();
        
        Map<OnetTrainingExperienceElementType,Integer> out = new HashMap<>();
        
        if( p.getStrParam5()==null || p.getStrParam5().isEmpty() )
        {
            p.setProfileTrainingExperienceCategoryMap(out);
            return out;
        }
        
        String [] da = p.getStrParam5().split(";");
        
        int onetTrainingExperienceElementTypeId;
        
        OnetTrainingExperienceElementType oiet;
        
        String n,v;
        
        for( int i=0;i<da.length-1; i+=2 )
        {
            n=da[i];
            v=da[i+1];
            
            if( n==null )
                continue;
            
            try
            {
                onetTrainingExperienceElementTypeId = Integer.parseInt( n );
            }
            catch( NumberFormatException ee )
            {
                // All are not numbers. 
                continue;
            }
            
            oiet = OnetTrainingExperienceElementType.getValue( onetTrainingExperienceElementTypeId );
            
            if( oiet==null )
                continue;
            
            out.put(oiet, Integer.valueOf(v) );
        }
        
        p.setProfileTrainingExperienceCategoryMap(out);
        
        return out;
    }

    
    
    
    
    public Map<String,Float> getProfileScoreMap( Profile p ) throws Exception
    {
        Map<String,Float> out = new HashMap<>();
        
        if( p==null || p.getTextParam1()==null || p.getTextParam1().isEmpty() )
            return out;
        
        String [] da = p.getTextParam1().split(";");
        
        String n,v;
        
        for( int i=0;i<da.length-1; i+=2 )
        {
            n=da[i];
            v=da[i+1];
            
            if( n==null )
                continue;
            
            n = URLDecoder.decode(n, "UTF8" );
            
            if( n.length()==0 )
                continue;
            
            out.put(n, Float.parseFloat(v) );
        }
        
        return out;
    }    
    
}
