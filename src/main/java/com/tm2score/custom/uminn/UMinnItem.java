/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.global.I18nUtils;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class UMinnItem implements Comparable<UMinnItem> {
    
    String text;
    float rating;
    float avgRating;
    float trueScore;
    
    long simcompetencyId;
    int radioButGroupId;
    int simNodeSeqId;
    
    
    List<SimJ.Intn.Intnitem> intItems;
    ItemResponse itemResponse;
    
    UMinnCompetency uminnCompetency;
    
    boolean last=false;

    @Override
    public String toString() {
        return "UMinnItem{ itemCount=" + (intItems==null ? 0 : intItems.size()) + ", itemResponse=" + (itemResponse==null ? "null" : "not null") + ", rating=" + rating + ", avgRating=" + avgRating + ", trueScore=" + trueScore + ", simcompetencyId=" + simcompetencyId + ", radioButGroupId=" + radioButGroupId + ", simNodeSeqId=" + simNodeSeqId + '}';
    }

    @Override
    public int compareTo(UMinnItem o) {
        
        if( o.getText()!=null && !o.getText().isEmpty() && text!=null )
            return text.compareTo( o.getText() );
        
        if( simcompetencyId!=0 && o.getSimcompetencyId()!=0 )
            return new Long( simcompetencyId ).compareTo( o.getSimcompetencyId() );
        
        if( rating>0 && o.getRating()>0 )
            return new Float(rating).compareTo( o.getRating() );
                
        return 0;
    }


    
    
    protected ScoreCategoryType getScoreCategoryType( )
    {
         float userVsFacultyGap = Math.abs(rating - trueScore);
         
         return getScoreCategoryTypeForGap( userVsFacultyGap );
    }    
    
    protected ScoreCategoryType getScoreCategoryTypeForGap( float userVsFacultyGap )
    {
        if( userVsFacultyGap<2 )
            return ScoreCategoryType.GREEN;
        else if( userVsFacultyGap< 4 )
            return ScoreCategoryType.YELLOW;
        else
            return ScoreCategoryType.RED;
    }
    
    public String getDifferenceRatingStr()
    {
        float dif = Math.abs(rating - trueScore);
        
        return I18nUtils.getFormattedNumber( Locale.US, dif, 1);      
    }

    
    
    
    public String getRatingStr()
    {
        return I18nUtils.getFormattedNumber( Locale.US, rating, 1);      
    }

    public String getAvgRatingStr()
    {
        return I18nUtils.getFormattedNumber( Locale.US, avgRating, 1);      
    }

    public String getFacultyRatingStr()
    {
        return I18nUtils.getFormattedNumber( Locale.US, trueScore, 1);      
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(float avgRating) {
        this.avgRating = avgRating;
    }

    public float getTrueScore() {
        return trueScore;
    }

    public void setTrueScore(float trueScore) {
        this.trueScore = trueScore;
    }

    public long getSimcompetencyId() {
        return simcompetencyId;
    }

    public void setSimcompetencyId(long simcompetencyId) {
        this.simcompetencyId = simcompetencyId;
    }

    public int getRadioButGroupId() {
        return radioButGroupId;
    }

    public void setRadioButGroupId(int radioButGroupId) {
        this.radioButGroupId = radioButGroupId;
    }

    public List<SimJ.Intn.Intnitem> getIntItems() {
        return intItems;
    }

    public void setIntItems(List<SimJ.Intn.Intnitem> intItems) {
        this.intItems = intItems;
    }

    public int getSimNodeSeqId() {
        return simNodeSeqId;
    }

    public void setSimNodeSeqId(int simNodeSeqId) {
        this.simNodeSeqId = simNodeSeqId;
    }

    public ItemResponse getItemResponse() {
        return itemResponse;
    }

    public void setItemResponse(ItemResponse itemResponse) {
        this.itemResponse = itemResponse;
    }

    public UMinnCompetency getUminnCompetency() {
        return uminnCompetency;
    }

    public void setUminnCompetency(UMinnCompetency uminnCompetency) {
        this.uminnCompetency = uminnCompetency;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }



    
}
