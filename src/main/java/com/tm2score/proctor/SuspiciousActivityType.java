package com.tm2score.proctor;

import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.faces.model.SelectItem;


public enum SuspiciousActivityType
{
    NONE(0,"sat.none"),
    FOCUS_OUT(1,"sat.focusout"),
    FOCUS_NEW(2,"sat.focusnew"),
    FOCUS_REPEAT(3,"sat.focusrepeat"),
    KEYPRESS(10,"sat.keypress"),
    AIPASTE(11,"sat.s2toraipaste"),
    ID_MATCH_FAILED(18,"sat.idfacematchfail"),
    ID_MATCH_LOW(19,"sat.idfacematchlow"),
    MULTIPLE_FACES(20,"sat.multiplefaces"),
    FACE_NOT_PRESENT(21,"sat.candidatefacenotpresent"),
    FACIAL_MISMATCH(22,"sat.facialmismatches"),
    FREQUENT_NEGATIVE_PITCH(23,"sat.freqnegativepitch"),
    FREQUENT_YAW(24,"sat.freqyaw"),
    HIGH_PITCH(25,"sat.highpitch"),
    HIGH_YAW(26,"sat.highyaw"),
    MOBILE_DEVICE_DETECTED(27,"sat.mobile"),
    SPEECH_DETECTED(28,"sat.speakingdetected"), // "Unauthorized Speaking Detected"),,
    SAME_IP_TEST_EVENTS(30,"sat.sameipsametestevents"),
    PROCTOR_NOTE(100,"sat.proctornote"),
    USER_NOTE(110,"sat.usernote");

    private final int suspiciousActivityTypeId;

    private final String key;

    private SuspiciousActivityType( int typeId , String key )
    {
        this.suspiciousActivityTypeId = typeId;

        this.key = key;
    }

    public boolean getIsMultiFaces()
    {
        return equals( MULTIPLE_FACES );
    }
    
    public boolean getIsFaceMissing()
    {
        return equals( FACE_NOT_PRESENT );
    }
    
    public boolean getIsAnyIdFaceMismatch()
    {
        return equals(ID_MATCH_FAILED) || equals(ID_MATCH_LOW);
    }
    
    public boolean getIsFaceMismatch()
    {
        return equals( FACIAL_MISMATCH );
    }
    
    public boolean getIsHighPitchYaw()
    {
        return equals( HIGH_PITCH ) || equals( HIGH_YAW );
    }
    
    public boolean getIsFrequentPitchYaw()
    {
        return equals( FREQUENT_NEGATIVE_PITCH ) || equals( FREQUENT_YAW );
    }
    
    
    
    public boolean getIsAltMobile()
    {
        return equals( MOBILE_DEVICE_DETECTED );
    }

    public boolean getIsSpeechDetected()
    {
        return equals(SPEECH_DETECTED);
    }
        
    
    public boolean getIsKeyPress()
    {
        return equals( KEYPRESS );
    }    
    
    public boolean getIsAnyNote()
    {
        return equals( PROCTOR_NOTE ) || equals( USER_NOTE );
    }
    
    public boolean getIsProctorNote()
    {
        return equals( PROCTOR_NOTE );
    }
    
    public boolean getIsUserNote()
    {
        return equals( USER_NOTE );
    }
    
    public boolean getShowTime()
    {
        return equals( FOCUS_NEW ) || equals( FOCUS_REPEAT );
    }
    
    public boolean getSameIpTestEvents()
    {
        return equals( SAME_IP_TEST_EVENTS );
    }
    
    public boolean getUsesCounter()
    {
        return !getIsAnyNote() && !equals(NONE) && !equals( SAME_IP_TEST_EVENTS );
    }
    
    public boolean getIsIndividualEntry()
    {
        return getIsAnyNote() || equals(NONE) || getIsKeyPress() || equals(ID_MATCH_FAILED) || equals(ID_MATCH_LOW);
    }
    
    
    
    public static List<SelectItem> getSelectItemList( Locale l )
    {
        if( l==null )
            l = Locale.US;
        
        List<SelectItem>  out = new ArrayList<>();
        SuspiciousActivityType[] vals = SuspiciousActivityType.values();
        for( int i=0 ; i<vals.length ; i++ )
            out.add( new SelectItem( vals[i].getSuspiciousActivityTypeId(), MessageFactory.getStringMessage(l, vals[i].key) ) );

        return out;
    }


    public static SuspiciousActivityType getValue( int id )
    {
        SuspiciousActivityType[] vals = SuspiciousActivityType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSuspiciousActivityTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
    
    public String getName( Locale l)
    {
        if( l==null )
            l = Locale.US;
        
        return MessageFactory.getStringMessage(l, key );        
    }

    public String getDetail( Locale l)
    {
        if( l==null )
            l = Locale.US;
        
        return MessageFactory.getStringMessage(l, key + ".detail" );        
    }
    
    public int getSuspiciousActivityTypeId()
    {
        return suspiciousActivityTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
