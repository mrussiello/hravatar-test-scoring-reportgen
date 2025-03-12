package com.tm2score.util;

public class STStringTokenizer
{
    private String inStr;

    private String delimiter;

    private char delimFirstChar;

    private int index=0;



    public STStringTokenizer( String inString, String delim )
    {
        inStr = inString;

        if( inStr == null )
            inStr = "";

        delimiter = delim;

        if(delimiter.length() > 0)
            delimFirstChar=delimiter.charAt(0);
   }


    public boolean hasMoreTokens()
    {
      if ( inStr == null )
      {
        return false;
      }
      else
      {
        if( (index) < inStr.length() )
        {
          return true;
        }
        else
        {
          return false;
        }
      }
    }




    public String nextToken()
    {

        // Start search for next delimiter at begin point
        int endIndex = index;

        int start = index;

        // If the delimiter is null, send back the whole string
        if(delimiter.length() == 0)
        {
            index = inStr.length();

            return inStr;
        }

        while((endIndex) < inStr.length())
        {
            if((inStr.charAt(endIndex) == delimFirstChar) && ((endIndex + delimiter.length()) <= inStr.length()))
            {
                if(inStr.substring(endIndex,(endIndex + delimiter.length())).equals(delimiter))
                {
                    break;
                }

                else endIndex++;
            }

            else endIndex++;
        }

        index = endIndex + delimiter.length();

        return inStr.substring(start,endIndex);
    }



    public int countTokens()
    {
        int count=1;

        int endIndex=0;

        // If the delimiter is null, 1
        if(delimiter.length() == 0)
        {
            if(inStr.length() == 0)
                return 0;

            else
                return 1;
        }

        while( (endIndex) < inStr.length())
         {
            if((inStr.charAt(endIndex) == delimFirstChar) && ((endIndex + delimiter.length()) < inStr.length()))
            {
                if(inStr.substring(endIndex,(endIndex + delimiter.length())).equals(delimiter))
                {
                    count++;

                    endIndex += delimiter.length();
                }

                else
                    endIndex++;
            }

            else
                endIndex++;
        }

        return count;
    }

    public int countDelims()
    {
        if(delimiter.length() == 0)
            return 0;
        
        int count=0;
        
        int idx = inStr.indexOf(delimiter);
                
        while(idx>=0)
        {
            count++;
            
            idx = inStr.indexOf( delimiter, idx+1 );
        }

        return count;
    }
    
    

}
