/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.sim;

import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.global.Constants;
import com.tm2score.util.STStringTokenizer;



/**
 *
 * @author Mike
 */
public class ScoreTextParser
{
    String blackText;
    String highCliffText;
    String redText;
    String redYellowText;
    String yellowText;
    String yellowGreenText;
    String greenText;
    String whiteText;

    ScoreColorSchemeType scoreColorSchemeType;

    public ScoreTextParser( String scoreText, ScoreColorSchemeType sct)
    {
        blackText = "";
        redText = "";
        redYellowText = "";
        yellowText = "";
        yellowGreenText = "";
        greenText = "";
        whiteText = "";
        scoreColorSchemeType = sct;

        if( scoreText == null || scoreText.trim().isEmpty() )
           return;

        STStringTokenizer st = new STStringTokenizer( scoreText, Constants.DELIMITER );

        //if( sct.equals( ScoreColorSchemeType.FIVECOLOR ) )

        if( sct.equals( ScoreColorSchemeType.SEVENCOLOR ) && st.hasMoreTokens() )
            blackText = st.nextToken();

        if( st.hasMoreTokens() )
            redText = st.nextToken();

        if( sct.getIsFiveOrSevenColor() && st.hasMoreTokens()  )
            redYellowText = st.nextToken();

        if( st.hasMoreTokens() )
            yellowText = st.nextToken();

        if( sct.getIsFiveOrSevenColor() && st.hasMoreTokens()  )
            yellowGreenText = st.nextToken();

        if( st.hasMoreTokens() )
            greenText = st.nextToken();

        if( sct.equals( ScoreColorSchemeType.SEVENCOLOR ) && st.hasMoreTokens() )
            whiteText = st.nextToken();

        if( st.hasMoreTokens() )
            this.highCliffText = st.nextToken();

    }

    /*
    public ScoreTextParser( String red, String redYellow, String yellow, String yellowGreen, String green, String highCliff)
    {
        this.redText = red;
        this.redYellowText = redYellow;
        this.yellowText = yellow;
        this.yellowGreenText = yellowGreen;
        this.greenText = green;
        this.highCliffText = highCliff;
    }
    */

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        ScoreTextParser stp = (ScoreTextParser) super.clone();

        return stp;
    }


    /*
    public String getPackedString()
    {
        blackText = blackText == null ?  "" : blackText.trim();
        redText = redText == null ?  "" : redText.trim();
        redYellowText = redYellowText == null ?  "" : redYellowText.trim();
        yellowText = yellowText == null ?  "" : yellowText.trim();
        yellowGreenText = yellowGreenText == null ?  "" : yellowGreenText.trim();
        greenText = greenText == null ?  "" : greenText.trim();
        whiteText = whiteText == null ?  "" : whiteText.trim();
        highCliffText = highCliffText == null ?  "" : highCliffText.trim();

        if( scoreColorSchemeType.equals( ScoreColorSchemeType.THREECOLOR ) && redText.isEmpty() && yellowText.isEmpty() && greenText.isEmpty() && highCliffText.isEmpty() )
            return null;

        if( scoreColorSchemeType.equals( ScoreColorSchemeType.FIVECOLOR ) && redText.isEmpty() && redYellowText.isEmpty() && yellowText.isEmpty()&& yellowGreenText.isEmpty() && greenText.isEmpty() && highCliffText.isEmpty() )
            return null;

        if( scoreColorSchemeType.equals( ScoreColorSchemeType.SEVENCOLOR ) && blackText.isEmpty() && redText.isEmpty() && redYellowText.isEmpty() && yellowText.isEmpty()&& yellowGreenText.isEmpty() && greenText.isEmpty() && whiteText.isEmpty() && highCliffText.isEmpty() )
            return null;

        
        if( scoreColorSchemeType.equals( ScoreColorSchemeType.THREECOLOR ) )
            return redText + Constants.DELIMITER + yellowText + Constants.DELIMITER + greenText + Constants.DELIMITER + highCliffText;

        if( scoreColorSchemeType.equals( ScoreColorSchemeType.FIVECOLOR ) )
            return redText + Constants.DELIMITER + redYellowText + Constants.DELIMITER + yellowText + Constants.DELIMITER + yellowGreenText + Constants.DELIMITER + greenText + Constants.DELIMITER + highCliffText;
        
        return blackText + Constants.DELIMITER + redText + Constants.DELIMITER + redYellowText + Constants.DELIMITER + yellowText + Constants.DELIMITER + yellowGreenText + Constants.DELIMITER + greenText + Constants.DELIMITER + whiteText + Constants.DELIMITER + highCliffText;
    }
    */

    public String getGreenText() {
        return greenText;
    }

    public void setGreenText(String greenText) {
        this.greenText = greenText;
    }

    public String getRedText() {
        return redText;
    }

    public void setRedText(String redText) {
        this.redText = redText;
    }

    public String getYellowText() {
        return yellowText;
    }

    public void setYellowText(String yellowText) {
        this.yellowText = yellowText;
    }

    public String getRedYellowText() {
        return redYellowText;
    }

    public void setRedYellowText(String redYellowText) {
        this.redYellowText = redYellowText;
    }

    public String getYellowGreenText() {
        return yellowGreenText;
    }

    public void setYellowGreenText(String yellowGreenText) {
        this.yellowGreenText = yellowGreenText;
    }

    public ScoreColorSchemeType getScoreColorSchemeType() {
        return scoreColorSchemeType;
    }

    public void setScoreColorSchemeType(ScoreColorSchemeType scoreColorSchemeType) {
        this.scoreColorSchemeType = scoreColorSchemeType;
    }

    public String getHighCliffText() {
        return highCliffText;
    }

    public void setHighCliffText(String highCliffText) {
        this.highCliffText = highCliffText;
    }

    public String getBlackText() {
        return blackText;
    }

    public void setBlackText(String blackText) {
        this.blackText = blackText;
    }

    public String getWhiteText() {
        return whiteText;
    }

    public void setWhiteText(String whiteText) {
        this.whiteText = whiteText;
    }


}
