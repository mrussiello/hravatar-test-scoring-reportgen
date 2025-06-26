package com.tm2score.av;

import com.tm2score.score.ScoredItemParadigmType;

public enum AvItemType
{    
    /*
    // Type 1 Read Text From Page. Records speech and sends back to HRA.
    // Scoring - First, do a Speech-to-text. Get the text and confidence. Store in avItemResponse.
    //           Compare the recorded speech -to-text to Stem1 and multiple by confidence. Similarity is 0 - 1.0 where match=1.0. Confidence is 0-1.0 where match=1.0,  Score is percent match 100*similarity. Set item Score to 100.
    //           Set Competency.scoreType=Pct Avail Points
    //           Set Intn.scoreParam1 of Interaction=1  (indicates Type 1)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions.  (IVR Only. No instructions for VOT)
    //           Set itemScore of submit button to 100 (Sets max points for item to 100)
    //           Set intn.textScoreParam1:
    //                   Statement or versions of statement delimited by semicolons ";" This is used for calculating max similarity. If empty will use stem1.
    //                   Add [NOBEEP] to beginning of textScoreParam1 of Intn if you want no beep.
    //                   Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           For VOT, be sure that there us a button that links to another clip with another ivr item otherwise the system will assume this is the last item. 
    //           avItemResponse.score is 0 - submitButton.itemScore  (typically 100. Defaults to 100)
    //           avItemResponse.assignedPoints = score
    //           [SCREENQ] (Optional - will use default if not present) appears on screen above the statement.  This is instructions.
    //           [IVRQ] (Optional - omitted if not provided - prelude is still read to user). If present, prelude is skipped. Can include an [AUDIO] tag if you have an audio. 
    //           [STEM1] (Required) is the statement only. This is what is presented on the screen.                 
    TYPE1(1, "Type 1 - Read Aloud", 8, 5, 0 , 60, "#*", "txa.Section1ItemNoAnswer", 0 ),
  
    // Type 2, Short Spoken Answer Intn.scoreParam1=2. Records speech and sends back to HRA. No branching, since speech recognition is done later (not by Twilio).
    // Scoring - First, do a Speech-to-text. Get the text and confidence. Store in avItemResponse.
    //           Compare the recorded speech-to-text to EACH possible correct answer. Correct is if the recorded speech contains the correct answer verbatim (not case sensitive).
    //           Set Competency.scoreType=Percent Correct
    //           Set intn.scoreParam1 of Interaction=2  (indicates Type 2)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions.  (IVR Only. No instructions for VOT)
    //           Set Intn.scoreParam3 to 1 indicates save audio for playback. (Generally this is done for VOT Only but can be set for either).
    //           Set intn.textscoreparam1 = 
    //               all possible correct answers, delimited by a semicolon ";". 
    //               Add [NOBEEP] to the end to remove the beep. 
    //               Include [SHOWQA] to have the scoring system show question and answer in text. Include at the end of textScoreParams, after the versons and uniqueIds
    //               If only assigning different points to different choices, include [POINTS]value;points;value;points;value;points ...  to indicate how many points to assign to each value. Should appear at the end of textScoreParam1 after all possible choices list.
//                   Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           Note that this item type does not support branching, since the matching is conducted off line. If you want to do speech matching followed by branching, 
    //              use a Type 8 item with scoreparam3=1 (Speech Only). 
    //           For VOT, be sure that there us a button that links to another clip with another ivr item otherwise the system will assume this is the last item. 
    //           avItemResponse.score is 0 or 1 (correct or not correct)
    //           avItemResponse.points = is 0 or intn.itemscore if correct unless there is a [POINTS] tag in textscoreparam1.                       
    //           [SCREENQ] (Optional) appears on screen. Should tell user to listen and answer in a complete sentence. 
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio.) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Required) is read as the question text, along with a prelude by the IVR, include and [AUDIO] tag if you have an audio. 
    // 
    TYPE2(2, "Type 2 - Short Spoken Answer", 8, 5, 0, 60, "*#", "txa.Section2ItemNoAnswer", 0 ),
    
    
    // Type 3, Long, Free-Form. Record s speech and sends back to HRA. For Speech-to-Text and playback only. No direct score but will include VoiceVibes Meta Scores. 
    // Scoring - First, do a Speech-to-text. Get the text and confidence. Store mp3 and the speech in avItemResponse.
    //           [SCREENQ] - (Optional - will use default if not present) this is the para that appears over the topic. 
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Optional - will use default if not present) is the question read by the ivr to the user. Can include an [AUDIO] tag.
    //           [STEM1] (Required) is displayed on the screen (typically has the topic) Required. Note that this value is used as the "title" in a text 
    //                    and title list when creating reports.  
    //           [STEM2] (Optional - will use default if not present) is displayed on the screen below stem1. Optional. 
    //                   Typically says you have 20 seconds to plan response then hit any numeric key when ready.
    //           Set Competency.scoreType=Average Points plus meta scores
    //           Set Intn.scoreParam1 of Interaction=3  (indicates Type 3)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions.  (IVR Only. No instructions for VOT)
    //           For VOT, be sure that there us a button that links to another clip with another ivr item otherwise the system will assume this is the last item. 
    //           Intn.textScoreParam1:
    //                Place the key [NOVIBES] in TextScoreParam1 for the item if you want to force Voice Vibes Analysis to be OFF. 
    //                Place the key [NOPREP] in TextScoreParam1 for the item if you want to force no preparation. Just goes straight to the beep. 
    //                Place the key [PREP30] in TextScoreParam1 for the item if you want to allow a 30 second prep time. 
    //                Place the key [PREP60] in TextScoreParam1 for the item if you want to allow a one minute prep time.
    //                Place the key [UNLIMITEDREPEATOK] in TextScoreParam1 for the item if you want to allow the user to repeat the question an unlimited number of times by saying repeat or pressing *.
    //                Place the key [NOMINTIME] in TextScoreParam1 for the item if you want to force no minimum amount of time. 
    //                Place [NOBEEP] in textScoreParam1 for avoiding Beep (works only with [NOPREP].
    //                Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           avItemResponse.score is a weighted average of features score (spelling, grammar) and Vibes Score. (0-100)
    //           avItemResponse.assignedPoints = score
    
    TYPE3(3, "Type 3 - Free Form", 8, 20, 30, 60, "#*", "txa.Section3TooShort", -1 ),
    
    
    // Type 4, Short DTMF Answer Intn.scoreParam1=4. Gathers DTMF or DTMF Speech or Speech input and sends the input back to HRA.
    // Scoring - Compare the recorded choice to the choices in item. Correct if item is scored as correct.   
    //           Set Competency.scoreType=Percent Correct
    //           Set intn.scoreParam1 of Interaction=4  (indicates Type 4)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions.  (IVR Only. No instructions for VOT)
    //           Set Intn.scoreParam3 to 1 to indicate speech mode ONLY. Must have pressOrSay enabled. 
    //           Set intnitem.correct yes for correct selection.
    //           Set intnitem.g2ItemScore to points for points-based competencies.
    //           If have NoneResp, set intn.trueScore to points for non-response
    //           For VOT, be sure that there us a button that links to another clip with another ivr item, or you set next item to branch to in each of the STEMs.
    //              otherwise the system will assume this is the last item. 
    //           avItemResponse.score is 0 or 1 (correct or not correct)
    //           avItemResponse.assignedPoints = selectedChoice.itemScore or intn.trueScore if None selected
    //           Intn.textScoreParam1:
    //               Place the key [NONERESP] in TextScoreParam1 for the item if you want the system to offer a "none of the above" type response that is not correct
    //               Place the key [NONERESPCORRECT]  in TextScoreParam1 for the item if you want the system to offer a "none of the above" type response that is correct.
    //               Include [SHOWQA] to have the scoring system show question and answer in text. Include at the end of textScoreParams, after the versons and uniqueIds
    //               Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           [SCREENQ] (Optional - will use default if not present) appears on screen.  It should just be instructions. Do not include the statement.
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Required) is read as the question text, along with a prelude by the IVR, can include an [AUDIO] tag if you have an audio. Audio should cover all distractors. 
    //           [STEM1] (Required) is the answer to be tied to key 1  
    //                   Optionally, for VOT tests, set STEM -> textscoreparam1 to item.uniqueid for the item to branch to if this choice is selected. 
    //           [STEM2] (Required)is the answer to be tied to key 2   
    //                   Optionally, for VOT tests, set STEM -> textscoreparam1 to item.uniqueid for the item to branch to if this choice is selected. 
    //           [STEM3 - 8] (Optional) is the answer to be tied to key 3 - 8   
    //                   Optionally, for VOT tests, set STEM -> textscoreparam1 to item.uniqueid for the item to branch to if this choice is selected. 
    TYPE4(4, "Type 4 - Short Keypad Answer", 0, 20, 0, 60, null, "txa.Section4ItemNoAnswer", 0 ),

    // Type 5, Listen to Sentence and Repeat it. Records speech and sends back to HRA. Intn.scoreParam1=5
    // Scoring - Compare the recorded sentence with the sentence actually spoken (TextScoreParam1). Similarity is 0 - 1.0 where match=1.0. Score is percent match 100*similarity. Set item Score to 100.
    //           Set Competency.scoreType=Pct Avail Points
    //           Set Intn.scoreParam1 of Interaction=5  (indicates Type 5)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions.  (IVR Only. No instructions for VOT)
    //           Set Intn.itemScore of submit button to 100 (Sets max points for item to 100)
    //           Set Intn.textScoreParam1 of Intn to statement or versions of statement delimted by semicolons ";". This is used for calculating max similarity. If empty will use ivrq.
    //           Intn.textScoreParam1:
    //                Place [NOBEEP] in beginning of textScoreParam1 for avoiding Beep.
    //                Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           For VOT, be sure that there us a button that links to another clip with another ivr item otherwise the system will assume this is the last item. 
    //           avItemResponse.score is 0-submitButton.itemScore  (typically 100. Defaults to 100)
    //           avItemResponse.assignedPoints = score
    //           [SCREENQ] (Optional - will use default)) appears on screen. Should tell user to listen and repeat what they hear exactly as they hear it. 
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Required) is read as the question text, along with a prelude by the IVR, can include an [AUDIO] tag if you have an audio. 
    TYPE5(5, "Type 5 - Listen and Repeat", 8, 5, 0, 60, "#*", "txa.Section5ItemNoAnswer", 2 ),

    // Type 6, Multi-DTMF Answer ending with #. Gathers DTMF or DTMF Speech or Speech input and sends the input back to HRA.
    //
    // Scoring - Compare the recorded or entered string with textscoreparam1 values. Correct if match, incorrect if not.    
    //           Set Competency.scoreType=Percent Correct
    //           Set Intn.scoreParam1 of Interaction=6  (indicates Type 6)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions. (IVR Only. No instructions for VOT)
    //           Set Intn.scoreParam3 to 1 to indicate speech only. 
    //           Intn.textScoreParam1:
    //               Set Intn.textscoreparam1 to all versions of correct answer, delimited by a ";". 
    //               Note that the value can be a string (not a number) and if so, a speech recognition option will be set to this value.
    //               Optionally, for VOT branching, include "|item unique id" after each possible answer, to branch to another item when this text option is matched.  
    //               For no match, will match to the one entitled "defaultnextintn" for matching text.
    //               Include [SHOWQA] to have the scoring system show question and answer in text. Include at the end of textScoreParams, after the versons and uniqueIds
    //               If only assigning different points to different choices, include [POINTS]value;points;value;points;value;points ...  to indicate how many points to assign to each value. Should appear at the end of textScoreParam1 (after all possible choices and unique ids.)
    //               Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           For VOT, be sure that there us a button that links to another clip with another ivr item, or intn.textscoreparam1 has values for branching. Otherwise the system will assume this is the last item.     
    //           avItemResponse.score is 0 or 1 (correct or not correct)
    //           avItemResponse.points = is 0 or intn.itemscore if correct unless there is a [POINTS] tag in textscoreparam1              
    //           [SCREENPRELUDE] - will display this on screen above question. Otherwise, uses default.
    //           [SCREENQ] (Optional - will be blank if not present) This is the question that appears on screen. 
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Required) is read as the question text, along with instructions on what to do (like press # when done). Can include an [AUDIO] tag if you have an audio. 
    TYPE6(6, "Type 6 - Long Keypad Answer", 0, 30, 0, 60, null, "txa.Section6ItemNoAnswer", 0 ),
    
    // Type 7, Informational, No section intro. Simply reads the question. Expects question to tell them to Press any key to continue. Intn.scoreParam1=7 
    // Scoring - none. Scored as 0 always.     
    //           Set Intn.scoreParam1 of Interaction=7  (indicates Type 7)
    //           For VOT, be sure that there us a button that links to another clip with another ivr item otherwise the system will assume this is the last item. 
    //           This type never has instructions
    //           avItemResponse.score is always 0
    //           avItemResponse.points = 0              
    //           Intn.textScoreParam1:
    //                Set Intn.textscoreparam1 to all versions of possible spoken words for to move on, like 'next'. If not present will use default.
    //                Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.
    //           [SCREENPRELUDE] - will display this on screen above question. Otherwise, uses default.
    //           [SCREENQ] (Optional - will be blank if not present) This is the statement or question you want to appear on screen.  
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Required) is read as the question text, along with instructions on what to do (like press any key when done). Can include an [AUDIO] tag if you have an audio.  
    TYPE7(7, "Type 7 - Information Only", 0, 15, 0, 60, null, "txa.Section7ItemNoAnswer", 0 ),
    
    // Type 8, Single-DTMF Answer. Gathers DTMF or DTMF Speech or Speech input and sends the input back to HRA.
    // Scoring - Compare the recorded entered string with textscoreparam1 values. Correct if match, incorrect if not.    
    //           Set Competency.scoreType=Percent Correct
    //           Set Intn.scoreParam1 of Interaction=8  (indicates Type 8)
    //           Set Intn.scoreParam2 to 1 indicates skip section instructions. (IVR Only. No instructions for VOT)
    //           Set Intn.scoreParam3 to 1 to indicate speech only. SPEECH ONLY 
    //           Intn.textScoreParam1:
    //                Set Intn.textscoreparam1 to all versions of correct answer(s) delimited by a ";". by a semicolon ";". 
    //                Note that the value can be a string (not a number) and if so, a speech recognition option will be set to this value.
    //                Optionally, for VOT branching, include "|item unique id" after each possible answer, to branch to another item when this option is matched.  For no match, will match to the one entitled "defaultnextintn" for matcing text.
    //                Include [SHOWQA] to have the scoring system show question and answer in text.
    //                If only assigning different points to different choices, include [POINTS]value;points;value;points;value;points ...  to indicate how many points to assign to each value. Should appear at the end of textScoreParam1 (after all possible choices and unique ids.)
    //                Add [FRCCOMPLT] to force the test to be marked as complete BEFORE this item is played to the user. Used in VOT to show a custom complete statement that allows the user to hang up instead of submitting, but still completes the test.                       
    //           For VOT, be sure that there us a button that links to another clip with another ivr item, or intn.textscoreparam1 has values for branching. Otherwise the system will assume this is the last item.     
    //           avItemResponse.score is 0 or 1 (correct or not correct)
    //           avItemResponse.points = is 0 or intn.itemscore if correct unless there is a [POINTS] tag in textscoreparam1              
    //           [SCREENPRELUDE] - will display this on screen above question. Otherwise, uses default.
    //           [SCREENQ] (Optional - will be blank if not present) This is the question you want to appear on the screen. 
    //           [IVRPRELUDE] (Optional. Uses default if not present. Only used if IVRQ has no audio) This is the prelude text read to the user over the phone.
    //           [IVRQ] (Required) is read as the question text, along with instructions on what to do (like "press or say 1 for yes and 0 for no"). Can include an [AUDIO] tag if you have an audio. 
    TYPE8(8, "Type 8 - Single Keypad Answer", 0, 30, 0, 60, null, "txa.Section8ItemNoAnswer", 0 ),
    */
    
