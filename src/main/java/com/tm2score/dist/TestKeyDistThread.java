/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.dist;

import com.tm2score.entity.event.TestKey;
import com.tm2score.global.Constants;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;

/**
 *
 * @author miker_000
 */
public class TestKeyDistThread extends BaseDistManager implements Runnable {
    
    TestKey tk;
    
    public TestKeyDistThread( TestKey tk )
    {
        this.tk = tk;
    }
 
    
    @Override
    public void run() 
    {
        try
        {
            // LogService.logIt( "TestKeyDistThread.run() START, TestKeyId=" + (this.tk==null ? "NULL" : tk.getTestKeyId() ) );
            distributeTestKeyResults(tk, false, 0, 0, Constants.ARCHIVE_DELAY_SECS );

        }
        catch( ScoringException e )
        {
            if( e.getSeverity()== ScoringException.NON_PERMANENT && e.getMessage()!=null && e.getMessage().contains("Unarchived TestKey record does not exist. Has probably been archived") )
                LogService.logIt( "TestKeyDistThread.run() ZZZ.1 ScoringException: " + e.getMessage() + " testKeyId=" + (tk==null ? "NULL" : tk.getTestKeyId() ) );
            else if( e.getSeverity()== ScoringException.NON_PERMANENT )
                LogService.logIt( "TestKeyDistThread.run() ZZZ.2 Non-Permanent " + e.toString() + " testKeyId=" + (tk==null ? "NULL" : tk.getTestKeyId() ) );
            else
                LogService.logIt( e, "TestKeyDistThread.run() ZZZ.3 PERMANENT ScoringException: " + e.getMessage() + " testKeyId=" + (tk==null ? "NULL" : tk.getTestKeyId() ) );            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestKeyDistThread.run() ZZZ.5 " + e.getMessage() + " testKeyId=" + (tk==null ? "NULL" : tk.getTestKeyId() ) );
        }
        
        // since this process is ending, remove from map.
        if( tk!=null )
            BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );  
        
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
