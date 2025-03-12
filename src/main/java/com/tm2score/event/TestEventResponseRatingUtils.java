/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.event;

import com.tm2score.entity.event.TestEventResponseRating;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.Org;
import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author miker_000
 */
public class TestEventResponseRatingUtils 
{    
    
    TestEventResponseRatingFacade terrFacade;
    
    
    public static Map<String,String> getOverallAverageRatingMap( List<TestEventResponseRating> trrl, Locale locale )
    {
        if( trrl==null || trrl.isEmpty() )
            return null;
        
        Map<String,float[]> vm = new HashMap<>();
        
        // vals[0]=count, vals[1]=total
        float[] vals;
        
        float rating;
        String name;
        String[] names;
        float[] ratings;
        
        for( TestEventResponseRating terr : trrl )
        {
            if( terr.getRatingNameListSize()<=0 )
            {
                LogService.logIt( "TestEventResponseRatingUtils.getOverallAverageRatingMap() ratingNameListSize invalid: " + terr.getRatingNameListSize() + ", skipping in averages. " + terr.toString() );
                continue;                
            }
            if( !terr.getHasAnyRatingData() )
            {
                LogService.logIt( "TestEventResponseRatingUtils.getOverallAverageRatingMap() No rating data. skipping in averages. " + terr.toString() );
                continue;                
            }
            
            names = terr.getRatingNameArray();
            ratings = terr.getRatingArray();
            for( int i=0;i<terr.getRatingNameListSize();i++ )
            {
                name = names[i];
                rating = ratings[i];
                
                vals = vm.get(name);
                if( vals==null )
                {
                    vals=new float[2];
                    vm.put(name, vals);
                }
                
                vals[0] += 1;
                vals[1] += rating;
            }
        }
        
        if( vm.size()<=0 )
            return null;
        
        Map<String,String> out = new TreeMap<>();
        
        if( locale==null )
            locale=Locale.US;
        
        for( String nm : vm.keySet() )
        {
            vals = vm.get(nm);
            
            if( vals==null || vals[0]<=0 )
                continue;
            
            vals[1] = vals[1] / vals[0];
            
            out.put( nm, I18nUtils.getFormattedNumber(locale, vals[1], 1 ) );
        }
        
        return out;
    }
    
    
    public static boolean getHasAnyNonSimCompetencyRatings(List<TestEventResponseRating> terrl)
    {
        if( terrl==null || terrl.isEmpty() )
            return false;
        
        for( TestEventResponseRating t : terrl )
        {
            if( t.getRatingNameList()==null )
                continue;
            
            if( !t.getHasAnyAltSimCompetencyId() )
                return true;
        }
        
        return false;
    }
    
