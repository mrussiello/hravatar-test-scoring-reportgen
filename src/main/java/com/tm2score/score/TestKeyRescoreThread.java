/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.service.LogService;
import java.util.List;

/**
 *
 * @author Mike
 */
public class TestKeyRescoreThread implements Runnable {

    List<Long> testKeyIdList;

    String description;
    boolean clearExternal;

    public TestKeyRescoreThread(List<Long> tkidl, boolean clearExternal, String description)
    {
        this.testKeyIdList = tkidl;
        this.description = description;
        this.clearExternal = clearExternal;
    }

    @Override
    public void run()
    {
        try
        {
            LogService.logIt("TestKeyRescoreThread.run() STARTING testKeyIdList.size=" + testKeyIdList.size() + ", " + description);

            ScoreManager sm = new ScoreManager();

            int[] tko = null;

            for (Long testKeyId : testKeyIdList)
            {
                tko = sm.rescoreTestKey(testKeyId, false, clearExternal);

                if (tko[0] > 0)
                    LogService.logIt("TestKeyRescoreThread.run() TestKeyId: " + testKeyId + " has been rescored. Reports have been deleted and will be picked up in the next automated cycle.");

                else if (tko[2] > 0)
                    LogService.logIt("TestKeyRescoreThread.run() TestKeyId: " + testKeyId + " is now pending externally computed scores. It will be completed in the next batch cycle during which the external scores are ready.");
                else
                    LogService.logIt("TestKeyRescoreThread.run() TestKeyId: " + testKeyId + " has NOT been rescored. Check logs for reason.");

            }

            LogService.logIt("TestKeyRescoreThread.run() COMPLETED testKeyIdList.size=" + testKeyIdList.size() + ", " + description);
        } catch (Exception e)
        {
            LogService.logIt(e, "TestKeyRescoreThread.run() testKeyIdList.size=" + testKeyIdList.size() + ", " + description);
        }
    }
}
