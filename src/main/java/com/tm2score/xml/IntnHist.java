/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.xml;

import com.tm2score.imo.xml.Clicflic;


/**
 *
 * @author miker_000
 */
public class IntnHist {
    
    Clicflic.History.Intn intn;
    
    public IntnHist( Clicflic.History.Intn clip )
    {
        this.intn = clip;
    }

    @Override
    public String toString()
    {
        return "IntnHist{" + "unique=" + intn.getU() + ", ns=" + intn.getNs() + ", seq=" + intn.getSq() + ", value=" + intn.getValue() + '}';
    }
    
    
    public int getClickCount()
    {
        return intn.getCk();
    }
    
    public float getCtime()
    {
        return intn.getCt()!=0f ? intn.getCt() : intn.getCtime();
    }

    public float getPoints()
    {
        return intn.getPts();
    }

    public int getCorrect()
    {
        return intn.getCrct();
    }

    public int getSeq()
    {
        return intn.getSq()!=0 ? intn.getSq() : intn.getSeq();
    }

    public int getShowCount()
    {
        return intn.getCx();
    }
    
    
    public int getNdseq()
    {
        return intn.getNs()!=0 ? intn.getNs() : intn.getNdseq();
    }
    
    public int getSnseq()
    {
        return intn.getSs()!=0 ? intn.getSs() : intn.getSnseq();
    }    
    
    public String getPrvsubseqs()
    {
        return intn.getPss();
    }
    
    public String getUnqid()
    {
        return intn.getU()!=null ? intn.getU() : intn.getUnqid();
    }
    
    public int getAccessibleForm()
    {
        return intn.getAf();
    }

    public String getValue()
    {
        return intn.getValue();
    }

    
    public String getSv1()
    {
        return getSv(1);
    }
    public String getSv2()
    {
        return getSv(2);
    }
    public String getSv3()
    {
        return getSv(3);
    }
    public String getSv4()
    {
        return getSv(4);
    }
    public String getSv5()
    {
        return getSv(5);
    }
    public String getSv6()
    {
        return getSv(6);
    }
    public String getSv7()
    {
        return getSv(7);
    }
    public String getSv8()
    {
        return getSv(8);
    }
    public String getSv9()
    {
        return getSv(9);
    }
    public String getSv10()
    {
        return getSv(10);
    }
    public String getSv11()
    {
        return getSv(11);
    }
    public String getSv12()
    {
        return getSv(12);
    }



    
    public String getSv( int idx )
    {
        String s = intn.getSv();

        
        String[] vals = s.split("~");
        
        if( vals.length<idx )
            return "0";
        
        String v =  vals[idx-1].trim();
        
        return v.isEmpty() ? "0" : v;
    }
}