    //////////////////////////////////////////////////////////////////////////////////////////
    // Uploaded User File Types. Handled differently by scoring programs.
    //  Note that these conform roughly to the UploadedUserFileProcessingType values in the UploadedUserFile
    //////////////////////////////////////////////////////////////////////////////////////////
    
    TYPE101(101, "Type 101 - Uploaded Audio/Video", 0, 0, 0, 0, null, null, 0 ),
    TYPE102(102, "Type 102 - Uploaded Audio Only", 0, 0, 0, 0, null, null, 0 ),
    TYPE112(112, "Type 112 - Uploaded Audio, Text Match Strict", 0, 0, 0, 0, null, null, 0 ),
    TYPE122(122, "Type 122 - Uploaded Audio, Text Match Loose", 0, 0, 0, 0, null, null, 0 );
    
    private final int avItemTypeId;

    private String key;
    
    private final int dtmfTimeoutSecs;
    
    private final int recordingTimeoutSecs;
    
    private final int recordingMinLengthSecs;
    
    private final int recordingMaxLengthSecs;
    
    private final String recordingFinishOnStr;

    private final String invalidAnswerLangKey;
    
    private final int maxPlays; // 0=Unlimited.
    
    private AvItemType( int p,
                         String key, 
                         int recTmoutSecs,
                         int dtmfTmoutSecs,
                         int minLenSecs,
                         int mxLenSecs, 
                         String finishStr,
                         String invalidKey,
                         int mxPlays )
    {
        this.avItemTypeId = p;
        this.key = key;
        this.recordingTimeoutSecs=recTmoutSecs;
        this.dtmfTimeoutSecs=dtmfTmoutSecs;
        this.recordingMaxLengthSecs=mxLenSecs;
        this.recordingFinishOnStr=finishStr;
        this.recordingMinLengthSecs=minLenSecs;
        this.invalidAnswerLangKey = invalidKey;
        this.maxPlays=mxPlays;
    }

