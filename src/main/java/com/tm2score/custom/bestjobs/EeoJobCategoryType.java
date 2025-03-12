package com.tm2score.custom.bestjobs;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum EeoJobCategoryType
{
    NONE(0,"Active", "ejct.none"),
    EXEC(1,"Executive/Senior Level Officials and Managers", "ejct.exec"),
    MGR(2,"First/Mid Level Officials and Managers", "ejct.mgr"),
    PROF(3,"Professionals", "ejct.prof"),
    TECH(4,"Technicians", "ejct.tech"),
    SALES(5,"Sales Workers", "ejct.sales"),
    ADMIN(6,"Administrative Support Workers", "ejct.admin"),
    CRAFT(7,"Craft", "ejct.craft"),
    OPS(8,"Operatives", "ejct.ops"),
    LABORERS(9,"Laborers and Helpers", "ejct.labor"),
    SERVICE(10,"Service Workers", "ejct.svc" );

    private final int eeoJobCategoryTypeId;
    private final String name;
    private final String key;


    private EeoJobCategoryType( int p , String name, String key )
    {
        this.eeoJobCategoryTypeId = p;

        this.name=name;
        this.key = key;
    }



    public int getEeoJobCategoryTypeId()
    {
        return this.eeoJobCategoryTypeId;
    }




    public static EeoJobCategoryType getType( int typeId )
    {
        return getValue( typeId );
    }



    public String getKey()
    {
        return key;
    }



    public static EeoJobCategoryType getValue( int id )
    {
        EeoJobCategoryType[] vals = EeoJobCategoryType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEeoJobCategoryTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    public String getName() {
        return name;
    }

    public String getName(Locale locale) {
        
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, key);
    }

    public String getDescription(Locale locale) {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, key + ".descrip");
    }
    
    
    
}
