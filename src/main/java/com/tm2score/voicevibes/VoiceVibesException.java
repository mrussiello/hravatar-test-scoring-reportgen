/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.voicevibes;

/**
 *
 * @author miker_000
 */
public class VoiceVibesException extends Exception {    
        
    
    public VoiceVibesException( Exception e )
    {
        super( e.getMessage() );
    }
    
    public VoiceVibesException( String msg )
    {
        super( msg );
    }
          
    
}