    public boolean getRequiresTranscription()
    {
        return equals(TYPE112) || equals(TYPE122); //  || equals(TYPE1) || equals(TYPE2) || equals(TYPE5) || equals(TYPE6);
    }
    
    public int getAudioSampleRate()
    {
        return isAnyWebUploadedAudio() ? 16000 : 8000;
    }

    public boolean isAnyWebUploadedAudioVideo()
    {
        return isAnyWebUploadedAudio();
    }
    
    public boolean isAnyWebUploadedAudio()
    {
        return equals( TYPE101 ) || equals( TYPE102 ) || equals( TYPE112 ) || equals( TYPE122 );
    }
    
    public int getMaxAlternativesForSpeechToText()
    {
        // if( equals(TYPE2) )
        //     return 5;
        
        return 1;
    }
    
    
    //public boolean supportsTextAndTitle()
    //{
    //    return equals(TYPE3) ;
    //}
    
    
    public boolean is100Score()
    {
        return false; // equals( TYPE1 ) || equals( TYPE3 ) || equals( TYPE5 );
    }


    public boolean is1Score()
    {
        return false; // equals( TYPE2 ) || equals( TYPE4 ) || equals( TYPE6 ) || equals( TYPE8 );
    }
    
    public boolean supportsEssayScoring()
    {
        return equals( TYPE101 ) || equals( TYPE102 );
        // return equals( TYPE3 ) || equals( TYPE101 ) || equals( TYPE102 );
    }
    
