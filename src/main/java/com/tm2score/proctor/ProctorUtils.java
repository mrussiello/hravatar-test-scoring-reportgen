/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.proctor;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventArchive;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.proctor.SuspiciousActivity;
import com.tm2score.entity.user.User;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.file.BucketType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.file.UploadedUserFileStatusType;
import com.tm2score.file.UploadedUserFileType;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author miker_000
 */
public class ProctorUtils 
{
    UserFacade userFacade;
    ProctorFacade proctorFacade;
    
    public void setupRemoteProctorEvent( Locale locale, TimeZone tz, TestEvent te ) throws Exception
    {
        //if( te.getRemoteProctorEvent()!=null && te.getRemoteProctorEvent().getUploadedUserFileList()!=null)
        //    return;
        
        RemoteProctorEvent rpe = te.getRemoteProctorEvent(); 
        
        if( rpe==null )
            rpe = getRemoteProctorEvent( te.getTestEventId() );
        
        if( rpe==null )
            return;
        
        te.setRemoteProctorEvent(rpe);
        setupRemoteProctorEvent( locale, tz, rpe, te, te.getUserId() );
    }
    
    public RemoteProctorEvent getRemoteProctorEvent( long testEventId ) throws Exception
    {
        if(proctorFacade==null )
            proctorFacade = ProctorFacade.getInstance();

        return proctorFacade.getRemoteProctorEventForTestEventId(testEventId);
    }
    
    
    public void setupRemoteProctorEvent( Locale locale, TimeZone tz, RemoteProctorEvent rpe, TestEvent te, long userId ) throws Exception
    {        
        if( rpe==null ) // || !OnlineProctoringType.getValue( rpe.getOnlineProctoringTypeId()).getIsAnyPremium() )
            return;
     
        if( tz==null )
            tz = TimeZone.getDefault();
        
        //if( OnlineProctoringType.getValue( rpe.getOnlineProctoringTypeId()).getIsAnyPremium() )  //  && rpe.getSuspiciousActivityList()==null )
        //{
        if( rpe.getSuspiciousActivityList()==null )
        {
            if(proctorFacade==null )
                proctorFacade = ProctorFacade.getInstance();

            rpe.setSuspiciousActivityList(proctorFacade.getSuspiciousActivityForTestEventId( rpe.getTestKeyId(), rpe.getTestEventId() ));
        }

        for( SuspiciousActivity sa : rpe.getSuspiciousActivityList() )
        {
            sa.setLocale( locale );
            if( sa.getSuspiciousActivityType().getIsUserNote() && sa.getUserId()>0 && sa.getUser()==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                sa.setUser( userFacade.getUser( sa.getUserId()  ) );
            }                        
        }

        if( OnlineProctoringType.getValue( rpe.getOnlineProctoringTypeId()).getIsAnyPremium() && rpe.getSuspensionHistoryList()==null )
            rpe.setSuspensionHistoryList(getSuspensionInfoList(locale, tz, rpe, userId, true ) );
        //}
        
        // Populate the UploadedUserFileList
        if( rpe.getUploadedUserFileList()==null )
        {
            long maxTestEventId = 0;
            if( te!=null && te.getBatteryId()>0 && te.getTestKey()!=null && !te.getTestKey().getTestKeyStatusType().getIsCompleteOrHigher() )
                maxTestEventId = te.getTestEventId();
            
            FileUploadFacade fuf = FileUploadFacade.getInstance();
            rpe.setUploadedUserFileList(fuf.getUploadedUserFilesForTestKey(rpe.getTestKeyId(), UploadedUserFileType.REMOTE_PROCTORING_ID.getUploadedUserFileTypeId(), maxTestEventId ));
            
            if( OnlineProctoringType.getValue( rpe.getOnlineProctoringTypeId()).getRecordsVideo() )
                rpe.getUploadedUserFileList().addAll(fuf.getUploadedUserFilesForTestEvent( rpe.getTestEventId(), UploadedUserFileType.REMOTE_PROCTORING.getUploadedUserFileTypeId() ));

            rpe.getUploadedUserFileList().addAll(fuf.getUploadedUserFilesForTestKey(rpe.getTestKeyId(), UploadedUserFileType.REMOTE_PROCTORING_IMAGES.getUploadedUserFileTypeId(), maxTestEventId ));
        }
    }
    
    
    
