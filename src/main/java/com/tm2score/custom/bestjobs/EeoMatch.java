/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.custom.bestjobs;

import java.io.Serializable;

/**
 *
 * @author miker
 */
public class EeoMatch implements Serializable, Comparable<EeoMatch> {
    
    int rank;
    String eeoTitle;
    int eeoJobCategoryId;
    
    int count;
    float total;
    Float averagePercentMatch=null;

    public EeoMatch( int rank, String title, int jobCategoryId )
    {
        this.rank = rank;
        this.eeoTitle=title;
        this.eeoJobCategoryId=jobCategoryId;
    }
    
    public EeoMatch( String title, int jobCategoryId )
    {
        this.eeoTitle=title;
        this.eeoJobCategoryId=jobCategoryId;
    }

    @Override
    public int compareTo(EeoMatch o) {
        
        return ((Float)getAveragePercentMatch()).compareTo(o.getAveragePercentMatch());
    }

    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.eeoJobCategoryId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EeoMatch other = (EeoMatch) obj;
        return this.eeoJobCategoryId == other.eeoJobCategoryId;
    }
    
    
    public void addPercentMatch( float percentMatch )
    {
        count++;
        total += percentMatch;
    }
    
    public EeoJobCategoryType getEeoJobCategoryType()
    {
        return EeoJobCategoryType.getValue(eeoJobCategoryId);
    }
    
    public String getEeoTitle() {
        return eeoTitle;
    }

    public void setEeoTitle(String eeoTitle) {
        this.eeoTitle = eeoTitle;
    }

    public int getEeoJobCategoryId() {
        return eeoJobCategoryId;
    }

    public void setEeoJobCategoryId(int eeoJobCategoryId) {
        this.eeoJobCategoryId = eeoJobCategoryId;
    }

    public float getAveragePercentMatch() 
    {
        if( averagePercentMatch==null || averagePercentMatch==0 )            
            averagePercentMatch = count>0 ? total/((float)count) : 0;
        
        return averagePercentMatch;
    }

    public void setAveragePercentMatch(Float p) {
        this.averagePercentMatch = p;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    
    
    
}
