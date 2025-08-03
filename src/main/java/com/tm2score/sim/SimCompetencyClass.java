package com.tm2score.sim;

import com.tm2score.score.iactnresp.IactnItemResp;
import com.tm2score.score.iactnresp.IactnResp;


public enum SimCompetencyClass
{
    NONCOGNITIVE(0,"Personality/ Non-Cog"),
    ABILITY(1,"Ability"),
    CORESKILL(2,"Core Skill"),
    KNOWLEDGE(3,"Knowledge"),
    EQ(4,"Emotional Intelligence"),
    CUSTOM(21,"Custom 1"),
    CUSTOM2(22,"Custom 2"),
    CUSTOM3(23,"Custom 3"),
    CUSTOM4(24,"Custom 4"),
    CUSTOM5(25,"Custom 5"),
    SCOREDTASK(100,"Scored Task"),
    SCOREDINTEREST(120,"Scored Interest"),
    SCOREDEXPERIENCE(130,"Scored Experience"),
    SCOREDBIODATA(140,"Scored Biodata"),
    SCOREDTYPING(141,"Scored Typing"),
    SCOREDDATAENTRY(146,"Scored Data Entry"),
    SCOREDESSAY(142,"Scored Essay"),
    SCOREDAUDIO(143,"Scored Audio" ),
    SCOREDAVUPLOAD(144,"Scored Audio/Video Upload" ),
    SCOREDIMAGEUPLOAD(145,"Scored Image Upload" ),
    SCOREDCHAT(147,"Scored Chat" ),
    ABILITY_COMBO(150,"Ability Combination"),
    SKILL_COMBO(151,"Skill Combination"),
    NONCOG_COMBO(152,"Non-Cognitive Combination"),
    VOICE_PERFORMANCE_INDEX(153,"Voice Performance Index"),
    INTERESTS_COMBO(154,"Interests Combination"),
    CUSTOM_COMBO(155,"Custom Combination"),
    BIODATA_COMBO(156,"Biodata Combination"),
    AGGREGATEABILITY(200,"AggregateAbility"),
    AGGREGATESKILL(201,"AggregateSkill"),
    AGGREGATEKNOWLEDGE(202,"AggregateKnowledge"),
    UNSCORED(300,"Unscored" );

    

    
    private final int simCompetencyClassId;

    private final String name;

    private SimCompetencyClass( int s , String n )
    {
        this.simCompetencyClassId = s;

        this.name = n;
    }

    public String getTopicCorrectStub()
    {
        if( equals( SCOREDCHAT ) )
            return "NoCorrect";
        
        return "";
    }

    public boolean getSupportsSubclass()
    {
        return  equals( SCOREDAVUPLOAD );        
    }
    

    public String getMetaName( int idx )
    {
        if( equals(SCOREDTYPING) )
        {
            return switch (idx) {
                case 2 -> "wpm";
                case 3 -> "adjwpm";
                case 4 -> "accuracy";
                default -> null;
            };
        }

        else if( equals(SCOREDDATAENTRY) )
        {
            return switch (idx) {
                case 2 -> "ksph";
                case 3 -> "adjksph";
                case 4 -> "acc";
                case 5 -> "errs";
                case 6 -> "strokes";
                case 7 -> "time";
                default -> null;
            };
        }

        else if( equals(SCOREDESSAY) )
        {
            return switch (idx) {
                case 2 -> "machinescore";
                case 3 -> "confidence";
                case 4 -> "spellerrs";
                case 5 -> "othererrs";
                case 6 -> "words";
                case 8 -> "plag";
                case 9 -> "transscore";
                default -> null;
            };
        }
        
        else if( equals(SCOREDCHAT) )
        {
            return switch (idx) {
                case 2 -> "rapport";
                case 3 -> "spelling";
                case 4 -> "respsecs";
                case 5 -> "negexp";
                default -> null;
            };
        }        
        
        return null;
    }
    
    public boolean getMetaIsCanonical( int idx )
    {
        if( equals(SCOREDTYPING) || equals(SCOREDDATAENTRY) )
            return idx==3 || idx==4;

        return false;
    }

    
    
    
    public boolean getSupportsPercentiles()
    {
        return !equals( NONCOGNITIVE ) && !equals( SCOREDBIODATA ) && !equals( BIODATA_COMBO ) && !equals( EQ ) && !equals( NONCOG_COMBO );
    }