    public String getSameIpUserInfo( RemoteProctorEvent rpe, Locale locale, TimeZone tz, boolean isHtml ) throws Exception
    {
        if( rpe==null || rpe.getSameIpTestEventInfo()==null || rpe.getSameIpTestEventInfo().isBlank() )
            return "";
        
        try
        {
            /// data[0]=User. data[1]=Date
            List<Object[]> data = ProctorHelpUtils.parseSameIpTestEventInfo( rpe.getSameIpTestEventInfo() );
            if( data==null || data.isEmpty() )
                return "";
            
            StringBuilder sb = new StringBuilder();
            User u;
            Date d;
            // I18nUtils.getFormattedDateTime(getLocale() , tk.getLastAccessDate(), tz );
            for( Object[] dat : data )
            {
                u = (User) dat[0];
                d = (Date) dat[1];
                
                if( u==null || d==null )
                    continue;
                
                if( sb.length()>0 )
                {
                    if( isHtml )
                        sb.append( "<br />" );
                    else
                        sb.append( "\n" );
                }
                
                sb.append( getUserName(u, locale) + " (" + I18nUtils.getFormattedDateTime( locale, d, tz ) + ")" );                
            }
            
            return sb.toString();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.getSameIpUserInfo() testEventId=" + (rpe==null ? "null" : rpe.getTestEventId() ) );
            throw e;
        }
    }
    
    
    public String getUserName( User u, Locale locale ) {

        if( u!=null && u.getUserType().getPseudo() )
            return MessageFactory.getStringMessage( locale, "g.Pseudonymized", null );
                    
        if( u!=null && (u.getUserType().getUsername() || u.getUserType().getUserId()) )
            return u.getEmail();
                    
        if( u==null || u.getUserType().getAnonymous() || u.getFullname()==null || u.getFullname().isEmpty() )
            return MessageFactory.getStringMessage( locale, "g.Anonymous", null ) + (u==null || u.getExtRef()==null || u.getExtRef().isEmpty() ? "" : " (" + u.getExtRef() + ")");

        return u.getFullname();
    }
    
    
    /**
     * Returns 
     * data[]
     *    data[0] = TestEventArchive
     *    data[1] = User
     *    
     * @param te
     * @return 
     */
    public List<Object[]> getSuspiciousTestEventInfoBasedOnIpAddress( TestEvent te )
    {
        List<Object[]> out = new ArrayList<>();
        
        if( te==null || te.getLastAccessDate()==null || te.getIpAddress()==null || te.getIpAddress().isBlank() )
            return out;
        
        try
        {
            Calendar cal = new GregorianCalendar();
            cal.setTime( te.getLastAccessDate() );
            cal.add( Calendar.HOUR_OF_DAY, -4 );
            Date minDate = cal.getTime();
            // cal.add( Calendar.DAY_OF_MONTH, 1 );
            cal.add( Calendar.HOUR_OF_DAY, 8 );            
            Date maxDate = cal.getTime();

            if( proctorFacade==null )
                proctorFacade=ProctorFacade.getInstance();

            List<TestEventArchive> teal = proctorFacade.getTestEventArchiveListForSameOrgProductIp( te.getTestEventId(), te.getUserId(), te.getOrgId(), te.getProductId(), te.getIpAddress(), minDate, maxDate);
            User otherUser;
            Object[] data;
            for( TestEventArchive tea : teal )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                otherUser = userFacade.getUser( tea.getUserId() );
                data = new Object[] { tea, otherUser };
                out.add(data);
            }            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ProctorUtils.getSuspiciousTestEventInfoBasedOnIpAddress() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
        }
        return out;
            
    }
    
    
    public List<String[]> getSuspensionInfoList( Locale locale, TimeZone tz, RemoteProctorEvent rpe, long userId, boolean skipProctorSuspensions) throws Exception
    {
        List<String[]> sil = new ArrayList<>();
        if( !rpe.getHasSuspensionHistory() )
            return sil;
                
        Date d;
        User u;
        long uid;
        for( String[] info : rpe.parseSuspensionHistory() )
        {
            d = new Date( Long.parseLong( info[0] ) );

            info[0] = I18nUtils.getFormattedDateTime(locale, d, tz );
            info[1] = MessageFactory.getStringMessage( locale, info[1].equalsIgnoreCase("0") ? "g.PPSuspended" : "g.PPUnsuspended" );
            if( !info[2].isBlank() )
            {
                if( info[2].equalsIgnoreCase("0") )
                {   
                    if( info[1].equalsIgnoreCase("0") )
                        info[2]=MessageFactory.getStringMessage( locale, "g.System" );
                    else
                        info[2]="";                    
                }
                else
                {
                    uid = Long.parseLong(info[2]);
                    if( uid==userId )
                        info[2]=MessageFactory.getStringMessage( locale, "g.System" );

                    else
                    {
                        if( skipProctorSuspensions )
                            continue;
                        
                        if( userFacade == null ) 
                            userFacade = UserFacade.getInstance();
                        u = userFacade.getUser(uid);
                        info[2] = u == null || u.getOrgId()!=rpe.getOrgId() ? MessageFactory.getStringMessage( locale, "g.Proctor" ) : u.getFullname();
                    }
                }
            }
            sil.add( info );
        }
        return sil;
    }
    
    
    public List<UploadedUserFile> getFauxUploadedUserFileListForReportThumbs( List<UploadedUserFile> ufl, boolean forceIncludeAll, int maxImagesToInclude ) throws Exception
    {
        List<UploadedUserFile> out = new ArrayList<>();
        
        if( ufl==null || ufl.isEmpty() )
            return out;
        
        BucketType bt = RuntimeConstants.getBooleanValue( "useTestFoldersForProctorRecordings" ) ? BucketType.PROCTORRECORDING_TEST : BucketType.PROCTORRECORDING;
        String imgBaseUrl = RuntimeConstants.getStringValue( "awsS3BaseUrl" ) + bt.getBucket() + "/" + bt.getBaseKey(); 
        
        String baseThumbUrl;
        String fn; 
        UploadedUserFile uuf;
        // int increment = 1;
        // float denom = 1;
        String directory;
        
        for( UploadedUserFile u : ufl )
        {
            if( u.getThumbFilename()==null || u.getThumbFilename().isBlank() || u.getMaxThumbIndex()<1 )
                continue;
            
            if( u.getThumbWidth()<=0 && u.getWidth()>0 )
                u.setThumbWidth( u.getWidth() );
            
            if( u.getThumbHeight()<=0 && u.getHeight()>0 )
                u.setThumbHeight( u.getHeight() );
            
            directory = u.getDirectory();
            
            if( directory.startsWith("/") )
                directory = directory.substring(1, directory.length() );
            
            baseThumbUrl = imgBaseUrl + directory + "/";
            //increment = 1;
            // denom = 1;
            //if( u.getMaxThumbIndex()<=16 || forceIncludeAll )
            //{
            //    increment=1;
            //}
            //else if( u.getMaxThumbIndex()<=32 )
            //    increment = 2;
            //else if( u.getMaxThumbIndex()<=48 )
            //    increment = 3;
            //else if( u.getMaxThumbIndex()<=64 )
            //    increment = 4;
            // else
            //    increment = Math.round(((float)u.getMaxThumbIndex())/16f);

            // increment = Math.round( (float)u.getMaxThumbIndex()/denom );

            for( int idx : getIndexesToInclude( u, forceIncludeAll, maxImagesToInclude ) )
            {
                
                // max reached.
                //if( maxImagesToInclude>0 && idx>=maxImagesToInclude )
                //    break;
                
                uuf = new UploadedUserFile();
                uuf.setUploadedUserFileId( u.getUploadedUserFileId() );
                uuf.setUploadedUserFileTypeId( u.getUploadedUserFileTypeId() );
                uuf.setUploadedUserFileStatusTypeId( UploadedUserFileStatusType.AVAILABLE.getUploadedUserFileStatusTypeId() );

                if( u.getThumbFilename().contains( ".IDX.") )
                    fn = StringUtils.replaceStr(u.getThumbFilename(), ".IDX." , "." + idx + "." );
                else if( u.getThumbFilename().contains( ".AWSCOUNT.") )
                    fn = StringUtils.replaceStr(u.getThumbFilename(), ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( idx, 5 ) + "." );
                else
                    fn = u.getThumbFilename();
                
                uuf.setThumbFilename(fn);
                                
                // date str
                uuf.setR1( u.getR1() );
                uuf.setR2( u.getR2() );
                uuf.setActId( u.getActId() );
                uuf.setTestEventId(u.getTestEventId());
                uuf.setTempStr2("");
                uuf.setTempInt1(0);
                uuf.setTempInt3(idx);
                uuf.setTempInt2(u.getOrientation());
                uuf.setOrientation(u.getOrientation());
                uuf.setTempStr1(  ReportUtils.getUploadedUserFileThumbUrl( uuf, fn ) );                   

                if( u.hasFailedIndex(idx) )
                {
                    int v = u.getFailedIndexErrorTypeId( idx );
                    
                    if( v>0 && v<ProctorImageErrorType.SUCCESSFUL_FACE_AND_COMPARISON.getProctorImageErrorTypeId() )
                    {
                        uuf.setFailedImage(true);
                        uuf.setProctorImageErrorTypeId( u.getFailedIndexErrorTypeId( idx ) );
                    }
                }

                if( u.hasPreTestIndex(idx) )
                    uuf.setPreTestImage(true);

                out.add(uuf);
            }    
        }
        
        return out;
    }
    
    private List<Integer> getIndexesToInclude( UploadedUserFile u, boolean forceIncludeAll, int maxImagesToInclude )
    {
        int incr;      
        List<Integer> out = new ArrayList<>();

        if( u.getFailedIndexMap()==null )
            u.initFailedIndexMap();
        
        // include up to 5 pre images
        // out.addAll( u.getPretestIndexMap().keySet() );
        
        
        // include all failed images.
        out.addAll( u.getFailedIndexMap().keySet() );
        
        if( u.getPassedIndexSet()==null )
            u.initPassedIndexSet();
        
        // LogService.logIt( "ProctorUtils.getIndexesToInclude() passedIndexSet.size=" + u.getPassedIndexSet().size() + ", failedSize=" + u.getFailedIndexMap().size() + ", out.size=" + out.size());
        
        if( !u.getPassedIndexSet().isEmpty() )
        {
            int remaining = u.getPassedIndexSet().size();
            if( remaining<=16 || forceIncludeAll )
                incr=1;
            else if( remaining<=32 )
                incr = 2;
            else if( remaining<=48 )
                incr = 3;
            else if( remaining<=64 )
                incr = 4;
            else
                incr = Math.round(((float)remaining)/16f);

            int ct = 0;
            int pretestPassCount = 0;
            int finalPassedPreTest = 0;
            for( int idx : u.getPassedIndexSet() )
            {
                
                // include up to 5 passed pretest images.
                if( u.hasPreTestIndex(idx) )
                {
                    // LogService.logIt( "ProctorUtils.getIndexesToInclude() PreTest idx=" + idx +", containsAlready=" + out.contains(idx) + ", pretestPassCount=" + pretestPassCount );
                    
                    if( pretestPassCount<5 )
                    {
                        if( out.contains(idx) )
                            continue;
                        pretestPassCount++;
                        out.add(idx);
                    }
                    
                    finalPassedPreTest = idx;
                }
            }
            
            // LogService.logIt( "ProctorUtils.getIndexesToInclude() maxImagesToInclude=" + maxImagesToInclude + ", out.size()=" + out.size() + ", finalPassedPreTest=" + finalPassedPreTest );
            
            for( int idx : u.getPassedIndexSet() )
            {                
                if( maxImagesToInclude>0 && out.size()>=maxImagesToInclude )
                    break;
                
                ct++;
                if( ct%incr>0 )
                    continue;
                
                if( out.contains(idx) )
                    continue;
                out.add(idx);
            }
            
            if( finalPassedPreTest>out.size() && !out.contains(finalPassedPreTest) )
                out.add(finalPassedPreTest);
        }
        
        // no passed images or old method (not tracked).
        else
        {      
            // ensure we add up to 4
            int pretestCount = 0;
            for( int i=1; i<=u.getMaxThumbIndex(); i++ )
            {
                // include some pretest images.
                if( u.hasPreTestIndex(i) )
                {
                    if( out.contains(i) )
                        continue;
                    out.add(i);
                    pretestCount++;
                    continue;
                }
                if( pretestCount>=4 )
                    break;
            }


            int remaining = u.getMaxThumbIndex()-out.size();
            if( remaining<=16 || forceIncludeAll )
                incr=1;
            else if( remaining<=32 )
                incr = 2;
            else if( remaining<=48 )
                incr = 3;
            else if( remaining<=64 )
                incr = 4;
            else
                incr = Math.round(((float)remaining)/16f);

            for( int i=1; i<=u.getMaxThumbIndex(); i+=incr )
            {
                if( maxImagesToInclude>0 && out.size()>=maxImagesToInclude )
                    break;

                if( out.contains(i) )
                    continue;
                out.add(i);
            }        
        }
        
        Collections.sort(out);
        return out;
    }
    
    
    public static String getProctorImageIdStr( UploadedUserFile uuf, Locale locale )
    {
        if( uuf.isFailedImage() )
        {
            String imageErrorTypeName = uuf.getProctorImageErrorTypeId()>0 ? " (" + ProctorImageErrorType.getValue( uuf.getProctorImageErrorTypeId() ).getName(locale) + ")" : "";
            
            if( uuf.isPreTestImage() )
                return MessageFactory.getStringMessage(locale, "g.RPhotoPretestFailed", null) + imageErrorTypeName;
            else if( uuf.getUploadedUserFileType().getIsRemoteProctoringId() )
                return MessageFactory.getStringMessage(locale, "g.RPhotoPretestIdFailed", null) + imageErrorTypeName;                    
            else
                return MessageFactory.getStringMessage(locale, "g.RPhotoFailed", null) + imageErrorTypeName;                    
        }
        else if( uuf.isPreTestImage() )
            return MessageFactory.getStringMessage(locale, "g.RPhotoPretest", null);
        
        else if( uuf.getUploadedUserFileType().getIsRemoteProctoringId() )
            return MessageFactory.getStringMessage(locale, "g.RPhotoId", null);                    
        
        else
            return MessageFactory.getStringMessage(locale, "g.RPhoto", null);                            
    }
    

    
    public List<String[]> getPremiumCaveatList( int proctoringIdCaptureTypeId, RemoteProctorEvent rpe, Locale loc, boolean hasMaxImages)
    {
        List<String[]> out = new ArrayList<>();
        if( rpe.getRemoteProctorEventStatusTypeId()>=RemoteProctorEventStatusType.IMAGE_COMPARISONS_COMPLETE.getRemoteProctorEventStatusTypeId() )
        {
            if( proctoringIdCaptureTypeId>0 && rpe.getIdFaceMatchPercent()>0 )
            {
                // avgScoreId = rpe.getIdFaceMatchPercent();
                out.add(new String[]{ lmsg(loc, "g.ImgCapMatchWithIdPrem"),Integer.toString( Math.round(rpe.getIdFaceMatchPercent()))+"%"} );
            }

            if( rpe.getMultiFaceThumbs()>0 )
            {                    
                int pctWithMultiFaces = Math.round(100*((float)rpe.getMultiFaceThumbs())/((float) rpe.getThumbsProcessed() ) );                                     
                out.add(new String[]{ lmsg(loc, "g.ImgCapMultiFacePrem"),Integer.toString(rpe.getMultiFaceThumbs()) + " (" + pctWithMultiFaces + "%)"} );
            }

            if( rpe.getThumbPairsFailed() + rpe.getThumbPairsPassed()>0 )
            {                    
                //tot = rpe.getThumbPairsFailed() + rpe.getThumbPairsPassed();
                //if( tot>0 )
                //{
                //    avgScoreFace =  Math.round( 100*((float)rpe.getThumbPairsPassed())/((float) rpe.getThumbPairsFailed() + rpe.getThumbPairsPassed() ) );
                //}                    
                out.add(new String[]{ lmsg(loc, "g.ImgCapThumbScorePrem"),Integer.toString( Math.round(rpe.getThumbScore()))+"%"} );
            }

            if( rpe.getThumbsProcessed()>0 )
            {
                int pctWithFaces = Math.round(100*((float)rpe.getThumbsPassed())/((float) rpe.getThumbsProcessed() ) );
                out.add( new String[]{ lmsg(loc, "g.ImgCapValidImagesPrem"),Integer.toString(rpe.getThumbsProcessed())} );
                out.add(new String[]{ lmsg(loc, "g.ImgCapValidFacesPrem"),Integer.toString(rpe.getThumbsPassed()) + " (" + pctWithFaces + "%)"} );
            }

            if( rpe.getThumbPairsFailed() + rpe.getThumbPairsPassed()>0 )
            {
                int pctMatch = Math.round(100*((float)rpe.getThumbPairsPassed())/((float) rpe.getThumbPairsFailed() + rpe.getThumbPairsPassed() ) );
                out.add(new String[]{ lmsg(loc, "g.ImgCapPairsProcessedPrem"),Integer.toString(rpe.getThumbPairsFailed() + rpe.getThumbPairsPassed())} );
                out.add(new String[]{ lmsg(loc, "g.ImgCapPairsPassedPrem"),Integer.toString(rpe.getThumbPairsPassed()) + " (" + pctMatch + "%)"} );
            }
            
            if( hasMaxImages )
                out.add(new String[]{ lmsg(loc, "g.ImgCapThumbMaxImgsExceeded", new String[]{RuntimeConstants.getStringValue("baseadmindomain")}) } );
        }                
        return out;        
    }
    
    
    // Standard Locale key
    private String lmsg( Locale loc, String key )
    {
        return lmsg( loc, key, null );
    }

    
    // Standard Locale key
    public String lmsg( Locale loc, String key, String[] prms )
    {
        return MessageFactory.getStringMessage( loc, key, prms );
    }
    
}
