/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.proctor;

import com.tm2score.entity.event.TestEventArchive;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.user.User;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * @author miker_000
 */
public class ProctorHelpUtils {
    
    private static Boolean BASIC_PROCTORING_IS_EXTERNAL = null;
    
    private static synchronized void init()
    {
        if( BASIC_PROCTORING_IS_EXTERNAL==null )
        {
            BASIC_PROCTORING_IS_EXTERNAL = RuntimeConstants.getBooleanValue( "BasicProctoringIsExternal" );
        }
    }
    
        
    public static boolean getUseExternalProctoring( TestKey tk )
    {
        return getUseExternalProctoring( tk.getOnlineProctoringType() );
    }
    
    public static boolean getUseExternalProctoring( OnlineProctoringType opt )
    {
        if( opt==null )
            return false;
        return opt.getIsAnyPremium() || ( opt.getIsAnyBasic() && getIsBasicProctoringExternal() );
    }
    
    public static boolean getIsBasicProctoringExternal()
    {
        if( BASIC_PROCTORING_IS_EXTERNAL==null )
            init();
        return BASIC_PROCTORING_IS_EXTERNAL;
    }
    
    /**
     * 
     *   testeventid;userid;date; ... 
     * 
     * @param data
     * @return 
     */
    public static String packSameIpTestEventInfo( List<Object[]> data )
    {
        if( data==null || data.isEmpty() )
            return "";
        
        StringBuilder sb = new StringBuilder();        
        TestEventArchive tea;
        
        int maxLen = 400;
        String temp = "";
        for( Object[] dat : data )
        {
            tea = (TestEventArchive)dat[0];
            
            temp = tea.getTestEventId() + ";" + tea.getUserId() + ";" + tea.getLastAccessDate().getTime();
            
            if( sb.length()+temp.length() + 1>maxLen )
                break;
            
            if( sb.length()>0 )
                sb.append(";");
            sb.append( temp );            
        }
        
        return sb.toString();
    }
    
    /**
     * Returns List<Object[]>
     * 
     *   data[0]=User
     *   data[1]=Date
     * 
     * @param inStr
     * @return 
     */
    public static List<Object[]> parseSameIpTestEventInfo( String inStr )
    {
        List<Object[]> out = new ArrayList<>();
        
        if( inStr==null || inStr.isBlank() )
            return out;
        
        try
        {
            //  EventFacade eventFacade = EventFacade.getInstance();
            UserFacade userFacade = UserFacade.getInstance();
            
            String[] keys = inStr.split(";");
            User u;
            Date d;
            for( int i=0;i<keys.length-2;i+=3 )
            {
                u = userFacade.getUser( Long.parseLong( keys[i+1] )  );
                d = new Date( Long.parseLong( keys[i+2] ) );
                out.add(new Object[] {u,d});
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ProctorHelpUtils.parseSameIpTestEventInfo() " + inStr );
        }
        
        return out;
    }
    
}
