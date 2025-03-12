/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.vwga;

import com.itextpdf.text.ListItem;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * Report Rules:
 *
 * ct3risktoend=1 means place risk factors at the end of the report
 * ct3riskremove=1 means no risk factors in the report, anywhere.
 *
 * allnointerview=1 means do not include the interview guide
 *
 *
 * @author Mike
 */
public abstract class BaseVWGAReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    
    @Override
    public void init( ReportData rd ) throws Exception
    {
        super.init(rd);
    }
    

    public void addStrengthsWeaknessesSection() throws Exception
    {
        LogService.logIt( "BaseVWGAReportTemplate.addStrengthsWeaknessesSection() START" );
        try
        {
            if( reportData.getReportRuleAsBoolean( "strengthsweaknessesoff")  )
                return;            

            boolean includeStandardHra = true;
                        
            float strengthsCutoff = reportData.getReportRuleAsFloat("strengthscutoff");
            float weaknessesCutoff = reportData.getReportRuleAsFloat("weaknessescutoff");
            
            if( strengthsCutoff<=0 )
                strengthsCutoff = 75f;

            if( weaknessesCutoff<=0 )
                weaknessesCutoff = 45f;
            
            List<TestEventScore> tesl = reportData.getTestEvent().getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            List<TestEventScore> stesl = new ArrayList<>();
            List<TestEventScore> wtesl = new ArrayList<>();
            
            for( TestEventScore tes : tesl )
            {
                if( tes.getScore()<=0 )
                {
                    LogService.logIt( "BaseVWGAReportTemplate.addStrengthsWeaknessesSection() Skipping competency because score 0 or lower. name=" + tes.getName() );
                    continue;
                }
                
                if( tes.getHide()!=0 && tes.getHide()!=3 )
                {
                    LogService.logIt( "BaseVWGAReportTemplate.addStrengthsWeaknessesSection() Skipping competency because tes.hide=" + tes.getHide() +", name=" + tes.getName() );
                    continue;
                }
                
                if( tes.getName().toLowerCase().contains("practice") )
                {
                    LogService.logIt( "BaseVWGAReportTemplate.addStrengthsWeaknessesSection() Skipping competency because name contains practice. name=" + tes.getName() );
                    continue;
                }
                
                if( !includeStandardHra && !tes.getSimCompetencyClass().getIsCombo() )
                {
                    LogService.logIt( "BaseVWGAReportTemplate.addStrengthsWeaknessesSection() Skipping competency it's not a Combo class. name=" + tes.getName() );
                    continue;
                }
                
                if( tes.getScore()<=weaknessesCutoff )
                    wtesl.add( tes );

                if( tes.getScore()>=strengthsCutoff )
                    stesl.add( tes );
            }
            
            Collections.sort(stesl );
            Collections.sort(wtesl );

            previousYLevel =  currentYLevel;

            PdfPCell c;
            PdfPTable t;
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            float y = previousYLevel;
            
            com.itextpdf.text.List cl;
            ListItem li;
            
            
            // y = addTitle( previousYLevel, "Candidate's Strengths", null );
            t = new PdfPTable( new float[] { 1f } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 0 );

            c = new PdfPCell(new Phrase( "Candidate's Strengths" , fontBold));
            c.setColspan(1);
            setRunDirection(c);
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(PAD);
            t.addCell(c);

            cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
            cl.setListSymbol( "\u2022");
            cl.setIndentationLeft( 10 );
            cl.setSymbolIndent( 10 );

            if( !stesl.isEmpty() )
            {
                for( TestEventScore tes : stesl  )                        
                {
                    li = new ListItem( 9,  tes.getName(), getFont() );
                    // li.setPaddingTop(5 );
                    li.setSpacingAfter( 5 );
                    cl.add( li );
                }
            }
            else
                cl.add( new ListItem( 9,  "No areas of strength noted.", getFont() ) );

            c = new PdfPCell();
            c.addElement(cl);
            c.setColspan(1);
            setRunDirection(c);
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(PAD);
            t.addCell(c);

            currentYLevel = addTableToDocument( y, t, true, true );
            y = currentYLevel + PAD;
            
            // y = addTitle( previousYLevel, "Candidate's Development Areas", null );
            t = new PdfPTable( new float[] { 1f } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 0 );

            c = new PdfPCell(new Phrase( "Candidate's Development Areas" , fontBold));
            c.setColspan(1);
            setRunDirection(c);
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(PAD);
            t.addCell(c);

            cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
            cl.setListSymbol( "\u2022");
            cl.setIndentationLeft( 10 );
            cl.setSymbolIndent( 10 );
            

            if( !wtesl.isEmpty() )
            {
                for( TestEventScore tes : wtesl  )                        
                {
                    li = new ListItem( 9,  tes.getName(), getFont() );
                    // li.setPaddingTop(5 );
                    li.setSpacingAfter( 5 );
                    cl.add( li );
                }
            }
            else
                cl.add( new ListItem( 9,  "No development areas noted.", getFont() ) );

            c = new PdfPCell();
            c.addElement(cl);
            c.setColspan(1);
            setRunDirection(c);
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(PAD);
            t.addCell(c);

            currentYLevel = addTableToDocument( y, t, true, true );
            
            previousYLevel = currentYLevel;
            
            // this.addNewPage();
                
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseVWGAReportTemplate.addStrengthsWeaknessesSection()" );
            throw new STException( e );
        }
    }    
    
    
    
  
}
