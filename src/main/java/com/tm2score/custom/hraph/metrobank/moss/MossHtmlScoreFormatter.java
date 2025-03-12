/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.metrobank.moss;

import com.tm2score.custom.hraph.bsp.itss.*;
import com.tm2score.format.*;

/**
 *
 * @author Mike
 */
public class MossHtmlScoreFormatter extends BaseItssHtmlScoreFormatter implements ScoreFormatter
{



    public MossHtmlScoreFormatter()
    {
        super();
        itss = new MossData();
    }


}
