package com.tm2score.entity.event;


import com.tm2score.entity.user.User;
import com.tm2score.event.TestEventResponseRatingType;
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import java.util.List;



@Entity
@Table( name="testeventresponserating" )
@NamedQueries({
    @NamedQuery ( name="TestEventResponseRating.findForTestEventId", query="SELECT o FROM TestEventResponseRating AS o WHERE o.testEventId=:testEventId ORDER BY o.createDate" )
})
public class TestEventResponseRating implements Serializable, Comparable<TestEventResponseRating>
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="testeventresponseratingid")
    private long testEventResponseRatingId;

    /*
     * 0=uploadeduserfile - use uploadeduserfileid
       1=avitemresponse  - use avitemresponseid
       2=sim competency - use simcompetencyid and seq
       3=non-competency - use nonCompetencyItemTypeId and seq
    */
    @Column(name="testeventresponseratingtypeid")
    private int testEventResponseRatingTypeId;
    
    @Column(name="userid")
    private long userId;

    @Column(name="testeventid")
    private long testEventId;
    
    @Column(name="avitemresponseid")
    private long avItemResponseId = -1;
    
    @Column(name="uploadeduserfileid")
    private long uploadedUserFileId = -1;
    
    /**
     * Convenience
     */
    //@Column( name = "simcompetencyclassid" )
    //private int simCompetencyClassId = -1;

    @Column(name="directsimcompetencyid")
    private long directSimCompetencyId = 0;

    
    @Column(name="simcompetencyid")
    private long simCompetencyId = 0;

    @Column(name="simcompetencyid2")
    private long simCompetencyId2 = 0;
    
    @Column(name="simcompetencyid3")
    private long simCompetencyId3 = 0;
    
    @Column(name="simcompetencyid4")
    private long simCompetencyId4 = 0;
    
    @Column(name="simcompetencyid5")
    private long simCompetencyId5 = 0;
    
    @Column(name="simcompetencyid6")
    private long simCompetencyId6 = 0;
    
    @Column(name="simcompetencyid7")
    private long simCompetencyId7 = 0;
    
    @Column(name="simcompetencyid8")
    private long simCompetencyId8 = 0;
    
    @Column(name="simcompetencyid9")
    private long simCompetencyId9 = 0;
    
    @Column(name="simcompetencyid10")
    private long simCompetencyId10 = 0;
    
    
    @Column(name="noncompetencyitemtypeid")
    private int nonCompetencyItemTypeId = 0;
    
    /**
     * Used for sim competencies or non-competency ratings. 1-based sequence id
     */
    @Column(name="sequenceid")
    private int sequenceId = -1;
    
    
    @Column(name="rating")
    private float rating;

    @Column(name="rating2")
    private float rating2;

    @Column(name="rating3")
    private float rating3;

    @Column(name="rating4")
    private float rating4;

    @Column(name="rating5")
    private float rating5;

    @Column(name="rating6")
    private float rating6;

    @Column(name="rating7")
    private float rating7;

    @Column(name="rating8")
    private float rating8;

    @Column(name="rating9")
    private float rating9;

    @Column(name="rating10")
    private float rating10;

    @Column(name="identifier")
    private String identifier;

    
    @Column(name="note")
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
    
    @Transient
    User user;
    
    @Transient
    private List<String> ratingNameList;
    
    
    
    @Override
    public int compareTo(TestEventResponseRating o) {

        if( createDate != null && o.getCreateDate() != null )
            return createDate.compareTo( o.getCreateDate() );

        if( testEventResponseRatingId>0 && o.getTestEventResponseRatingId()>0 )
            return ((Long) testEventResponseRatingId).compareTo( o.getTestEventResponseRatingId() );

        return ((Long) userId).compareTo( o.getUserId() );
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (this.testEventResponseRatingId ^ (this.testEventResponseRatingId >>> 32));
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
        final TestEventResponseRating other = (TestEventResponseRating) obj;
        if (this.testEventResponseRatingId != other.testEventResponseRatingId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestEventResponseRating{" + "testEventResponseRatingId=" + testEventResponseRatingId + ", testEventResponseRatingTypeId=" + testEventResponseRatingTypeId + ", userId=" + userId + ", testEventId=" + testEventId + ", avItemResponseId=" + avItemResponseId + ", uploadedUserFileId=" + uploadedUserFileId + ", directSimCompetencyId=" + directSimCompetencyId + ", nonCompetencyItemTypeId=" + nonCompetencyItemTypeId + ", sequenceId=" + sequenceId + ", rating=" + rating + ", rating2=" + rating2 + ", rating3=" + rating3 + ", note=" + this.note + '}';
    }
    
    public boolean getHasAnyAltSimCompetencyId()
    {        
        return simCompetencyId>0 ||
                simCompetencyId2>0 ||
                simCompetencyId3>0 ||
                simCompetencyId4>0 ||
                simCompetencyId5>0 ||
                simCompetencyId6>0 ||
                simCompetencyId7>0 ||
                simCompetencyId8>0 ||
                simCompetencyId9>0 ||
                simCompetencyId10>0;
        
    }
    
    public boolean getHasAltSimCompetencyId( long altSimCompetencyId )
    {
        if( altSimCompetencyId<=0 )
            return false;
        
        return simCompetencyId==altSimCompetencyId ||
                simCompetencyId2==altSimCompetencyId ||
                simCompetencyId3==altSimCompetencyId ||
                simCompetencyId4==altSimCompetencyId ||
                simCompetencyId5==altSimCompetencyId ||
                simCompetencyId6==altSimCompetencyId ||
                simCompetencyId7==altSimCompetencyId ||
                simCompetencyId8==altSimCompetencyId ||
                simCompetencyId9==altSimCompetencyId ||
                simCompetencyId10==altSimCompetencyId;
                
    }
    
    public void setAltSimCompetencyIds( List<Long> idList )
    {
        this.simCompetencyId=0;
        this.simCompetencyId2=0;
        this.simCompetencyId3=0;
        this.simCompetencyId4=0;
        this.simCompetencyId5=0;
        this.simCompetencyId6=0;
        this.simCompetencyId7=0;
        this.simCompetencyId8=0;
        this.simCompetencyId9=0;
        this.simCompetencyId10=0;
        
        if( idList==null )
            return;
        
        int sz = idList.size();

        if( sz>=1 )
            simCompetencyId=idList.get(0);

        if( sz>=2 )
            simCompetencyId2=idList.get(1);
        if( sz>=3 )
            simCompetencyId3=idList.get(2);
        if( sz>=4 )
            simCompetencyId4=idList.get(3);
        if( sz>=5 )
            simCompetencyId4=idList.get(4);
        if( sz>=6 )
            simCompetencyId4=idList.get(5);
        if( sz>=7 )
            simCompetencyId4=idList.get(6);
        if( sz>=8 )
            simCompetencyId4=idList.get(7);
        if( sz>=9 )
            simCompetencyId4=idList.get(8);
        if( sz>=10 )
            simCompetencyId4=idList.get(9);
    }
        
    
    public String getRatingAsStr() {
        return Integer.toString( Math.round(rating) );
    }
    public String getRatingAsStr2() {
        return Integer.toString( Math.round(rating2) );
    }
    public String getRatingAsStr3() {
        return Integer.toString( Math.round(rating3) );
    }
    public String getRatingAsStr4() {
        return Integer.toString( Math.round(rating4) );
    }
    public String getRatingAsStr5() {
        return Integer.toString( Math.round(rating5) );
    }
    public String getRatingAsStr6() {
        return Integer.toString( Math.round(rating6) );
    }
    public String getRatingAsStr7() {
        return Integer.toString( Math.round(rating7) );
    }
    public String getRatingAsStr8() {
        return Integer.toString( Math.round(rating8) );
    }
    public String getRatingAsStr9() {
        return Integer.toString( Math.round(rating9) );
    }
    public String getRatingAsStr10() {
        return Integer.toString( Math.round(rating10) );
    }

    
    public void sanitizeUserInput()
    {
        // note = StringUtils.sanitizeForSqlQuery(note);
    }
    
    public boolean getHasAnyRatingData()
    {
        return rating>0 || rating2>0 || rating3>0 || rating4>0 || rating5>0 || rating6>0 || rating7>0 || rating8>0 || rating9>0 || rating10>0;
    }
    
    public boolean getHasAnyData()
    {
        return getHasAnyRatingData() || (note!=null && !note.isBlank() );
    }
        
    public float getRatingForAltSimCompetencyId( long altSimCompetencyId )
    {
        if( !getHasAltSimCompetencyId( altSimCompetencyId ) )
            return -1;
        if( simCompetencyId==altSimCompetencyId )
            return rating;
        if( simCompetencyId2==altSimCompetencyId )
            return rating2;
        if( simCompetencyId3==altSimCompetencyId )
            return rating3;
        if( simCompetencyId4==altSimCompetencyId )
            return rating4;
        if( simCompetencyId5==altSimCompetencyId )
            return rating5;
        if( simCompetencyId6==altSimCompetencyId )
            return rating6;
        if( simCompetencyId7==altSimCompetencyId )
            return rating7;
        if( simCompetencyId8==altSimCompetencyId )
            return rating8;
        if( simCompetencyId9==altSimCompetencyId )
            return rating9;
        if( simCompetencyId10==altSimCompetencyId )
            return rating10;
        return -1;
    }
    
    public float[] getRatingArray()
    {
        return new float[] {rating,rating2,rating3,rating4,rating5,rating6,rating7,rating8,rating9,rating10};
    }
    

    public TestEventResponseRatingType getTestEventResponseRatingType()
    {
        return TestEventResponseRatingType.getValue( testEventResponseRatingTypeId );
    }
    
    public long getTestEventResponseRatingId() {
        return testEventResponseRatingId;
    }

    public void setTestEventResponseRatingId(long testEventResponseRatingId) {
        this.testEventResponseRatingId = testEventResponseRatingId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public long getAvItemResponseId() {
        return avItemResponseId;
    }

    public void setAvItemResponseId(long avItemResponseId) {
        this.avItemResponseId = avItemResponseId;
    }

    public long getUploadedUserFileId() {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId) {
        this.uploadedUserFileId = uploadedUserFileId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getRating2() {
        return rating2;
    }

    public void setRating2(float rating2) {
        this.rating2 = rating2;
    }

    public float getRating3() {
        return rating3;
    }

    public void setRating3(float rating3) {
        this.rating3 = rating3;
    }

    public String getNote() {
        
        if( note!=null && note.isBlank() )
            note=null;
        
        return note;
    }

    public void setNote(String note) {

        if( note!=null && note.isBlank() )
            note=null;
        
        this.note = note;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public long getSimCompetencyId() {
        return simCompetencyId;
    }

    public void setSimCompetencyId(long simCompetencyId) {
        this.simCompetencyId = simCompetencyId;
    }

    public int getNonCompetencyItemTypeId() {
        return nonCompetencyItemTypeId;
    }

    public void setNonCompetencyItemTypeId(int nonCompetencyItemTypeId) {
        this.nonCompetencyItemTypeId = nonCompetencyItemTypeId;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getTestEventResponseRatingTypeId() {
        return testEventResponseRatingTypeId;
    }

    public void setTestEventResponseRatingTypeId(int testEventResponseRatingTypeId) {
        this.testEventResponseRatingTypeId = testEventResponseRatingTypeId;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public float getRating4() {
        return rating4;
    }

    public void setRating4(float rating4) {
        this.rating4 = rating4;
    }

    public float getRating5() {
        return rating5;
    }

    public void setRating5(float rating5) {
        this.rating5 = rating5;
    }

    public float getRating6() {
        return rating6;
    }

    public void setRating6(float rating6) {
        this.rating6 = rating6;
    }

    public float getRating7() {
        return rating7;
    }

    public void setRating7(float rating7) {
        this.rating7 = rating7;
    }

    public float getRating8() {
        return rating8;
    }

    public void setRating8(float rating8) {
        this.rating8 = rating8;
    }

    public float getRating9() {
        return rating9;
    }

    public void setRating9(float rating9) {
        this.rating9 = rating9;
    }

    public float getRating10() {
        return rating10;
    }

    public void setRating10(float rating10) {
        this.rating10 = rating10;
    }

    public long getSimCompetencyId2() {
        return simCompetencyId2;
    }

    public void setSimCompetencyId2(long simCompetencyId2) {
        this.simCompetencyId2 = simCompetencyId2;
    }

    public long getSimCompetencyId3() {
        return simCompetencyId3;
    }

    public void setSimCompetencyId3(long simCompetencyId3) {
        this.simCompetencyId3 = simCompetencyId3;
    }

    public long getSimCompetencyId4() {
        return simCompetencyId4;
    }

    public void setSimCompetencyId4(long simCompetencyId4) {
        this.simCompetencyId4 = simCompetencyId4;
    }

    public long getSimCompetencyId5() {
        return simCompetencyId5;
    }

    public void setSimCompetencyId5(long simCompetencyId5) {
        this.simCompetencyId5 = simCompetencyId5;
    }

    public long getSimCompetencyId6() {
        return simCompetencyId6;
    }

    public void setSimCompetencyId6(long simCompetencyId6) {
        this.simCompetencyId6 = simCompetencyId6;
    }

    public long getSimCompetencyId7() {
        return simCompetencyId7;
    }

    public void setSimCompetencyId7(long simCompetencyId7) {
        this.simCompetencyId7 = simCompetencyId7;
    }

    public long getSimCompetencyId8() {
        return simCompetencyId8;
    }

    public void setSimCompetencyId8(long simCompetencyId8) {
        this.simCompetencyId8 = simCompetencyId8;
    }

    public long getSimCompetencyId9() {
        return simCompetencyId9;
    }

    public void setSimCompetencyId9(long simCompetencyId9) {
        this.simCompetencyId9 = simCompetencyId9;
    }

    public long getSimCompetencyId10() {
        return simCompetencyId10;
    }

    public void setSimCompetencyId10(long simCompetencyId10) {
        this.simCompetencyId10 = simCompetencyId10;
    }


    public long getDirectSimCompetencyId() {
        return directSimCompetencyId;
    }

    public void setDirectSimCompetencyId(long s) {
        this.directSimCompetencyId = s;
    }

    public List<String> getRatingNameList() {
        return ratingNameList;
    }

    public void setRatingNameList(List<String> ratingNameList) {
        this.ratingNameList = ratingNameList;
    }
    
    public int getRatingNameListSize()
    {
        return ratingNameList==null ? 0 : ratingNameList.size();
    }
    
    public String[] getRatingNameArray()
    {
        if( ratingNameList==null )
            return new String[0];
        
        return ratingNameList.toArray(String[]::new);
    }
    
    

}
