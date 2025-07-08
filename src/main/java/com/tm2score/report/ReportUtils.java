/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.custom.coretest.Activity;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.EventFacade;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.service.LogService;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.event.NormFacade;
import com.tm2score.file.BucketType;
import com.tm2score.file.FileXferUtils;
import com.tm2score.file.MediaTempUrlSourceType;
import com.tm2score.file.UploadedUserFileType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.purchase.ProductType;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.service.EncryptUtils;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimJUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.NVPair;
import com.tm2score.util.STStringTokenizer;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.ClipHist;
import com.tm2score.xml.JaxbUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class ReportUtils
{
    // @Inject
    EventFacade eventFacade;
    
    NormFacade normFacade;

    
    /**
     * Returns data[0] = percentile
     *         data[1] = count
     *         data[2] = percentileorg
     *         data[3] = countorg
     *         data[4] = percentilecc
     *         data[5] = countcc
     * 
     * @param productId
     * @param testEvent
     * @param tes
     * @param orgId
     * @param countryCode
     * @param custom1
     * @return
     * @throws Exception 
     */
    public float[] getCustomPercentile( int productId, TestEvent testEvent, TestEventScore tes, int orgId, String countryCode, String custom1, String custom2, String custom3) throws Exception
    {
        if( normFacade == null )
            normFacade = NormFacade.getInstance();
        
        int minSimVersionIdForMajorVersion = 0;
        
        if( testEvent.getSimId()>0 && (testEvent.getProductTypeId()==ProductType.SIM.getProductTypeId() || testEvent.getProductTypeId()==ProductType.CT5DIRECTTEST.getProductTypeId()) )
        {
            minSimVersionIdForMajorVersion = 1;
            
            if( testEvent.getSimXmlObj()==null )
            {
                if( eventFacade==null )
                    eventFacade = EventFacade.getInstance();
                
                
                testEvent.setSimDescriptor( eventFacade.getSimDescriptor( testEvent.getSimId(), testEvent.getSimVersionId(), false ));
                testEvent.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( testEvent.getSimDescriptor().getXml() ));
            }
            
            if( testEvent.getSimXmlObj().getMinsimveridformajorversion()>0 )
                minSimVersionIdForMajorVersion = testEvent.getSimXmlObj().getMinsimveridformajorversion();
            
        }
        
        
        Map<String,Object> data = normFacade.getPercentile(productId, 0, testEvent.getSimId(), testEvent.getSimVersionId(), testEvent.getTestEventId(), minSimVersionIdForMajorVersion, tes, orgId, countryCode, custom1, custom2, custom3 );
        
        float[] out = new float[6];
        
        if( data == null )
            return out;
        
        Float p = (Float) data.get("percentile");        
        out[0] = p==null ? 0 : p.floatValue();        
        Integer tt = (Integer) data.get( "count" );        
        out[1] = tt==null ? 0 : tt.floatValue();

        p = (Float) data.get("percentileorg");        
        out[2] = p==null ? 0 : p.floatValue();        
        tt = (Integer) data.get( "countorg" );        
        out[3] = tt==null ? 0 : tt.floatValue();
        
        p = (Float) data.get("percentilecc");        
        out[4] = p==null ? 0 : p.floatValue();        
        tt = (Integer) data.get( "countcc" );        
        out[5] = tt==null ? 0 : tt.floatValue();
        
        return out;
        
    }

    
    public static String getMediaTempUrlSourceLink( int orgId, UploadedUserFile uuf, int thumbIndex, String fn, MediaTempUrlSourceType mediaTempUrlSourceType )
    {
        if( orgId<=0 || uuf==null || mediaTempUrlSourceType==null )
            return "";
        
        if( !RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") )
            return getUploadedUserFileThumbUrl( uuf, fn );
        
        try
        {
            return RuntimeConstants.getStringValue("adminappbasuri") + RuntimeConstants.getStringValue("mediaTempUrlSourcePath") + "/" + mediaTempUrlSourceType.getMediaTempUrlSourceTypeId() + "/" + thumbIndex + "/" + EncryptUtils.urlSafeEncrypt(orgId) + "/" + EncryptUtils.urlSafeEncrypt(uuf.getUploadedUserFileId() ) + "/" + fn;
        }   
        catch( Exception e )
        {
            LogService.logIt( e, "ReportUtils.getMediaTempUrlSourceLink() orgId=" + orgId + ", uploadedUserFileId=" + uuf.getUploadedUserFileId() + ", filename=" + fn );
            return getUploadedUserFileThumbUrl( uuf, fn );
        }
    }
    
    public static String getUploadedUserFileThumbUrl( UploadedUserFile uuf, String fn )
    {
        // String thumbUrl = null;
        UploadedUserFileType uft = uuf.getUploadedUserFileType();
        BucketType bt = BucketType.USERUPLOAD;
        boolean aws = false;
        
        if( uft.getIsResponse() )
        {
            bt = BucketType.USERUPLOAD;
            aws = RuntimeConstants.getBooleanValue("useAwsMediaServer");
        }
        else if( uft.getIsAnyPremiumRemoteProctoring() )
        {
            bt = RuntimeConstants.getBooleanValue("useTestFoldersForProctorRecordings") ? BucketType.PROCTORRECORDING_TEST : BucketType.PROCTORRECORDING;
            aws = RuntimeConstants.getBooleanValue("useAwsForProctorRecording");            
        }
        
        if( aws )
        {
            String dir = uuf.getDirectory();
            if( dir.startsWith("/") )
                dir = dir.substring(1, dir.length() );
            if( RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") )
            {
                try
                {
                    return FileXferUtils.getPresignedUrlAws( dir, fn, bt.getBucketTypeId(), null, RuntimeConstants.getIntValue( "awsTempUrlMinutes") );
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "ReportUtils.getUploadedUserFileThumbUrl() dir=" + dir + ", filename=" + fn  );
                    return "";
                }
            }

            // Normal Method.
            return RuntimeConstants.getStringValue( "awsS3BaseUrl") + bt.getBucket() + "/" + bt.getBaseKey() + dir + "/" + fn;
        }
        
        // Not AWS
        return RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + fn;           
    }
    
    public static String getTestNameToUseInReporting( TestEvent te, Product p, Locale rLoc )
    {
            String n = p.getName();

            // Test is in English.
            if(  rLoc.getLanguage().equalsIgnoreCase("en") &&  p.getNameEnglish()!=null && !p.getNameEnglish().isEmpty()  )
            {
                // Either Sim or Product is not english.
                if(  p.getLangStr()!=null && !I18nUtils.getLanguageFromLocaleStr(p.getLangStr()).equalsIgnoreCase("en") )
                   n = p.getNameEnglish();
            }

            return n;
    }
    
    
    
    public static String getCompetencyNameToUseInReporting( TestEvent te, TestEventScore tes, SimJ simJ, Product p, Locale rLoc )
    {
            String cns = tes.getName();

            String simLang = simJ!=null ? simJ.getLang() : null;

            // Test is in English.
            if(  rLoc.getLanguage().equalsIgnoreCase("en") &&  tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty()  )
            {
                // Either Sim or Product is not english.
                if( (simLang!=null && !simLang.toLowerCase().startsWith("en")) || ( p!=null && p.getLangStr()!=null && !I18nUtils.getLanguageFromLocaleStr(p.getLangStr()).equalsIgnoreCase("en") ) )
                   cns = tes.getNameEnglish();
            }

            return cns;
    }
    
    
    public static String getScoreTextFromStr( String scrTxt )
    {
        return getKeyValueFromStr( scrTxt, Constants.SCORETEXTKEY );
    }
    
    public static String getScoreValueFromStr( String scrTxt )
    {
        return getKeyValueFromStr( scrTxt, Constants.SCOREVALUEKEY );
    }

    private static boolean getScoreTextStrHasAnyKey( String scrTxt )
    {
        return IvrStringUtils.containsKey(Constants.SCOREVALUEKEY, scrTxt, true) || IvrStringUtils.containsKey(Constants.SCORETEXTKEY, scrTxt, true) ;
    }

    
    private static String getKeyValueFromStr( String scrTxt, String key )
    {
        if( scrTxt==null )
            return scrTxt;
        
        scrTxt = scrTxt.trim();
        
        if( scrTxt.isEmpty() )
            return scrTxt;

        // Key not present.
        if( !IvrStringUtils.containsKey(key, scrTxt, false ) )
        {
            // If any other key is present, just return empty.
            if( getScoreTextStrHasAnyKey( scrTxt ) )
                return "";
            
            return scrTxt;
        }
        
        // Score text can be divided between the text to use as the score value and actual score text. 

        // check to see if a key is present.
        String sv = IvrStringUtils.getTagValue(scrTxt, key );

        // If key value was present use it.
        if( sv!=null )
            return sv.trim();
        
        // no key value. Use 
        else
            return "";        
    }


    
    public static String getReportFlagStringValue( String name, TestKey tk, Product p, Suborg s, Org o, Report r)
    {
        String v = null;
        
        if( tk!=null )
        {
            v = tk.getCustomParameterValue( name );
            
            if( v!=null )
                return v;
        }
        
        NVPair pr = null;
        
        if( s != null )
        {
            pr = StringUtils.getNVPairFromList(name, s.getReportFlags(), "|");
            
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if( v!=null && !v.equals( "0") )
                    return v;
            }
        }
        
        if( o != null )
        {
            pr = StringUtils.getNVPairFromList(name, o.getReportFlags(), "|");
            
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if( v!=null )
                    return v;
            }
        }

        if( p != null )
        {
            pr = StringUtils.getNVPairFromList(name, p.getStrParam11(), "|");
            
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if( v!=null )
                    return v;
            }
        }
        // LogService.logIt( "ReportUtils.getReportFlagStringValue() XXX" );
        
        if( r != null )
        {
            // LogService.logIt( "ReportUtils.getReportFlagStringValue() AAA" );
            
            pr = StringUtils.getNVPairFromList( name, r.getReportFlags(), "|");
            
            // LogService.logIt( "ReportUtils.getReportFlagStringValue() XXX pr: " + (pr!=null) + ", ");
            if( pr!=null )
            {
                v = (String) pr.getValue();
                
                if(v!=null )
                    return v;
            }
        }
        
        // LogService.logIt( "ReportUtils.getReportFlagStringValue() XXX returning null " );
        return null;        
    }

    
    
    /**
     * Returns null
     * @param name
     * @param tk
     * @param s
     * @param o
     * @param r
     * @return 
     */
    public static Integer getReportFlagIntValue( String name, TestKey tk, Product p, Suborg s, Org o, Report r)
    {
        String v = getReportFlagStringValue(name, tk, p, s, o, r );
        
        if( v==null || v.isEmpty() )
            return 0;
        
        return getInteger( v );
    }

    public static float getReportFlagFloatValue( String name, TestKey tk, Product p, Suborg s, Org o, Report r)
    {
        String v = getReportFlagStringValue(name, tk, p, s, o, r );
        
        return getFloat( v );
    }

    public static boolean getReportFlagBooleanValue( String name, TestKey tk, Product p, Suborg s, Org o, Report r)
    {
        Integer v = getReportFlagIntValue(name, tk, p, s, o, r );
        
        // LogService.logIt( "ReportUtils.getReportFlagBooleanValue() name=" + name + ", v=" + v );
        
        return v!=null && v==1;
    }

    

    private static Integer getInteger( String s )
    {
        if( s==null || s.isEmpty() )
            return  null;
        
        try
        {
            int i = Integer.parseInt( s );
            
            return i; //  new Integer(i);
        }
        catch( Exception e )
        {
            LogService.logIt( "ReportUtils.getInteger() Unable to parse string to integer: " + s + ", " + e.toString() );            
        }
        return null;
    }


    private static Float getFloat( String s )
    {
        if( s==null || s.isEmpty() )
            return null;
        
        try
        {
            float i = Float.parseFloat( s );
            
            return i;
        }
        catch( Exception e )
        {
            LogService.logIt( "ReportUtils.getInteger() Unable to parse string to integer: " + s + ", " + e.toString() );            
        }
        return null;
    }
    
    
    public static List<CaveatScore> getTopicCaveatScoreListFromLegacyCaveatStr( String cl )
    {
        List<CaveatScore> out = new ArrayList<>();

        if( cl==null || cl.isEmpty() )
            return out;

        
        //String cl = StringUtils.getBracketedArtifactFromString( textParam1 , Constants.CAVEATSKEY );
        //if( cl == null || cl.isEmpty() )
        //    return out;

        STStringTokenizer st = new STStringTokenizer( cl , Constants.DELIMITER );

        STStringTokenizer tt;
        String[] t;

        while( st.hasMoreTokens() )
        {
            t = new String[5];
            t[0] = st.nextToken();
            if( t[0].contains("TOPIC") )
            {
                tt = new STStringTokenizer( t[0] , "~" );
                if( tt.hasMoreTokens() )
                    t[0]=tt.nextToken();  // TOPIC                
                if( tt.hasMoreTokens() )
                    t[1]=tt.nextToken();  // Topic name or NOTOPIC
                if( tt.hasMoreTokens() )
                    t[2]=tt.nextToken();  // T1
                if( tt.hasMoreTokens() )
                    t[3]=tt.nextToken();  // T2
                if( tt.hasMoreTokens() )
                    t[4]=tt.nextToken();  // T3

                // LogService.logIt( "Parse tokens. TOPIC=" + t[0] + ", name=" + t[1] + ", t1=" + t[2]  + ", t2=" + t[3] + ", t3=" + t[4]);

                out.add( new CaveatScore(out.size()+1, CaveatScoreType.TOPIC_CORRECT.getCaveatScoreTypeId(), Integer.parseInt(t[2]), Integer.parseInt(t[3]), Integer.parseInt(t[4]), t[1], null ) );                
            }
            
        }

        return out;
    }

    public static List<CaveatScore> getLegacyCaveatScoreListFromLegacyCaveatStr( String cl )
    {
        List<CaveatScore> out = new ArrayList<>();

        if( cl == null || cl.isEmpty() )
            return out;

        //String cl = StringUtils.getBracketedArtifactFromString( textParam1 , Constants.CAVEATSKEY );
        //if( cl == null || cl.isEmpty() )
        //    return out;

        STStringTokenizer st = new STStringTokenizer( cl , Constants.DELIMITER );

        String t;

        while( st.hasMoreTokens() )
        {
            t = st.nextToken();
            if( t!=null && !t.isBlank() )
                out.add( new CaveatScore(out.size()+1, CaveatScoreType.LEGACY_STRING.getCaveatScoreTypeId(), t, null ) );
        }

        return out;
    }

    
    
    /**
     * Returns LIST of:
     * 
     *   data[0]=Key - normally TOPIC
     *   data[1]=Topic Name
     *   data[2]=Value
     * 
     * @param cl
     * @param l
     * @return 
     *
    public static List<String[]> getParsedTopicScores( List<String> cl, Locale l, int simCompetencyClassId)
    {
        List<String[]> out = new ArrayList<>();
        
        List<String> tl = new ArrayList<>();
        
        for( String c : cl )
        {
            if( c.startsWith( Constants.TOPIC_KEY + "~" ) )
                tl.add( c );            
        }
        
        for( String c : tl )
            out.add(parseTopicCaveatStr(c, l, tl.size()==1, simCompetencyClassId ) );
        
        return out;
    }
    */

    /**
     * Returns LIST of:
     * 
     *   data[0]=Key - normally TOPIC
     *   data[1]=Topic Name
     *   data[2]=Value
     * 
     * @param cl
     * @param l
     * @return 
     */
    public static List<String[]> getParsedTopicScoresForCaveatScores( List<CaveatScore> cl, Locale l, int simCompetencyClassId)
    {
        List<String[]> out = new ArrayList<>();
        
        List<CaveatScore> tl = new ArrayList<>();
        
        for( CaveatScore c : cl )
        {
            if( c.getCaveatScoreType().getIsTopic() ) // Constants.TOPIC_KEY + "~" ) )
                tl.add( c );            
        }
        
        for( CaveatScore c : tl )
        {
            c.setLocale(l);
            
            out.add(parseTopicCaveatScore(c, tl.size()==1, simCompetencyClassId ) );
        }
        
        return out;
    }

    
    /**
     * format of inStr is KEY~S1~S2~S3
     * 
     * Returns
     *   data[0]=Key - normally TOPIC
     *   data[1]=Topic Name
     *   data[2]=Value
     */
    public static String[] parseTopicCaveatScore( CaveatScore cs, boolean isOneLine, int simCompetencyClassId)
    {
        // LogService.logIt( "ReportUtils.parseTopicCaveatStr() inStr=" + inStr );
                    
        String[] out = new String[3];
                
        if( cs==null || !cs.getHasValidInfo() )
            return out;
        
        String[] ct = new String[]{ "TOPIC", cs.getStrValue(), cs.getValueAsStr(),cs.getValue2AsStr(),cs.getValue3AsStr() };
        int partial = cs.getValue3Int();
        
        String stub = "";
        
        if( simCompetencyClassId>=0 )
            stub = SimCompetencyClass.getValue( simCompetencyClassId ).getTopicCorrectStub();
          
        out[0]="TOPIC";

        out[1] = cs.getStrValue().equals( "NOTOPIC" ) ? MessageFactory.getStringMessage( cs.getLocale(), isOneLine ? "g.CaveatOneTopicName" : "g.CaveatGeneralTopic" ) : ct[1];

        // either there is no partially correct items or this is an old score.
        if( partial<=0 ) 
            out[2] = MessageFactory.getStringMessage( cs.getLocale(), "g.CaveatXofYCorrect" + stub, ct ); 

        // Has Partials!
        else
            out[2] = MessageFactory.getStringMessage( cs.getLocale(), "g.CaveatXofYCorrectWithPartial" + stub, ct ); 
        
        return out;        
    }
    
    
    
    /**
     * format of inStr is KEY~S1~S2~S3
     * 
     * Returns
     *   data[0]=Key - normally TOPIC
     *   data[1]=Topic Name
     *   data[2]=Value
     *
    public static String[] parseTopicCaveatStr( String inStr, Locale locale, boolean isOneLine, int simCompetencyClassId)
    {
        // LogService.logIt( "ReportUtils.parseTopicCaveatStr() inStr=" + inStr );
                    
        String[] out = new String[3];
                
        if( inStr==null || inStr.isEmpty() )
            return out;
        
        String[] ct = inStr.split("~");
        
        int partial = 0;
        
        String stub = "";
        
        if( simCompetencyClassId>=0 )
            stub = SimCompetencyClass.getValue( simCompetencyClassId ).getTopicCorrectStub();
        
        if( ct[0]!=null && ct.length>=4 )
        {
            out[0]=ct[0];
            
            out[1] = ct[1].equals( "NOTOPIC" ) ? MessageFactory.getStringMessage( locale, isOneLine ? "g.CaveatOneTopicName" : "g.CaveatGeneralTopic" ) : ct[1];

            if( ct.length>4 && ct[4]!=null && !ct[4].isEmpty() ) 
                partial = Integer.parseInt(ct[4]);
            
            // either there is no partially correct items or this is an old score.
            if( partial<=0 ) 
                out[2] = MessageFactory.getStringMessage( locale, "g.CaveatXofYCorrect" + stub, ct ); 
            
            // Has Partials!
            else
                out[2] = MessageFactory.getStringMessage( locale, "g.CaveatXofYCorrectWithPartial" + stub, ct ); 
                
        }
        
        return out;        
    }
    */

    
    
    public static void swapTopicNamesInCaveatScores( Map<String,String> equivTopicNameMap, List<CaveatScore> topicCsl )
    {
        // In order to swap we need both SimJs and we need some topics. 
        if( equivTopicNameMap==null || topicCsl==null || topicCsl.isEmpty() )
            return;
                
        String tn2;
        
        for( CaveatScore t : topicCsl )
        {
            if( t.getStrValue()!=null && ! t.getStrValue().isEmpty() )
            {
                tn2 = equivTopicNameMap.get(  t.getStrValue() );
                
                // Swap if found.
                if( tn2!=null && !tn2.isEmpty() )
                    t.setStrValue( tn2 );
            }
        }        
    }
    
    public static void swapTopicNames( Map<String,String> equivTopicNameMap, List<String[]> topicCaveatListIn )
    {
        // In order to swap we need both SimJs and we need some topics. 
        if( equivTopicNameMap==null || topicCaveatListIn==null || topicCaveatListIn.isEmpty() )
            return;
                
        String tn2;
        
        for( String[] t : topicCaveatListIn )
        {
            if( t[1]!=null && !t[1].isEmpty() )
            {
                tn2 = equivTopicNameMap.get( t[1] );
                
                // Swap if found.
                if( tn2!=null && !tn2.isEmpty() )
                    t[1] = tn2;
            }
        }        
    }
    
    
    public static Map<String,String> createEquivTopicNameMap( SimJUtils simJUtils, SimJUtils equivSimJUtils )
    {
        Map<String,String> out = new HashMap<>();
        
        if( equivSimJUtils==null || simJUtils==null )
            return out;
        
        SimJ.Intn eqIntn;
        SimJ.Intn.Intnitem eqIitm;
        
        String tn;
        String tn2;
        
        for( SimJ.Intn intn : simJUtils.getSimJ().getIntn() )
        {
            if( intn.getUniqueid()==null || intn.getUniqueid().isEmpty() )
                continue;
            
            eqIntn = null;
            
            for( SimJ.Intn ie : equivSimJUtils.getSimJ().getIntn() )
            {
                if( ie.getUniqueid()!=null && !ie.getUniqueid().isEmpty() && ie.getUniqueid().equalsIgnoreCase( intn.getUniqueid() ))
                {
                    eqIntn=ie;
                    break;
                }  
            }
                        
            if( eqIntn==null )
                continue;
            
            // Interaction Level
            tn = IvrStringUtils.getTagValueWithDecode( intn.getTextscoreparam1(), Constants.TOPIC_KEY );
            
            if( tn!=null && !tn.isEmpty() )
            {
                tn2 = IvrStringUtils.getTagValueWithDecode( eqIntn.getTextscoreparam1(), Constants.TOPIC_KEY );
                
                if( tn2!=null && !tn2.isEmpty() )
                    out.put( tn,tn2 );
            }
            
            // Interaction Item Level
            for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
            {
                tn = IvrStringUtils.getTagValueWithDecode( iitm.getTextscoreparam1(), Constants.TOPIC_KEY );
            
                if( tn!=null && !tn.isEmpty() )
                {
                    eqIitm=null;
                    
                    for( SimJ.Intn.Intnitem ieitm : eqIntn.getIntnitem() )
                    {
                        if( ieitm.getSeq()==iitm.getSeq() )
                        {
                            eqIitm = ieitm;
                            break;
                        }
                        
                    }
                    
                    if( eqIitm==null )
                        continue;
                        
                    tn2 = IvrStringUtils.getTagValueWithDecode( eqIitm.getTextscoreparam1(), Constants.TOPIC_KEY );

                    if( tn2!=null && !tn2.isEmpty() )
                        out.put( tn,tn2 );
                }
            }
        }
        
        return out;
    }

    
    
    
    /**
     * Uses simletId, simletVersionId, simletNodeId, and iden
     * @param ir
     * @return
     * @throws Exception 
     */
    public float[] getAverageItemScoreForItemScore( ItemResponse ir ) throws Exception
    {
        if( eventFacade == null ) 
            eventFacade = EventFacade.getInstance();
        
        return eventFacade.getAverageItemScoreForItemScore( ir );        
    }
    
   

    public List<ItemResponse> getItemResponsesForTestEvent( TestEvent te, boolean archiveOk) throws Exception
    {
        if( eventFacade == null ) 
            eventFacade = EventFacade.getInstance();
        
        List<ItemResponse> irl = eventFacade.getItemResponsesForTestEvent( te.getTestEventId() );
        
        if( archiveOk && (irl.isEmpty()) )
            irl.addAll( eventFacade.getItemResponseArchivesForTestEvent(te.getTestEventId()));
        
        return irl;
    }
    
    public void loadTestEventSimXmlObject( TestEvent te ) throws Exception
    {

        if( te.getSimXmlObj() == null )
        {
            if( te.getSimDescriptor() == null )
            {
                // Product
                if( te.getProduct() == null )
                {
                    if( eventFacade == null ) 
                        eventFacade = EventFacade.getInstance();

                    te.setProduct( eventFacade.getProduct( te.getProductId() ));

                    if( te.getProduct() == null )
                        throw new Exception( "ReportUtils.loadTestEventSimXmlObject() Cannot find product for TestEvent. " + te.toString() );
                }

                if( eventFacade == null ) 
                    eventFacade = EventFacade.getInstance();
                // get the SimDescriptor and it's object.
                
                te.setSimDescriptor( eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), true ) );
            }

            te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( te.getSimDescriptor().getXml() ) );
        }
    }

    public void loadTestEventResultXmlObject( TestEvent te ) throws Exception
    {
        if( te.getResultXml() == null || te.getResultXml().isEmpty() )
            return;

        if( te.getResultXmlObj() == null )
            te.setResultXmlObj( JaxbUtils.ummarshalImoResultXml( te.getResultXml() ) );
    }

    public List<Activity> getActivityList( TestEvent te ) throws Exception
    {
        loadTestEventSimXmlObject( te );

        loadTestEventResultXmlObject( te );

        List<Activity> out = new ArrayList<>();

        if( te.getResultXmlObj()==null )
            return out;

        Clicflic.History h = te.getResultXmlObj().getHistory();

        List<String> clipSegList = new ArrayList<>();

        String t;
        int level;

        Clicflic.History.Clip resultClipO;
        ClipHist resultClip;
        
        
        for( Object o : te.getResultXmlObj().getHistory().getIntnOrClip())
        {
            if( o instanceof Clicflic.History.Intn )
                continue;
            
            resultClipO = (Clicflic.History.Clip)o;            
            resultClip = new ClipHist(resultClipO);
            
            // avoid repeats.
            if( clipSegList.contains( resultClip.getNdseq() + "-" + resultClip.getSnseq() ) )
                continue;

            clipSegList.add( resultClip.getNdseq() + "-" + resultClip.getSnseq() );

            for( SimJ.Clip clip : te.getSimXmlObj().getClip() )
            {
                if( clip.getSeq() == resultClip.getNdseq() )
                {
                    for( SimJ.Clip.Clipseg cseg : clip.getClipseg() )
                    {
                        if( cseg.getSeq() == resultClip.getSnseq() )
                        {
                            if( cseg.getActivitysummary() != null && !cseg.getActivitysummary().isEmpty() )
                            {
                                t = cseg.getActivitysummary();

                                t = t.trim();

                                if( t.isEmpty() )
                                    continue;

                                try
                                {
                                    t = UrlEncodingUtils.decodeKeepPlus(t, "UTF8" );
                                }
                                catch( Exception e )
                                {
                                    LogService.logIt( e, "ReportUtils.getActivityList() Unable to decode Activity: " + t + ", " + te.toString() );
                                    continue;
                                }

                                if( !t.startsWith( "[1]" ) && !t.startsWith( "[2]" ) && !t.startsWith( "[3]" ) )
                                    t = "[1]" + t;

                                if( t.startsWith( "[1]" ) )
                                    level = 1;
                                else if( t.startsWith( "[2]" ) )
                                    level = 2;
                                else if( t.startsWith( "[3]" ) )
                                    level = 3;
                                else
                                    level = 1;

                                t = t.substring(3, t.length() );

                                out.add( new Activity( level, t ) );
                            }
                        }
                    }
                }
            }
        }

        return out;
    }


}
