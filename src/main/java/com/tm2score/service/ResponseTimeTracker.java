/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.service;

import java.util.Objects;

/**
 *
 * @author Mike
 */
public class ResponseTimeTracker
{

    public String name;
    public int count=0;
    public long total=0;

    public ResponseTimeTracker( String nm )
    {
        name = nm;
    }


    public void addValue( float amount )
    {
        count++;

        total += amount;
    }

    public float getAverage()
    {
        if( count == 0 )
            return 0;

        long avg = total/count;

        return (float)total/(float)(count * 1000);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResponseTimeTracker other = (ResponseTimeTracker) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }



}
