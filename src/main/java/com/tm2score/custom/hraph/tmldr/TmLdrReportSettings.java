/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.tmldr;

import com.itextpdf.text.Image;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.CT2ReportSettings;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportSettings;
import com.tm2score.service.LogService;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Mike
 */
public class TmLdrReportSettings extends CT2ReportSettings implements ReportSettings {


     public static String fiveCircleImageFilename = "tlgraphic.png";

     public Image fiveCircleImage;


    public void initExtra(ReportData reportData) throws Exception {


        if (fiveCircleImage == null) {

            String imgBaseUrl = RuntimeConstants.getStringValue("baseurl") + "/resources/images/tmldr";

            fiveCircleImage = ITextUtils.getITextImage( getLocalImageUrl( imgBaseUrl, fiveCircleImageFilename ) );

            fiveCircleImage.scalePercent( 70 );

        }

    }

    public URL getLocalImageUrl(String baseUrl, String fn) {
        return com.tm2score.util.HttpUtils.getURLFromString(baseUrl + "/" + fn);
    }




}
