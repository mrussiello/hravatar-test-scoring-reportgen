/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.score;

import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.STStringTokenizer;
import java.io.Serializable;
import java.util.Locale;

/**
 *
 * @author miker
 */
public final class CaveatScore implements Serializable, Comparable<CaveatScore> {

    int displayOrder;
    int caveatScoreTypeId;
    float value;
    float value2;
    float value3;
    String strValue;
    Locale locale;
    // int scrDigits = 0;

    public CaveatScore(int displayOrder, String inStr, Locale locale)
    {
        if (locale == null)
            locale = Locale.US;
        this.locale = locale;
        this.parseInputStr(inStr, displayOrder);
    }

    public CaveatScore( int displayOrder, int caveatScoreTypeId, float value, float value2, float value3, String strValue, Locale locale)
    {
        this.displayOrder = displayOrder;
        this.caveatScoreTypeId = caveatScoreTypeId;
        this.value = value;
        this.value2 = value2;
        this.value3 = value3;
        this.strValue=strValue;

        if (locale == null)
            locale = Locale.US;
        this.locale = locale;
    }
    
    
    public CaveatScore( int displayOrder, int caveatScoreTypeId, float value, float value2, String strValue, Locale locale)
    {
        this.displayOrder = displayOrder;
        this.caveatScoreTypeId = caveatScoreTypeId;
        this.value = value;
        this.value2 = value2;
        this.strValue=strValue;

        if (locale == null)
            locale = Locale.US;
        this.locale = locale;
    }

    public CaveatScore( int displayOrder, int caveatScoreTypeId, String strValue, Locale locale)
    {
        this.displayOrder = displayOrder;
        this.caveatScoreTypeId = caveatScoreTypeId;
        this.strValue=strValue;
        if (locale == null)
            locale = Locale.US;
        this.locale = locale;
    }
    
    @Override
    public int compareTo(CaveatScore o)
    {
        if (getCaveatScoreType().getHeadingLevel() != o.getCaveatScoreType().getHeadingLevel())
        {
            return Integer.valueOf(getCaveatScoreType().getHeadingLevel()).compareTo(o.getCaveatScoreType().getHeadingLevel());
        }
        return getNameForTable().compareTo(o.getNameForTable());
    }
    
