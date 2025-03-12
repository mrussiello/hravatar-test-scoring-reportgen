package com.tm2score.ibmcloud;

import java.util.ArrayList;
import java.util.List;

public enum SentinoTraitType
{
    /*
     For Cultural Fit, just use CPI.
    
     For Performance, use 
    */
    BIG5_AGREEABLE(51,"agreeableness", 1 ), 
    BIG5_CONSCIENTIOUSNESS(52,"conscientiousness", 1 ), 
    BIG5_EXTRAVERSION(53,"extraversion", 1 ),
    BIG5_NEUROTIC(54,"neuroticism", 1 ),                   
    BIG5_OPENNESS(55,"openness", 1 ),
    
    SIX_ACHIEVEMENT(100,"achievement-striving", 4),     
    SIX_ADAPTABILITY(101,"adaptability", 4),           
    SIX_INDEPENDENCE(102,"independence", 4),           
    SIX_INDUSTRIOUSNESS(103,"industriousness", 4),  
    SIX_METHODICALNESS(104,"methodicalness", 4),     
    SIX_RESOURCEFULNESS(105,"resourcefulness", 4),  
    SIX_SELFSUFFICIENCY(106,"self-sufficiency", 4), 
    SIX_UNPRETENTIOUSNESS(107,"unpretentiousness", 4),  
    
    CPI_ADVENTUROUSNESS(200,"adventurousness", 3),    
    CPI_AMIABILITY(201,"amiability", 3),             
    CPI_ASSERTIVENESS(202,"assertiveness", 3),    
    CPI_CALMNESS(203,"calmness", 3),                    
    CPI_COMPETENCE(204,"competence", 3),            
    CPI_COMPLEXITY(205,"complexity", 3),
    CPI_COMPREHENSION(206,"comprehension", 3),
    CPI_DEPTH(207,"depth", 3),                            
    CPI_DISORDER(208,"disorder", 3),
    CPI_DOMINANCE(209,"dominance", 3),
    CPI_DUTIFULNESS(210,"dutifulness", 3),          
    CPI_FORCEFULNESS(211,"forcefulness", 3),        
    CPI_GOODNATURE(212,"good-nature", 3),             
    CPI_HAPPINESS(213,"happiness", 3),
    CPI_INSIGHT(214,"insight", 3),
    CPI_INTELLECT(215,"intellect", 3),
    CPI_INTROVERSION(216,"introversion", 3),
    CPI_LIBERALISM(217,"liberalism", 3),
    CPI_OPTIMISM(218,"optimism", 3),
    CPI_PLANFULNESS(219,"planfulness", 3),           // Drive?
    CPI_POISE(220,"poise", 3),                             // Emotional Self-control?
    CPI_POLITENESS(221,"politeness", 3),
    CPI_RESPONSIBILITY(222,"responsibility", 3),
    CPI_SECURITY(223,"security", 3),
    CPI_SELFCONTROL(224,"self-control", 3),          // Emotional Self-control
    CPI_SELFDISCIPLINE(225,"self-discipline", 3), // Drive
    CPI_SELFEFFICACY(226,"self-efficacy", 3),       
    CPI_SENTIMENTALITY(227,"sentimentality", 3),  
    CPI_SOCIABILITY(228,"sociability", 3),            
    CPI_STABILITY(229,"stability", 3),                 
    CPI_TEMPERANCE( 230,"temperance", 3),             
    CPI_TIMIDITY(231,"timidity", 3),
    CPI_TOLERANCE(232,"tolerance", 3),                 
    
    AB5C_IMPULSE(250,"impulse control", 6),
    AB5C_LEADERSHIP(251,"leadership", 6),
    BISBAS_DRIVE(300,"drive", 5),                            
    HEXACO_FAIRNESS(350,"fairness", 2),                   
    MPQ_SOCIAL_POTENCY(400,"social potency", 8), 
    NEO_SYMPATHY(450,"sympathy", 9),                        
    NEO_COOPERATION(451,"cooperation", 9),  
    NEO_ACHIEVEMENT(452,"achievement", 9),          
    VIA_CITIZENSHIP(500,"citizenship", 7);                 


    
    private final int sentinoTraitTypeId;
    private final int sentinoGroupTypeId;
    private final String sentinoKey;


    private SentinoTraitType( int p, String sentinoKey, int sentinoGroupTypeId)
    {
        this.sentinoTraitTypeId = p;
        this.sentinoKey = sentinoKey;
        this.sentinoGroupTypeId=sentinoGroupTypeId;
    }

    
    public int getSentinoGroupTypeId() {
        return sentinoGroupTypeId;
    }

    public String getSentinoKey() {
        return sentinoKey;
    }
    
    public SentinoGroupType getSentinoGroupType()
    {
        return SentinoGroupType.getValue( this.sentinoGroupTypeId );
    }
        
        
   
    public String getName()
    {
        return sentinoKey;
    }
    
    public int getSentinoTraitTypeId() {
        return sentinoTraitTypeId;
    }

    public static List<SentinoTraitType> getForGroupId( int sentinoGroupTypeId )
    {
        List<SentinoTraitType> out = new ArrayList<>();
        
        SentinoTraitType[] vals = SentinoTraitType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getSentinoGroupTypeId()==sentinoGroupTypeId )
                out.add(vals[i]);
        }

        return out;
    }
      

    
    
    public static SentinoTraitType getForGroupIdAndSentinoKey( int sentinoGroupTypeId, String sentinoKey )
    {
        SentinoTraitType[] vals = SentinoTraitType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getSentinoGroupTypeId()!=sentinoGroupTypeId )
                continue;
            if( sentinoKey!=null && sentinoKey.equals(vals[i].sentinoKey) )
                return vals[i];
        }

        return null;
    }
      

    
    public static SentinoTraitType getValue( int id )
    {
        SentinoTraitType[] vals = SentinoTraitType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getSentinoTraitTypeId() == id )
                return vals[i];
        }

        return null;
    }
      
}
