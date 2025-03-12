/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.twilio;

/**
 *
 * @author miker_000
 */
public class TwilioException extends Exception {
    
    
    public TwilioException( String message )
    {
        super(message);
    }

    @Override
    public String toString() {
        return "TwilioException message=" + super.getMessage();
    }
    
    
}
