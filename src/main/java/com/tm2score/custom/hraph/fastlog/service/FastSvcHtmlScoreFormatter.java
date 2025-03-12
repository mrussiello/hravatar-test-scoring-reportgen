/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.fastlog.service;

import com.tm2score.custom.hraph.bsp.itss.*;
import com.tm2score.format.*;

/**
 *
 * @author Mike
 */
public class FastSvcHtmlScoreFormatter extends BaseItssHtmlScoreFormatter implements ScoreFormatter
{



    public FastSvcHtmlScoreFormatter()
    {
        super();
        itss = new FastSvcData();
    }


}
