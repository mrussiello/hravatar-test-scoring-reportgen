/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

/**
 *
 * @author miker_000
 */
public interface MergableScoreObject {
    
    int getMergableScoreObjectTypeId();
    
    String getPackedTokenStringForTestEventScore();
}
