package com.tm2score.purchase;

public enum CreditSourceType
{
    UNKNOWN(0,"Unknown"),
    WEB_PURCHASE(1,"Website Purchase"),
    AFFILIATE_RESELLER(2,"Affiliate Reseller"),
    ADMIN_CREATE(3,"Added By Admin"),
    FREE_PROMO(4,"Free Promotion");

    private final int creditSourceTypeId;

    private String key;

    private CreditSourceType( int p , String key )
    {
        this.creditSourceTypeId = p;

        this.key = key;
    }


    public int getCreditSourceTypeId()
    {
        return this.creditSourceTypeId;
    }


    public String getName()
    {
        return key;
    }


    public static CreditSourceType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }



    public static CreditSourceType getValue( int id )
    {
        CreditSourceType[] vals = CreditSourceType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCreditSourceTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }

}
