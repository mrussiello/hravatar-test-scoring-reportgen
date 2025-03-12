/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.global.ErrorTxtObject;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class ScoringException extends Exception
{
    public static final int NON_PERMANENT = 0;
    public static final int PERMANENT = 1;

    // 0 = not permanent. 1=permanent
    private int severity = 0;
    private ErrorTxtObject errorTxtObj;
    
        


    public ScoringException( String message, int severity, ErrorTxtObject eo)
    {
        super( message );
        this.severity = severity;
        //this.typeId = typeId;
        
        errorTxtObj = eo;
        if( errorTxtObj != null && message != null )
            errorTxtObj.appendErrorTxt(new Date().toString() + " " + message );
    }

    public int getSeverity() {
        return severity;
    }

    @Override
    public String toString()
    {
        return "ScoringException { " + getMessage() + ", severity=" + this.severity + ", errorTxtObj=" + (errorTxtObj==null ? "null" : errorTxtObj.toString() ) + " }";

    }

    public ErrorTxtObject getErrorTxtObj() {
        return errorTxtObj;
    }

}
