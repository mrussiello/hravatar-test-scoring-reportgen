/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.onet;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Mike
 */
public class OnetElement implements Serializable, Comparable<OnetElement>
{
    private String name;
    private String description;
    private String onetElementId;
    private int onetElementTypeId;
    private float importance;

    private long longParam1;
    private float floatParam1;
    private String strParam1;
    
    private int onetFreqTypeId=0;
    
    private List<String[]> detWorkActivities;
    
    private String contextCategory;


    @Override
    public String toString() {
        return "OnetElement{" + "name=" + name + ", onetElementId=" + onetElementId + ", onetElementTypeId=" + onetElementTypeId + '}';
    }

    @Override
    public int compareTo(OnetElement o) {
        if( name != null && o.getName() != null )
            return name.compareTo( o.getName() );

        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.onetElementId);
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
        final OnetElement other = (OnetElement) obj;
        if (!Objects.equals(this.onetElementId, other.onetElementId)) {
            return false;
        }
        return true;
    }

    public OnetElementType getOnetElementType()
    {
        return OnetElementType.getValue(onetElementTypeId);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOnetElementId() {
        return onetElementId;
    }

    public void setOnetElementId(String onetElementId) {
        this.onetElementId = onetElementId;
    }

    public int getOnetElementTypeId() {
        return onetElementTypeId;
    }

    public void setOnetElementTypeId(int onetElementTypeId) {
        this.onetElementTypeId = onetElementTypeId;
    }

    public long getLongParam1() {
        return longParam1;
    }

    public void setLongParam1(long longParam1) {
        this.longParam1 = longParam1;
    }

    public float getFloatParam1() {
        return floatParam1;
    }

    public void setFloatParam1(float floatParam1) {
        this.floatParam1 = floatParam1;
    }

    public float getImportance() {
        return importance;
    }

    public void setImportance(float importance) {
        this.importance = importance;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public int getOnetFreqTypeId() {
        return onetFreqTypeId;
    }

    public void setOnetFreqTypeId(int onetFreqTypeId) {
        this.onetFreqTypeId = onetFreqTypeId;
    }

    public List<String[]> getDetWorkActivities() {
        return detWorkActivities;
    }

    public void setDetWorkActivities(List<String[]> detWorkActivities) {
        this.detWorkActivities = detWorkActivities;
    }

    public String getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(String contextCategory) {
        this.contextCategory = contextCategory;
    }


}
