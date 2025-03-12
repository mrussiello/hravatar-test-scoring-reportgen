/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.api;

import com.tm2score.entity.event.TestKey;
import com.tm2score.event.ResultPostType;

/**
 *
 * @author Mike
 */
public class ResultPosterFactory {

    public static ResultPoster getResultPosterInstance( TestKey tk )
    {
        //if( tk.getResultPostTypeId()==0 )
        //    return new DefaultResultPoster( tk );
        //if( tk.getTestKeySourceTypeId()==TestKeySourceType.API.getTestKeySourceTypeId() && tk.getApiTypeId()==ApiType.ICIMS.getApiTypeId() )
        //{
        //    return new IcimsResultPoster( tk );
        //}
        
        //if( tk.getTestKeySourceTypeId()==TestKeySourceType.API.getTestKeySourceTypeId() && tk.getApiTypeId()==ApiType.ADP_WFN.getApiTypeId() )
        //{
        //    return new AdpWfnResultPoster( tk );
        //}
        
        if( tk.getResultPostUrl()!= null && !tk.getResultPostUrl().isEmpty() )
            return tk.getResultPostTypeId()==ResultPostType.DEFAULT.getResultPostTypeId() ? new DefaultResultPoster( tk ) : new DefaultResultPoster( tk );
        
        return null;
    }
}
