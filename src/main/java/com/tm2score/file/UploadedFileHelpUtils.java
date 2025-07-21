/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.file;

import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.service.LogService;
import com.tm2score.util.MsWordUtils;
import com.tm2score.util.PdfUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.IntnHist;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 *
 * @author miker
 */
public class UploadedFileHelpUtils {
    
    FileUploadFacade fileUploadFacade;
    
    public String parseUploadedUserFileForText( UploadedUserFile uuf ) throws Exception
    {
        if( uuf==null )
        {
            LogService.logIt( "UploadedFileHelpUtils.parseUploadedUserFileForText() uploadedUserFile is null!" );
            return null;
        }

        if( uuf.getUploadedText()!=null && !uuf.getUploadedText().isBlank() )
            return uuf.getUploadedText();
            
        // String initFilename = null;
        InputStream fis=null;
        
        try
        {
            // initFilename = uuf.getInitialFilename();
            String uploadedFilename = uuf.getFilename();

            if( !uploadedFilename.toLowerCase().endsWith(".pdf") && !uploadedFilename.toLowerCase().endsWith(".doc") && !uploadedFilename.toLowerCase().endsWith(".docx") && !uploadedFilename.toLowerCase().endsWith(".txt") )
                throw new Exception( "UploadedUserFile has an unrecognized file type. uploadedFilename=" + uploadedFilename );

            String cntntHdr = uuf.getMime(); // uf.getHeader( "content-type" );

            FileContentType fct  = FileContentType.getFileContentTypeFromContentType(cntntHdr, uploadedFilename);

            if( !fct.getIsPdf() && !fct.getIsWord() && !fct.equals(FileContentType.TEXT_PLAIN) )
                throw new Exception( "Uploaded File has an unrecognized file type. uploadedFilename=" + uploadedFilename + ", fileContentType=" + fct.toString());
            
            FileXferUtils fxfer = new FileXferUtils();
            
            fis = fxfer.getFileInputStream( uuf.getDirectory(), uuf.getFilename(), BucketType.USERUPLOAD.getBucketTypeId() );            
            if( fis==null )
                throw new Exception( "Could not obtain InputStream for uploaded resume file. uploadedUserFileId=" + (uuf==null ? "null" : uuf.getUploadedUserFileId() ));
            
            String text;

            if( fct.getIsPdf() )
            {
                text = PdfUtils.convertPdfToText(fis);
            }
            else if( fct.getIsWord() )
            {
                text = MsWordUtils.convertWordToText(fis, uploadedFilename);
                if( (text==null || text.isBlank()) && fct.getBaseExtension().equalsIgnoreCase("docx") )
                    LogService.logIt( "UploadedFileHelpUtils.parseUploadedUserFileForText() unable to parse Word document. It may be an old .doc version. uploadedFilename=" + uploadedFilename );
            }
            else
            {
                try (Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8))
                {
                    text = scanner.useDelimiter("\\A").next();
                }
            }

            LogService.logIt( "UploadedFileHelpUtils.parseUploadedUserFileForText() text.length=" + (text==null ? "null" : text.length() ) );
            
            if( text!=null && !text.isBlank() )
            {
                uuf.setUploadedText( text );
                if( fileUploadFacade==null )
                    fileUploadFacade=FileUploadFacade.getInstance();
                fileUploadFacade.saveUploadedUserFile(uuf);
            }
            
            return text;
            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "UploadedFileHelpUtils.parseUploadedUserFileForText() uploadedUserFileId=" + (uuf==null ? "null" : uuf.getUploadedUserFileId() ) );
            return null;
        }
        finally
        {
            if( fis!=null )
                fis.close();
        }
    }

    public UploadedUserFile getUploadedUserFile( long testEventId, int ct5ItemId, int ct5ItemPartId ) throws Exception
    {
        try
        {
            if( testEventId<=0 || ct5ItemId<=0 || ct5ItemPartId<=0 )
            {
                LogService.logIt( "UploadedFileHelpUtils.getUploadedUserFile() params invalid. testEventId=" + testEventId + ", ct5ItemId=" + ct5ItemId + ", ct5ItemPartId=" + ct5ItemPartId );
                return null;
            }
            if( fileUploadFacade==null )
                fileUploadFacade=FileUploadFacade.getInstance();
            return fileUploadFacade.getUploadedUserFile(testEventId, ct5ItemId, ct5ItemPartId, UploadedUserFileType.RESPONSE.getUploadedUserFileTypeId(), true);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "UploadedFileHelpUtils.getUploadedUserFile() testEventId=" + testEventId + ", ct5ItemId=" + ct5ItemId + ", ct5ItemPartId=" + ct5ItemPartId );
            throw e;
        }
    }

    
    public UploadedUserFile getUploadedUserFile( long uploadedUserFileId ) throws Exception
    {
        try
        {
            if( uploadedUserFileId<=0 )
            {
                LogService.logIt( "UploadedFileHelpUtils.getUploadedUserFile() uploadedUserFileId invalid: " + uploadedUserFileId );
                return null;
            }
            if( fileUploadFacade==null )
                fileUploadFacade=FileUploadFacade.getInstance();
            return fileUploadFacade.getUploadedUserFile(uploadedUserFileId, true);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "UploadedFileHelpUtils.getUploadedUserFile() uploadedUserFileId=" + uploadedUserFileId );
            throw e;
        }
    }
    
    public long getUploadedUserFileIdFromResultXml( IntnHist resultIntnObj ) throws Exception
    {
        String val = null;
        String v = null;
        String uuidStr = null;

        try
        {
            if( resultIntnObj==null )
            {
                LogService.logIt( "UploadedFileHelpUtils.getUploadedUserFileIdFromResultXml() resultIntnObj is null" );
                return 0;
            }

            val = resultIntnObj.getValue();
            if( val==null || val.isBlank() )
            {
                LogService.logIt( "UploadedFileHelpUtils.getUploadedUserFileIdFromResultXml() value is " + (val==null ? "null" : "blank") );
                return 0;
            }

            String[] pairs = val.split("~");
            if( pairs.length<2 )
            {
                LogService.logIt( "UploadedFileHelpUtils.getUploadedUserFileIdFromResultXml() value cannot be parsed: " + val );
                return 0;
            }

            String subnodeIdStr = null;
            for( int i=0;i<pairs.length-1; i+=2 )
            {
                subnodeIdStr = pairs[i];
                v = pairs[i+1];
                if( subnodeIdStr.equals("99999995") && v.startsWith("medcap") )
                {
                    v = StringUtils.getUrlDecodedValue(v).trim();
                    LogService.logIt( "UploadedFileHelpUtils.getUploadedUserFileIdFromResultXml() have value=" + v );
                    String[] parts = v.split(";");

                    if( parts.length>=4 )
                    {
                        uuidStr = parts[3];
                        return Long.parseLong(uuidStr);
                    }
                }
            }
            return 0;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "UploadedFileHelpUtils.getUploadedUserFileIdFromResultXml() resultXml.value=" + val  + ", v=" + v + ", uuidStr=" + uuidStr );
            throw e;
        }
    }
}
