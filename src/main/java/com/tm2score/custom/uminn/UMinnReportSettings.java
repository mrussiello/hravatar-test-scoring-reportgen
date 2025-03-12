/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.CT2ReportSettings;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportSettings;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public abstract class UMinnReportSettings extends CT2ReportSettings implements ReportSettings {

    // Map<String,String> customSettingsMap;
    
    static Map<String,float[]> averageScoreCache = null;
    
    static int MIN_COUNT_FOR_CACHE = 100;
    
    public String logoUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Q8vQJ8K3q0E-/img_8x1481054844094.png";    
    public String pcmUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Q8vQJ8K3q0E-/img_9x1481054844781.png";    
    public String headerLogoUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Q8vQJ8K3q0E-/img_12x1481102616782.png";     
    public String headerLogoWhiteTransUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Q8vQJ8K3q0E-/img_12x1481102616782.png";    
    public String twitterLogoUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Q8vQJ8K3q0E-/img_13x1481106795247.jpg";
    public String uminnMaroonColStr = "7a0019";        
    public BaseColor uminnMaroon = new BaseColor( 0x7a, 0x00, 0x19 );    
    public BaseColor uminnMaroonLite = new BaseColor( 0xf5, 0xb7, 0xc3 );  // f5b7c3
    public boolean useAltusLangKeys = false;
    public String altusKeySuffix = "";
    
    Image custLogo;
    Image twitterLogo;    
    Image headerLogo;
    
    static Image pcmImage;
    static Image headerLogoWhiteTrans;

    public BaseFont baseFontBoldArial;
    
    public Font fontXLargeMaroon;
    public Font fontXLargeBoldMaroon;
    public Font fontXXLargeBoldMaroon;
    public Font headerFontXLargeMaroon;
    public Font headerFontLargeMaroon;
    public Font headerFontXXLargeMaroon;
    
    public Font titleHeaderFont;
    public Font titleHeaderFont2;
    
    public Font fontSmallLightItalicMaroon;

    public int XPLFONTSZ = 20;
    
    public List<UMinnCompetency> competencyList;
    
    public float[] custom1PercentileData;
    
    public ReportData reportData = null;
    
    private UMinnReportUtils uminnReportUtils;
    
    protected ReportUtils reportUtils;
    
    
    protected List<UMinnScene> uminnSceneList;
     
     
    public abstract void initForSource();
    

    public void initExtra(ReportData reportData) throws Exception 
    {        
        initForSource();
        
        String filesRoot = RuntimeConstants.getStringValue("filesroot") + "/coretest/fonts/";        
        baseFontBoldArial = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        
        
        titleHeaderFont = new Font(baseFontCalibri, 22);
        titleHeaderFont.setColor(BaseColor.WHITE);

        titleHeaderFont2 = new Font(baseFontBoldArial, 20);
        titleHeaderFont2.setColor(BaseColor.WHITE);

        fontXLargeMaroon = new Font(baseFontCalibri, XPLFONTSZ);
        fontXLargeBoldMaroon = new Font(baseFontCalibriBold, XPLFONTSZ, Font.NORMAL);
        fontXXLargeBoldMaroon = new Font(baseFontCalibriBold, XXLFONTSZ, Font.NORMAL);

        headerFontLargeMaroon = new Font(headerBaseFont, LFONTSZ);
        headerFontXLargeMaroon = new Font(headerBaseFont, XPLFONTSZ);
        headerFontXXLargeMaroon = new Font(headerBaseFont, XXLFONTSZ);

        fontSmallLightItalicMaroon = new Font(baseFontCalibriItalic, XSFONTSZ);



        fontXLargeMaroon.setColor(uminnMaroon);
        fontXLargeBoldMaroon.setColor(uminnMaroon);



        headerFontLargeMaroon.setColor(uminnMaroon);
        headerFontXLargeMaroon.setColor(uminnMaroon);
        headerFontXXLargeMaroon.setColor(uminnMaroon);
        fontSmallLightItalicMaroon.setColor(uminnMaroon);

        ct2Colors.pageBgColor = uminnMaroon;
        ct2Colors.hraBaseReportColor = uminnMaroon;

        ct2Colors.headerDarkBgColor = uminnMaroon;

        ct2Colors.scoreBoxShadeBgColor = uminnMaroonLite;

        ct2Colors.barGraphCoreShade1 = new BaseColor( 0x91, 0x21, 0x38 ); // f68d2f // new BaseColor( 0xf6, 0x8d, 0x2f ); // f68d2f
        ct2Colors.barGraphCoreShade2 = new BaseColor( 0xff, 0x3b, 0x62 );      // ff3b62       

        try
        {
            if( logoUrl != null && !logoUrl.isBlank() )
                custLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logoUrl ) );

            if( pcmUrl != null && !pcmUrl.isBlank() )
                pcmImage = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( pcmUrl ) );

            if( headerLogoUrl != null && !headerLogoUrl.isBlank() )
                headerLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( headerLogoUrl ) );

            if( headerLogoWhiteTransUrl!=null && !headerLogoWhiteTransUrl.isBlank() )
                headerLogoWhiteTrans = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( headerLogoWhiteTransUrl ) );

            if( twitterLogo==null )
                twitterLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( twitterLogoUrl ) );

            // LogService.logIt( "UMinnReportSettings.initExtra() AAA headerLogoWhiteTrans.scaledHeight " +  headerLogoWhiteTrans.getScaledHeight() );

            twitterLogo.scalePercent(70 );
            custLogo.scalePercent( 80);
            pcmImage.scalePercent( 48 );
            headerLogo.scalePercent( 46 );
            headerLogoWhiteTrans.scalePercent( 38 );

            // LogService.logIt( "UMinnReportSettings.initExtra() BBB headerLogoWhiteTrans.scaledHeight " +  headerLogoWhiteTrans.getScaledHeight() );                
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnReportSettings.initFonts() getting images. "  );
        }

        // LogService.logIt( "UMinnReportSettings.initExtra() headerLogoWhiteTrans is " + (headerLogoWhiteTrans==null ? "null" : "not null")  );

        setHraLogoWhiteTextSmall(headerLogoWhiteTrans);
    }

    protected TestEventScore getTestEventScore( UMinnCompetencyType uct )
    {
        return  getTestEventScore( uct, reportData.te.getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()) );
    }
    
    protected TestEventScore getTestEventScore( UMinnCompetencyType uct, List<TestEventScore> tesl )
    {
        for( TestEventScore tes : tesl )
        {
            // if( tes.getName().equalsIgnoreCase( uct.getName() ) || tes.getName().equalsIgnoreCase( uct.getName2() )  )
            if( StringUtils.isValidNameMatch( tes.getName(),tes.getNameEnglish(), uct.getName(), uct.getName2()) )
                return tes;
        }
        
        if( RuntimeConstants.getBooleanValue( "reportDebugMode" ) && !tesl.isEmpty()  )
        {
            return tesl.get(0);
        }
        
       return null; 
    }
    
    protected void initData() throws Exception
    {
        try
        {
            if( reportUtils == null )
                reportUtils = new ReportUtils();   
            
            if( reportData.tk.getCustom1()!= null && !reportData.tk.getCustom1().isEmpty() )                
                custom1PercentileData = reportUtils.getCustomPercentile(reportData.te.getProductId() , reportData.te, reportData.te.getOverallTestEventScore(), 0, null, reportData.tk.getCustom1(), null, null );
            //    custom1PercentileData = reportUtils.getCustomPercentile(reportData.te.getProductId() , reportData.te, reportData.te.getOverallTestEventScore(), 1, null, reportData.tk.getCustom1(), null, null );
            
            competencyList = new ArrayList<>();
            
            UMinnCompetency uc;
            
            TestEventScore tes;
            
            List<TestEventScore> tesl = reportData.te.getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId());
            
            for( UMinnCompetencyType uct : UMinnCompetencyType.values() )
            {
                tes = getTestEventScore( uct, tesl );
                
                if( tes == null )
                    continue;
                
                uc = new UMinnCompetency( uct, tes, reportData.getLocale() );
                
                // LogService.logIt( "UMinnReportSettings.initData() adding UMinnComptency: " + uc.getName() );
                
                competencyList.add( uc );
            }
            
            // LogService.logIt( "UMinnReportSettings.initData() Found " + competencyList.size() + " competencies." );
               
            // Next, get all the data for each competency
            
            reportUtils.loadTestEventSimXmlObject(reportData.te);
            
            SimJ simJ = reportData.te.getSimXmlObj();
            
            List<ItemResponse> irl = reportUtils.getItemResponsesForTestEvent(reportData.te, true);
            
            List<ItemResponse> followUpIrl = new ArrayList<>();
            
            for( ItemResponse ir : irl )
            {
                if( ir.getSimletNodeUniqueId()!=null && ir.getSimletNodeUniqueId().toLowerCase().contains( "follow-up" ) )
                    followUpIrl.add( ir );
            }
            
            UMinnCompetencyType ct;
            List<String> behs;   
            String temp;
            
            for( UMinnCompetency uuc : competencyList )
            {
                ct = uuc.uMinnCompetencyType;
                
                uuc.setDefinition( lmsg( ct.getKey() + ".cd" ) );
                uuc.setSummary( lmsg( ct.getKey() + ".summary", new String[]{uuc.getScoreName(reportData.getReportRuleAsInt("scorescheme"))} ) );

                behs = new ArrayList<>();                
                for( int i=1;i<=4;i++ )
                {
                    temp = lmsg( ct.getKey() + ".beh.hi." + i );
                    
                    if( temp==null )
                        break;
                    
                    behs.add( temp.trim() );
                }                
                uuc.setHighBehs(behs);
                    
                behs = new ArrayList<>();                
                for( int i=1;i<=4;i++ )
                {
                    temp = lmsg( ct.getKey() + ".beh.lo." + i );
                    
                    if( temp==null )
                        break;
                    
                    behs.add( temp.trim() );
                }                
                uuc.setLowBehs(behs);

                behs = new ArrayList<>();                
                for( int i=1;i<=10;i++ )
                {
                    if( i==3 && this.useAltusLangKeys && ct.equals( UMinnCompetencyType.PCCARE))
                        temp = lmsg( ct.getKey() + ".recdev." + i+ this.altusKeySuffix );
                    
                    else
                        temp = lmsg( ct.getKey() + ".recdev." + i );
                    
                    if( temp==null || temp.isEmpty() || temp.contains( "KEY NOT FOUND" ) )
                        break;
                    
                    behs.add( temp.trim() );
                }                
                uuc.setDevOps(behs);
                
                setUMinnItemList( uuc, simJ, irl );

                // LogService.logIt( "UMinnReportSettings.initData() BBB.1 " + uuc.toString() );

            }
                        
            setSceneList( irl, followUpIrl, simJ );
            
            // LogService.logIt( "UMinnReportSettings.initData() uminnSceneList contains: " + this.uminnSceneList.size()  );
            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "UMinnReportSettings.initData() " );
        }
    }
    
    
    protected void setSceneList( List<ItemResponse> irl, List<ItemResponse> followUpIrl, SimJ simJ ) throws Exception
    {
        try
        {
            uminnSceneList = new ArrayList<>();

            for( ItemResponse ir : irl )
            {
                getOrCreateUminnScene( ir );
            }

            // Next, collect all uminnScenarios for each Scene
            
            List<UMinnItem> sceneUMinnItemsList;
            
            ItemResponse followUpIr;
            String itmTxt;
            
            for( UMinnScene ums : uminnSceneList )
            {
                sceneUMinnItemsList = new ArrayList<>();
                
                for( UMinnCompetency uc : this.competencyList )
                {
                    for( UMinnItem us : uc.getUMinnItemList() )
                    {
                        if( us.getItemResponse()==null )
                            continue;
                        
                        if( us.getItemResponse().getSimletId()==ums.getSimletId() )
                            sceneUMinnItemsList.add( us );
                    }
                }
                
                Collections.sort( sceneUMinnItemsList );
                
                ums.setUMinnItemList(sceneUMinnItemsList);
                
                followUpIr = getFollowUpItemResponseForScene( ums , followUpIrl );
                
                if( followUpIr != null )
                {
                    UMinnItem umi = new UMinnItem();
                    
                    umi.setItemResponse(followUpIr);
                    
                    itmTxt = getSelectedTextForFollowUpItem( simJ, followUpIr );
                    
                    if( itmTxt != null && !itmTxt.isEmpty() )
                        itmTxt = URLDecoder.decode(itmTxt, "UTF8");
                    
                    umi.setText( itmTxt );
                    
                    ums.setFollowUpItem(umi);
                }
                
                // LogService.logIt("UMinnReportSettings.setSceneList() simletId=" + ums.getSimletId() + ", UMinn Items Found=" + sceneUMinnItemsList.size() );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UMinnReportSettings.setSceneList() " );
            
            throw e;
        }
        
    }
    
    
    protected UMinnScene getOrCreateUminnScene( ItemResponse ir ) throws Exception
    {
        for( UMinnScene ums : uminnSceneList )
        {
            if( ums.getSimletId() == ir.getSimletId() )
                return ums;
        }
        
        Object[] data = UMinnConstants.getInfoForSimletId( ir.getSimletId() );
        
        if( data == null )
        {
            LogService.logIt( "UMinnReportSettings.getOrCreateUminnScene() Cannot find Scene for simletId=" + ir.getSimletId() );
            return null;
            // throw new Exception( "Cannot find Scene for simletId=" + ir.getSimletId() );
        }
        
        String sceneText = (String) data[1];
        UMinnScene u = new UMinnScene( uminnSceneList.size()+1, ir.getSimletId(), ir.getSimletVersionId(), sceneText );
        
        uminnSceneList.add( u );
        
        return u;
            
    }
    
    protected ItemResponse getFollowUpItemResponseForScene( UMinnScene u , List<ItemResponse> irl )
    {
        for( ItemResponse ir : irl )
        {
            if( ir.getSimletId()==u.getSimletId() && ir.getSimletVersionId()==u.getSimletVersionId() )
                return ir;
        }
        
        return null;
    }
    

    protected void setUMinnItemList( UMinnCompetency uuc, SimJ simJ, List<ItemResponse> irl ) throws Exception
    {
        List<UMinnItem> usl = null;
            
        try
        {            
            SimJ.Simcompetency simCompetency = getSimCompetency( simJ, uuc.getName() );
            
            if( simCompetency == null )
            {
                uuc.setUMinnItemList( new ArrayList<>() );   
                return;
            }
            
            // Next, get the RadioButton Groups for this simCompetency
            usl = getUMinnScenariosForSimCompetency( simJ, simCompetency.getId() );
            
            if( usl == null )
            {    
                uuc.setUMinnItemList( new ArrayList<>() );   
                return;            
            }
            
            List<UMinnItem> out = new ArrayList<>();
            
            if( reportUtils == null )
                reportUtils = new ReportUtils();   
                  
            float[] avgScoreInfo;
            
            for( UMinnItem us : usl )
            {
                us.setItemResponse( getItemResponseForUMinnScenario( us, irl ) );
                
                // No item Response, don't include it. 
                if( us.getItemResponse()==null )
                {
                	  if( reportData.te.getTestEventId()>0 )
                        LogService.logIt( "UMinnReportSettings.setUMinnItemList() Could not find an item response for testEvent=" + reportData.te.getTestEventId() + ", UMinnItem: " + us.toString() + ", for competency " + uuc.toString() + " skipping this UMinnItem." );
                    continue;
                }
                
                // now we need to get some data. 
                
                // need to find the Rating
                us.setRating( us.getItemResponse().getItemScore() );
                
                avgScoreInfo = getAverageScoreInfoFmCache( us.getItemResponse() );
                
                if( avgScoreInfo == null )
                {
                    avgScoreInfo = reportUtils.getAverageItemScoreForItemScore( us.getItemResponse() );
                    
                    addAverageScoreInfoToCache( avgScoreInfo, us.getItemResponse() );
                }
                
                // Now need to get the average rating.
                us.setAvgRating( avgScoreInfo[0] );
                
                us.setUminnCompetency(uuc);
                // LogService.logIt( "UMinnReportSettings.setScenarioList() Adding Scenario: " + us.toString() );
                
                out.add( us );
            }
            
            uuc.setUMinnItemList(out);            
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "UMinnReportSettings.setScenarioList() " );
            
            throw new STException(e);
        }
    }
    
    
    protected String getAvgScoreInfoId( ItemResponse ir ) throws Exception
    {
        if( ir==null )
            throw new Exception( "ItemResponse is null!" );

        if( ir.getIdentifier()==null )
            throw new Exception( "ItemResponse.identifier is null! " + ir.toString() );

        return ir.getSimletId()+ "_" + ir.getSimletVersionId() + "_" + ir.getSimletNodeSeq() + "_" + ir.getIdentifier();
    }
    
    protected float[] getAverageScoreInfoFmCache( ItemResponse ir ) throws Exception
    {
        if( averageScoreCache==null )
            averageScoreCache = new HashMap<>();
        
        return averageScoreCache.get( getAvgScoreInfoId( ir ) );
    }
    
    protected void addAverageScoreInfoToCache( float[] avgScoreInfo , ItemResponse ir ) throws Exception
    {
        if( avgScoreInfo==null || avgScoreInfo[1]<MIN_COUNT_FOR_CACHE )
            return;
        
        if( averageScoreCache==null )
            averageScoreCache = new HashMap<>();
        
        averageScoreCache.put( getAvgScoreInfoId( ir ), avgScoreInfo );
    }
    
    
    protected ItemResponse getItemResponseForUMinnScenario( UMinnItem us, List<ItemResponse> irl ) throws Exception
    {
        for( ItemResponse ir : irl )
        {
            // must be same sim competency
            if( ir.getSimCompetencyId()!=us.getSimcompetencyId() )
                continue;
            
            // Same interaction
            if( us.getSimNodeSeqId() != ir.getSimNodeSeq() )
                continue;
            
            // same radio button group
            if( ir.getRadioButtonGroupId()-1 != us.getRadioButGroupId() )
                continue;
            
            // OK it's a match. 
            return ir;
        }
        
        return null;
    }
    
    
    protected List<UMinnItem> getUMinnScenariosForSimCompetency( SimJ simJ, long simCompetencyId ) throws Exception            
    {
        List<UMinnItem> out = new ArrayList<>();
        
        if( simJ == null || simCompetencyId<=0 )
            return out;
        
        UMinnItem us;
        
        List<SimJ.Intn.Intnitem> iil;        
        
        for( SimJ.Intn intn : simJ.getIntn() )
        {
            for( SimJ.Intn.Radiobuttongroup rbg : intn.getRadiobuttongroup() )
            {
                if( rbg.getSimcompetencyid() != simCompetencyId )
                    continue;
                
                // OK this is a hit. We have a Radio Button Group that matches the sim competency
                us = new UMinnItem();
                
                us.setSimcompetencyId(simCompetencyId);
                us.setTrueScore( rbg.getScoreparam1() );
                us.setRadioButGroupId( rbg.getRadiobuttongroupid() );
                us.setSimNodeSeqId( intn.getSeq() );

                iil = new ArrayList<>();
                
                for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
                {
                    if( iitm.getRadiobuttongroup()== rbg.getRadiobuttongroupid() )
                        iil.add( iitm );
                    
                    if( rbg.getQuestionid()==null || rbg.getQuestionid().isEmpty() )
                        continue;
                    
                    if( iitm.getId()==null || iitm.getId().isEmpty() )
                        continue;
                    
                    if( iitm.getId().equals( rbg.getQuestionid() ) )
                        us.setText( URLDecoder.decode( iitm.getContent(), "UTF8" )  );
                }
                
                us.setIntItems(iil);
                
                if( us.getText()==null || us.getText().isEmpty() )
                    us.setText( "Not found." );
                
                out.add( us );
            }
        }
        
        return out;
    }
    

    protected String getSelectedTextForFollowUpItem( SimJ simJ, ItemResponse ir ) throws Exception            
    {        
        for( SimJ.Intn intn : simJ.getIntn() )
        {
            if( intn.getSimletid()!=ir.getSimletId())
                continue;
            
            if( intn.getSimletnodeseq()!=ir.getSimletNodeSeq() )
                continue;
            
            for( SimJ.Intn.Intnitem sii : intn.getIntnitem() )
            {
                try
                {
                    if( ir.getSelectedSubnodeSeqIds()==null || ir.getSelectedSubnodeSeqIds().trim().isEmpty() )
                        continue;
                    
                    if( sii.getSeq() == Integer.parseInt( ir.getSelectedSubnodeSeqIds().trim( ) ))
                        return sii.getContent();
                }
                catch( NumberFormatException e )
                {
                    LogService.logIt( e, "UMinnReportSettings.getSelectedTextForFollowUpItem() intn=" + intn.getName() + " (" + intn.getSeq() + "), " + ir.toString() );
                }
            }            
        }
        
        return null;
    }
    

    
    protected SimJ.Simcompetency getSimCompetency( SimJ simJ, String name ) throws Exception
    {
        if( name == null || name.isEmpty() || simJ==null)
            return null;
        
        for( SimJ.Simcompetency scc : simJ.getSimcompetency() )
        {
            if( scc.getName().equals(name) )
                return scc;
            
            if( scc.getNameenglish()!=null && !scc.getNameenglish().isEmpty() && scc.getNameenglish().equals(name) )
                return scc;                

            if( URLDecoder.decode( scc.getName(), "UTF8" ).equals(name) )
                return scc;
            
            if( scc.getNameenglish()!=null && !scc.getNameenglish().isEmpty() && URLDecoder.decode( scc.getNameenglish(), "UTF8" ).equals(name) )
                return scc;                

        }
        
        return null;
    }
    
    public URL getLocalImageUrl(String baseUrl, String fn) 
    {
        return com.tm2score.util.HttpUtils.getURLFromString(baseUrl + "/" + fn);
    }

    @Override
    public int getFontTypeIdForLocale( Locale locale )
    {
        return 4;
    }
    
    
    
    public void initMessages()
    {
        if( this.uminnReportUtils==null )
            uminnReportUtils = new UMinnReportUtils();
    }


    public String lmsg( String key )
    {
        initMessages();
        
        return uminnReportUtils.getKey(key );
    }

    public String lmsg( String key, String[] prms )
    {
        initMessages();
        
        String msgText = uminnReportUtils.getKey(key );
        
        return MessageFactory.substituteParams(Locale.US , msgText, prms );
    }
    
    



}