    public String getScoreTableRow()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td " + (getCaveatScoreType().getColspan()>1 ? "colspan=\"" + getCaveatScoreType().getColspan() + "\"" : "" ) + " style=\"font-weight:normal;vertical-align:top\">" + getCol1() + (getCaveatScoreType().getColspan()<=1 ? ":" : "") + "</td>");
        
        if( getCaveatScoreType().getColspan()<=1 )
            sb.append(  "<td style=\"font-weight:normal;vertical-align:top\">" + getCol2() + "</td>");
        
        sb.append( "</tr>");
        return sb.toString();
    }

    /**
     * Format is [CAVEAT2XX]caveatScoreTypeId~value~value2~strValue
     * @param inStr 
     */
    public void parseInputStr(String inStr, int displayOrder )
    {
        if (inStr == null || inStr.isBlank())
        {
            LogService.logIt("CaveatScore.parseInputStr() inputStr is invalid: " + inStr);
            return;
        }
        
        this.displayOrder=displayOrder;
        
        if( inStr.contains("[" + Constants.CAVEAT2_KEY)  && inStr.contains("]") )
        {
            inStr = inStr.substring( inStr.indexOf("]") + 1, inStr.length() );
        }        
                
        try
        {
            STStringTokenizer st = new STStringTokenizer( inStr, "~" );
            if( st.hasMoreTokens() )
            {
                caveatScoreTypeId = Integer.parseInt(st.nextToken().trim());
                
                if( st.hasMoreTokens() )
                {
                    value = Float.parseFloat(st.nextToken().trim());

                    if( st.hasMoreTokens() )
                    {
                        value2 = Float.parseFloat(st.nextToken().trim());
 
                        if( st.hasMoreTokens() )
                        {
                            value3 = Float.parseFloat(st.nextToken().trim());
 
                            if( st.hasMoreTokens() )
                                 strValue = st.nextToken().trim();
                        }
                    }
                    
                }
            }

            /*
            String t = null;
            t = inStr.substring(inStr.indexOf("[" + Constants.CAVEAT2_KEY) + Constants.CAVEAT2_KEY.length() + 1, inStr.indexOf("]"));
            displayOrder = Integer.parseInt(t);

            t = inStr.substring(inStr.indexOf("]") + 1, inStr.indexOf("~"));
            caveatScoreTypeId = Integer.parseInt(t);

            int idx = inStr.indexOf("~");
            
            if( idx<0 )
                return;
            
            int idx2 = inStr.indexOf("~", idx + 1);

            if (idx2>0 && idx2>idx)
            {
                t = inStr.substring(idx + 1, idx2);
                value = Float.parseFloat(t);
                
                if( inStr.length()>idx2+1 )
                    strValue = inStr.substring(idx2+1, inStr.length() );
            }
            
            t = inStr.substring(idx + 1, inStr.length() );
                value = Float.parseFloat(t);
            */
            
        } catch (NumberFormatException e)
        {
            LogService.logIt(e, "CaveatScoreType.parseStr() NONFATAL " + inStr );

        }
    }
    
    //public String getSingleStringOutput()
    //{
    //    return getSingleStringOutput( 0 );
    //}

    public String getSingleStringOutput()
    {
        if( locale==null )
            locale=Locale.US;
                
        CaveatScoreType cst = getCaveatScoreType();
        
        if( getIsTopic() )
            return getSingleStringOutputForTopic(0, false);
        
        return getCol1() + (cst.getColspan()==2 ? "" : ": " + getCol2());
    }
    
    public String getSingleStringOutputForTopic( int simCompetencyClassId, boolean isOneLine )
    {
        if( locale==null )
            locale=Locale.US;
                
        CaveatScoreType cst = getCaveatScoreType();
        
        if( !getIsTopic() )
            return getSingleStringOutput();
        
        String[] dd = ReportUtils.parseTopicCaveatScore(this, true, simCompetencyClassId);
        
        return dd[1] + ": " + dd[2];
    }
    
    
    public boolean getIsTopic()
    {
        return getCaveatScoreType().getIsTopic();
    }

    public String getBracketedStrWithKey()
    {
        return "[" + Constants.CAVEAT2_KEY + displayOrder + "]" + caveatScoreTypeId + "~" + value + "~" + value2 + "~" + value3 + "~" + (strValue==null ? "" : strValue.trim());
    }

    
    public boolean getHasValidInfo()
    {
        return !getCaveatScoreType().equals(CaveatScoreType.NONE );
    }

    public String getCol1()
    {
        return getName();
    }
    public String getCol2()
    {
        if (locale == null)
            locale = Locale.US;
        return MessageFactory.getStringMessage(locale, getCaveatScoreType().getKeyX(), new String[]{this.getValueAsStr(), this.getValue2AsStr(), this.getValue3AsStr()} );
    }
    

    public String getName()
    {
        if (locale == null)
            locale = Locale.US;
        return MessageFactory.getStringMessage(locale, getCaveatScoreType().getKey(), new String[]{strValue} );
    }
    
    
    public String getNameForTable()
    {
        if (locale == null)
            locale = Locale.US;
        return this.getCaveatScoreType().getNameX(locale);
    }


    public CaveatScoreType getCaveatScoreType()
    {
        return CaveatScoreType.getValue(caveatScoreTypeId);
    }

    public int getCaveatScoreTypeId()
    {
        return caveatScoreTypeId;
    }

    @Override
    public String toString()
    {
        return "CaveatScore{" + "displayOrder=" + displayOrder + ", caveatScoreTypeId=" + caveatScoreTypeId + ", value=" + value + ", value2=" + value2 + ", value3=" + value3 + ", strValue=" + strValue + '}';
    }

    
    
    public String getValueAsStr()
    {
        // LogService.logIt( "CaveatScore.getValueAsStr() valueInt=" + Integer.toString(getValueInt() ) +", " + toString() );
        if (locale == null)
            locale = Locale.US;

        if( getCaveatScoreType().getScoreDigits()<=0 )
            return Integer.toString(getValueInt() );
        
        return I18nUtils.getFormattedNumber(locale, value, getCaveatScoreType().getScoreDigits());
    }
    public String getValue2AsStr()
    {
        if (locale == null)
            locale = Locale.US;

        if( getCaveatScoreType().getScoreDigits()<=0 )
            return Integer.toString(getValue2Int() );
        
        return I18nUtils.getFormattedNumber(locale, value2, this.getCaveatScoreType().getScoreDigits());
    }
    public String getValue3AsStr()
    {
        if (locale == null)
            locale = Locale.US;

        if( getCaveatScoreType().getScoreDigits()<=0 )
            return Integer.toString(getValue3Int() );
        
        return I18nUtils.getFormattedNumber(locale, value3, this.getCaveatScoreType().getScoreDigits());
    }
    
    public float getValue()
    {
        if( getCaveatScoreType().getScoreDigits()<=0 )
            return getValueInt();
        return value;
    }
    
    public int getValueInt()
    {
        return Float.valueOf(value).intValue();
    }

    public float getValue2()
    {
        if( getCaveatScoreType().getScoreDigits()<=0 )
            return getValue2Int();
        
        return value2;
    }

    public int getValue2Int()
    {
        return Float.valueOf(value2).intValue();
    }

    public float getValue3()
    {
        if( getCaveatScoreType().getScoreDigits()<=0 )
            return getValue3Int();
        
        return value3;
    }

    public int getValue3Int()
    {
        return Float.valueOf(value3).intValue();
    }

    
    public String getStrValue()
    {
        return strValue;
    }
    

    public int getDisplayOrder()
    {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder)
    {
        this.displayOrder = displayOrder;
    }

    //public void setScrDigits(int scrDigits)
    //{
    //    this.scrDigits = scrDigits;
    //}

    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    public void setStrValue(String strValue)
    {
        this.strValue = strValue;
    }
    

}
