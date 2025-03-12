/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.service.LogService;
import com.tm2score.voicevibes.VoiceVibesResult;
import com.tm2score.voicevibes.VoiceVibesUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class MergableScoreObjectCombiner {
    
    public static List<MergableScoreObject> combineLikeObjects( List<MergableScoreObject> inList ) throws Exception 
    {
        // LogService.logIt( "MergableScoreObjectCombiner.combineLikeObjects() inList contains " + inList.size() + " objects. " );
        
        List<MergableScoreObject> out = new ArrayList<>();
        
        if( inList==null || inList.isEmpty() )
            return out;
        
        for( MergableScoreObjectType msot : MergableScoreObjectType.values() )
        {
            out.addAll( combineObjectsForType( getListForType( inList, msot), msot ) );
        }
        
        return out;
    }
    
    
    private static List<MergableScoreObject> combineObjectsForType( List<MergableScoreObject> typeList, MergableScoreObjectType msot ) throws Exception
    {
        // LogService.logIt( "MergableScoreObjectCombiner.combineObjectsForType() inList contains " + typeList.size() + " objects. Type=" + msot.getName() );
        List<MergableScoreObject> out = new ArrayList<>();
     
        if( msot.equals( MergableScoreObjectType.VOICEVIBES ) )
        {
            List<VoiceVibesResult> vvrl = new ArrayList<>();
            
            for( MergableScoreObject mso : typeList )
            {
                vvrl.add( (VoiceVibesResult) mso );
            }

            VoiceVibesResult vvr = VoiceVibesUtils.combineVoiceVibesResults(vvrl);
            
            out.add(vvr);
        }
        
        return out;
    }
    
    
    private static List<MergableScoreObject> getListForType( List<MergableScoreObject> inList, MergableScoreObjectType msot)
    {
        List<MergableScoreObject> out = new ArrayList<>();

        for( MergableScoreObject mso : inList )
        {
            if( mso.getMergableScoreObjectTypeId()== msot.getMergableScoreObjectTypeId() )
                out.add( mso );
        }
        
        return out;
    }
}

