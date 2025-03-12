/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import java.util.List;

/**
 *
 * @author miker_000
 */
public class UMinnScene {
    
    
    
    
    int index;
    
    long simletId;
    int simletVersionId;
    
    String sceneText;
    
    List<UMinnItem> uminnItemList;
    
    UMinnItem followUpItem;
    
    
    public UMinnScene( int index, long simletId, int simletVersionId, String sceneText )
    {
        this.index=index;
        this.simletId=simletId;
        this.simletVersionId=simletVersionId;
        this.sceneText=sceneText;
    }

    
    @Override
    public String toString() {
        return "UMinnScene{" + "index=" + index + ", simletId=" + simletId + ", simletVersionId=" + simletVersionId + ", sceneText=" + sceneText + ", scenarios=" + (uminnItemList==null ? "null" : uminnItemList.size() + "" ) + '}';
    }
    
    

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getSimletId() {
        return simletId;
    }

    public void setSimletId(long simletId) {
        this.simletId = simletId;
    }

    public int getSimletVersionId() {
        return simletVersionId;
    }

    public void setSimletVersionId(int simletVersionId) {
        this.simletVersionId = simletVersionId;
    }

    public String getSceneText() {
        return sceneText;
    }

    public void setSceneText(String sceneText) {
        this.sceneText = sceneText;
    }

    public List<UMinnItem> getUMinnItemList() {
        return uminnItemList;
    }

    public void setUMinnItemList(List<UMinnItem> ul) {
        this.uminnItemList = ul;
    }

    public UMinnItem getFollowUpItem() {
        return followUpItem;
    }

    public void setFollowUpItem(UMinnItem followUpItem) {
        this.followUpItem = followUpItem;
    }
    
    
    
}
