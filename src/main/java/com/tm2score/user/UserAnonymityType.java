package com.tm2score.user;



public enum UserAnonymityType
{
    NAME_EMAIL(0,"Default","uayt.Default" ),                
    LOGON_PWD(1,"Logon Pwd","uayt.LogonPwd" ),                
    USERID(3,"User ID","uayt.UserId" ),
    ANONYMOUS(98,"Anonymous","uayt.Anon" ),
    PSEUDO(99,"Anonymous","uayt.Pseudo" );      


    private final int userAnonymityTypeId;

    private final String name;
    private final String key;
    

    public boolean getHasName()
    {
        return equals( NAME_EMAIL );
    }
    
    public boolean getHasUsername()
    {
        return equals( LOGON_PWD );
    }
    
    public boolean getHasUserId()
    {
        return equals( USERID );
    }
    
    public boolean getHasUsernameOrUserId()
    {
        return getHasUserId() || getHasUsername();
    }

    public boolean getAnonymous()
    {
        return equals( ANONYMOUS );
    }
    
    public boolean getPseudo()
    {
        return equals( PSEUDO );
    }
    
    
    
    private UserAnonymityType( int s , String n, String k )
    {
        this.userAnonymityTypeId = s;
        this.name = n;
        this.key=k;
    }
        
    
    public static UserAnonymityType getValue( int id )
    {
        UserAnonymityType[] vals = UserAnonymityType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUserAnonymityTypeId() == id )
                return vals[i];
        }

        return NAME_EMAIL;
    }

    

    public int getUserAnonymityTypeId()
    {
        return userAnonymityTypeId;
    }

    
    public String getName()
    {
        return name;
    }

    


}
