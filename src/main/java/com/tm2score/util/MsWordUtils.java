/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.util;

import com.tm2score.service.LogService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 *
 * @author miker
 */
public class MsWordUtils 
{
    
public static String convertWordToTextFile(File wordFile) 
{
        String text = "";
        try (FileInputStream fis = new FileInputStream(wordFile))
        {
            if (wordFile.getName().toLowerCase().endsWith(".doc")) 
            {
                HWPFDocument document = new HWPFDocument(fis);
                try (WordExtractor extractor = new WordExtractor(document)) 
                {
                    text = extractor.getText();
                }
            } 
            else if (wordFile.getName().toLowerCase().endsWith(".docx")) 
            {
                XWPFDocument document = new XWPFDocument(fis);
                try (XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    text = extractor.getText();
                }
            }
            fis.close();
        } 
        catch(IndexOutOfBoundsException | IOException e) 
        {
            LogService.logIt( "MsWordUtils.convertWordToTextFile() " + e.toString() +", " + (wordFile==null ? "null" : wordFile.getName() + ", " + wordFile.getAbsolutePath()) );
            return null;
        }
        catch(Exception e) 
        {
            LogService.logIt( e, "MsWordUtils.convertWordToTextFile() " + (wordFile==null ? "null" : wordFile.getName() + ", " + wordFile.getAbsolutePath()) );
            return null;
        }
        return text;
    }
    
    public static String convertWordToText(InputStream fis, String filename ) 
    {
        String text = "";
        try
        {
            filename = filename.toLowerCase();
            if (filename.endsWith(".doc")) 
            {
                HWPFDocument document = new HWPFDocument(fis);
                try (WordExtractor extractor = new WordExtractor(document)) 
                {
                    text = extractor.getText();
                }
            } 
            else if (filename.endsWith(".docx")) 
            {
                XWPFDocument document = new XWPFDocument(fis);
                try (XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    text = extractor.getText();
                }
            }
            fis.close();
        } 
        catch(IOException e) 
        {
            LogService.logIt( "MsWordUtils.convertWordToText() " + e.toString() +", " + (filename==null ? "null" : filename) );
            return null;
        }
        catch(Exception e) 
        {
            LogService.logIt( e, "MsWordUtils.convertWordToText() " + (filename==null ? "null" : filename) );
            return null;
        }
        return text;
    }



}