    //public boolean supportsIntnLevelDtmfGatherBasedBranching()
    //{
    //    return equals(TYPE6) || equals(TYPE8);        
    //}
    
    //public boolean supportsIntnLevelAdminFlagsAndInfo()
    //{
    //    return equals( TYPE1 ) || equals( TYPE2 ) || equals( TYPE3 ) || equals( TYPE4 ) || equals( TYPE5 ) || equals(TYPE6)  || equals(TYPE7) || equals(TYPE8);
    //}
    
    //public boolean supportsIntnItemDtmfGatherBasedBranching()
    //{
    //    return equals(TYPE4) ;        
    //}
    
    
    public boolean supportsVoiceVibesAnalysis()
    {
        return equals(TYPE101) || equals( TYPE102 );
        // return equals(TYPE3) || equals(TYPE101) || equals( TYPE102 );
    }    
    
    //public boolean requiresDistractorValues()
    //{
    //    return equals(TYPE4);
    //}
    
    public boolean getIsTwilio()
    {
        return !isAnyWebUploadedAudioVideo();
    }
        
    
    public boolean getStoreRecordedAudio()
    {
        return false;
        // return equals(TYPE1) || equals(TYPE3) || equals(TYPE5);        
    }
    
    public ScoredItemParadigmType getScoredItemParadigmType()
    {
        if( equals(TYPE101) || equals(TYPE102) )
            return ScoredItemParadigmType.AV_UPLOAD;
        
        //if( equals(TYPE1) || equals(TYPE3) || equals(TYPE5) )
         //   return ScoredItemParadigmType.IVR_RECORDING;
        
        // if( equals(TYPE6) || equals(TYPE7) || equals(TYPE8) || equals(TYPE112) || equals(TYPE122)  )
        if( equals(TYPE112) || equals(TYPE122)  )
            return ScoredItemParadigmType.FILL_BLANK;
        
        return ScoredItemParadigmType.MULTIPLE_CHOICE;
    }
    
