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
public class ClipHist {
    
    Clicflic.History.Clip clip;
    
    public ClipHist( Clicflic.History.Clip clip )
    {
        this.clip = clip;
    }
    
    public float getHang()
    {
        return clip.getH();
    }

    public int getNdseq()
    {
        return clip.getNs();
    }

    public int getSnseq()
    {
        return clip.getSs();
    }


    public String getUnqid()
    {
        return clip.getU();
    }

}
