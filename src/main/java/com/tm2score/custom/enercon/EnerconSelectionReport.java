/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.enercon;

import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import java.util.Collections;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class EnerconSelectionReport extends BaseCT2ReportTemplate implements ReportTemplate
{
    /*
    private static final String[] customScoreTextHighest = new String[]{
        "Likely to be a practical and no-nonsense engineer. Theory is not the biggest draw to engineering, building and fixing things is. This engineer might have left school a little earlier (if it were possible) and got to work building and fixing things. Likes to work independently, be free to take action, and not be burdened by structure, policies, or established routines. Does not like paperwork. Is more traditional and focused than open-minded and analytical.", // Realistic
        "Likely to be an analytical and precise engineer. A lack of precision means not enough research and learning has been done. May have been tempted to stay in school longer and learn more. Spends time reading and learning and has interests in sciences other than engineering. May even like to write about scientific findings to show the precision of thought and classification that was achieved. Often described by others as ‘analytical.’", // I
        "Likely to be an engineer who is an independent thinker. Places high value on a creative, unique, and correct solution. The uniqueness of their work is a part of their self-expression. Their artistic flare may also lead them to be fans of the arts, enjoy creative writing, and value form over function.", // A
        "The data from the validation study suggest that, typically, engineers with Social as their highest work interest will not be fulfilled and engaged in an engineering role at Enercon. However, if you are processing an applicant for whom Social is their highest-rated work interest, continue to examine this entire score report. If Social is their highest score, good job fit will be very unlikely without having Realistic or Investigative in their top three work interests. Because engineers’ work focuses on mechanical and practical tasks, the work is typically not humanistic enough for Socials.", // S
        "Likely to be an energetic and gregarious engineer. Enterprising engineers gravitate toward jobs involving people since they desire to influence people’s thoughts and actions. They like building a coalition because it creates more power to influence. Likes to lead and maybe even reads about leadership, even if not a big reader otherwise. High Enterprisers are sometimes described as dominant.", // E
        "Likely to be an organized and precise engineer. Personal motto is: there is a right place for everything, and everything should be in its right place. The only thing better than making a checklist is getting to check off all the items on the list. Details make life worth living. They are also likely to value hierarchy and lines of authority in the organization. They are cautious and risk-averse.", // C
    };

    private static final String[] customScoreTextTop3 = new String[]{
        "Pursues clear solutions to specific problems. Likely to prefer working with things rather than people. Generally prefers jobs that require movement rather than being stationary. Likely an outdoorsy type and nature enthusiast. Likes hands-on work with machines and tools.", // Realistic
        "More likely to enjoy methodical work. Solutions result from a good collision of ideas, information, and data. Likes math, reading, gathering information, and maybe even conducting experiments. May enjoy studying, learning, and experimenting for the sake of knowledge itself.", // I
        "Like to work independently in a work environment that feels conducive to discovery. When Artistic is paired with Realistic and/or Investigative in the top three work interests, engineers are more imaginative and open-minded. More likely than their peers without Artistic in their profile to enjoy writing, the arts, and aesthetics.", // A
        "Likely to be a cooperative, helpful, and patient engineer. Be careful not to confuse Social work interests with the personality trait Extraversion/Extraverted. Not all Socials are people-people. Although Socials tend to value things like teamwork and cooperation, they value those things because they are often needed to make an impact on the lives of others. For engineers with Social in their work interest profile, their desire to make life or knowledge better for people should be partnered with an interest in Realistic and/or Investigative work.", // S
        "More likely to be persuasive, competitive, and optimistic engineers. Though not typical engineering tasks, marketing, negotiating, and selling are likely to be enjoyable tasks for Enterprising Engineers. They will be more willing than other engineers to take a risk to make an impact.", // E
        "These engineers will likely be thorough in their work and expect the same from their coworkers. They want the goals for their job to be well-documented and not made up as they go along. Tasks that involve data and categories fit these engineers well because they can precisely sort the data into categories. These engineers tend to be thorough, methodical, and conservative.", // C
    };

    private static final String[] customScoreTextPitfalls = new String[]{
        "Common Pitfalls for Highly Realistic Engineers:\n - Acts first and ask questions later\n - Overlooks precision to get to a solution\n - Goes straight to a solution without analyzing the problem enough", // Realistic
        "Common Pitfalls for Highly Investigative Engineers:\n" +
" - Paralysis by analysis\n" +
" - Considering too many scenarios before taking action\n" +
" - Not getting 'good' work done because too much time spent in search of 'perfect'", // I
        "Common Pitfalls for Highly Artistic Engineers:\n" +
" - Feeling stifled by routine tasks\n" +
" - The 'business' interfering with their craft\n" +
" - Discovery curtailed by productivity demands", // A
        "Common Pitfalls for Highly Social Engineers:\n" +
" - Not enough people interaction\n" +
" - The ‘business’ comes before helping people\n" +
" - Not speaking up when they see a problem", // S
        "Common Pitfalls for Highly Enterprising Engineers:\n" +
" - Can be manipulative\n" +
" - Avoids in-depth research\n" +
" - Ignores important information", // E
        "Common Pitfalls for Highly Conventional Engineers:\n" +
" - Overly critical \n" +
" - Easily stressed out by ambiguity\n" +
" - Becomes Frustrated with imprecise or undocumented processes" // C
    };
    */
    
    
    public static String[] howToUseThisReport = new String[]{"How to Use This Report", "Review the candidate's overall job-fit scores. Determine if the scores indicate a better fit for Power or Nuclear. Some candidates may be a good fit for either role. Next, use the individual competency score narratives to evaluate fit in more depth. Finally, if interviewing the candidate, select interview questions to help probe relevant competency scores further. Use the interview question scoring anchors to maximize the reliability of interview scoring across candidates. Taken together, the assessment scores and interview scores will provide a more accurate profile of job fit than either one by itself."};
            
    
    public EnerconSelectionReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "EnerconSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();

            addResponseRatingSection();

            addComparisonSection();

            if( reportData.getReportRuleAsBoolean("cmptysumoff") ||
                reportData.getReport().getIncludeCompetencyScores()!=1 ||
                ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
                  reportData.getReport().getIncludeSubcategoryNumeric()!=1 && 
                  reportData.getReport().getIncludeCompetencyColorScores()!=1 )  )            
            {}
            else
                addNewPage();
            
            addAssessmentOverview();

            addDetailedReportInfoHeader();
            
            addHowToUseThisReportSection();
            
            addAltScoreSection();            

            // Tasks before competencies
            if( reportData.getReport().getIncludeTaskInfo() == 1)
                addTasksInfo();

            
            addAbilitiesInfo();

            addInterestsInfo();

            addKSInfo();

            addAIMSInfo();

            addEQInfo();
            
            addBiodataInfo();

            addAIInfo();

            addWritingSampleInfo();
            
            addIbmInsightSection();

            // LogService.logIt( "EnerconSelectionReport");
            
            if( !reportData.getReportRuleAsBoolean("hideimagecaptureinfo") )
                addIdentityImageCaptureSection();

            addProctorCertificationsSection();
            
            addSuspiciousActivitySection();

            addSuspensionsSection();

            addItemScoresSection();


            // addCompetencyInfo();

            // Tasks after competencies
            if( reportData.getReport().getIncludeTaskInfo() == 2)
                addTasksInfo();

            addTopJobMatchesSummarySection();

            addMinQualsApplicantDataInfo();

            addEducTrainingInfo();

            addReportRiskFactorSection();

            addUploadedFilesSection();

            addPreparationNotesSection();

            addCalculationSection(true);

            if( !reportData.getReportRuleAsBoolean( "usernotesoff" ) )
                addNewPage();

            addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EnerconSelectionReport.generateReport() " );
            throw new STException( e );
        }
    }

    protected void addHowToUseThisReportSection() throws Exception
    {
        try
        {
            float startY = this.currentYLevel-2*PAD;
            addTitle( startY, howToUseThisReport[0], howToUseThisReport[1] );           
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EnerconSelectionReport.addHowToUseThisReportSection() " );
            throw new STException( e );
        }
    }
    
    
    @Override
    protected void addAbilitiesInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY ); // new ArrayList<>();

            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY_COMBO ) );

            if( tesl.size() <= 0 )
                return;

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            // LogService.logIt( "EnerconSelectionReport.addAbilitiesInfo() found " + tesl.size() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            
            addAnyCompetenciesInfo(tesl, "g.AbilitiesTitle", ttext, "g.AbilitiesSubtitle", sttext, "g.Detail", "g.Description", null, null, true, showInterview, false, true  );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EnerconSelectionReport.addAbilitiesInfo()" );
            throw new STException( e );
        }
    }
    
    
    
    /*
    @Override
    public String getScoreTextForCompetency( TestEventScore tes )
    {
        // LogService.logIt( "EnerconSelectionReport.getScoreTextForCompetency() tes.name=" + tes.getName() + ", is interest=" + tes.getSimCompetencyClass().getIsInterest() );
        if( tes==null ) // || !tes.getSimCompetencyClass().getIsInterest() )        
            return reportData.getCompetencyScoreText( tes ); // tes.getScoreText();  
        
        int cat = getRiasecCategory( tes );
        if( cat<0 || cat>5 )
        {
            LogService.logIt( "EnerconSelectionReport.getScoreTextForCompetency() tes.name=" + tes.getName() + ", nameEnglish=" + tes.getNameEnglish() + ", unable to gind RIASEC category for TestEventScore. simCompetencyClassId=" + tes.getSimCompetencyClassId() );
            return reportData.getCompetencyScoreText( tes );
        }
        
        boolean isHighestInterest = true;        
        int countAbove = 0;
        for( TestEventScore tesx : reportData.getTestEvent().getTestEventScoreList() )
        {
            if( !tesx.getSimCompetencyClass().equals( tes.getSimCompetencyClass() ) )
                continue;
            
            if( !tesx.equals( tes ) && tesx.getScore()>tes.getScore() )
            {
                isHighestInterest=false;
                countAbove++;
            }
        }
        boolean isTopThree = countAbove<3;
        
        if( isHighestInterest )
            return customScoreTextHighest[cat] + "\n\n"+ customScoreTextPitfalls[cat];
        
        if( isTopThree )
            return customScoreTextTop3[cat] + "\n\n"+ customScoreTextPitfalls[cat];
            
        // TODO
        return "";
    }
    */
    
    /*
      0=R
      1=I
      2=A
      3=S
      4=E
      5=C
    */
    private int getRiasecCategory( TestEventScore tes )
    {
        
        // TESTING ONLY
        if( 1==2 )
            return 1;
        
        if( tes==null )
            return -1;
        
        if( tes.getName().toLowerCase().contains("realistic") || (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().contains("realistic") ) )
            return 0;

        if( tes.getName().toLowerCase().contains("investigative") || (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().contains("investigative") ) )
            return 1;

        if( tes.getName().toLowerCase().contains("artistic") || (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().contains("artistic") ) )
            return 2;

        if( tes.getName().toLowerCase().contains("social") || (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().contains("social") ) )
            return 3;

        if( tes.getName().toLowerCase().contains("enterprising") || (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().contains("enterprising") ) )
            return 4;

        if( tes.getName().toLowerCase().contains("conventional") || (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().contains("conventional") ) )
            return 5;
        
        return -1;
        
    }

    

    @Override
    public boolean showInterpretationForTes( TestEventScore tes )
    {
        if( tes.getSimCompetencyClass().isAbility() || tes.getSimCompetencyClass().getIsInterest())
            return false;
        
        return super.showInterpretationForTes(tes);
    }

    
    

}
