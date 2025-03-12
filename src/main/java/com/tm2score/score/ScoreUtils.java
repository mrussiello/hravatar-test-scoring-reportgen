/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.Org;
import com.tm2score.event.PercentileScoreType;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.file.BucketType;
import com.tm2score.file.FileXferUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.purchase.ProductType;
import com.tm2score.service.LogService;
import com.tm2score.sim.OverallRawScoreCalcType;
import com.tm2score.sim.OverallScaledScoreCalcType;
import com.tm2score.user.UserFacade;
import jakarta.ejb.EJBException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class ScoreUtils {
    
        
    
    
    
    public static boolean getIncludeRawOverallScore( Org org, TestEvent te )
    {
        //if( 1==2 )
        //    return true;
        
        if( org==null && te!=null )
        {
            if( te.getOrg()==null )
            {
                try
                {
                    te.setOrg( UserFacade.getInstance().getOrg( te.getOrgId() ) );
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "ScoreUtils.getIncludeRawOverallScore() loading org. testEventId=" + te.getTestEventId() );
                }
            }
            
            org = te.getOrg();            
        }
        
        if( org==null || org.getShowOverallRawScore()!=1 )
            return false;

        if( te==null || !ProductType.getValue( te.getProductTypeId() ).getIsSimOrCt5Direct()  || te.getProduct()==null )
            return false;
        
        ConsumerProductType cpt = ConsumerProductType.getValue( te.getProduct().getConsumerProductTypeId() );
        
        if( !cpt.equals( ConsumerProductType.ASSESSMENT_JOBSPECIFIC ) && !cpt.equals( ConsumerProductType.ASSESSMENT_COMPETENCY ) )
            return false;
        
        // At this point it's a job spec test and the org is set to show raw score. 
        // Need to check to make sure that the sim is not set to use z-score transformation.
        //if( ProductType.getValue( te.getProductTypeId() ).getIsSim() )
        //{
            //if( te.getTestEventScoreList()==null )
            //{
            //    try
            //    {
            //        te.setTestEventScoreList( EventFacade.getInstance().getTestEventScoresForTestEvent( te.getTestEventId(), false ) );
            //    }
            //    catch( Exception e )
            //    {
            //        LogService.logIt( e, "ScoreUtils.getIncludeRawOverallScore() loading TestEventScores. " + te.getTestEventId() );
            //        return true;
            //    }
            //}

            //if( te.getOverallTestEventScore()!=null )
            //{
            //    String tp1 = te.getOverallTestEventScore().getTextParam1();
                
             //   if( tp1!=null && tp1.contains( Constants.OVERRIDESHOWRAWSCOREKEY ) )
             //       return false;
            //}
            
            // if( te.getSimXmlObj()==null )
            //{
            //    try
            //    {
            //        SimDescriptor sd = EventFacade.getInstance().getSimDescriptor(te.getSimId(), te.getSimVersionId(), true );

            //        if( sd == null )
            //            throw new Exception( "No sim descriptor found for simId=" + te.getSimVersionId() + ", " + te.toString() );

            //        te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ) );                
            //    }
                
            //    catch( Exception e )
            //    {
            //        LogService.logIt( e, "ScoreUtils.getIncludeRawOverallScore() loading sim descriptor. " + te.getTestEventId() );
            //        return true;
            //    }
            //}
            
            //if( te.getSimXmlObj()!=null && 
            //    OverallScaledScoreCalcType.getValue(  te.getSimXmlObj().getOverallscorecalctype()).getIsTransform() && 
            //    OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype()).getUsesRawCompetencyScores() )
            //    return false;
        //}
        
        return true;
    }
    
    
    public static float getEquivalentZScore( float score, float mean, float std )
    {
        return std>0 ? (score - mean)/std : 0;
    }
    
    
    
    
    public static int getScoreCategoryTypeId( SimJ.Simcompetency simCompetencyObj, float scaledScore, ScoreColorSchemeType scst )
    {
        ScoreCategoryType scoreCat = ScoreCategoryType.getForScore(scst,
                                                                    scaledScore,
                                                                    simCompetencyObj.getHighcliffmin(), 
                                                                    simCompetencyObj.getWhitemin(),
                                                                    simCompetencyObj.getGreenmin(),
                                                                    simCompetencyObj.getYellowgreenmin(),
                                                                    simCompetencyObj.getYellowmin(),
                                                                    simCompetencyObj.getRedyellowmin(), 
                                                                    simCompetencyObj.getRedmin(),
                                                                    0,
                                                                    simCompetencyObj.getCategorydisttype(),
                                                                    simCompetencyObj.getHighclifflevel() );

        if( simCompetencyObj.getCategoryadjustmentthreshold()>0 && scaledScore <= simCompetencyObj.getCategoryadjustmentthreshold() )
            scoreCat = scoreCat.adjustOneLevelUp( scst );

        return scoreCat.getScoreCategoryTypeId();
    }

    public static int getPercentileScoreTypeIdForTestEvent(TestEvent te) {
        if (te == null || 
            (te.getProductTypeId()!=ProductType.SIM.getProductTypeId() && te.getProductTypeId()!=ProductType.CT5DIRECTTEST.getProductTypeId()) || te.getSimXmlObj() == null) {
            // (te.getProductTypeId() != ProductType.SIM.getProductTypeId() && te.getProductTypeId() != ProductType.IFRAMETEST.getProductTypeId() && te.getProductTypeId() != ProductType.IVR.getProductTypeId() && te.getProductTypeId() != ProductType.VOT.getProductTypeId()) || te.getSimXmlObj() == null) {
            return PercentileScoreType.LEGACY.getPercentileScoreTypeId();
        }
        if (OverallScaledScoreCalcType.getValue(te.getSimXmlObj().getOverallscaledscorecalctype()).getIsTransform() && OverallRawScoreCalcType.getValue(te.getSimXmlObj().getOverallscorecalctype()).getUsesRawCompetencyScores()) {
            return PercentileScoreType.WEIGHTED_AVG_ZSCORES.getPercentileScoreTypeId();
        }
        return PercentileScoreType.LEGACY.getPercentileScoreTypeId();
    }
    
    
    
    /**
     * map of topic name, int[]
     *    int[0] = number correct
     *    int[1] = number total this topic.
     *    int[2] = number of items that were partially correct.
     *    int[3] = total number of items this topic. 
     */
    public static Map<String,int[]> getSingleTopicTopicMap( String topic, boolean correct, boolean partial )
    {
        if( topic==null || topic.trim().isEmpty() )
            topic = "NOTOPIC";
        
        topic= topic.trim();
        
        Map<String,int[]> out = new HashMap<>();
        
        int[] data = out.get(topic);
        
        if( data==null )
            data = new int[4];
        
        if( correct)
            data[0]++;
        
        else if( partial )
            data[2]++;
        
        // total items.
        data[1]++;
        
        // Total items
        data[3]++;
        
        out.put( topic, data ); 
        
        return out;
        
    }
    
    
    public static String getS3UrlFromAudioUri( String audioUri )
    {
        if( audioUri!=null && !audioUri.isBlank() && !audioUri.toLowerCase().startsWith("http") )
        {
            if( audioUri.startsWith("/") )
                audioUri=audioUri.substring(1,audioUri.length());
            return "s3://" + BucketType.USERUPLOAD.getBucket() + "/" + BucketType.USERUPLOAD.getBaseKey() + audioUri;
        }
        
        return audioUri;
    }
    
    public static String getUrlFromAudioUri( String audioUri ) throws Exception
    {
        // googleUri = iir.getAudioUri();
        if( RuntimeConstants.getBooleanValue("useAwsMediaServer") && RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") && audioUri!=null && !audioUri.isBlank() && !audioUri.toLowerCase().startsWith("http") )
        {
            int dx = audioUri.lastIndexOf("/");
            String dir = dx<0 ?  "" : audioUri.substring(0, dx );
            if( dir.endsWith("/") )
                dir = dir.substring(0, dir.length()-1 );
            String fn = dx<0 ? audioUri : audioUri.substring(dx+1, audioUri.length() );

            return FileXferUtils.getPresignedUrlAws( dir, fn,BucketType.USERUPLOAD.getBucketTypeId(), null, 15 );
        }

        else if( !audioUri.toLowerCase().startsWith("http") )
        {
            if( audioUri.startsWith( "/" ))
                audioUri=audioUri.substring(1, audioUri.length() );
            
            if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                return RuntimeConstants.getStringValue("awsS3BaseUrl")  + BucketType.USERUPLOAD.getBucket() + "/" + BucketType.USERUPLOAD.getBaseKey() + audioUri;
            else
                return RuntimeConstants.getStringValue("uploadedUserFileBaseUrl") + "/" + audioUri;
                
        }

        else
            return audioUri;
    }
    
    public static int getExceptionPermanancy(Throwable e)
    {
        return isExceptionPermanent(e) ? 1 : 0;
    }
    
    public static boolean isExceptionPermanent(Throwable e)
    {
        if( nonPermanentException( e ) )
        {
            LogService.logIt( "StandardTestEventScorer.isExceptionPermanent() Exception is non-permanent: " + e.toString() );
            return false;
        }
        
        Throwable t = getCause(e);
        if( t!=null && nonPermanentException( t ))
        {
            LogService.logIt( "StandardTestEventScorer.isExceptionPermanent() Exception is non-permanent: " + e.toString() + ", caused by " + t.toString() );
            return false;
        }

        return true;        
    }
    
    private static boolean nonPermanentException( Throwable e )
    {
        if( e==null )
            return true;
        
        return e.toString().contains("OutOfMemoryError") || e instanceof SQLException || e instanceof EJBException;
    }
    
    static Throwable getCause(Throwable e) 
    {
        Throwable cause = null; 
        Throwable result = e;

        while(null != (cause = result.getCause())  && (result != cause) ) 
        {
            result = cause;
        }
        return result;
    }    
    
    
    
}
