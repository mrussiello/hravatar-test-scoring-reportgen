/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import com.tm2score.report.ReportSettings;
import com.tm2score.format.*;
import com.tm2score.report.ReportData;
import com.tm2score.score.CaveatScore;
import com.tm2score.service.LogService;
import java.util.List;

/**
 *
 * This class is used to make any CoreTest-Specific Changes to the Standard
 * ReportSettings
 *
 * @author Mike
 */
public class CTReportSettings extends StandardReportSettings implements ReportSettings {

    ReportData reportData;

    @Override
    public void initSettings(ReportData reportData) throws Exception
    {
        super.initSettings(reportData);
        this.reportData = reportData;
    }

    public void initColors()
    {
        // Nothing. 
    }

    public PdfPTable getCaveatScoreTable(List<CaveatScore> csl, Font fontToUse) throws Exception
    {
        if (csl == null || csl.isEmpty())
            return null;

        try
        {
            PdfPTable t = new PdfPTable(2);
            setRunDirection(t);
            t.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell c;
            for (CaveatScore cs : csl)
            {
                if (cs.getLocale() == null)
                    cs.setLocale(reportData.getLocale());

                c = new PdfPCell(new Phrase("\u2022 " + cs.getCol1() + ":", fontToUse));
                if(cs.getCaveatScoreType().getColspan()>1)
                    c.setColspan(2);
                else
                    c.setColspan(1);

                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setVerticalAlignment( Element.ALIGN_TOP);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);
                
                if(cs.getCaveatScoreType().getColspan()>1)
                    continue;
                
                c = new PdfPCell(new Phrase(cs.getCol2(), fontToUse));
                c.setColspan(1);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);
            }

            return t;
        } catch (Exception e)
        {
            LogService.logIt(e, "CtReportSettings.getCaveatScoreTable() ");
            throw e;
        }
    }

    public void setRunDirection(PdfPTable t)
    {
        if (t == null || reportData == null || reportData.getLocale() == null)
            return;

        t.setRunDirection(reportData.getTextRunDirection());

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    public void setRunDirection(PdfPCell c)
    {
        if (c == null || reportData == null || reportData.getLocale() == null)
            return;

        // if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        c.setRunDirection(reportData.getTextRunDirection());
    }

}
