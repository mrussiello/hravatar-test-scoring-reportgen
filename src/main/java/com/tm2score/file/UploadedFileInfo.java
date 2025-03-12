/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.file;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.score.TextAndTitle;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class UploadedFileInfo
{
    private static String baseUrl = null;

    private UploadedUserFile uploadedUserFile;
    private TextAndTitle textAndTitle;
    private  TestEventScore testEventScore;
    private Locale locale;

    public UploadedFileInfo( TestEventScore tes, TextAndTitle tt, UploadedUserFile uuf, Locale l )
    {
        this.testEventScore = tes;
        this.textAndTitle = tt;
        this.uploadedUserFile = uuf;
        this.locale = l;
    }


    public FileContentType getFileContentType()
    {
        if( uploadedUserFile == null )
            return null;

        return uploadedUserFile.getFileContentType();

    }

    public int getMaxIconWidth()
    {
        if( uploadedUserFile == null )
            return 180;

        if( uploadedUserFile.getWidth()>0 && uploadedUserFile.getWidth()<180)
            return uploadedUserFile.getWidth();

            return 180;
    }


    public UploadedUserFile getUploadedUserFile() {
        return uploadedUserFile;
    }

    public void setUploadedUserFile(UploadedUserFile uploadedUserFile) {
        this.uploadedUserFile = uploadedUserFile;
    }

    public TextAndTitle getTextAndTitle() {
        return textAndTitle;
    }

    public void setTextAndTitle(TextAndTitle textAndTitle) {
        this.textAndTitle = textAndTitle;
    }

    public TestEventScore getTestEventScore() {
        return testEventScore;
    }

    public void setTestEventScore(TestEventScore testEventScore) {
        this.testEventScore = testEventScore;
    }



}
