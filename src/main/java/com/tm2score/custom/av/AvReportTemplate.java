/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.av;

import com.tm2score.custom.ivr.*;
import com.itextpdf.text.Annotation;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.custom.coretest2.BaseCT2ReportTemplate;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.av.AvEventFacade;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.CellBackgroundCellEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.StringUtils;
import com.tm2score.voicevibes.VoiceVibesResult;
import com.tm2score.voicevibes.VoiceVibesScaleScore;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public abstract class AvReportTemplate extends BaseCT2ReportTemplate  implements ReportTemplate {
        
    private AvEventFacade avEventFacade = null;
    
   

    
    protected void addAvUploadSampleInfo() throws Exception
    {
        try
        {
            initVars();

            if( reportData.getReportRuleAsBoolean( "hideavsampleinfoforall") )
                return;
                
            
            //if( !reportData.includeCompetencyScores() )
            //    return;

            java.util.List<TestEventScore> teslst = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAVUPLOAD, true, true ); // new ArrayList<>();
            // teslst.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAVUPLOAD, true ) ); // new ArrayList<>();

            //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() A1 found " + teslst.size() + " ScoredAVUpload TestEventScore records." );
            
            if( teslst.size() <= 0 )
                return;

            if( audioPlayImage == null )
                audioPlayImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( AUDIO_PLAYBACK_URL ) );
            
            if( videoPlayImage == null )
                videoPlayImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( VIDEO_PLAYBACK_URL ) );
                        
            Collections.sort( teslst, new DisplayOrderComparator() );  // new TESNameComparator() );
            
            //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() A2 found " + teslst.size() + " testEventScore records." );

            
            previousYLevel =  currentYLevel - 10;

            if( previousYLevel <= footerHgt )
            {
                document.newPage();
                currentYLevel = 0;
                previousYLevel = 0;
            }

            
            int scoreCount = 0;
            
            java.util.List<TestEventScore> tesl = new ArrayList<>();

            for( TestEventScore tes : teslst )
            {         
                // if supposed to hide even the sample info
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() && reportData.getReportRuleAsBoolean( "hideavsampleinfoforhiddenscores") )
                   continue;
                
                
                //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() AA1 " +  tes.getName() + ", hide=" + tes.getHide() + ", score=" + tes.getScore() );
                // if( (!SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() || tes.getScore()<0) && tes.getTextBasedResponseList(null, true, false ).isEmpty() )
                //     continue;

                //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() AA1 " +  tes.getName() + ", hide=" + tes.getHide() + ", score=" + tes.getScore() );
                if( tes.getTextBasedResponseList(null, true, false ).isEmpty() )
                    continue;
                
                //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() AA2 " );
                
                if( SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() && tes.getScore()>=0 )
                    scoreCount++;
                
                // if supposed to hide
                //if( tes.getHide()>0 )
                //    continue;

                // skip non-auto-scored competencies.
                //if( tes.getScore()<0 )
                //    continue;

                tesl.add( tes );
            }

            //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() BBB1 tesl.size()=" +  tesl.size() );
            
            if( tesl.size() <= 0 )
                return;
            
            TestEventScore otes = reportData.te.getOverallTestEventScore();
            
            List<TextAndTitle> nonCompAvUploads = otes.getTextBasedResponseList( NonCompetencyItemType.AV_UPLOAD.getTitle(), true, true );
            
            // LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() BBB1 tesl.size=" + tesl.size() + ", nonCompAvUploads.size()=" +  nonCompAvUploads.size() );
            
            float y = addTitle(previousYLevel, lmsg( "g.VideoResponsesTitle" ), lmsg( "g.VideoResponsesSubtitle" ), null, null  );

            currentYLevel = y;
            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t;
            
            boolean first = true;
                        
            // For each competency
            for( TestEventScore tes : tesl )
            {
                // LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo()TES Start " +  tes.getName() );
                t = new PdfPTable( reportData.getIsLTR() ? new float[] { 3.5f, 5.5f } : new float[] { 5.5f, 3.5f } );

                float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

                // t.setHorizontalAlignment( Element.ALIGN_CENTER );
                t.setTotalWidth( outerWid );
                t.setLockedWidth( true );
                setRunDirection( t );

                // This tells iText to always use the first row as a header on subsequent pages.
                t.setHeaderRows( 1 );
                

                c = t.getDefaultCell();
                c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBackgroundColor( ct2Colors.hraBlue );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );

                /*
                if( scoreCount>0 )
                {
                    // Add header row.
                    t.addCell( new Phrase( lmsg( "g.Detail" ) , fontLmWhite) );

                    if( devel )
                        t.addCell( new Phrase( lmsg( "g.HelpfulTips" ) , fontLmWhite ) );

                    else
                        t.addCell( new Phrase( lmsg( reportData.includeInterview() ? "g.InterviewGuide" : "g.Description" ) , fontLmWhite ) );
                }
                */
                
                c.setBackgroundColor( BaseColor.WHITE );
                                  
                // Next, add text/link section to table
                addAvUploadInfoToTable(tes , t, outerWid );
            
                if( nonCompAvUploads!=null && !nonCompAvUploads.isEmpty() )
                {
                    addAvUploadInfoToTable(nonCompAvUploads , t, outerWid, false );
                    
                    // do this so it doesn't repeat.
                    nonCompAvUploads = null;
                }
                
                if( !first )
                    addNewPage();
                
                // Add table
                currentYLevel = addTableToDocument(currentYLevel, t, false, true );
                
                first = false;
                // Voice Vibes - separate table 
                //t = createVoiceVibesInfoTable( tes, outerWid );
                
                //if( t != null )
                //    currentYLevel = addTableToDocument( currentYLevel, t );                
            } // each competency
            
            // currentYLevel = addTableToDocument( y, t );
            
        }   
        catch( Exception e )
        {
            LogService.logIt( e, "AvReportTemplate.addVoiceSampleInfo()" );

            throw new STException( e );
        }
    }
            
            

    protected void addVoiceVibesInfo() throws Exception
    {
        try
        {
            //if( !reportData.includeCompetencyScores() )
            //    return;

            java.util.List<TestEventScore> teslst = new ArrayList<>(); // getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY ); // new ArrayList<>();
            // teslst.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAUDIO ) ); // new ArrayList<>();
            teslst.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAVUPLOAD, true, true ) ); // new ArrayList<>();

            if( teslst.size()<=0 )
                return;

            Collections.sort( teslst, new DisplayOrderComparator() );  // new TESNameComparator() );
            
            //LogService.logIt( "AvReportTemplate.addVoiceVibesInfo() AAA.1 Start tesl.size=" + teslst.size() + ", currentYLevel=" + currentYLevel );

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            VoiceVibesResult vvr;
            
            for( TestEventScore tes : teslst )
            {
                // if supposed to hide
                // if supposed to hide even the sample info
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() && reportData.getReportRuleAsBoolean( "hidevibesinfoforhiddenscores") )
                   continue;

                // skip non-auto-scored competencies.
                if( tes.getScore()<0 )
                    continue;
                
                if( tes.getTextParam1()==null || tes.getTextParam1().isBlank() )
                    continue;
            
                vvr = VoiceVibesResult.getFromPackedString(tes.getTextParam1());
                
                if( vvr==null || vvr.getScoreList()==null || vvr.getScoreList().isEmpty() )
                    continue;
                
                tesl.add( tes );
            }
            
            // LogService.logIt( "AvReportTemplate.addVoiceVibesInfo() AAA after trimming. tesl=" + tesl.size() );

            if( tesl.size()<=0 )
                return;
            
            this.addNewPage();
            
            float y = addTitle(currentYLevel, lmsg( "g.VoiceVibesTitle" ), lmsg( "g.VoiceVibesSubtitle" ), null, null  );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t; //  = new PdfPTable( reportData.getIsLTR() ? new float[] { 3.5f, 5.5f } : new float[] { 5.5f, 3.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            currentYLevel = y;
            
            boolean first = true;
            
            // For each competency
            for( TestEventScore tes : tesl )
            {                
                // Voice Vibes - separate table - see if it has data
                t = createVoiceVibesInfoTable( tes, outerWid );
                
                if( t!=null )
                {
                    // LogService.logIt( "AvReportTemplate.addVoiceVibesInfo() Adding table. first=" + first );
                    if( !first )
                        addNewPage();
                    
                    currentYLevel = addTableToDocument(currentYLevel, t, false, true );                        
                    first = false;
                }                
            } // each competency
            
            // currentYLevel = addTableToDocument( y, t );
            
        }   
        catch( Exception e )
        {
            LogService.logIt( e, "AvReportTemplate.addVoiceVibesInfo()" );

            throw new STException( e );
        }
    }
            
    
            
    protected void addAvUploadInfoToTable( TestEventScore tes, PdfPTable t, float outerWid ) throws Exception
    {
        java.util.List<TextAndTitle> ttl = tes.getTextBasedResponseList(null, true, false );
                        
        // LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() START tes=" + tes.getName() +", textTitles.size()=" + ttl.size() );

        addAvUploadInfoToTable(ttl,  t,  outerWid, true );
    }            
            
    protected void addAvUploadInfoToTable( List<TextAndTitle> ttl, PdfPTable t, float outerWid, boolean isScoredTes) throws Exception
    {
        try
        {            
            //LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() textTitles.size()=" + ttl.size() );            
            
        
            boolean showVideoUrls = !reportData.getReportRuleAsBoolean( "pdfvideoviewoff" ) && !reportData.getTestKey().getHideMediaInReports();
            // LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() START tes=" + tes.getName() );

            boolean hideSpeechText = reportData.getReportRuleAsBoolean( "speechtextoff" );
            // java.util.List<TextAndTitle> ttl = tes.getTextBasedResponseList(null, true, false );
                        
            if( ttl.isEmpty() )
                return;
            
            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = true;
            
            
            
            PdfPCell c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );   
            
            // Add header row.
            if( isScoredTes )
            {
                c = new PdfPCell(new Phrase( lmsg( "g.Question" ) , fontLmWhite ));
                // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true));
                setRunDirection( c );                 
                t.addCell( c );
                
                c = new PdfPCell(new Phrase( lmsg( "g.Response" ) , fontLmWhite ));
                // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false));
                setRunDirection( c );                 
                t.addCell( c );
            }
            
            //Phrase ep = new Phrase( "", getFontSmall() );

            //Phrase p;
            
            String playUrl;
            
            long iirId;
            
            Paragraph par;
            
            Chunk chk;

            PdfPTable t2;
            
            AvItemResponse iir;
            UploadedUserFile uuf;
            
            String speechTextEnglish;            
            Locale testContentLocale = I18nUtils.getLocaleFromCompositeStr( reportData.te.getProduct().getLangStr() );            
            boolean english = testContentLocale.getLanguage().equalsIgnoreCase("en");            
            boolean englishReport = this.getReportLocale().getLanguage().equalsIgnoreCase( "en" );
            
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();
            
            if( fileUploadFacade==null )
                fileUploadFacade=FileUploadFacade.getInstance();
            // boolean first = true;
            
            boolean isVideo;
            String thumbFn;
            String thumbUrl;
            int thumbWid;
            int rotateIcon;
            boolean hasPlayableMedia;
            
            String reportMediaPlaybackUrl = reportData.getTestKey().getReportMediaViewUrl();
            
            boolean isScored = isScoredTes;
            BaseColor forceBackgroundColor;
            
            // For each competency
            for( TextAndTitle tt : ttl )
            {
                useGrayBg = !useGrayBg;
                
                forceBackgroundColor = useGrayBg ? graybg : BaseColor.WHITE;
                
                //hasPlayableMedia=false;
                //uuf=null;
                //isVideo=false;
                thumbFn = null;
                //thumbUrl = null;
                thumbWid = 0;
                rotateIcon = 0;
                
                iirId = tt.getUploadedUserFileId();
                
                iir = isScoredTes ? avEventFacade.getAvItemResponse(iirId, false) : null;
                
                // Only apply this edit if it's not a sample report (testEventId=0)
                if( iir!=null && reportData.te.getTestEventId()>0 && iir.getTestEventId()!=reportData.te.getTestEventId() )
                    iir=null;
                
                if( iir==null || iir.getUploadedUserFileId()<=0 )
                    isScored = false;
                
                //if( iir!=null && iir.hasPlayableMedia() )
                //    hasPlayableMedia=true;
                
                //LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() " + tt.toString() + " iirId=" +iirId +", iir=" + (iir==null ? "null" : "not null, " + iir.getUploadedUserFileId() ) );
                    
                // if( iir != null && iir.getUploadedUserFileId()>0 )
                // {
                uuf = isScored ? fileUploadFacade.getUploadedUserFile(iir.getUploadedUserFileId(), true ) : fileUploadFacade.getUploadedUserFile(iirId, true );
                
                // Only apply this edit if it's not a sample report (testEventId=0)
                if( uuf!=null && reportData.te.getTestEventId()>0 && uuf.getTestEventId()!=reportData.te.getTestEventId()  )
                    uuf=null;
                
                if( uuf!=null )
                {
                    hasPlayableMedia=true;
                    
                    isVideo = uuf.getFileContentType().getIsVideo();
                    if( isVideo && reportData.getOrg().getCandidateImageViewTypeId()<=0 )
                    {
                        thumbFn = uuf.getThumbFilename();                        
                        if( thumbFn!=null && thumbFn.contains( ".AWSCOUNT." ) )
                            thumbFn = StringUtils.replaceStr( thumbFn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );

                        else if( thumbFn!=null && thumbFn.contains(  ".IDX." ) )
                            thumbFn = StringUtils.replaceStr( thumbFn, ".IDX." , ".1." );                
                        thumbWid = uuf.getThumbWidth();
                        rotateIcon = uuf.getOrientationForIText();
                    }                    
                }

                else
                {
                    LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() Unable to find AvItemResponse or UploadedUserFile for iirId=" +iirId + ", isScored=" + isScored + ", testeEventId=" + reportData.getTestEvent().getTestEventId() );                    
                    continue;
                }
                    
                    //LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() iirId=" +iirId +", iir=" + (uuf==null ? "null" : "not null, " + uuf.getUploadedUserFileId() + ", fct=" + uuf.getFileContentTypeId() + ", thumbFn=" + thumbFn ) );                    
                // }
                
                speechTextEnglish = null;
                
                if( !hideSpeechText && !english && englishReport )
                    speechTextEnglish = tt.getString1();
                
                
                if( tt.getText() == null || tt.getText().isEmpty() || !isScoredTes )
                    tt.setText( lmsg( "g.NoResponseTextAvailable" ) );

                else if( hideSpeechText )
                    tt.setText( lmsg( "g.NoResponseTextHidden" ) );
                
                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( speechTextEnglish!=null && !speechTextEnglish.isEmpty() ? Rectangle.LEFT | Rectangle.RIGHT   : Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                // First, add a table
                t2 = new PdfPTable( reportData.getIsLTR() ? new float[] { 2.2f, 5.5f } : new float[] { 5.5f, 2.2f } );
                t2.setWidthPercentage(100);
                setRunDirection( t2 );
                
                c = new PdfPCell( new Paragraph( tt.getText() + "\n", getFontSmall() ) );
                c.setBackgroundColor( forceBackgroundColor );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 3 );
                setRunDirection( c );
                t2.addCell( c );  
                
                if( !hasPlayableMedia  )
                {
                    c = new PdfPCell( new Paragraph( lmsg( "g.NoPlayableMediaAvailable" ) + "\n", getFontSmall() ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setColspan(2);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setPadding( 3 );
                    setRunDirection( c );
                    t2.addCell( c );  
                }
                
                else if( reportData.getTestKey().getHideMediaInReports() )
                {
                    c = new PdfPCell( new Paragraph( lmsg( "g.NoPlayableMediaAvailableHidden" ) + "\n", getFontSmall() ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setColspan(2);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setPadding( 3 );
                    setRunDirection( c );
                    t2.addCell( c );                      
                }
                
                else
                {
// TODO re-enable for prod.
                    
                    // This is for custom media playback urls that will be used to obtain the real play url via REST
                    if( 1==1 && reportMediaPlaybackUrl!=null && !reportMediaPlaybackUrl.isBlank() )
                    {
                        String uufid=EncryptUtils.urlSafeEncrypt( uuf.getUploadedUserFileId() );
                        int type=1;
                        URL uurl = com.tm2score.util.HttpUtils.getURLFromString( reportMediaPlaybackUrl );
                        String qs = uurl.getQuery();
                        if( qs==null )
                            qs = "";
                        if( !qs.isBlank() )
                            qs += "&";
                        qs += "type=" + type + "&uid=" + uufid;
                        
                        playUrl = uurl.getProtocol() + "://" + uurl.getAuthority() + uurl.getPath() + "?" + qs;                        
                    }
                    else
                        playUrl = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + ( isScored ? "/ta/avpb/" : "/ta/uavpb/" ) + reportData.getTestEvent().getTestEventId() + "/"  + iirId;
                    // playUrl = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/misc/av/avpb-entry.xhtml?teid=" + EncryptUtils.urlSafeEncrypt( reportData.getTestEvent().getTestEventId() ) + "&iirid="  + EncryptUtils.urlSafeEncrypt( iirId );

                    Image playImg = null;

                    // LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() iirId=" +iirId +", iir=" + (uuf==null ? "null" : "not null, " + uuf.getUploadedUserFileId() + ", fct=" + uuf.getFileContentTypeId() + ", thumbFn=" + thumbFn + " isVideo=" + isVideo ) );                    

                    if( isVideo && uuf!=null && uuf.getUploadedUserFileStatusType().getAvailable() && thumbFn !=null && !thumbFn.isEmpty()  )
                    {
                        thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( uuf, thumbFn );
                        //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                        //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + thumbFn;

                        //else
                        //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + thumbFn;   

                        // LogService.logIt(  "AvReportTemplate.addAvUploadInfoToTable() thumbUrl=" + thumbUrl );                    
                        playImg = getImageInstance(thumbUrl, reportData.te.getTestEventId());// ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( thumbUrl ) );

                        if( playImg!=null )
                        {
                            if( thumbWid>0 && thumbWid>60 )
                                playImg.scalePercent( 100f*60f/((float) thumbWid) );
                            else if( thumbWid>0 )
                            {}
                            else
                                playImg.scalePercent( 24f );                            
                        }

                        if( rotateIcon!=0 )
                            playImg.setRotationDegrees( rotateIcon );
                    }

                    //else                    
                    if( playImg==null )
                    {
                        playImg = Image.getInstance(isVideo ? AvReportTemplate.videoPlayImage : AvReportTemplate.audioPlayImage );                      
                        playImg.scalePercent( 32f );
                    }

                    if( showVideoUrls )
                    {
                        playImg.setAnnotation( new Annotation( 0,0,0,0,playUrl));   
                        
                        //PdfAction pdfa = PdfAction.javaScript( "openUrl( '" + playUrl + "' , 'medwin" + iirId + "' );", pdfWriter);
                        //PdfAnnotation paa = new PdfAnnotation( this.pdfWriter, 0,0,0,0, pdfa );
                        //playImg.setAdditional(pdfa);

                        // playImg.s
                        
                        // PdfAction pdfa = PdfAction.gotoRemotePage( playUrl , lmsg( isVideo ? "g.ViewRecordingC" : "g.ListenToRecordingC"), false, true );  
                        //Annotation a = new Annotation( 0,0,0,0,playUrl);                       
                    }

                    playImg.setAlignment( Image.ALIGN_MIDDLE | Image.ALIGN_CENTER );

                    if( uuf!=null && !uuf.getUploadedUserFileStatusType().getAvailable() )
                        c = new PdfPCell( new Phrase("") );
                    else
                        c = new PdfPCell( playImg );

                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);

                    if( !showVideoUrls )
                    {
                        c.setColspan(2);
                        // c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    }

                    c.setPadding( 3 );
                    c.setPaddingTop( 8 );
                    setRunDirection( c );
                    t2.addCell( c );                

                    if( showVideoUrls )
                    {
                        par = new Paragraph( "\n\n", getFontSmall() );
                        chk = new Chunk( lmsg( isVideo ? "g.ViewRecordingC" : "g.ListenToRecordingC") + "\n" );

                        chk.setAction( new PdfAction( playUrl, false ) );
                        // DOES NOT WORK - chk.setAction(PdfAction.gotoRemotePage( playUrl , lmsg( isVideo ? "g.ViewRecordingC" : "g.ListenToRecordingC"), false, true ));
                        // DOES NOT WORK chk.setAction(PdfAction.javaScript( "if(this.hostContainer){ this.hostContainer.open( '" + playUrl + "' , 'medwin" + iirId + "' ); }", pdfWriter));                        
                        par.add( chk );

                        //playUrl += "/rrrrrddd";
                        //chk = new Chunk( playUrl );
                        // chk = new Chunk( lmsg( isVideo ? "g.ViewRecordingC" : "g.ListenToRecordingC") );
                        
                        //PdfAction pdfa = PdfAction.gotoRemotePage( playUrl , lmsg( isVideo ? "g.ViewRecordingC" : "g.ListenToRecordingC"), false, true );                                
                        // PdfAction pdfa = PdfAction.javaScript( "openUrl( '" + playUrl + "','tgtwin" + iirId + "' );", pdfWriter);
                        // LogService.logIt( "AvReportTemplate.addAvUploadInfoToTable() Action is openUrl( '" + playUrl + "' , 'medwin" + iirId + "' );" );
                        // PdfAction pdfa = PdfAction.javaScript( "openUrl( '" + playUrl + "' , 'medwin" + iirId + "' );", pdfWriter);
                        // PdfAction pdfa = PdfAction.javaScript( "if(this.hostContainer){ this.hostContainer.open( '" + playUrl + "' , 'medwin" + iirId + "' ); }", pdfWriter);
                        //chk.setAction( pdfa );
                        
                        
                        //pdfa = PdfAction.javaScript( "openUrl( '" + playUrl + "' , 'medwin" + iirId + "' );", pdfWriter);
                        //PdfAnnotation paa = new PdfAnnotation( this.pdfWriter, 0,0,0,0, pdfa );
                        //chk.setAnnotation( new PdfAnnotation(this.pdfWriter, 0,0,0,0,playUrl));   
                        // chk.setAction( new PdfAction( playUrl, true ) );
                        // chk.setAnchor( playUrl );

                        // par.add( chk );

                        chk = new Chunk( "\n " );

                        par.add( chk );

                        if( uuf!=null && !uuf.getUploadedUserFileStatusType().getAvailable() )
                            c = new PdfPCell( new Phrase("") );
                        else
                            c = new PdfPCell( par );

                        c.setBackgroundColor( forceBackgroundColor );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setPadding( 3 );
                        setRunDirection( c );
                        t2.addCell( c );
                    }

                }
                    
                c = new PdfPCell( t2 );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( speechTextEnglish!=null && !speechTextEnglish.isEmpty() ? Rectangle.LEFT | Rectangle.RIGHT : Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );
                
                if( speechTextEnglish!=null && !speechTextEnglish.isEmpty() )
                {
                    c = new PdfPCell( new Phrase( lmsg("g.EnglishC", null ), getFontSmall() ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderWidth( 0.5f );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( speechTextEnglish, getFontSmall() ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderWidth( 0.5f );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    t.addCell( c );
                    
                }
                                
            } // each speaking sample
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AvReportTemplate.addAvUploadInfoToTable()" );

            throw new STException( e );
        }
    }
    

    protected PdfPTable createVoiceVibesInfoTable( TestEventScore tes, float outerWid ) throws Exception
    {
        try
        {            
            // LogService.logIt(  "AvReportTemplate.createVoiceVibesInfoTable() includeIt=" + reportData.getReport().getIncludeWritingSampleInfo() + ", " + tes.toString() );
            
            // Next Voice Vibes
            String tp1 = tes.getTextParam1();
            
            if( tp1==null || tp1.isBlank() )
                return null;
            
            VoiceVibesResult vvr = VoiceVibesResult.getFromPackedString(tp1);

            // LogService.logIt(  "AvReportTemplate.createVoiceVibesInfoTable() " + (vvr==null ? "null" : "not null, scoreList.size=" + vvr.getScoreList().size() ) );
            
            if( vvr==null || vvr.getScoreList()==null || vvr.getScoreList().isEmpty() )
                return null;

            
            // 5-column table
            // First, add a table
            PdfPTable vvt = new PdfPTable( reportData.getIsLTR() ? new float[] {0.25f,3,.5f,1.5f,3,1.5f} :  new float[] {1.5f,3,1.5f,.5f,3,0.25f} );
            vvt.setTotalWidth( outerWid );
            vvt.setLockedWidth( true );
            setRunDirection( vvt );

            // This tells iText to always use the first row as a header on subsequent pages.
            // t.setHeaderRows( 1 );

            PdfPCell c = new PdfPCell( new Phrase(lmsg( "g.VoiceVibesInfoForX", new String[]{ tes.getName() } ) , fontLmWhite) );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setColspan(6);
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor( ct2Colors.hraBlue );
            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            c.setCellEvent( new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, true, true, true ) );
            vvt.addCell( c );
            
            PdfPCell vc = vvt.getDefaultCell();
            vc.setBackgroundColor( BaseColor.WHITE );
            vc.setBorder( Rectangle.NO_BORDER );
            setRunDirection( vc  );
            vc.setPadding( 1 );
            
            List<VoiceVibesScaleScore> vvssl = vvr.getStructureScaleScoreList();            
            addVoiceVibesCategoryToTable( vvt, vvssl, "g.VoiceVibesStructure" );

            vvssl = vvr.getVarietyScaleScoreList();            
            addVoiceVibesCategoryToTable( vvt, vvssl, "g.VoiceVibesVariety" );

            vvssl = vvr.getGoodVibesScaleScoreList();            
            addVoiceVibesCategoryToTable( vvt, vvssl, "g.VoiceVibesGoodVibes" );

            vvssl = vvr.getBadVibesScaleScoreList();            
            addVoiceVibesCategoryToTable( vvt, vvssl, "g.VoiceVibesBadVibes" );
                            
            return vvt;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AvReportTemplate.addWritingSampleInfo()" );

            throw new STException( e );
        }
    }

    
    /**
     * 
     * @param t - a five column table.
     * 
     * @param sl
     * @param titleKey
     * @throws Exception 
     */
    protected void addVoiceVibesCategoryToTable( PdfPTable t, List<VoiceVibesScaleScore> sl, String titleKey ) throws Exception
    {
        if( sl==null || sl.isEmpty() )
            return;
        
        
        
        // LogService.logIt( "AvReportTemplate.addVoiceVibesCategoryToTable() ScoreList.size=" + sl.size() + ", titleKey=" + titleKey );
        
        // Add Title Row 
        PdfPCell c = new PdfPCell( new Phrase( lmsg(titleKey), getFontBold() ) );
        c.setColspan(6);
        c.setBackgroundColor( BaseColor.WHITE );
        // c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
        c.setBorder( Rectangle.NO_BORDER );
        //c.setBorderWidth( 0.5f );
        //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
        c.setPadding( 2 );
        c.setPaddingTop(6);
        setRunDirection( c );
        t.addCell( c );
        
        ScoreCategoryType sct = null;
        
        String scoreStr;
        
        VoiceVibesScaleScore vss;
        
        boolean last = false;
        
        for( int i=0;i<sl.size(); i++ )
        {
            vss = sl.get(i);
            
            last = i==sl.size()-1;
            
            if( vss.getVoiceVibesScaleType().isGoodVibe() )
                sct = ScoreCategoryType.GREEN;
            else if( vss.getVoiceVibesScaleType().isBadVibe() )
                sct = ScoreCategoryType.RED;
            else
                sct = ScoreCategoryType.YELLOW;
            
            vss.setLocale( this.getReportLocale() );
            
            scoreStr = I18nUtils.getFormattedNumber( getReportLocale(), vss.getScore(), 0 );
            
            if( vss.getVoiceVibesScaleType().isShowTextForNumScore() )
                scoreStr = lmsg( vss.getVoiceVibesScaleType().getScoreLangKeyForTextNumScore( vss.getScore() ) );
            
            c = new PdfPCell( new Phrase( "", getFontSmall() ) );
            c.setBackgroundColor( BaseColor.WHITE );
            // c.setBorder( last ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 1 );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( vss.getLocalizedName(), getFont() ) );
            c.setBackgroundColor( BaseColor.WHITE );
            // c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 1 );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( scoreStr, getFont() ) );
            c.setBackgroundColor( BaseColor.WHITE );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            // c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 1 );
            setRunDirection( c );
            t.addCell( c );
            
            c = new PdfPCell( new Phrase( lmsg(vss.getScaleLowKey()), getFontSmall() ) );
            c.setBackgroundColor( BaseColor.WHITE );
            // c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            setRunDirection( c );
            t.addCell( c );
            
            c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
            // c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER  );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 1 );
            c.setFixedHeight(16);
            
            if( vss.getVoiceVibesScaleType().isVibe() )
                c.setCellEvent(new VoiceVibesVibeGraphicCellEvent( vss , reportData.p, sct, ct2Colors, baseFontCalibri, getReportLocale() ) );
            else if( !vss.getVoiceVibesScaleType().isNoGood() )
                c.setCellEvent(new VoiceVibesStandardGraphicCellEvent( vss , reportData.p, ct2Colors, baseFontCalibri, getReportLocale() ) );            
                
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( lmsg(vss.getScaleHighKey()), getFontSmall() ) );
            c.setBackgroundColor( BaseColor.WHITE );
            // c.setBorder( last ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 1 );
            c.setPaddingLeft( 3 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            setRunDirection( c );
            t.addCell( c );
        }

    }
    
    
    
    
}