    public boolean getCollectsSamples()
    {
         return equals( SCOREDESSAY ) || equals( SCOREDAUDIO ) || equals( SCOREDAVUPLOAD ) || equals( SCOREDIMAGEUPLOAD );
    }
    
    public boolean isKSA()
    {
        return  equals( ABILITY ) || equals( CORESKILL ) ||equals( KNOWLEDGE ) || equals( EQ ) || equals( SCOREDCHAT ) ||
                equals( AGGREGATESKILL ) || equals( AGGREGATEKNOWLEDGE ) || equals( AGGREGATEABILITY ) ||
                equals( SCOREDTYPING ) || equals( SCOREDDATAENTRY ) || equals( SCOREDESSAY ) || equals( SCOREDAUDIO ) || 
                equals( SCOREDAVUPLOAD ) || equals( ABILITY_COMBO ) || equals(SKILL_COMBO);
    }

    public boolean isKnowledgeSkillAbility()
    {
        return equals( ABILITY ) || equals( CORESKILL ) ||equals( KNOWLEDGE );
    }
    
    public boolean isAbility()
    {
        return  equals( ABILITY ) || equals( AGGREGATEABILITY ) || equals( ABILITY_COMBO );
    }

    public boolean isKS()
    {
        return  equals( CORESKILL ) ||equals( KNOWLEDGE ) || equals( SCOREDCHAT ) ||
                equals( AGGREGATESKILL ) || equals( AGGREGATEKNOWLEDGE ) ||
                equals( SCOREDTYPING ) || equals( SCOREDDATAENTRY ) || equals( SCOREDESSAY ) || 
                equals( SCOREDAUDIO ) || equals( SCOREDAVUPLOAD ) || equals(SKILL_COMBO) ;
    }

    public boolean isAnyCustom()
    {
        return equals( CUSTOM ) ||equals( CUSTOM2 ) || equals( CUSTOM3 ) || equals( CUSTOM4 ) || equals( CUSTOM5 ) || equals( CUSTOM_COMBO);
    }
    
    public boolean isCoreSkill()
    {
        return  equals( CORESKILL );
    }
    
    public boolean getProducesTopics()
    {        
        return equals( CORESKILL ) || equals( KNOWLEDGE ) || equals( SCOREDCHAT );        
    }
    

    public boolean isBiodata()
    {
        return equals( SCOREDBIODATA ) || equals(BIODATA_COMBO);
    }
    
    public boolean isAIDerived()
    {
        return equals( VOICE_PERFORMANCE_INDEX );
    }
    
    
    public boolean isUnscored()
    {
        return equals( UNSCORED );
    }
    
    public boolean isScoredEssay()
    {
        return equals( SCOREDESSAY );
    }

    public boolean isScoredChat()
    {
        return equals( SCOREDCHAT );
    }
    
    public boolean isScoredAudio()
    {
        return equals( SCOREDAUDIO );
    }
    
    public boolean isScoredAvUpload()
    {
        return equals( SCOREDAVUPLOAD );
    }

    public boolean isScoredImageUpload()
    {
        return equals( SCOREDIMAGEUPLOAD );
    }


    public boolean isScoredTyping()
    {
        return equals( SCOREDTYPING );
    }
    
    public boolean isScoredDataEntry()
    {
        return equals( SCOREDDATAENTRY );       
    }

    public boolean isInterests()
    {
        return  equals( SCOREDINTEREST ) || equals( INTERESTS_COMBO );
    }

    
    public boolean isAIMS()
    {
        return  equals( NONCOGNITIVE ) || equals( NONCOG_COMBO );
    }

    public boolean isEQ()
    {
        return  equals( EQ ) ;
    }

    public boolean isScoredExperience()
    {
        return equals( SCOREDEXPERIENCE );
    }



    public boolean getSupportsQuasiDichotomous()
    {
        return  equals( ABILITY ) || equals( CORESKILL ) ||equals( KNOWLEDGE ) || equals( SCOREDCHAT );
    }

