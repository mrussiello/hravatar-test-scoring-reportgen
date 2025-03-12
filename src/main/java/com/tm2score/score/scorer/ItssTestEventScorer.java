/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.custom.hraph.bsp.itss.*;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest2.CT2TestEventScorer;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.profile.ProfileEntry;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.Constants;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.profile.ProfileFacade;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.IntnHist;
import com.tm2score.xml.JaxbUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author miker_000
 */
public class ItssTestEventScorer extends CT2TestEventScorer {
    
    public Itss itss;
    
    public ItssTestEventScorer()
    {
        super(); 
    }
    // protected static final String[] ROLE_TITLES = new String[] {"Administrative", "Application Systems", "Infrastructure", "IT Acquisition", "Program Management", "Security"};
    
    @Override
    public void scoreTestEvent( TestEvent testEvent, SimDescriptor simDescriptor, boolean skipVersionCheck) throws Exception
    {
        
       try
       {
           super.scoreTestEvent(testEvent, simDescriptor, skipVersionCheck );
           te = testEvent;           
           sd = simDescriptor;
           
           calculateAltScores();

           LogService.logIt( "ItssTestEventScorer.scoreTestEvent() COMPLETED SCORING te.scoreFormatTypeId=" + te.getScoreFormatTypeId() + ", " + te.toString() );
       }

       catch( ScoringException e )
       {
           LogService.logIt( "ItssTestEventScorer.scoreTestEvent() " + e.toString() + ", "  + te.toString() );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt( e, "ItssTestEventScorer.scoreTestEvent() "  + te.toString() );

           throw new ScoringException( e.getMessage() + " ItssTestEventScorer.scoreTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
       }
    }
    
    
    @Override
    public String getAdditionalTextScoreContentPacked() throws Exception
    {

        String fmSuper = super.getAdditionalTextScoreContentPacked();
        
        StringBuilder sb = new StringBuilder();
        try
        {
            List<String> top3Roles = getTopThreePreferredRoles();

            for( String roleName : top3Roles )
            {
                if( roleName==null || roleName.trim().isEmpty() )
                    continue;

                roleName = roleName.trim();
                
                if( sb.length() > 0 )
                    sb.append( Constants.DELIMITER );

                sb.append( roleName  );
                    // riskFactors.add( MessageFactory.getStringMessage( locale , ct3Rft.getKey()) );
            }
            
            if( sb.length() > 0 )
                return  (fmSuper!=null && !fmSuper.isEmpty() ? fmSuper : "" ) + ";;;" + Constants.PREFERREDROLENAMES + ";;;" + Constants.DELIMITER + sb.toString();

            else
                return  fmSuper!=null && !fmSuper.isEmpty() ? fmSuper : "";

        }

        catch( Exception e )
        {
            LogService.logIt( e, "ItssTestEventScorer.getAdditionalTextScoreContentPacked() " + te.toString() );

            throw e;
        }
    }

    
    
    
    protected List<String> getTopThreePreferredRoles() throws Exception
    {
        List<String> out = new ArrayList<>();
        
        if( te == null || te.getResultXml()==null || te.getResultXml().isEmpty() )
            return out;
        
        if( sd==null || sd.getXml()==null || sd.getXml().isEmpty() )
            return out;
        
        if( te.getResultXmlObj() == null )
            te.setResultXmlObj( JaxbUtils.ummarshalImoResultXml( te.getResultXml() ) );
        
        if( te.getSimXmlObj()==null )
            te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ));
                
        SimJ.Intn intn = getSimJIntnForUniqueId( itss.getPreferredRolesIntnUniqueId(), te.getSimXmlObj() );
        
        if( intn==null )
        {
            LogService.logIt( "ItssTestEventScorer.getTopThreePreferredRoles() Could not find SimJ.intn with uniqueId=" + itss.getPreferredRolesIntnUniqueId() + " in SimJ." + te.getTestEventId() );
            return out;
        }
        
        Clicflic.History.Intn cnh = getClicflicIntnForUniqueId( itss.getPreferredRolesIntnUniqueId(), te.getResultXmlObj() );
        
        if( cnh==null )
        {
            LogService.logIt( "Could not find clicflic.history.intn with uniqueId=" + itss.getPreferredRolesIntnUniqueId() + " in resultXml. " + te.getTestEventId() );
            return out;
        }

        // SimJ.Intn.Intnitem dragTgt;
        SimJ.Intn.Intnitem dragTenant;
        Map<Integer,List<Integer>> dragTgtMap = getDragTgtMap( cnh, intn );
        
        List<Integer> tenantItemSeqLst;
        String roleName;
        
        for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
        {
            if( iitm.getDrgtgt()!=1 )
                continue;
            
            tenantItemSeqLst = dragTgtMap.get( new Integer(iitm.getSeq() ));
            
            if( tenantItemSeqLst==null || tenantItemSeqLst.isEmpty() )
                continue;
            
            dragTenant = getSimJIntnItemForSeq( tenantItemSeqLst.get(0), intn);
            
            if( dragTenant != null )
            {
                roleName = getRoleNameForDragTenant( UrlEncodingUtils.decodeKeepPlus( dragTenant.getContent(), "UTF8" ) );
                
                if( roleName!=null && !roleName.trim().isEmpty() )
                    out.add( roleName );
            }
        }
        
