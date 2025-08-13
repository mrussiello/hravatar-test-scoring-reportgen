/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.user;

import com.tm2score.api.AssessmentResult;
import com.tm2score.api.AssessmentStatusCreator;
import com.tm2score.dist.DistManager;
import com.tm2score.entity.event.SurveyEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.user.LogonHistory;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.faces.FacesUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.reminder.ReminderUtils;
import com.tm2score.report.ReportManager;
import com.tm2score.report.TestEventRegenReportThread;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoringException;
import com.tm2score.score.TestEventRescoreThread;
import com.tm2score.score.TestKeyEventSelectionType;
import com.tm2score.score.TestKeyRescoreThread;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.twilio.TwilioSmsUtils;
import com.tm2score.service.Tracker;
import com.tm2score.util.GooglePhoneUtils;
import com.tm2score.util.STHttpSessionListener;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Mike
 */
@Named
@RequestScoped
public class UserUtils extends FacesUtils {

    private String logonName;

    private String logonKey;

    private UserFacade userFacade;
    private EventFacade eventFacade;

    private UserBean userBean;

    private boolean booleanParam1 = false;

    private String strParam1 = null;
    private String strParam2 = null;
    private String strParam3 = null;

    public static UserUtils getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (UserUtils) fc.getApplication().getELResolver().getValue(fc.getELContext(), null, "userUtils");
    }

    public UserBean getUserBean()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (userBean == null)
            userBean = (UserBean) fc.getApplication().getELResolver().getValue(fc.getELContext(), null, "userBean");

        return userBean;
    }

    public StreamedContent getReportFileToDownload()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return null;
            }

            TestEventScore tes = userBean.getTestEventScore();

            // LogService.logIt( "UserUtils.getReportFileToDownload() tes=" + (tes==null ? "null" : tes.toString() ) );
            if (tes == null)
                return null;

            byte[] bytes = tes.getReportBytes();

            // LogService.logIt( "UserUtils.getReportFileToDownload() report size is " + bytes.length );
            ByteArrayInputStream baos = new ByteArrayInputStream(bytes);

            return DefaultStreamedContent.builder().contentType("application/pdf").name(tes.getReportFilename()).stream(() -> baos).build();
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.getReportFileToDownload() ");

            return null;
        }
    }

    public String getOrgIdsToSkip()
    {
        String s = RuntimeConstants.getStringValue("OrgIdsToSkip");
        if (s == null || s.isBlank())
            return "";

        return s;
    }

    public String processPerformScoreBatchArchive()
    {
        return performScoreBatch(true);
    }

    public String processPerformScoreBatch()
    {
        return performScoreBatch(false);
    }

    private String performScoreBatch(boolean withArchive)
    {
        try
        {
            LogService.logIt("UserUtils.processPerformScoreBatch() STARTING withArchive=" + withArchive);

            /*
            LogService.logIt( "UserUtils.processPerformScoreBatch() Parse INTS: ''=" + NumberUtils.parseIntegerInputStr( Locale.US, "" ) + "\n" +
                    "ppp=" + NumberUtils.parseIntegerInputStr( Locale.US, "ppp" ) + "\n" +
                    "ppp5=" + NumberUtils.parseIntegerInputStr( Locale.US, "ppp5" ) + "\n" +
                    "5ppp5=" + NumberUtils.parseIntegerInputStr( Locale.US, "5ppp5" ) + "\n" +
                    "-1=" + NumberUtils.parseIntegerInputStr( Locale.US, "-1" ) + "\n" +
                    "239=" + NumberUtils.parseIntegerInputStr( Locale.US, "239" ) + "\n" +
                    "23-9=" + NumberUtils.parseIntegerInputStr( Locale.US, "23-9" ) + "\n" +
                    "$1=" + NumberUtils.parseIntegerInputStr( Locale.US, "$1" ) + "\n" +
                    "1@=" + NumberUtils.parseIntegerInputStr( Locale.US, "1@" ) + "\n" +
                    "111 111 022=" + NumberUtils.parseIntegerInputStr( Locale.US, "111 111 022" ) + "\n" +
                    "111,111.022=" + NumberUtils.parseIntegerInputStr( Locale.US, "111,111.022" ) + "\n" +
                    "111 111,022 (GERMANY)=" + NumberUtils.parseIntegerInputStr( Locale.GERMANY, "111 111,022" ) + "\n" +
                    "111.111.111,022 (SPAIN)=" + NumberUtils.parseIntegerInputStr( Locale.of("ES"), "111 111.111,022" ) );

            LogService.logIt( "UserUtils.processPerformScoreBatch() Parse FLOATS: ''=" + NumberUtils.parseFloatInputStr( Locale.US, "" ) + "\n" +
                    "ppp=" + NumberUtils.parseFloatInputStr( Locale.US, "ppp" ) + "\n" +
                    "-1=" + NumberUtils.parseFloatInputStr( Locale.US, "-1" ) + "\n" +
                    "239=" + NumberUtils.parseFloatInputStr( Locale.US, "239" ) + "\n" +
                    "23-9=" + NumberUtils.parseFloatInputStr( Locale.US, "23-9" ) + "\n" +
                    "$1=" + NumberUtils.parseFloatInputStr( Locale.US, "$1" ) + "\n" +
                    "1@=" + NumberUtils.parseFloatInputStr( Locale.US, "1@" ) + "\n" +
                    "111 111 022=" + NumberUtils.parseFloatInputStr( Locale.US, "111 111 022" ) + "\n" +
                    "111,111.022=" + NumberUtils.parseFloatInputStr( Locale.US, "111,111.022" ) + "\n" +
                    "111111.0222=" + NumberUtils.parseFloatInputStr( Locale.US, "111111.0222" ) + "\n" +
                    "111 111,022 (GERMANY)=" + NumberUtils.parseFloatInputStr( Locale.GERMANY, "111 111,022" ) + "\n" +
                    "111.111.111,022 (SPAIN)=" + NumberUtils.parseFloatInputStr( Locale.of("ES"), "111.111.111,022" ) );
             */
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("Unauthorized Action.");
                return "StayInSamePlace";
            }

            if (!ScoreManager.OK_TO_START_ANY)
            {
                this.setStringErrorMessage("Cannot start a new batch - all batches are OFF");
                return "StayInSamePlace";
            }

            if (ScoreManager.BATCH_IN_PROGRESS || ReportManager.BATCH_IN_PROGRESS || DistManager.BATCH_IN_PROGRESS)
            {
                this.setStringErrorMessage("Cannot start a new batch because a batch is in progress. BATCH_IN_PROGRESS=" + ScoreManager.BATCH_IN_PROGRESS + ", ReportManager.BATCH=" + ReportManager.BATCH_IN_PROGRESS + ", DistManager.BATCH=" + DistManager.BATCH_IN_PROGRESS);
                return "StayInSamePlace";
            }

            ScoreManager sm = new ScoreManager();

            int[] data = sm.doScoreBatch(withArchive, true); // scm.scoreBatch();

            setStringInfoMessage("Score Batch Completed " + data[0] + " TestKeys scored, " + data[1] + " testEvents scored, " + data[2] + " test keys in pending status, " + data[3] + " test events in pending status, " + data[5] + " test events from partially completed batteries scored, " + data[6] + " test events from partially pending external, " + data[7] + " test keys returned to completed because not pending but has unscored testevents (probably proctoring processing)");
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processPerformScoreBatch()");
            setMessage(e);
        }

        return null;

    }

    public Date getCurrentDateTime()
    {
        return new Date();
    }

    public String processGenRegenReportsTestKeyIds()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            String testKeyIdz = userBean.getTestKeyIdz2();

            if (testKeyIdz == null || testKeyIdz.isBlank())
                throw new Exception("TestKeyIds is invalid. Need at least one testKeyId, delimited by commas.");

            List<Long> testKeyIdList = StringUtils.getLongList(testKeyIdz);

            if (testKeyIdList.isEmpty())
                throw new Exception("TestKeyIds contains no valid entries. Need at least one.");

            boolean forceCalcSection = userBean.isForceCalcSection();
            boolean forceSendCandidateReports = userBean.getSendResendCandidateReportEmails();
            Date maxLastCandidateSendDate = userBean.getMaxLastSendDate();

            if (forceSendCandidateReports && maxLastCandidateSendDate == null)
            {
                this.setStringErrorMessage("Last send date before is required when sending/resending candidate report emails.");
                return "StaySamePlace";
            }

            ReportManager rm = new ReportManager();

            for (Long testKeyId : testKeyIdList)
            {
                //if( rmb.getTestEventScoreList() == null )
                rm.setTestEventScoreList(new ArrayList<>());

                int ct = rm.genOrRegenReportsTestKey(testKeyId, userBean.getReportId(), forceCalcSection, forceSendCandidateReports, maxLastCandidateSendDate, false, RuntimeConstants.getBooleanValue("create_reports_init_as_archived"));

                // if( userBean.getReportTestEventScoreList() == null )
                userBean.setReportTestEventScoreList(new ArrayList<>());

                userBean.getReportTestEventScoreList().addAll(0, rm.getTestEventScoreList());

                if (testKeyIdList.size() < 100)
                    setStringInfoMessage("Generated " + ct + " reports for TestKeyId: " + userBean.getTestKeyId2());
            }
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processGenRegenReportsTestKeyIds()");

            setMessage(e);
        }

        return null;

    }

    public String processRegenReportsSimSimVersionId()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            long simId = userBean.getSimId2();
            int simVersionId = userBean.getSimVersionId2();
            int orgId = userBean.getSimOrgId2();
            int suborgId = userBean.getSimSuborgId2();
            boolean forceCalcSection = userBean.isForceCalcSection3();
            long minTestEventId = userBean.getMinTestEventId();
            boolean forceSendCandidateReports = userBean.getSendResendCandidateReportEmails3();
            Date maxLastCandidateSendDate = userBean.getMaxLastSendDate3();
            long reportId = userBean.getReportId4();

            Date start = userBean.getStartDate3();
            Date end = userBean.getEndDate3();

            if (simId <= 0 && orgId <= 0)
            {
                this.setStringErrorMessage("Either Sim ID (and simversionId) or orgId (and optionally subOrgId) is required.");
                return "StaySamePlace";
            }

            if (simId > 0 && simVersionId <= 0)
            {
                this.setStringErrorMessage("Sim Version ID is required when SimId is specified.");
                return "StaySamePlace";
            }

            if (forceSendCandidateReports && maxLastCandidateSendDate == null)
            {
                this.setStringErrorMessage("Last send date before is required when sending/resending candidate report emails.");
                return "StaySamePlace";
            }

            if (orgId > 0 && suborgId < 0)
                suborgId = 0;

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            List<Long> tel = eventFacade.getTestEventIdsForSimIdAndOrOrg(simId, simVersionId, orgId, suborgId, minTestEventId, TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), new int[]
            {
                TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId()
            }, start, end, 1000, 0);

            LogService.logIt("UserUtils.processRegenReportsSimSimVersionId() simId=" + simId + ", simVersionId=" + simVersionId + ", orgId=" + orgId + ", suborgId=" + suborgId + " found " + tel.size() + " test events.");

            if (tel.size() > 1)
            {
                TestEventRegenReportThread ters = new TestEventRegenReportThread(simId, simVersionId, orgId, suborgId, start, end, minTestEventId, tel, reportId, this, "Regen Reports SimId=" + simId + ", simVersionId=" + simVersionId + ", orgId=" + orgId + ", suborgId=" + suborgId, RuntimeConstants.getBooleanValue("create_reports_init_as_archived"), forceCalcSection, forceSendCandidateReports, maxLastCandidateSendDate);

                new Thread(ters).start();

                setStringInfoMessage(tel.size() + " TestEvents were identified for report generation so this process is being executed in the background. Check the system logs for information.");

                userBean.clear();

                return null;
            } else if (!tel.isEmpty())
            {
                Long teId = tel.get(0);

                ReportManager rm = new ReportManager();

                //if( rmb.getTestEventScoreList() == null )
                rm.setTestEventScoreList(new ArrayList<>());

                List<Report> rl = rm.genRegenReportTestEvent(teId, userBean.getReportId4(), forceCalcSection, false, false, false, maxLastCandidateSendDate);

                if (rl != null && !rl.isEmpty())
                {
                    String s = "";

                    for (Report r : rl)
                    {
                        if (!s.isEmpty())
                            s += " and ";

                        s += r.getName();
                    }

                    setStringInfoMessage("Generated the \'" + s + "\' report(s) for TestEventId: " + teId);

                    if (userBean.getReportTestEventScoreList() == null)
                        userBean.setReportTestEventScoreList(new ArrayList<>());

                    userBean.getReportTestEventScoreList().addAll(0, rm.getTestEventScoreList());
                } else
                    setStringErrorMessage("Did not generate any reports for TestEventId: " + teId);

            } else
                setStringInfoMessage("No matching events were found. Please revise your criteria.");
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processRegenReportsSimSimVersionId() simId=" + userBean.getSimId2() + ", simVersionId=" + userBean.getSimVersionId2());

            setMessage(e);
        }

        return null;

    }

    public String processGenRegenReportsTestEventIds()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            String testEventIdz = userBean.getTestEventIdz2();

            if (testEventIdz == null || testEventIdz.isBlank())
                throw new Exception("testEventIds is invalid. Need at least one testEventId, delimited by commas.");

            List<Long> testEventIdList = StringUtils.getLongList(testEventIdz);

            if (testEventIdList.isEmpty())
                throw new Exception("testEventIds contains no valid entries. Need at least one.");

            // if( userBean.getTestEventId2() <= 0 )
            //     throw new Exception( "TestEventId is invalid." );
            boolean forceCalcSection = userBean.isForceCalcSection2();
            boolean forceSendCandidateReports = userBean.getSendResendCandidateReportEmails2();
            Date maxLastCandidateSendDate = userBean.getMaxLastSendDate2();

            if (forceSendCandidateReports && maxLastCandidateSendDate == null)
            {
                this.setStringErrorMessage("Last send date before is required when sending/resending candidate report emails.");
                return "StaySamePlace";
            }

            ReportManager rm = new ReportManager();

            for (Long testEventId : testEventIdList)
            {
                //if( rmb.getTestEventScoreList() == null )
                rm.setTestEventScoreList(new ArrayList<>());

                String locStr = userBean.getLocaleStr2();

                Locale loc = null;

                if (locStr != null && !locStr.isEmpty())
                {
                    try
                    {
                        loc = I18nUtils.getLocaleFromCompositeStr(locStr);
                        LogService.logIt("UserUtils.processGenRegenReportsTestEventId() input localeStr=" + locStr + ", converted to a locale=" + loc.toString() + ", loc.language=" + loc.getLanguage() + ", testEventId=" + testEventId);
                    } catch (Exception e)
                    {
                        LogService.logIt(e, "UserUtils.processGenRegenReportsTestEventId() localeStr was " + locStr + ", testEventId=" + testEventId);
                        loc = null;
                    }
                }

                if (loc == null)
                {
                    LogService.logIt(  "UserUtils.processGenRegenReportsTestEventId() BBB.1 Starting report genRegen. loc=null, testEventId=" + testEventId);
                    
                    List<Report> rl = rm.genRegenReportTestEvent(testEventId, userBean.getReportId2(), forceCalcSection, false, testEventIdList.size() > 10 ? RuntimeConstants.getBooleanValue("create_reports_init_as_archived") : false, forceSendCandidateReports, maxLastCandidateSendDate);
                    if (rl != null && !rl.isEmpty())
                    {
                        String s = "";

                        for (Report r : rl)
                        {
                            if (!s.isEmpty())
                                s += " and ";

                            s += r.getName();
                        }

                        if (testEventIdList.size() < 100)
                            setStringInfoMessage("Generated the \'" + s + "\' report(s) for TestEventId: " + testEventId);

                        if (userBean.getReportTestEventScoreList() == null)
                            userBean.setReportTestEventScoreList(new ArrayList<>());

                        userBean.getReportTestEventScoreList().addAll(0, rm.getTestEventScoreList());
                    } else if (testEventIdList.size() < 100)
                        setStringErrorMessage("Did not generate any reports. testEventId=" + testEventId);
                } else
                {
                    LogService.logIt(  "UserUtils.processGenRegenReportsTestEventId() BBB.2 Starting report genRegen. loc=" + loc.toString() + ", testEventId=" + testEventId);
                    
                    TestEventScore tes = rm.generateReportForTestEventAndLanguage(testEventId, userBean.getReportId2(), loc.toString(), 0, false, forceCalcSection, forceSendCandidateReports, maxLastCandidateSendDate);

                    if (tes != null)
                    {
                        if (testEventIdList.size() < 100)
                            setStringInfoMessage("Generated the report: \'" + tes.getName() + "\' for TestEventId: " + testEventId + ", tesId=" + tes.getTestEventScoreId());

                        if (userBean.getReportTestEventScoreList() == null)
                            userBean.setReportTestEventScoreList(new ArrayList<>());

                        userBean.getReportTestEventScoreList().add(0, tes);
                    } else if (testEventIdList.size() < 100)
                        setStringErrorMessage("Did not generate any reports. testEventId=" + testEventId);
                }

                Thread.sleep(100);
            }

        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processRegenReportsTestEventId() userBean.getTestEventIdz2()=" + userBean.getTestEventIdz2());

            setMessage(e);
        }

        return null;

    }

    public String processGenSampleReport()
    {
        getUserBean();

        int productId = userBean.getProductId3();
        long reportId = userBean.getReportId3();

        try
        {
            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            ReportManager rm = new ReportManager();

            Locale locale = Locale.US;

            if (userBean.getLocaleStr3() != null && !userBean.getLocaleStr3().isEmpty())
            {
                locale = I18nUtils.getLocaleFromCompositeStr(userBean.getLocaleStr3());
            }

            //if( rmb.getTestEventScoreList() == null )
            rm.setTestEventScoreList(new ArrayList<>());

            if (productId <= 0)
                throw new Exception("ProductId is invalid: " + productId);

            TestEventScore tes = rm.generateSampleReport(productId, reportId, booleanParam1, false, false, locale);

            if (userBean.getReportTestEventScoreList() == null)
                userBean.setReportTestEventScoreList(new ArrayList<>());

            if (tes == null || tes.getReportBytes() == null || tes.getReportBytes().length <= 0)
                throw new Exception("No bytes found. " + (tes == null ? "null " : (tes.getReportBytes() == null ? "0" : tes.getReportBytes().length) + " bytes"));

            if (tes.getTestEventScoreId() <= 0)
                tes.setTestEventScoreId(((new Date()).getTime()));

            userBean.getReportTestEventScoreList().add(0, tes);

            return null;
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processGenSampleReport() productId=" + productId + ", reportId=" + reportId);

            setMessage(e);
        }

        return null;

    }

    public String processPerformDistributeBatchArchive()
    {
        return performDistributeBatch(true);
    }

    public String processPerformDistributeBatch()
    {
        return performDistributeBatch(false);
    }

    private String performDistributeBatch(boolean withArchive)
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            LogService.logIt("UserUtils.processPerformDistributeBatch() STARTING.");

            DistManager dmb = new DistManager();

            int[] count = dmb.doDistBatch(withArchive, true);

            setStringInfoMessage("Dist Batch Completed  Sent " + count[0] + " test admin emails and " + count[1] + " text messages and " + count[2] + " test-taker emails.");
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processPerformDistributeBatch()");
            setMessage(e);
        }

        return null;
    }

    public String processPerformReportBatchArchive()
    {
        return performReportBatch(true);
    }

    public String processPerformReportBatch()
    {
        return performReportBatch(false);
    }

    private String performReportBatch(boolean includeArchivedTestKeys)
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("Unauthorized Action.");
                return "/index.xhtml";
            }

            if (!ScoreManager.OK_TO_START_ANY)
            {
                this.setStringErrorMessage("Cannot start a new batch - all batches are OFF");
                return "StayInSamePlace";
            }

            LogService.logIt("UserUtils.processPerformReportBatch() STARTING.");

            ReportManager rm = new ReportManager();

            // if( rmb.getTestEventScoreList() == null )
            rm.setTestEventScoreList(new ArrayList<>());

            int[] data = rm.generateReportBatch(includeArchivedTestKeys, true, false, ReportManager.CREATE_INITIALLY_AS_ARCHIVED);

            LogService.logIt("UserUtils.processPerformReportBatch() Completed. " + data[0] + " TestKeys evaluated. " + data[1] + " TestEvents processed, " + data[2] + " Errors, " + data[3] + " partially completed battery test events processed.");

            userBean.setReportTestEventScoreList(rm.getTestEventScoreList());

            setStringInfoMessage("Report Batch Completed " + data[0] + " TestKeys evaluated. " + data[1] + " test events processed, " + data[2] + " Errors, " + data[3] + " partially completed battery test events processed.");
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processPerformReportBatch() ");
            setMessage(e);
        }

        return null;
    }

    public String processClearPartialScores()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            ScoreManager sm = new ScoreManager();

            int[] out = sm.clearPartiallyScoredTestKeys();

            setStringInfoMessage("Process Complete. Cleaned " + out[0] + " partially completed TestKeys and upgraded " + out[1] + " completed testKeys to score complete status.");
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processClearPartialScores()");
            setMessage(e);
        }

        return null;

    }

    public String processToggleScoreDebug()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            ScoreManager.DEBUG_SCORING = !ScoreManager.DEBUG_SCORING;

            setStringInfoMessage("Debug for Scoring is now " + (ScoreManager.DEBUG_SCORING ? "ON" : "OFF"));
        } else
        {
            this.setStringErrorMessage("You are not authorized to perform this action.");
            return "/index.xhtml";
        }

        return null;
    }

    public String processToggleReportDebug()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            ReportManager.DEBUG_REPORTS = !ReportManager.DEBUG_REPORTS;

            setStringInfoMessage("Debug for Scoring is now " + (ReportManager.DEBUG_REPORTS ? "ON" : "OFF"));
        } else
        {
            this.setStringErrorMessage("You are not authorized to perform this action.");
            return "/index.xhtml";
        }

        return null;
    }

    public String processToggleDistDebug()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            DistManager.DEBUG_DIST = !DistManager.DEBUG_DIST;

            setStringInfoMessage("Debug for Scoring is now " + (DistManager.DEBUG_DIST ? "ON" : "OFF"));
        }

        return null;
    }

    public String processParseNum()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            if (strParam1 == null || strParam1.isBlank())
                this.setStringErrorMessage("Field is empty. Cannot parse.");

            else
            {
                Locale loc = userBean.getLocaleStr4() == null || userBean.getLocaleStr4().isBlank() ? Locale.US : I18nUtils.getLocaleFromCompositeStr(userBean.getLocaleStr4());
                float n = NumberUtils.parseFloatInputStr(loc, strParam1);
                this.setStringInfoMessage("Parsed value is " + n);
            }
        } else
        {
            this.setStringErrorMessage("You are not authorized to perform this action.");
            return "/index.xhtml";
        }

        return null;

    }

    public String processSetRuntimeConstant()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            if (strParam2 == null || strParam2.isBlank())
            {
                setStringErrorMessage("Field Name is empty.");
                return null;
            }
            if (strParam3 == null || strParam3.isBlank())
            {
                if (RuntimeConstants.getIsValueAString(strParam2))
                {
                    setStringInfoMessage("The Field Value is empty. Since the datatype is String the variable will be set to an empty String.");
                    strParam3 = "";
                } else
                {
                    this.setStringErrorMessage("An empty field value is invalid for this field. Please revise and try again.");
                    return null;
                }
                // return null;
            }

            RuntimeConstants.setValueFmString(strParam2, strParam3);
        } else
        {
            this.setStringErrorMessage("You are not authorized to perform this action.");
            return "/index.xhtml";
        }

        return null;

    }

    public String processChangeTestKeyEventSelectionType()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            if (userFacade == null)
                userFacade = UserFacade.getInstance();

            userFacade.clearSharedCache();
            // userFacade.clearSharedCacheDiscern();

            setStringInfoMessage("Shared DBMS Cache cleared.");
        } else
        {
            this.setStringErrorMessage("You are not authorized to perform this action.");
            return "/index.xhtml";
        }

        TestKeyEventSelectionType tkest = TestKeyEventSelectionType.getValue(userBean.getTestKeyEventSelectionTypeId());

        RuntimeConstants.TESTKEYEVENT_SELECTION_TYPE = tkest;

        this.setStringInfoMessage("Set TestKeyEventSelectionType to " + tkest.getName());

        return null;
    }

    public String processClearDmbsCache()
    {
        getUserBean();

        if (userBean.getUserLoggedOnAsAdmin())
        {
            if (userFacade == null)
                userFacade = UserFacade.getInstance();

            userFacade.clearSharedCache();

            //  userFacade.clearSharedCacheDiscern();
            setStringInfoMessage("Shared DBMS Cache cleared.");
        } else
        {
            this.setStringErrorMessage("You are not authorized to perform this action.");
            return "/index.xhtml";
        }

        return null;
    }

    public Date getSystemStartDate()
    {
        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        STHttpSessionListener sl = (STHttpSessionListener) context.getAttribute(Constants.SYSTEM_SESSION_COUNTER);

        return sl == null ? new Date() : sl.getStartDate();
    }

    public List<SelectItem> getTestKeyEventSelectItemList()
    {
        return TestKeyEventSelectionType.getSelectItemList();
    }

    public List<String[]> getStatusList()
    {
        return Tracker.getStatusList();
    }

    public List<String[]> getStatusMap()
    {
        List<String[]> out = new ArrayList<>();

        Map<String, Float> sm = Tracker.getStatusMap();

        // LogService.logIt( "UserUtils.getStatusMap()  Status Map has " + sm.size() + " entries" );
        for (String k : sm.keySet())
        {
            out.add(new String[]
            {
                k, sm.get(k).toString()
            });
        }

        return out;
    }

    public boolean getNewBatchProcessingOn()
    {
        return ScoreManager.OK_TO_START_ANY;
    }

    public String processSendTestKeyReminder()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            if (userBean.getTestKeyId4() <= 0)
                this.setStringErrorMessage("TestKeyId is required");

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(userBean.getTestKeyId4(), true);

            if (tk == null)
                throw new Exception("TestKey not found. testKeyId=" + userBean.getTestKeyId4());

            if (!tk.getTestKeyStatusType().getIsActive() && !tk.getTestKeyStatusType().equals(TestKeyStatusType.STARTED))
                throw new Exception("TestKeyStatusType invalid for this testKey.");

            if (tk.getUserId() > 0)
                tk.setUser(UserFacade.getInstance().getUser(tk.getUserId()));

            if (tk.getUserEmail() == null || tk.getUserEmail().isEmpty() || !EmailUtils.validateEmailNoErrors(tk.getUserEmail()))
                throw new Exception("UserUtils.processSendTestKeyReminder()  Could not send a reminder email because no valid email found. " + tk.toString());

            ReminderUtils reminderUtils = new ReminderUtils(EmailUtils.getInstance());

            reminderUtils.sendReminderEmail(tk);

            this.setStringInfoMessage("Reminder email sent to " + tk.getUserEmail());

            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processSendTestKeyReminder() ");

            this.setMessage(e);

            return null;
        }

    }

    public String processSendTestText()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            if (userBean.getTextRecipPh() == null || userBean.getTextRecipPh().isEmpty())
            {
                this.setStringErrorMessage("Phone number empty!");
                return null;
            }

            if (userFacade == null)
                userFacade = UserFacade.getInstance();

            Org o = userFacade.getOrg(userBean.getUser().getOrgId());

            if (!o.getIsSmsOk())
            {
                this.setStringErrorMessage("Texting in your Org is not allowed.");
                return null;
            }

            String ph = GooglePhoneUtils.getFormattedPhoneNumberE164(userBean.getTextRecipPh(), userBean.getUser().getCountryCode());

            boolean smsOk = GooglePhoneUtils.getIsPhoneNumberAllowedForSms(ph, o, userBean.getUser(), null);
            if (!smsOk)
            {
                this.setStringErrorMessage("Phone number " + ph + " is not allowed because the target company is not allowed.");
                return null;
            }

            int sent = TwilioSmsUtils.sendTextMessageViaThread(userBean.getTextRecipPh(), userBean.getUser().getCountryCode(), Locale.US, null, "This is a test message from HR Avatar. Have a super great awesome day!");

            if (sent > 0)
                setStringInfoMessage("SMS Message sent to " + userBean.getTextRecipPh());
            else
                setStringErrorMessage("SMS Message NOT sent to " + userBean.getTextRecipPh() + ", sent=" + sent);

            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processSendTestText() ");

            this.setMessage(e);

            return null;
        }

    }

    public String processToggleNewBatchProcessing()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            ScoreManager.OK_TO_START_ANY = !ScoreManager.OK_TO_START_ANY;
            ReportManager.OK_TO_START_ANY = !ReportManager.OK_TO_START_ANY;

            this.setStringInfoMessage("New Batches (all kinds) are " + (ScoreManager.OK_TO_START_ANY ? "ON" : "OFF"));

            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processToggleNewBatchProcessing() " + ScoreManager.OK_TO_START_ANY);

            return null;
        }
    }

    public String processClearAllForNewBatches()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
                return null;

            ScoreManager.OK_TO_START_ANY = true;
            ReportManager.OK_TO_START_ANY = true;
            DistManager.OK_TO_START_ANY = true;

            ScoreManager.OK_TO_START_NEW = true;

            ScoreManager.BATCH_IN_PROGRESS = false;
            ReportManager.BATCH_IN_PROGRESS = false;
            DistManager.BATCH_IN_PROGRESS = false;

            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processClearAllForNewBatches() ");

            return null;
        }
    }

    /*
    public String processLoginToDiscern() {
        try
        {
            getUserBean();

            if( !userBean.getUserLoggedOnAsAdmin() )
                return null;

            DiscernUtils discernUtils = new DiscernUtils();
            discernUtils.loginToDiscern();
            setStringInfoMessage( "Successfully logged on to Discern" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.processLoginToDiscern()" );
            setMessage( e );
        }

        return null;
   }

    public String processLogoffDiscern() {
        try
        {
            getUserBean();

            if( !userBean.getUserLoggedOnAsAdmin() )
                return null;

            DiscernUtils discernUtils = new DiscernUtils();
            discernUtils.logoffDiscern();

            setStringInfoMessage( "Successfully logged off Discern" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.processLogoffDiscern()" );
            setMessage( e );
        }

        return null;
   }
     */
    public String processLogonAttempt()
    {
        try
        {
            getUserBean();

            if (PasswordUtils.hasTooManyFailedLogons(logonName)) // userBean.getFailedLogonAttempts() >= Constants.MAX_FAILED_LOGON_ATTEMPTS )
                throw new STException("g.TooManyFailedLogonAttempts");

            HttpServletRequest req = FacesContext.getCurrentInstance() == null ? null : (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

            String ipAddress = req == null ? null : req.getRemoteAddr();
            if (PasswordUtils.hasTooManyFailedLogons4Ip(ipAddress))
                throw new STException("g.TooManyFailedLogonAttempts");

            boolean lockout = false;

            if (logonName == null || logonName.isEmpty())
                return null;

            if (logonKey == null || logonKey.isEmpty())
                return null;

            if (userFacade == null)
                userFacade = UserFacade.getInstance();

            // find user by info
            User u = userFacade.getUserByLogonInfo(logonName, logonKey);

            //if( u!=null && !u.getRoleType().getIsAdmin() )
            //    u = null;
            if (u != null && u.getLockoutDate() != null)
            {
                Calendar cal = new GregorianCalendar();
                cal.add(Calendar.MINUTE, -1 * Constants.LOGON_LOCKOUT_MINUTES);

                if (cal.getTime().before(u.getLockoutDate()))
                {
                    lockout = true;
                    u = null;
                } else
                {
                    u.setLockoutDate(null);

                    if (userFacade == null)
                        userFacade = UserFacade.getInstance();

                    userFacade.saveUser(u);
                }
            } else if (u == null)
            {
                // increment.
                //PasswordUtils.addFailedLogon( logonName );
                //PasswordUtils.addFailedLogon4Ip(ipAddress);
                // userBean.setFailedLogonAttempts( userBean.getFailedLogonAttempts() + 1 );

                // lockout this user for 30 if too many attempts.
                if (PasswordUtils.hasTooManyFailedLogons(logonName)) // userBean.getFailedLogonAttempts()>=Constants.MAX_FAILED_LOGON_ATTEMPTS )
                {
                    if (userFacade == null)
                        userFacade = UserFacade.getInstance();

                    User u2 = userFacade.getUserByUsername(logonName);

                    if (u2 != null)
                    {
                        lockout = true;
                        u2.setLockoutDate(new Date());
                        userFacade.saveUser(u2);
                    }
                }
            }

            // Check for expired password.
            if (u != null)
            {
                ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).changeSessionId();

                Calendar cal = new GregorianCalendar();
                cal.add(Calendar.MONTH, -1 * Constants.MAX_PASSWORD_AGE_MONTHS);

                if (u.getPasswordStartDate() == null)
                {
                    u.setPasswordStartDate(cal.getTime());
                    u.setResetPwd(Constants.YES);
                    if (userFacade == null)
                        userFacade = UserFacade.getInstance();
                    userFacade.saveUser(u);
                }

                if (u.getResetPwd() != Constants.YES && u.getPasswordStartDate().before(cal.getTime()))
                {
                    u.setResetPwd(Constants.YES);
                    if (userFacade == null)
                        userFacade = UserFacade.getInstance();
                    userFacade.saveUser(u);
                }

                if (u.getResetPwd() == Constants.YES)
                {
                    setInfoMessage("g.PasswordHasExpiredLogonDenied", null);
                    u = null;
                }
            }

            logonName = null;

            logonKey = null;

            // VALID logon
            if (u != null && u.getRoleType().getIsAdmin())
            {
                userBean.setUser(u);

                userBean.setReportTestEventScoreList(null);

                PasswordUtils.clearFailedLogons(u.getUsername());

                String ua = null;
                String ip = null;

                if (req != null)
                {
                    ua = req.getHeader("User-Agent");
                    ip = req.getRemoteAddr();
                }

                LogonHistory logonHistory = userFacade.addLogonHistory(u, LogonType.USER.getLogonTypeId(), ua, ip);
                userBean.setLogonHistoryId(logonHistory.getLogonHistoryId());

                // set the message on last login
                Date lastLogin = userFacade.getLastLogonDate(u.getUserId(), userBean.getLogonHistoryId());

                if (lastLogin != null)
                {
                    LogService.logIt("UserUtils.completeLogon: lastLogin=" + lastLogin.toString());

                    Calendar cal = new GregorianCalendar();
                    cal.setTime(u.getPasswordStartDate() == null ? new Date() : u.getPasswordStartDate());
                    cal.add(Calendar.MONTH, Constants.MAX_PASSWORD_AGE_MONTHS);
                    Date pwdExpDate = cal.getTime();
                    setStringInfoMessage("Last login: " + lastLogin.toString() + ", password expires: " + pwdExpDate.toString());
                }

            } // INVALID Logon
            else
            {
                PasswordUtils.addFailedLogon(logonName);
                PasswordUtils.addFailedLogon4Ip(ipAddress);

                if (lockout)
                    this.setStringErrorMessage("Logons temporarily disabled. Wait 30 minutes and try again.");
                else
                    this.setStringErrorMessage("Logon info invalid.");
            }

            userBean.setTestKeyEventSelectionTypeId(RuntimeConstants.TESTKEYEVENT_SELECTION_TYPE.getTestKeyEventSelectionTypeId());

            return null;
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processLogonAttempt()");

            setMessage(e);
        }

        return null;
    }

    public String processRescoreSurveyEvents()
    {
        return rescoreEvents(true);
    }

    public String processRescoreTestEvents()
    {
        return rescoreEvents(false);
    }

    public String rescoreEvents(boolean survey)
    {
        getUserBean();

        try
        {
            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            //if( userBean.getTestEventId() <= 0 )
            //    throw new Exception( "TestEventId is invalid. " + userBean.getTestEventId() );
            List<Long> tel = new ArrayList<>();

            if (userBean.getTestEventIds() == null || userBean.getTestEventIds().isEmpty())
            {
                setStringErrorMessage("Test event id or ids (comma delimited) is required.");
                return "StayInSamePlace";
            }

            String descrip = "Rescore " + (survey ? "SurveyEvents" : "TestEvents") + ". ";

            if (userBean.getTestEventIds().indexOf("-") > 0)
            {
                String te1 = userBean.getTestEventIds().substring(0, userBean.getTestEventIds().indexOf("-")).trim();
                String te2 = userBean.getTestEventIds().substring(userBean.getTestEventIds().indexOf("-") + 1, userBean.getTestEventIds().length()).trim();

                try
                {
                    long tei1 = Long.parseLong(te1);
                    long tei2 = Long.parseLong(te2);

                    if (tei2 <= tei1)
                        throw new Exception("Range is invalid: " + tei1 + " - " + tei2);

                    LogService.logIt("UserUtils.rescoreEvents() Range is " + tei1 + " - " + tei2);

                    descrip += " Range of EventIds is " + tei1 + " - " + tei2;

                    for (long t = tei1; t <= tei2; t++)
                    {
                        tel.add(t);
                    }
                } catch (NumberFormatException e)
                {
                    setStringErrorMessage("Cannot parse EventId Range " + te1 + " - " + te2);
                    return "StayInSamePlace";
                }
            } else
            {

                String[] idl = userBean.getTestEventIds().split(",");

                long id;

                descrip += " EventIds are: " + userBean.getTestEventIds();

                for (String idx : idl)
                {
                    idx = idx.trim();
                    if (idx.isEmpty())
                        continue;
                    try
                    {
                        id = Long.parseLong(idx);
                        if (id > 0)
                            tel.add(id);
                    } catch (NumberFormatException e)
                    {
                        throw new Exception("Cannot parse EventId " + idx + " please enter only 1 id or delimit with commas.");
                    }
                }
            }

            if (tel.size() > 20)
            {
                TestEventRescoreThread ters = new TestEventRescoreThread(userBean.getUser(), tel, this, descrip, userBean.getSimDescripXml(), false, userBean.isClearExternal(), userBean.isSkipVersionCheck(), userBean.isResetSpeechText(), survey);

                new Thread(ters).start();

                setStringInfoMessage("More than 20 Events were identified for rescoring, so this process is being executed in the background. Check the system logs for information.");

                userBean.clear();

                return null;
            }

            rescoreTestOrSurveyEvents(tel, userBean.getSimDescripXml(), false, false, userBean.isClearExternal(), userBean.isSkipVersionCheck(), userBean.isResetSpeechText(), survey);

            userBean.clear();
        } catch (STException e)
        {
            setMessage(e);
        } catch (ScoringException e)
        {
            LogService.logIt("UserUtils.rescoreEvents() STERR " + e.toString());
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.rescoreEvents() " + userBean.getTestEventId());
            setMessage(e);
        }

        return null;
    }

    public String processRescoreSimIdVersionIdPairsOrOrg()
    {
        getUserBean();
        long simId = userBean.getSimId();
        int orgId = userBean.getSimOrgId();
        int suborgId = userBean.getSimSuborgId();

        try
        {
            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            List<Long[]> simIdPairs = new ArrayList<>();

            if (userBean.getSimIdVersionIdPairs() != null && !userBean.getSimIdVersionIdPairs().isBlank())
            {
                String[] pairinfo = userBean.getSimIdVersionIdPairs().trim().split(",");

                for (int i = 0; i < pairinfo.length - 1; i += 2)
                {
                    if (Long.parseLong(pairinfo[i].trim()) <= 0 || Long.parseLong(pairinfo[i + 1].trim()) <= 0)
                    {
                        this.setStringErrorMessage("BOTH Sim Id AND SimVersionId are required for each sim to be rescored (unless it's for the whole org then leave simIds blank)");
                        return "StayInSamePlace";

                    }
                    simIdPairs.add(new Long[]
                    {
                        Long.valueOf(pairinfo[i].trim()), Long.valueOf(pairinfo[i + 1].trim())
                    });
                }
            }

            //if( userBean.getTestEventId() <= 0 )
            //    throw new Exception( "TestEventId is invalid. " + userBean.getTestEventId() );
            Date start = userBean.getStartDate();
            Date end = userBean.getEndDate();

            if (simIdPairs.size() <= 0 && orgId <= 0)
            {
                this.setStringErrorMessage("Either at least one Sim ID/simversionId pair, or orgId (and optionally subOrgId) is required.");
                return "StayInSamePlace";
            }

            //if( simId > 0 && userBean.getSimVersionId()<=0 )
            //{
            //    this.setStringErrorMessage("Sim Version ID is required when SimId is specified." );
            //    return "StayInSamePlace";
            //}
            if (orgId > 0 && suborgId < 0)
                suborgId = 0;

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            boolean percentilesOnly = userBean.getBooleanParam1();

            TestEventRescoreThread ters;
            // Full Org
            if (simIdPairs.isEmpty())
            {
                List<Long> tel = eventFacade.getTestEventIdsForSimIdAndOrOrg(0, 0, orgId, suborgId, 0, percentilesOnly ? TestEventStatusType.SCORED.getTestEventStatusTypeId() : TestEventStatusType.COMPLETED.getTestEventStatusTypeId(), TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), new int[]
                {
                    TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId(), TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId()
                }, start, end, 0, 0);

                ters = new TestEventRescoreThread(userBean.getUser(), tel, this, "Rescore / Recalc Percentiles for Org. orgId=" + orgId + ", suborgId=" + suborgId, null, percentilesOnly, userBean.isClearExternal2(), false, false, false);
                new Thread(ters).start();
                setStringInfoMessage("More than 1 TestEvent was identified for " + (percentilesOnly ? "percentile recalculation" : "rescoring,") + " so this process is being executed in the background. Check the system logs for information.");
                userBean.clear();
                return null;
            }

            ters = new TestEventRescoreThread(userBean.getUser(), simIdPairs, orgId, suborgId, start, end, null, this, "Rescore / Recalc Percentiles for " + (simIdPairs == null ? "null" : simIdPairs.size()) + " simId sim/versionId pairs and orgId=" + orgId + ", suborgId=" + suborgId, percentilesOnly, userBean.isClearExternal2(), false, false, false);
            new Thread(ters).start();
            setStringInfoMessage("Process for rescoring " + simIdPairs.size() + " sims is being executed in the background. Check the system logs for information.");
            userBean.clear();
            return null;
        } catch (STException e)
        {
            setMessage(e);
        } catch (ScoringException e)
        {
            LogService.logIt("UserUtils.processRescoreSimOrOrg() STERR " + e.toString() + ", simId=" + simId);
            setMessage(e);
            if (userBean.getUser() != null)
            {
                EmailUtils emu = new EmailUtils();
                emu.sendEmailToAdmin("TestEvent Rescore Process FAILED", "TestEventRescoreThread.run() FAILED " + e.toString());
            }
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processRescoreSimOrOrg() " + ", simId=" + simId);
            setMessage(e);
            if (userBean.getUser() != null)
            {
                EmailUtils emu = new EmailUtils();
                emu.sendEmailToAdmin("TestEvent Rescore Process FAILED", "TestEventRescoreThread.run() FAILED " + e.toString());
            }

        }

        return null;
    }

    /*
    public String processRescoreSimOrOrg()
    {
        getUserBean();
        long simId = userBean.getSimId();
        int orgId = userBean.getSimOrgId();
        int suborgId = userBean.getSimSuborgId();


        try
        {
            if( !userBean.getUserLoggedOnAsAdmin() )
            {
                this.setStringErrorMessage( "You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            //if( userBean.getTestEventId() <= 0 )
            //    throw new Exception( "TestEventId is invalid. " + userBean.getTestEventId() );

            Date start = userBean.getStartDate();
            Date end = userBean.getEndDate();
            
            if( simId<=0 && orgId<=0 )
            {
                this.setStringErrorMessage( "Either Sim ID (and simversionId) or orgId (and optionally subOrgId) is required." );
                return "StayInSamePlace";
            }

            if( simId > 0 && userBean.getSimVersionId()<=0 )
            {
                this.setStringErrorMessage("Sim Version ID is required when SimId is specified." );
                return "StayInSamePlace";
            }

            if( orgId>0 && suborgId<0 )
                suborgId=0;

            if( eventFacade==null )
                eventFacade = EventFacade.getInstance();

            boolean percentilesOnly = userBean.getBooleanParam1();

            List<Long> tel = eventFacade.getTestEventIdsForSimIdAndOrOrg(simId, userBean.getSimVersionId(), orgId, suborgId, 0, percentilesOnly ? TestEventStatusType.SCORED.getTestEventStatusTypeId() : TestEventStatusType.COMPLETED.getTestEventStatusTypeId(), TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), new int[]{TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId(), TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId()}, start, end, 0, 0 );
            
            
            if( tel.size()>1 )
            {
                TestEventRescoreThread ters = new TestEventRescoreThread( tel, this, "Recalc Percentiles SimId=" + simId + ", simVersionId=" + userBean.getSimVersionId() + ", orgId=" + orgId + ", suborgId=" + suborgId, null, percentilesOnly, userBean.isClearExternal2(), false, false, false );

                new Thread( ters ).start();

                setStringInfoMessage( "More than 1 TestEvent was identified for " + (percentilesOnly ? "percentile recalculation" : "rescoring,") + " so this process is being executed in the background. Check the system logs for information." );

                userBean.clear();

                return null;
            }


            rescoreTestOrSurveyEvents(tel, null, false, percentilesOnly, userBean.isClearExternal2(), false, false, false  );

            userBean.clear();
        }                
        catch(STException e )
        {
            setMessage( e );
        }
        catch( ScoringException  e )
        {
            LogService.logIt( "UserUtils.processRescoreSimOrOrg() STERR " + e.toString() + ", simId=" + simId );
            setMessage( e );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserUtils.processRescoreSimOrOrg() " + ", simId=" + simId  );
            setMessage( e );
        }

        return null;
    }
     */
    public void recalcPercentilesForTestEvents(List<Long> tel, String simDescripXml, boolean isThread) throws Exception
    {
        ScoreManager sm = new ScoreManager();

        if (eventFacade == null)
            eventFacade = EventFacade.getInstance();

        int count = 0;
        int skippedUnscored = 0;
        int skippedExpired = 0;

        String msg;

        for (Long teid : tel)
        {
            LogService.logIt("UserUtils.recalcPercentilesForTestEvents() recalcPercentiles for Test EventId=" + teid);

            TestEvent te = eventFacade.getTestEvent(teid, true);

            if (te == null)
            {
                if (!isThread)
                {
                    setStringErrorMessage("TestEvent not found for testEventId=" + teid);
                    return;
                }

                throw new Exception("TestEvent not found for id=" + teid);
            }

            if (te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId())
            {
                skippedUnscored++;
                continue;
            }

            if (te.getTestEventStatusTypeId() >= TestEventStatusType.EXPIRED.getTestEventStatusTypeId())
            {
                skippedExpired++;
                continue;
            }

            sm.recalcPercentilesForTestEvent(teid.longValue(), simDescripXml);

            TestKey tk = eventFacade.getTestKey(te.getTestKeyId(), true);

            tk.setTestKeyStatusTypeId(TestKeyStatusType.SCORED.getTestKeyStatusTypeId());
            tk.setErrorCnt(0);
            eventFacade.saveTestKey(tk);

            count++;
        }

        msg = "RECALC PERCENTILES PROCESS COMPLETE " + count + " testevents affected. " + skippedUnscored + " skipped due to unscored events. " + skippedExpired + " because test event is in an expired or error state.";

        if (isThread)
            LogService.logIt("UserUtils.recalcPercentilesForTestEvents() " + msg);

        else
            setStringInfoMessage(msg);
    }

    public void regenerateReports(List<Long> testEventIdList, long reportId, boolean isThread, boolean createAsArchived, boolean forceCalcSection, boolean sendResendCandidateReportEmails, Date maxLastCandidateSendDate) throws Exception
    {
        long testEventId = 0;

        int count = 0;
        int errCount = 0;

        ReportManager rm = new ReportManager();

        for (Long teid : testEventIdList)
        {
            try
            {
                testEventId = teid;

                //if( rmb.getTestEventScoreList() == null )
                rm.setTestEventScoreList(new ArrayList<>());

                List<Report> rl = rm.genRegenReportTestEvent(teid, reportId, forceCalcSection, false, createAsArchived, sendResendCandidateReportEmails, maxLastCandidateSendDate);

                if (rl != null && !rl.isEmpty())
                {
                    String s = "";

                    for (Report r : rl)
                    {
                        if (!s.isEmpty())
                            s += " and ";

                        s += r.getName();
                    }

                    LogService.logIt("UserUtils.regenerateReports() Generated the \'" + s + "\' report(s) for TestEventId: " + teid + ", sendResendCandidateReportEmails=" + sendResendCandidateReportEmails);

                    Thread.sleep(1000);
                } else
                    setStringErrorMessage("Did not generate any reports for TestEventId: " + teid);

                count++;
            } catch (Exception e)
            {
                errCount++;
                LogService.logIt(e, "UserUtils.regenerateReports() testEventId=" + testEventId + ", totalCount=" + count + ", error count=" + errCount);

                if (errCount > 5)
                    throw e;
            }
        }
    }

    public void rescoreTestOrSurveyEvents(List<Long> tel, String simDescripXml, boolean isThread, boolean percentilesOnly, boolean clearExternal, boolean skipVersionCheck, boolean resetSpeechText, boolean survey) throws Exception
    {
        if (survey)
            rescoreSurveyEvents(tel, simDescripXml, isThread, skipVersionCheck);
        else
            rescoreTestEvents(tel, simDescripXml, isThread, percentilesOnly, clearExternal, skipVersionCheck, resetSpeechText);

    }

    public void rescoreTestEvents(List<Long> tel, String simDescripXml, boolean isThread, boolean percentilesOnly, boolean clearExternal, boolean skipVersionCheck, boolean resetSpeechText) throws Exception
    {
        if (percentilesOnly)
        {
            recalcPercentilesForTestEvents(tel, simDescripXml, isThread);
            return;
        }

        ScoreManager sm = new ScoreManager();

        if (eventFacade == null)
            eventFacade = EventFacade.getInstance();

        int count = 0;
        int countPending = 0;
        int skippedIncomplete = 0;
        int skippedExpired = 0;

        String msg;

        TestKey tk;
        boolean updateTestKeyStatus = true;

        TestEvent te;

        for (Long teid : tel)
        {
            LogService.logIt("UserUtils.rescoreTestEvents() START (re)scoring Test EventId=" + teid);

            updateTestKeyStatus = true;

            te = eventFacade.getTestEvent(teid, true);

            if (te == null)
            {
                if (!isThread)
                {
                    setStringErrorMessage("TestEvent not found for testEventId=" + teid);
                    return;
                }
                throw new Exception("TestEvent not found for id=" + teid);
            }

            tk = eventFacade.getTestKey(te.getTestKeyId(), true);

            if (tk == null)
            {
                if (!isThread)
                {
                    setStringErrorMessage("Cannot find testKey for TestEvent. " + te.toString());
                    return;
                }

                throw new Exception("Cannot find testKey for TestEvent. " + te.toString());
            }

            if (tk.getBatteryId() > 0 && tk.getTestKeyStatusTypeId() < TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId())
            {
                LogService.logIt("UserUtils.rescoreTestEvents() TestKey is battery and not complete. Still rescoring the TestEvent but won't update TestKey Status.");
                updateTestKeyStatus = false;
            }

            if (te.getTestEventStatusTypeId() == TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId() || te.getTestEventStatusTypeId() == TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId())
            {
                te.setTestEventStatusTypeId(TestEventStatusType.COMPLETED.getTestEventStatusTypeId());

                if (te.getTestEventArchiveId() > 0)
                    eventFacade.saveTestEventArchive(te.getTestEventArchive());
                else
                    eventFacade.saveTestEvent(te);
            }

            if (te.getTestEventStatusTypeId() < TestEventStatusType.COMPLETED.getTestEventStatusTypeId())
            {
                skippedIncomplete++;
                continue;
            }

            if (te.getTestEventStatusTypeId() >= TestEventStatusType.EXPIRED.getTestEventStatusTypeId())
            {
                skippedExpired++;
                continue;
            }

            sm.rescoreTestEvent(teid, simDescripXml, clearExternal, skipVersionCheck, resetSpeechText);

            // reload
            te = eventFacade.getTestEvent(teid, true);

            if (te.getTestEventStatusTypeId() == TestEventStatusType.SCORED.getTestEventStatusTypeId())
            {
                boolean batteryRescored = sm.recalculateTestKeyBatteryScore(te.getTestKeyId());

                tk = eventFacade.getTestKey(te.getTestKeyId(), true);

                if (updateTestKeyStatus)
                {
                    tk.setTestKeyStatusTypeId(TestKeyStatusType.SCORED.getTestKeyStatusTypeId());
                    tk.setErrorCnt(0);
                    eventFacade.saveTestKey(tk);
                }

                msg = "TestEventId: " + teid.longValue() + " has been rescored. Reports have been deleted and will be picked up in the next automated cycle. BatteryRescored=" + batteryRescored;

                if (isThread)
                    LogService.logIt("UserUtils.rescoreTestEvents() " + msg);
                else
                    setStringInfoMessage(msg);

                count++;
            } else if (te.getTestEventStatusTypeId() == TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId())
            {
                tk = eventFacade.getTestKey(te.getTestKeyId(), true);

                if (updateTestKeyStatus)
                {
                    tk.setTestKeyStatusTypeId(TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId());
                    eventFacade.saveTestKey(tk);
                }

                msg = "TestEventId: " + teid.longValue() + " is now pending externally completed scores. The TestKey has been placed in this status also and will be fully re-scored after the externally computed scores are ready. Reports have been deleted and will be picked up in the next automated cycle after scoring. ";

                if (isThread)
                    LogService.logIt("UserUtils.rescoreTestEvents() " + msg);
                else
                    setStringInfoMessage(msg);

                countPending++;
            }
        }

        msg = "RESCORE PROCESS COMPLETE " + count + " testevents rescored.  " + countPending + " test events rescored but pending external scores. " + skippedIncomplete + " skipped due to incomplete events. " + skippedExpired + " because test event is in an expired or error state.";

        if (isThread)
            LogService.logIt("UserUtils.rescoreTestEvents() " + msg);

        else
            setStringInfoMessage(msg);

    }

    public void rescoreSurveyEvents(List<Long> tel, String simDescripXml, boolean isThread, boolean skipVersionCheck) throws Exception
    {
        ScoreManager sm = new ScoreManager();

        if (eventFacade == null)
            eventFacade = EventFacade.getInstance();

        int count = 0;
        int skippedIncomplete = 0;

        String msg;

        TestKey tk;

        SurveyEvent te;

        for (Long teid : tel)
        {
            // LogService.logIt( "UserUtils.rescoreTestEvents() scoring Test EventId=" + teid );

            te = eventFacade.getSurveyEvent(teid);

            if (te == null)
            {
                if (!isThread)
                {
                    setStringErrorMessage("TestEvent not found for surveyEventId=" + teid);
                    return;
                }
                throw new Exception("SurveyEvent not found for id=" + teid);
            }

            tk = eventFacade.getTestKey(te.getTestKeyId(), true);

            if (tk == null)
            {
                if (!isThread)
                {
                    setStringErrorMessage("Cannot find testKey for SurveyEvent. " + te.toString());
                    return;
                }

                throw new Exception("Cannot find testKey for SurveyEvent. " + te.toString());
            }

            if (te.getSurveyEventStatusTypeId() < TestEventStatusType.COMPLETED.getTestEventStatusTypeId())
            {
                skippedIncomplete++;
                continue;
            }

            if (te.getSurveyEventStatusTypeId() > TestEventStatusType.COMPLETED.getTestEventStatusTypeId())
            {
                te.setSurveyEventStatusTypeId(TestEventStatusType.COMPLETED.getTestEventStatusTypeId());
                eventFacade.saveSurveyEvent(te);
            }

            sm.rescoreSurveyEvent(te.getSurveyEventId());

            // reload
            te = eventFacade.getSurveyEvent(teid);

            if (te.getSurveyEventStatusTypeId() == TestEventStatusType.SCORED.getTestEventStatusTypeId())
            {

                msg = "SurveyEventId: " + teid + " has been rescored.";

                if (isThread)
                    LogService.logIt("UserUtils.rescoreSurveyEvents() " + msg);
                else
                    setStringInfoMessage(msg);

                count++;
            }
        }

        msg = "SURVEY RESCORE PROCESS COMPLETE " + count + " surveyevents rescored.  " + skippedIncomplete + " skipped due to incomplete events.";

        if (isThread)
            LogService.logIt("UserUtils.rescoreSurveyEvents() " + msg);
        else
            setStringInfoMessage(msg);
    }

    public boolean getScoreBatchInProgress()
    {
        return ScoreManager.BATCH_IN_PROGRESS;
    }

    public boolean getReportBatchInProgress()
    {
        return ReportManager.BATCH_IN_PROGRESS;
    }

    public boolean getDistributionBatchInProgress()
    {
        return DistManager.BATCH_IN_PROGRESS;
    }

    public String processRedistributeTestKeyIds()
    {
        String tkids = null;

        try
        {
            getUserBean();

            tkids = userBean.getTestKeyIdsStr2();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            LogService.logIt("UserUtils.processRedistributeTestKeyId() AAA testKeyIds=" + userBean.getTestKeyIdsStr2());

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            boolean resendResultsPost = userBean.isResendResultsPost();
            
            TestKey tk; //  = eventFacade.getTestKey(userBean.getTestKeyId3(), true);

            if (tkids == null || tkids.isBlank())
                throw new Exception("TestKeyIds not found. TestKeyIds should be delimited by commas");

            Set<Long> tkidSet = new HashSet<>();
            long tkid;
            String[] vals = tkids.split(",");
            for (String val : vals)
            {
                if (val.isBlank())
                    continue;
                tkid = Long.parseLong(val.trim());
                tk = eventFacade.getTestKey(tkid, true);

                if (tk == null)
                    throw new Exception("TestKey not found for TestKeyId=" + tkid);

                tkidSet.add(tkid);
            }

            LogService.logIt("UserUtils.processRedistributeTestKeyIds() Found " + tkidSet.size() + " unique TestKeyIds to distribute. resendResultsPost=" + resendResultsPost );

            redistributeTestKeyIds(tkidSet, resendResultsPost);

            userBean.clear();
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processRedistributeTestKeyIds() " + (tkids == null ? "null" : tkids));
            setMessage(e);
        }
        return null;
    }

    public void redistributeTestKeyIds(Set<Long> tkidSet, boolean resendResultsPost) throws Exception
    {
        try
        {

            LogService.logIt("UserUtils.redistributeTestKeyIds() Have " + tkidSet.size() + " unique TestKeyIds to distribute. resendResultsPost=" + resendResultsPost);

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            if (tkidSet.size() <= 10)
            {
                DistManager smb = new DistManager();

                TestKey tk;
                int[] tko;
                for (long testKeyId : tkidSet)
                {
                    tk = eventFacade.getTestKey(testKeyId, true);

                    if (tk == null)
                        throw new Exception("TestKey not found for TestKeyId=" + testKeyId);

                    if (!tk.getTestKeyStatusType().equals(TestKeyStatusType.REPORTS_COMPLETE)
                            && !tk.getTestKeyStatusType().equals(TestKeyStatusType.DISTRIBUTION_STARTED)
                            && !tk.getTestKeyStatusType().equals(TestKeyStatusType.API_DISTRIBUTION_COMPLETE)
                            && !tk.getTestKeyStatusType().equals(TestKeyStatusType.DISTRIBUTION_COMPLETE)
                            && !tk.getTestKeyStatusType().equals(TestKeyStatusType.DISTRIBUTION_ERROR))
                        throw new Exception("TestKey Status is not valid for redistribution: " + tk.getTestKeyStatusType().getTestKeyStatusTypeId());

                    // reset first dist complete if resend.
                    if( resendResultsPost )
                        tk.setFirstDistComplete(0);
                    
                    // tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
                    if( tk.getFirstDistComplete()==2 )
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.API_DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId());
                    else
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId());

                    eventFacade.saveTestKey(tk);

                    Tracker.addRedistribution();

                    BaseScoreManager.addTestKeyToDateMap(tk.getTestKeyId());

                    try
                    {
                        tko = smb.distributeTestKeyResults(tk, false, 0, 0, 0);

                        BaseScoreManager.removeTestKeyFromDateMap(tk.getTestKeyId());

                        if (tko[0] > 0 || tko[1] > 0 || tko[2] > 0)
                            setStringInfoMessage("TestKeyId: " + testKeyId + " has been redistributed. " + tko[0] + " administrator emails sent and " + tko[1] + " text messages sent and " + tko[2] + " Test-Taker emails sent.");
                    } catch (STException e)
                    {
                        setMessage(e);
                        BaseScoreManager.removeTestKeyFromDateMap(testKeyId);
                    } catch (ScoringException e)
                    {
                        LogService.logIt("UserUtils.redistributeTestKeyIds() " + e.toString() + ", testKeyId=" + testKeyId);
                        setMessage(e);
                        BaseScoreManager.removeTestKeyFromDateMap(testKeyId);
                    } catch (Exception e)
                    {
                        LogService.logIt(e, "UserUtils.redistributeTestKeyIds() testKeyId=" + testKeyId);
                        setMessage(e);
                        BaseScoreManager.removeTestKeyFromDateMap(testKeyId);
                    }
                }
            } else
            {
                setStringInfoMessage("Starting batch in thread because more than 10 test keys requested. Check logs for results.");
                DistributionBatchThread dbt = new DistributionBatchThread(tkidSet, resendResultsPost);
                new Thread(dbt).start();
            }
        } catch (STException e)
        {
            throw e;
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.redistributeTestKeyIds() testKeyId=" + userBean.getTestKeyId3());
            throw e;
        }
    }

    public String processClearResults()
    {
        getUserBean();

        long tkid = userBean.getTestKeyId6();

        try
        {
            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            userBean.clear();

            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processClearResults() tkid=" + tkid);
            setMessage(e);
        }
        return null;

    }

    public String processGenerateAssessmentResultXml()
    {
        getUserBean();

        long tkid = userBean.getTestKeyId6();

        try
        {
            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            userBean.setResultStr1(null);
            LogService.logIt("UserUtils.processGenerateAssessmentResultXml() AAA testKeyId=" + tkid);

            if (tkid <= 0)
                throw new Exception("TestKeyId is invalid: " + tkid + ", please try again.");

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(tkid, true); //  = eventFacade.getTestKey(userBean.getTestKeyId3(), true);

            if (tk == null)
                throw new Exception("TestKey not found for testKeyId=" + tkid);

            /*
                       0 - Do not include score information in response.
                       1 - Include score information except PDF
                       2 - Include score information including any PDF reports
                       3 - Produce PDF report in provided language (ONLY)
            
             */
            int includeScoreCode = userBean.getIntParam1();

            if (includeScoreCode < 1 || includeScoreCode > 2)
                throw new Exception("Include Score Code invalid must be 1 or 2: " + includeScoreCode);

            AssessmentResult arr = new AssessmentResult();

            AssessmentStatusCreator asc = new AssessmentStatusCreator();

            String xml = asc.getAssessmentResultFromTestKey(arr, tk, includeScoreCode, null, null, null);

            userBean.setResultStr1(xml);

            LogService.logIt("UserUtils.processGenerateAssessmentResultXml() AAA testKeyId=" + tkid + " result XML.length=" + (xml == null ? "null" : xml.length()));
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processRedistributeTestKeyIds() tkid=" + tkid);
            setMessage(e);
        }
        return null;
    }

    public String processRedistributeTestEventIds()
    {
        String teids = null;

        try
        {
            getUserBean();

            teids = userBean.getTestEventIds2();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            LogService.logIt("UserUtils.processRedistributeTestEventIds() AAA teids=" + teids);

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            TestKey tk; //  = eventFacade.getTestKey(userBean.getTestKeyId3(), true);

            if (teids == null || teids.isBlank())
                throw new Exception("TestEventIds not found. TestEventIds should be delimited by commas");

            boolean resendResultsPost = userBean.isResendResultsPost2();
            
            Set<Long> tkidSet = new HashSet<>();
            Set<Long> teidSet = new HashSet<>();
            long tkid;
            long teid;
            TestEvent te;
            String[] vals = teids.split(",");
            for (String val : vals)
            {
                if (val.isBlank())
                    continue;
                teid = Long.parseLong(val.trim());

                if (teidSet.contains(teid))
                    continue;
                teidSet.add(teid);

                te = eventFacade.getTestEvent(teid, true);
                if (te == null)
                    throw new Exception("TestEvent not found for TestEventId=" + teid);

                tkid = te.getTestKeyId();
                tk = eventFacade.getTestKey(tkid, true);
                if (tk == null)
                    throw new Exception("TestKey not found for TestKeyId=" + tkid + ", for TestEventId=" + teid);
                tkidSet.add(tkid);
            }

            LogService.logIt("UserUtils.processRedistributeTestEventIds() Found " + tkidSet.size() + " unique TestKeyIds to distribute for " + teidSet.size() + " unique testEventIds. resendResultsPost=" + resendResultsPost );

            redistributeTestKeyIds(tkidSet, resendResultsPost);

            userBean.clear();
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processRedistributeTestKeyIds() " + (teids == null ? "null" : teids));
            setMessage(e);
        }
        return null;
    }

    public String processScorePartiallyCompletedBattery()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            if (userBean.getTestKeyId5() <= 0)
                throw new Exception("TestKeyId is invalid.");

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(userBean.getTestKeyId5(), true);

            if (tk == null)
                throw new Exception("TestKey not found for testKeyId=" + userBean.getTestKeyId5());

            if (!tk.getTestKeyStatusType().equals(TestKeyStatusType.ACTIVE)
                    && !tk.getTestKeyStatusType().equals(TestKeyStatusType.STARTED))
                throw new Exception("TestKey Status is not valid for scoring a partially completed battery test key: " + tk.getTestKeyStatusType().getTestKeyStatusTypeId());

            if (tk.getBatteryId() <= 0)
                throw new Exception("TestKey is not a Battery Type. testKeyId=" + userBean.getTestKeyId5());

            List<TestEvent> tel = eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), false);
            boolean hasComplete = false;

            for (TestEvent te : tel)
            {
                if (te.getTestEventStatusType().getIsCompleteOrHigher())
                {
                    hasComplete = true;
                    break;
                }
            }

            if (!hasComplete)
                throw new Exception("TestKey " + tk.getTestKeyId() + " contains ZERO completed test events. Can't score it.");

            ScoreManager sm = new ScoreManager();

            int[] out = sm.scoreTestKey(tk, true, true);

            setStringInfoMessage("TestKeyId: " + userBean.getTestKeyId5() + " has been scored. " + out[1] + " test events score, " + out[3] + " test events placed in pending status, " + out[4] + " test events skipped because they were never completed.");

            userBean.clear();
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "processScorePartiallyCompletedBattery()");

            setMessage(e);
        }

        return null;
    }

    public String processRescoreTestKeyIds()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            List<Long> tkl = new ArrayList<>();

            if (userBean.getTestKeyIdsStr() == null || userBean.getTestKeyIdsStr().isEmpty())
                throw new Exception("Test key id or ids (comma delimited) is required.");

            String descrip = "Rescore TestKeyIds. ";

            if (userBean.getTestKeyIdsStr().indexOf("-") > 0)
            {
                String te1 = userBean.getTestKeyIdsStr().substring(0, userBean.getTestKeyIdsStr().indexOf("-")).trim();
                String te2 = userBean.getTestKeyIdsStr().substring(userBean.getTestKeyIdsStr().indexOf("-") + 1, userBean.getTestKeyIdsStr().length()).trim();

                try
                {
                    long tei1 = Long.parseLong(te1);
                    long tei2 = Long.parseLong(te2);

                    if (tei2 <= tei1)
                        throw new Exception("Range is invalid: " + tei1 + " - " + tei2);

                    LogService.logIt("UserUtils.processRescoreTestKeyIds() Range is " + tei1 + " - " + tei2);

                    descrip += " Range of testEventIds is " + tei1 + " - " + tei2;

                    for (long t = tei1; t <= tei2; t++)
                    {
                        tkl.add(t);
                    }
                } catch (NumberFormatException e)
                {
                    throw new Exception("Cannot parse TestKeyId Range " + te1 + " - " + te2);
                }
            } else
            {

                String[] idl = userBean.getTestKeyIdsStr().split(",");

                long id;

                descrip += " TestKeyIds are: " + userBean.getTestKeyIdsStr();

                for (String idx : idl)
                {
                    idx = idx.trim();
                    if (idx.isEmpty())
                        continue;
                    try
                    {
                        id = Long.parseLong(idx);
                        if (id > 0)
                            tkl.add(id);
                    } catch (NumberFormatException e)
                    {
                        throw new Exception("Cannot parse TestKeyId " + idx + " please enter only 1 id or delimit with commas.");
                    }
                }
            }

            if (tkl.size() > 20)
            {
                TestKeyRescoreThread ters = new TestKeyRescoreThread(tkl, userBean.getClearExternal3(), descrip);

                new Thread(ters).start();

                setStringInfoMessage("More than 20 TestKeys were identified for rescoring, so this process is being executed in the background. Check the system logs for information.");

                userBean.clear();

                return null;
            }

            if (tkl.isEmpty())
                throw new Exception("No TestKeyIds found for rescore.");

            ScoreManager sm = new ScoreManager();

            int[] tko = null;

            for (Long testKeyId : tkl)
            {
                tko = sm.rescoreTestKey(testKeyId, false, userBean.getClearExternal3());

                if (tko[0] > 0)
                    setStringInfoMessage("TestKeyId: " + testKeyId + " has been rescored. Reports have been deleted and will be picked up in the next automated cycle.");

                else if (tko[2] > 0)
                    setStringInfoMessage("TestKeyId: " + testKeyId + " is now pending externally computed scores. It will be completed in the next batch cycle during which the external scores are ready.");
                else
                    setStringInfoMessage("TestKeyId: " + testKeyId + " has NOT been rescored. Typically this is because a previous score attempt occured within 30 seconds. Please wait and try again. ");

            }

            userBean.clear();
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "processRescoreTestKeyIds()");
            setMessage(e);
        }

        return null;

    }

    public String processInvitationBatch()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            EmailUtils emailUtils = EmailUtils.getInstance();

            ReminderUtils reminderUtils = new ReminderUtils(emailUtils);

            String msg = reminderUtils.doInvitationBatch();

            setStringInfoMessage(msg);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processInvitationBatch() ");
        }

        return null;
    }

    public String processReminderBatch()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            EmailUtils emailUtils = EmailUtils.getInstance();

            ReminderUtils reminderUtils = new ReminderUtils(emailUtils);

            String msg = reminderUtils.doReminderBatch();

            setStringInfoMessage(msg);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processReminderBatch() ");
        }

        return null;
    }

    public String processOrgSubscriptionExpireBatch()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            EmailUtils emailUtils = EmailUtils.getInstance();

            ReminderUtils reminderUtils = new ReminderUtils(emailUtils);

            String msg = reminderUtils.doExpireSubscriptionBatch();

            setStringInfoMessage(msg);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processOrgSubscriptionExpireBatch() ");
        }

        return null;
    }

    public String processCreditsExpireBatch()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            EmailUtils emailUtils = EmailUtils.getInstance();

            ReminderUtils reminderUtils = new ReminderUtils(emailUtils);

            String msg = reminderUtils.doExpireCreditsBatch();

            setStringInfoMessage(msg);

            msg = reminderUtils.doCreditZeroBatch();

            setStringInfoMessage(msg);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processCreditsExpireBatch() ");
        }

        return null;
    }

    public String processTkExpireWarningBatch()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            EmailUtils emailUtils = EmailUtils.getInstance();

            ReminderUtils reminderUtils = new ReminderUtils(emailUtils);
            String msg = reminderUtils.doTestKeyExpireWarningBatch();

            setStringInfoMessage(msg);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processTkExpireWarningBatch() ");
        }

        return null;
    }

    public String processOrgAutoTestExpireBatch()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            EmailUtils emailUtils = EmailUtils.getInstance();

            ReminderUtils reminderUtils = new ReminderUtils(emailUtils);

            String msg = reminderUtils.doExpireOrgAutoTestRecordsBatch();

            setStringInfoMessage(msg);
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processOrgAutoTestExpireBatch() ");
        }

        return null;
    }

    public String processRescorePin()
    {
        try
        {
            getUserBean();

            if (!userBean.getUserLoggedOnAsAdmin())
            {
                this.setStringErrorMessage("You are not authorized to perform this action.");
                return "/index.xhtml";
            }

            if (userBean.getPin() == null || userBean.getPin().isEmpty())
            {
                this.setStringErrorMessage("Pin is not valid or missing. Cannot rescore.");
                return "StayInSamePlace";
            }

            if (eventFacade == null)
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKeyForPin(userBean.getPin());

            ScoreManager sm = new ScoreManager();

            int[] tko = sm.rescoreTestKey(tk.getTestKeyId(), false, false);

            setStringInfoMessage("Pin: " + userBean.getPin() + " has been submitted for rescore. Reports have been deleted and will be picked up in the next automated cycle.");

            userBean.clear();
        } catch (STException e)
        {
            setMessage(e);
        } catch (Exception e)
        {
            LogService.logIt(e, "processRescoreTestKeyId()");

            setMessage(e);
        }

        return null;

    }

    public void clearAllSessionInfo()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (fc == null)
        {
            LogService.logIt("UserUtils.clearAllSessionData() NONFATAL unable to remove session data because FacesContext is NULL!!!");
            return;
        }

        fc.getExternalContext().getSessionMap().remove("adminEssayBean");

        getUserBean();
        userBean.clearBean();

    }

    public String processUserLogOff()
    {
        try
        {
            getUserBean();

            processLogOff(LogoffType.USER.getLogoffTypeId());

            FacesContext fc = FacesContext.getCurrentInstance();

            if (fc == null)
                throw new Exception("FacesContext is null.");

            fc.getExternalContext().redirect("/ts/index.xhtml");
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processUserLogOff()");

            setMessage(e);
        }

        return "/index.xhtml";
    }

    public void processLogOff(int logoffTypeId) throws Exception
    {
        try
        {
            getUserBean();

            clearAllSessionInfo();

            if (userFacade == null)
                userFacade = UserFacade.getInstance();

            if (userBean.getUserLoggedOnAsAdmin())
                LogService.logIt("UserUtils.processUserLogOff() Logging Off User: " + userBean.getUser().getUserId());

            if (userBean.getUserLoggedOnAsAdmin() && userBean.getLogonHistoryId() > 0)
                userFacade.addUserLogout(userBean.getLogonHistoryId(), logoffTypeId);

            userBean.setLogonHistoryId(0);

            userBean.setUser(null);

            userBean.clear();

            FacesContext fc = FacesContext.getCurrentInstance();

            if (fc == null)
                throw new Exception("FacesContext is null.");
            fc.getExternalContext().invalidateSession();
        } catch (Exception e)
        {
            LogService.logIt(e, "UserUtils.processLogOff()");
            throw e;
        }
    }

    public String getLogonKey()
    {
        return logonKey;
    }

    public void setLogonKey(String logonKey)
    {
        this.logonKey = logonKey;
    }

    public String getLogonName()
    {
        return logonName;
    }

    public void setLogonName(String logonName)
    {
        this.logonName = logonName;
    }

    public boolean getBooleanParam1()
    {
        return booleanParam1;
    }

    public void setBooleanParam1(boolean booleanParam1)
    {
        this.booleanParam1 = booleanParam1;
    }

    public String getStrParam1()
    {
        return strParam1;
    }

    public void setStrParam1(String strParam1)
    {
        this.strParam1 = strParam1;
    }

    public String getStrParam2()
    {
        return strParam2;
    }

    public void setStrParam2(String strParam2)
    {
        this.strParam2 = strParam2;
    }

    public String getStrParam3()
    {
        return strParam3;
    }

    public void setStrParam3(String strParam3)
    {
        this.strParam3 = strParam3;
    }

}