    public boolean requiresRecordVoice()
    {
        return equals(TYPE101) || equals(TYPE102) || equals(TYPE112) || equals(TYPE122);
        // return equals(TYPE1) || equals(TYPE2) || equals(TYPE3) || equals(TYPE5) || equals(TYPE101) || equals(TYPE102) || equals(TYPE112) || equals(TYPE122);
    }

    public int getRecordingTimeoutSecs() {
        return recordingTimeoutSecs;
    }

    public int getRecordingMaxLengthSecs() {
        return recordingMaxLengthSecs;
    }

    public String getRecordingFinishOnStr() {
        return recordingFinishOnStr;
    }

    public String getInvalidAnswerLangKey() {
        return invalidAnswerLangKey;
    }
    
    
   
    public String getKey()
    {
        return this.key;
    }

    public int getAvItemTypeId() {
        return avItemTypeId;
    }
    
    public static AvItemType getValue( int id )
    {
        AvItemType[] vals = AvItemType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getAvItemTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public int getRecordingMinLengthSecs() {
        return recordingMinLengthSecs;
    }
    
    

    public static AvItemType getValue( String inKey )
    {
        if( inKey==null || inKey.isEmpty() )
            return null;
        
        AvItemType[] vals = AvItemType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].key.equals(inKey) )
                return vals[i];
        }

        return null;
    }

       

}
