/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.tm2score.service.LogService;
import java.io.ByteArrayOutputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author miker_000
 */
public class AudioAppender {
    
    public static byte[] getAppendedAudio( AudioInputStream clip1, AudioInputStream clip2 )
    {
        try 
        {
            AudioInputStream appendedFiles = 
                            new AudioInputStream(
                                new SequenceInputStream(clip1, clip2),     
                                clip1.getFormat(), 
                                clip1.getFrameLength() + clip2.getFrameLength());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            AudioSystem.write(appendedFiles, 
                            AudioFileFormat.Type.WAVE, 
                            baos );
            
            return baos.toByteArray();
            
        } catch (Exception e) 
        {
            LogService.logIt( e, "AudioAppender.getAppendedAudio() " );
            return null;
        }        
    }

    public static byte[] getAppendedAudio( List<AudioInputStream> clipList )
    {
        if( clipList==null || clipList.isEmpty() )
            return null;
        
        AudioInputStream clip1 = clipList.get(0);
        
        AudioFormat format = clip1.getFormat();
        long frameLen = 0;
        for( AudioInputStream ais : clipList )
        {
            frameLen+=ais.getFrameLength();
        }
        
        try 
        {
            LogService.logIt( "AudioAppender.getAppendedAudio() Appending " + clipList.size() + " files. Format=" + format.toString() );            
            
            AudioInputStream appendedFiles = 
                            new AudioInputStream(   new SequenceInputStream(Collections.enumeration(clipList)),     
                                                    clip1.getFormat(), 
                                                    frameLen);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            AudioSystem.write(appendedFiles, 
                              AudioFileFormat.Type.WAVE, 
                              baos );
            
            return baos.toByteArray();
            
        } catch (Exception e) 
        {
            LogService.logIt( e, "AudioAppender.getAppendedAudio() " );
            return null;
        }        
    }

    
}
