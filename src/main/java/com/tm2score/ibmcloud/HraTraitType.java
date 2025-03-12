package com.tm2score.ibmcloud;

import com.tm2score.global.Constants;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum HraTraitType
{
    /*
     For Cultural Fit, just use CPI.
    
     For Performance, use 
    */
    
    
    ADAPTABILITY(101,"Adaptability", "htrtyp.adaptability", true, true, new int[]{101} ), 
    DRIVE(102,"Drive", "htrtyp.drive", true, false, new int[]{100,103,105,202,211} ), 
    INTEGRITY(103,"Integrity", "htrtyp.integrity", true, false, new int[]{104,107,210,219} ), 
    COMPETITIVESPIRIT(104,"Competitive Spirit", "htrtyp.competitivespirit", true, false, new int[]{200,211,202,209} ), 
    TEAMWORK(105,"Teamwork", "htrtyp.teamwork", true, false, new int[]{201,212,228,232,22} ), 
    EMPATHY_EMOTIONALSC(106,"Empathy and Emotional Self-Control", "htrtyp.empathyemotional", true, false, new int[]{220,227,224,230} ), 
    RESILIENCE(107,"Resilience", "htrtyp.resilience", true, false, new int[]{106,102,226} ), 
        
    
    ADVENTUROUSNESS(200,"adventurousness", "htrtyp.adventure", false, true, new int[]{200} ),    
    AMIABILITY(201,"amiability", "htrtyp.amiability", false, true, new int[]{201} ),             
    ASSERTIVENESS(202,"assertiveness", "htrtyp.assertiveness", false, true, new int[]{202} ),    
    CALMNESS(203,"calmness", "htrtyp.calmness", false, true, new int[]{203} ),                    
    COMPETENCE(204,"competence", "htrtyp.competence", false, true, new int[]{204} ),             
    COMPLEXITY(205,"complexity", "htrtyp.complexity", false, true, new int[]{205} ),
    COMPREHENSION(206,"comprehension", "htrtyp.comprehension", false, true, new int[]{206} ),
    DEPTH(207,"depth", "htrtyp.depth", false, true, new int[]{207} ),                            
    DISORDER(208,"disorder", "htrtyp.disorder", false, true, new int[]{208} ),
    DOMINANCE(209,"dominance", "htrtyp.dominance", false, true, new int[]{209} ),
    DUTIFULNESS(210,"dutifulness", "htrtyp.dutifulness", false, true, new int[]{210} ),           
    FORCEFULNESS(211,"forcefulness", "htrtyp.forcefulness", false, true, new int[]{211} ),       
    GOODNATURE(212,"good-nature", "htrtyp.goodnature", false, true, new int[]{212} ),             
    HAPPINESS(213,"happiness", "htrtyp.happiness", false, true, new int[]{213} ),
    INSIGHT(214,"insight", "htrtyp.insight", false, true, new int[]{214} ),
    INTELLECT(215,"intellect", "htrtyp.intellect", false, true, new int[]{215} ),
    INTROVERSION(216,"introversion", "htrtyp.introversion", false, true, new int[]{216} ),
    LIBERALISM(217,"liberalism", "htrtyp.liberalism", false, true, new int[]{217} ),
    OPTIMISM(218,"optimism", "htrtyp.optimism", false, true, new int[]{218} ),
    PLANFULNESS(219,"planfulness", "htrtyp.planfulness", false, true, new int[]{219} ),          
    POISE(220,"poise", "htrtyp.poise", false, true, new int[]{220} ),                             
    POLITENESS(221,"politeness", "htrtyp.politeness", false, true, new int[]{221} ),
    RESPONSIBILITY(222,"responsibility", "htrtyp.responsibility", false, true, new int[]{222} ),
    SECURITY(223,"security", "htrtyp.security", false, true, new int[]{223} ),
    SELFCONTROL(224,"self-control", "htrtyp.selfcontrol", false, true, new int[]{224} ),          
    SELFDISCIPLINE(225,"self-discipline", "htrtyp.selfdiscipline", false, true, new int[]{225} ), 
    SELFEFFICACY(226,"self-efficacy", "htrtyp.selfefficacy", false, true, new int[]{226} ),      
    SENTIMENTALITY(227,"sentimentality", "htrtyp.sentimentality", false, true, new int[]{227} ), 
    SOCIABILITY(228,"sociability", "htrtyp.sociability", false, true, new int[]{228} ),            
    STABILITY(229,"stability", "htrtyp.stability", false, true, new int[]{229} ),               
    TEMPERANCE( 230,"temperance", "htrtyp.temperance", false, true, new int[]{230} ),            
    TIMIDITY(231,"timidity", "htrtyp.timidity", false, true, new int[]{231} ),
    TOLERANCE(232,"tolerance", "htrtyp.tolerance", false, true, new int[]{232} );


    
    private final int hraTraitTypeId;
    private final int[] sentinoTraitTypeIds;
    private final String langKey;
    private final String name;
    private final boolean performance;
    private final boolean culture;
    
    private static final String[] LEVELS = new String[]{"","verylow","low","med","high","veryhigh"};


    private HraTraitType( int p, String name, String langKey, boolean perform, boolean culture, int[] sentinoTraitTypeIds)
    {
        this.hraTraitTypeId=p;
        this.name = name;
        this.langKey=langKey;
        this.sentinoTraitTypeIds=sentinoTraitTypeIds;
        this.performance=perform;
        this.culture=culture;
    }

    public String getName() {
        return name;
    }

    public String getLangKey() {
        return langKey;
    }

    public boolean getIsPerformance() {
        return performance;
    }

    public boolean getIsCulture() {
        return culture;
    }
    
        
    
    public static List<HraTraitType> getPerformanceTraitList()
    {
        List<HraTraitType> out = new ArrayList<>();        
        HraTraitType[] vals = HraTraitType.values();
        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].performance )
                out.add(vals[i]);
        }
        return out;        
    }

    public static List<HraTraitType> getCultureTraitList()
    {
        List<HraTraitType> out = new ArrayList<>();        
        HraTraitType[] vals = HraTraitType.values();
        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].culture )
                out.add(vals[i]);
        }
        return out;        
    }

    
   
    public String getName( Locale loc )
    {
        if( loc==null )
            loc=Locale.US;
        
        String s =  MessageFactory.getStringMessage( Constants.SENTINO_BUNDLE, loc, langKey, null );
        
        // LogService.logIt( "HraTraitType.getName() langKey=" + langKey + ", s=" + s );
        
        return s;
    }
    
    public String getDescription( Locale loc )
    {
        if( loc==null )
            loc=Locale.US;
        
        return MessageFactory.getStringMessage( Constants.SENTINO_BUNDLE, loc, langKey + ".descrip", null );
    }
    
    public String getScoreText(Locale loc, float hraScore)
    {
        String suffix = "veryhigh";
        if( hraScore<35 )
            suffix="verylow";
        else if( hraScore<50 )
            suffix="low";
        else if( hraScore<65 )
            suffix="med";
        else if( hraScore<80 )
            suffix="high";

        return MessageFactory.getStringMessage( Constants.SENTINO_BUNDLE, loc, langKey + "." + suffix, null );        
    }

    /*
     level =none
    */
    public String getScoreText( Locale loc, int level )
    {
        if( loc==null )
            loc=Locale.US;
        
        return MessageFactory.getStringMessage( Constants.SENTINO_BUNDLE, loc, langKey + "." + LEVELS[level], null );
    }

    
    
    public int[] getSentinoTraitTypeIds() {
        return sentinoTraitTypeIds;
    }
    
    

    public int getHraTraitTypeId() {
        return hraTraitTypeId;
    }
    
    public static HraTraitType getValue( int id )
    {
        HraTraitType[] vals = HraTraitType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getHraTraitTypeId() == id )
                return vals[i];
        }

        return null;
    }
      
}