    public static void setTestEventResponseRatingNames( Org o, Product product, List<TestEventScore> tesl, Locale locale, List<TestEventResponseRating> terrl)
    {
        if( terrl==null || terrl.isEmpty() )
            return;
        
        List<String> genericNameList = TestEventResponseRatingUtils.getTestEventResponseRatingNameList(o, product, null, null, locale);
        
        List<String> nl;
        
        for( TestEventResponseRating t : terrl )
        {
            if( t.getRatingNameList()!=null )
                continue;
            
            if( !t.getHasAnyAltSimCompetencyId() )
            {
                t.setRatingNameList(genericNameList);
                continue;
            }
            
            nl = new ArrayList<>();
            
            if( t.getSimCompetencyId()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId(), tesl ) );
            if( t.getSimCompetencyId2()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId2(),  tesl ) );
            if( t.getSimCompetencyId3()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId3(),  tesl ) );
            if( t.getSimCompetencyId4()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId4(),  tesl ) );
            if( t.getSimCompetencyId5()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId5(),  tesl ) );
            if( t.getSimCompetencyId6()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId6(),  tesl ) );
            if( t.getSimCompetencyId7()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId7(),  tesl ) );
            if( t.getSimCompetencyId8()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId8(),  tesl ) );
            if( t.getSimCompetencyId9()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId9(),  tesl ) );
            if( t.getSimCompetencyId10()>0 )
                nl.add( getTestEventScoreNameForSimCompetencyId( t.getSimCompetencyId10(),  tesl ) );
            
            t.setRatingNameList(nl);            
        }
    }
    
    public static String getTestEventScoreNameForSimCompetencyId( long simCompetencyId, List<TestEventScore> tesl )
    {
        if( tesl==null )
            return "NOT FOUND (A)";
        
        for( TestEventScore tes : tesl )
        {
            if( tes.getSimCompetencyId()==simCompetencyId )
                return tes.getName();
        }
        return "NOT FOUND (B)";
    }
    
    
    public static List<String> getTestEventResponseRatingNameList( Org o, Product p, List<Long> simCompetencyIdList, List<TestEventScore> tesl, Locale locale)
    {
        List<String> out = new ArrayList<>();
        
        if( simCompetencyIdList!=null && !simCompetencyIdList.isEmpty()&& tesl!=null && !tesl.isEmpty() )
        {
            scidloop:
            for( Long simCompetencyId : simCompetencyIdList )
            {
                if( simCompetencyId<=0 )
                    continue;
                
                for( TestEventScore tes :tesl )
                {
                    if( !tes.getTestEventScoreType().getIsCompetency() )
                        continue;

                    if( tes.getSimCompetencyId()==simCompetencyId )
                    {
                        out.add( tes.getName() );
                        continue scidloop;
                    }
                }        
                
                // If get here we have an issue.
                LogService.logIt( "TestEventResponseRatingUtils.getTestEventResponseRatingNameList() Cannot find a TestEventScore with simCompetencyId=" + simCompetencyId  );
            }
        }
        
        if( !out.isEmpty() )
            return out;
        
        if( p!=null && p.getProductType().getIsSimOrCt5Direct() && p.getStrParam3()!=null && !p.getStrParam3().isBlank() )
        {
            for( String s : p.getStrParam3().split(";") )
            {
                if( s.isBlank() )
                    continue;
                out.add(s.trim());
            }
        }

        if( !out.isEmpty() )
            return out;
                
        if( locale==null )
            locale=Locale.US;
               
        boolean hasNames = false;
        
        if( o.getRatingName1()!=null && !o.getRatingName1().isBlank() )
        {
            out.add( o.getRatingName1() );
            hasNames = true;
        }
        else
        {
            out.add( MessageFactory.getStringMessage(locale, "g.RatingX", new String[]{Integer.toString(1)}));
        }

        if( o.getRatingName2()!=null && !o.getRatingName2().isBlank() )
        {
            out.add( o.getRatingName2() );
            hasNames = true;
        }
        else if( hasNames )
            return out;
        else
            out.add( MessageFactory.getStringMessage(locale, "g.RatingX", new String[]{Integer.toString(2)}));

        if( o.getRatingName3()!=null && !o.getRatingName3().isBlank() )
            out.add( o.getRatingName3() );
        else if( hasNames )
            return out;
        else
            out.add( MessageFactory.getStringMessage(locale, "g.RatingX", new String[]{Integer.toString(3)}));
        
        if( o.getRatingName4()!=null && !o.getRatingName4().isBlank() )
            out.add( o.getRatingName4() );
        else
            return out;
        
        if( o.getRatingName5()!=null && !o.getRatingName5().isBlank() )
            out.add( o.getRatingName5() );
        else
            return out;
        
        if( o.getRatingName6()!=null && !o.getRatingName6().isBlank() )
            out.add( o.getRatingName6() );
        else
            return out;
        
        if( o.getRatingName7()!=null && !o.getRatingName7().isBlank() )
            out.add( o.getRatingName7() );
        else
            return out;
        
        if( o.getRatingName8()!=null && !o.getRatingName8().isBlank() )
            out.add( o.getRatingName8() );
        else
            return out;
        
        if( o.getRatingName9()!=null && !o.getRatingName9().isBlank() )
            out.add( o.getRatingName9() );
        else
            return out;
        
        if( o.getRatingName10()!=null && !o.getRatingName10().isBlank() )
            out.add( o.getRatingName10() );
        
        return out;        
    }
    
    
    
    
    public List<TestEventResponseRating> getTestEventResponseRatingList( long testEventId ) throws Exception
    {
        try
        {
            if( terrFacade==null )
                terrFacade = TestEventResponseRatingFacade.getInstance();
            
            return terrFacade.getTestEventResponseRatingsForTestEventId(testEventId);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventResponseRatingUtils.getTestEventResponseRatingList() testEventId=" + testEventId );
            throw e;
        }
    }
    
    /**
     * Creates a 
     *   object[0]=count
     *   object[1]=average rating
     * @param testEventId
     * @return 
     */
    public Object[] getAverageResponseRatingForSimCompetency( List<TestEventResponseRating> terrList, long simCompetencyId ) throws Exception
    {
        Object[] out = new Object[2];
        try
        {
            float total=0;
            float rating;
            int count=0;
            for( TestEventResponseRating terr : terrList )
            {
                if( !terr.getHasAltSimCompetencyId(simCompetencyId) )
                    continue;
                rating = terr.getRatingForAltSimCompetencyId(simCompetencyId);
                if( rating<0 )
                    continue;
                count++;
                total+=rating;
            }
            if( count>0 )
                total = total/count;
            out[0]=count;
            out[1]=total;
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventResponseRatingUtils.getAverageResponseRatingMapForSimCompetency() simCompetencyId=" + simCompetencyId);
            throw e;
        }
            
    }
}