    public boolean getUsesKnowledgeInterpretation()
    {
        return equals( CORESKILL ) ||equals( KNOWLEDGE ) ||  equals( AGGREGATESKILL ) || equals( AGGREGATEKNOWLEDGE ) || equals( SCOREDTYPING ) || equals( SCOREDDATAENTRY ) || equals( SCOREDCHAT ) ;
    }

    public boolean getUsesAbilityInterpretation()
    {
        return equals( ABILITY ) || equals( AGGREGATEABILITY ) || equals( ABILITY_COMBO );
    }

    public boolean getUsesBioInterpretation()
    {
        return equals( SCOREDBIODATA ) || equals(BIODATA_COMBO);
    }

    public boolean getUsesWritingInterpretation()
    {
        return equals( SCOREDESSAY );
    }



    public float getAggregatePoints( IactnItemResp iir )
    {
        if( equals( AGGREGATEKNOWLEDGE ) )
            return iir.getIntnItemObj().getScoreparam1();

        if( equals( AGGREGATESKILL ) )
            return iir.getIntnItemObj().getScoreparam2();

        if( equals( AGGREGATEABILITY ) )
            return iir.getIntnItemObj().getScoreparam3();

        return 0;
    }

    public float getAggregateMaxPoints( IactnResp ir )
    {
        if( equals( AGGREGATEKNOWLEDGE ) )
            return ir.getMaxPointsArray()[1];

        if( equals( AGGREGATESKILL ) )
            return ir.getMaxPointsArray()[2];

        if( equals( AGGREGATEABILITY ) )
            return ir.getMaxPointsArray()[3];

        return 0;
    }

    public float getAggregateMaxPoints( float[] mpa )
    {
        if( mpa == null )
            return 0;

        if( equals( AGGREGATEKNOWLEDGE ) && mpa.length>=2 )
            return mpa[1];

        if( equals( AGGREGATESKILL ) && mpa.length>=3 )
            return mpa[2];

        if( equals( AGGREGATEABILITY ) && mpa.length>=4 )
            return mpa[3];

        return 0;
    }


    public boolean getIsDirectCompetency()
    {
        return !getIsTask() && !getIsInterest() && !getIsExperience() && !getIsBiodata() &&!getIsAggregate() && !getIsCombo();
    }

    public boolean getRequiresCalcAcrossAll()
    {
        return getIsTask() || getIsInterest() || getIsExperience() || getIsBiodata() || getIsAggregate() || getIsCombo() || isAIDerived();
    }


    public boolean getIsCombo()
    {
        return equals( ABILITY_COMBO ) || equals(SKILL_COMBO) || equals(NONCOG_COMBO) || equals(INTERESTS_COMBO) || equals(CUSTOM_COMBO) || equals(BIODATA_COMBO);        
    }
    
    public boolean getIsAnyCustom()
    {
        return equals( CUSTOM ) ||equals( CUSTOM2 ) || equals( CUSTOM3 ) || equals( CUSTOM4 ) || equals( CUSTOM5 ) || equals( CUSTOM_COMBO );
    }
    

    public boolean getIsTask()
    {
        return equals( SCOREDTASK );
    }

    public boolean getIsInterest()
    {
        return equals( SCOREDINTEREST ) || equals(INTERESTS_COMBO);
    }

    public boolean getIsExperience()
    {
        return equals( SCOREDEXPERIENCE );
    }

    public boolean getIsBiodata()
    {
        return equals( SCOREDBIODATA ) || equals(BIODATA_COMBO);
    }

    public boolean getIsAggregateOrTask()
    {
        return getIsTask() || getIsAggregate();
    }

    public boolean getIsAggregate()
    {
        return simCompetencyClassId>=200 && simCompetencyClassId < 300;
    }




    public static SimCompetencyClass getValue( int id )
    {
        SimCompetencyClass[] vals = SimCompetencyClass.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyClassId() == id )
                return vals[i];
        }

        return NONCOGNITIVE;
    }


    public int getSimCompetencyClassId()
    {
        return simCompetencyClassId;
    }

    public String getName()
    {
        return name;
    }

}