        return out;
    }
    
    protected String getRoleNameForDragTenant( String iitmContent )
    {
        if( iitmContent==null || iitmContent.trim().isEmpty() )
            return null;
        
        iitmContent = iitmContent.toLowerCase();
        
        for( String roleName : itss.getRoleTitles() )
        {
            if( iitmContent.contains(roleName.toLowerCase() ) )
                    return roleName;
        }
        
        return null;
    }
    
    /**
     * Returns a Map of Drag Target Seq , List of Drag Tenant Seqs
     * @param cnh
     * @return 
     */
    protected Map<Integer,List<Integer>> getDragTgtMap( Clicflic.History.Intn cnh, SimJ.Intn intn  ) throws Exception
    {
        Map<Integer,List<Integer>> out = new HashMap<>();
        
        String ti = cnh.getValue();
        
        if( ti==null || ti.trim().isEmpty() )
            return out;
        
        String[] pairs = ti.split("~");
        
        String dtSeq;
        String drgbleSeqs;
        String[] drgbleSeqArr;
        List<Integer> drgbleSeqLst;
        
        SimJ.Intn.Intnitem iitm;
        
        for( int i=0;i<pairs.length-1; i+=2 )
        {
            if( pairs[i]==null || pairs[i].isEmpty() )
                continue;
            
            if( pairs[i+1]==null || pairs[i+1].isEmpty() )
                continue;
            
            dtSeq= UrlEncodingUtils.decodeKeepPlus(pairs[i],"UTF8");
            
            if( dtSeq==null || dtSeq.isEmpty() )
                continue;
            
            iitm = getSimJIntnItemForSeq( Integer.parseInt( dtSeq ), intn );
            
            // only ook at drag targets
            if( iitm==null || iitm.getDrgtgt()!=1 )
                continue;
            
            drgbleSeqs=UrlEncodingUtils.decodeKeepPlus(pairs[i+1],"UTF8");
            
            drgbleSeqArr=drgbleSeqs.split(",");
            drgbleSeqLst = new ArrayList<>();
            
            for( int j=0;j<drgbleSeqArr.length;j++ )
            {
                if( drgbleSeqArr[j]==null || drgbleSeqArr[j].isEmpty() )
                    continue;
                
                drgbleSeqLst.add( new Integer( drgbleSeqArr[j] ) );
            }

            out.put( new Integer(dtSeq),drgbleSeqLst ) ;           
        }
        
        return out;
    }
    
    
    protected SimJ.Intn.Intnitem getSimJIntnItemForSeq( int seq, SimJ.Intn intn )
    {
        for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
        {
            if( iitm.getSeq()== seq )
                return iitm;
        }
        
        return null;
    }

    protected Clicflic.History.Intn getClicflicIntnForUniqueId( String uniqueId, Clicflic clicflic )
    {
        if( clicflic==null || clicflic.getHistory()==null || clicflic.getHistory().getIntnOrClip()==null )
            return null;
        
        if( uniqueId==null || uniqueId.trim().isEmpty() )
            return null;
        
        uniqueId = uniqueId.trim();
        
        Clicflic.History.Intn intnO;
        IntnHist intn;
        
        for( Object o : clicflic.getHistory().getIntnOrClip() )
        {
            if( !o.getClass().equals( Clicflic.History.Intn.class ) )
                continue;
            
            intnO = (Clicflic.History.Intn)o;
            intn = new IntnHist( intnO);
            
            if( intn.getUnqid()==null || intn.getUnqid().trim().isEmpty() )
                continue;
            
            if( uniqueId.equals( intn.getUnqid().trim() ) )
                return intnO;
        }
        
        return null;
    }

    
    protected SimJ.Intn getSimJIntnForUniqueId( String uniqueId, SimJ simJ )
    {
        if( simJ==null || simJ.getIntn()==null )
            return null;
        
        if( uniqueId==null || uniqueId.trim().isEmpty() )
            return null;
        
        uniqueId = uniqueId.trim();
        
        for( SimJ.Intn intn : simJ.getIntn() )
        {
            if( intn.getUniqueid()==null )
                continue;
            
            if( uniqueId.equals( intn.getUniqueid().trim() ) )
                return intn;
        }
        
        return null;
    }
    
    
    protected void calculateAltScores() throws Exception
    {
        try 
        {
            LogService.logIt( "ItssTestEventScorer.calculateAltScores() AAAA testEventId=" + te.getTestEventId() );
            
            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();
            
            List<TestEventScore> tesl = eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true );
            
            te.setTestEventScoreList(tesl);
            
            // Get the profiles to evaluate.    
            tesl = te.getTestEventScoreList( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() );

            List<TestEventScore> ctesl =   te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
            ctesl.addAll( te.getTestEventScoreList( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() ) );
                        
            ProfileFacade profileFacade = ProfileFacade.getInstance();
            
            Product p = te.getProduct();
            
            if( p == null )
            {
                p = eventFacade.getProduct( te.getProductId() );
                te.setProduct(p);
            }
            
            List<Profile> pl = profileFacade.getProfileListForProductIdAndOrgIdAndProfileUsageType( te.getProductId(), te.getOrgId(), ProfileUsageType.ALTERNATE_OVERALL_COMPETENCY_WEIGHTS.getProfileUsageTypeId() );

            Set<Profile> ps = new HashSet<>();
            
            ps.addAll( pl );
            // profileFacade.getProfileListForProfileUsageTypeIdProductId( ProfileUsageType.ALTERNATE_OVERALL_COMPETENCY_WEIGHTS.getProfileUsageTypeId(), te.getProductId() );

            
            LogService.logIt( "ItssTestEventScorer.calculateAltScores() BBB ProfileSet.len=" + ps.size() );

            
            TestEventScore tes;
            TestEventScore tes2;
            Profile prof;
            
            float altScore = 0;
            
            // For each role
            for( String role : itss.getRoleTitles() )
            {
                // get the profile
                prof = getProfileFmList( role, ps );
                
                // if not found
                if( prof==null )
                {
                    LogService.logIt( "ItssTestEventScorer.calculateAltScores() CCC role=" + role + ", profile=NULL" );
                    continue;
                }
                
                // get the entries
                if( prof.getProfileEntryList()== null )
                    prof.setProfileEntryList( profileFacade.getProfileEntryList( prof.getProfileId() ));
                
                // see if an existing TES is present.
                tes = getTestEventScoreFmList( role, tesl );
                
                // calculate score
                altScore = calcAltScore( prof, ctesl );
                
                LogService.logIt( "ItssTestEventScorer.calculateAltScores() DDD role=" + role + ", profile=" + prof.toString() + ", tes=" + (tes==null ? "null" : "not null " + tes.getTestEventScoreId() ) );
                
                // no existing TES - create one.
                if( tes==null )
                {
                    tes = new TestEventScore();
                    tes.setTestEventId( te.getTestEventId());
                    tes.setTestEventScoreTypeId( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() );
                    tes.setName(role);
                    tes.setNameEnglish(role);
                    tes.setScore(altScore);
                    tes.setTestEvent(te);
                    tes2 = getMatchingExistingTestEventScore(tes);
                    if( tes2!=null && tes2.getTestEventScoreId()>0 )
                        tes.setTestEventScoreId( tes2.getTestEventScoreId() );
                }
                
                // save results.
                tes.setCreateDate( new Date() );
                tes.setName(role);
                tes.setNameEnglish(role);
                tes.setScore(altScore);
                eventFacade.saveTestEventScore(tes);
            }
        }
        
        
        catch( Exception e )
        {
           LogService.logIt( e, "ItssTestEventScorer.scoreTestEvent() "  + te.toString() );

           throw new ScoringException( e.getMessage() + " ItssTestEventScorer.scoreTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
        }
        
    }
    
    private float calcAltScore( Profile p, List<TestEventScore> ctesl ) throws Exception
    {
        float score = 0;
        
        if( p==null )
            return score;
        
        if( ctesl==null )
            return score;
        
        if( p.getProfileEntryList()==null )
            p.setProfileEntryList( ProfileFacade.getInstance().getProfileEntryList( p.getProfileId() ));
        
        TestEventScore tes;
        float weights = 0;

        for( ProfileEntry pe : p.getProfileEntryList() )
        {
            tes = getTestEventScoreFmList(  pe.getName(), ctesl );
            
            if( tes == null )
                continue;
            
            if( pe.getWeight()<=0 )
                continue;
            
            score += tes.getScore()*pe.getWeight();
            weights += pe.getWeight();
        }
    
        if( weights> 0 )
            score = score/weights;
        
        return score;
    }
    
    private TestEventScore getTestEventScoreFmList(  String name, List<TestEventScore> tesl )
    {
        if( name==null || name.isEmpty() )
            return null;
        
        for( TestEventScore tes : tesl )
        {
            if( tes.getName()!=null && tes.getName().equalsIgnoreCase(name) )
                return tes;
            if( tes.getNameEnglish()!=null && tes.getNameEnglish().equalsIgnoreCase(name) )
                return tes;
        }
        
        return null;
    }
    
    private Profile getProfileFmList( String roleName, Collection<Profile> ps )
    {
        if( roleName==null || roleName.isEmpty() )
            return null;
        
        Iterator<Profile> pi = ps.iterator();
        
        Profile p;
        
        while( pi.hasNext() )
        {
            p = pi.next();
            
            // LogService.logIt( "ItssEventScorer.getProfileFmList() Checking: " + p.getName() );
            
            if( p.getName()!=null && p.getName().trim().equalsIgnoreCase(roleName))
                return p;
            
            if( p.getStrParam1()!=null && p.getStrParam1().trim().equalsIgnoreCase(roleName))
                return p;
        }
        
        return null;
    }
    
    
    public String toString()
    {
        return "ItssTestEventScorer () " + (te==null ? "te is null" : te.toString() );
        
    }

    
}
