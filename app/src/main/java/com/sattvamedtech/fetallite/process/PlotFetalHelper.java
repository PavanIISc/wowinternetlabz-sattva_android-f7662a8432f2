package com.sattvamedtech.fetallite.process;

import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.interfaces.PlotCallback;

public class PlotFetalHelper {

    public static void plotData(int[] iFQrs, int[] iMQrs) {
        Logger.logInfo("PlotHelper", "mFQrs.length: " + iFQrs.length);
        if (iFQrs.length > 25) {
            for (int aFQrs : iFQrs) {
                if (aFQrs >= 2000 && aFQrs <= 12000) {
                    ApplicationUtils.mFqrsMasterList.add(aFQrs + (ApplicationUtils.algoProcessStartCount * 10000));
                }
            }
            computeAndPlot(false);
            ApplicationUtils.lastFetalPlotIndex = ApplicationUtils.mFqrsMasterList.size();
        } else {
            ApplicationUtils.mFqrsMasterList.add(2200 + (ApplicationUtils.algoProcessStartCount * 10000));
            ApplicationUtils.mFqrsMasterList.add(11800 + (ApplicationUtils.algoProcessStartCount * 10000));
            computeAndPlot(true);
            ApplicationUtils.lastFetalPlotIndex = ApplicationUtils.mFqrsMasterList.size() + 8;
        }

        Logger.logInfo("PlotHelper", "Plotting for 15k done at " + (System.currentTimeMillis() - ApplicationUtils.mStartMS));
    }

    private static void computeAndPlot(boolean isDummy) {
        for (int i = ApplicationUtils.lastFetalPlotIndex; i < ApplicationUtils.mFqrsMasterList.size(); i++) {
            double aFHR = 110;
            if (!isDummy) {
                aFHR = (60.0 * 1000.0 * 8) / (ApplicationUtils.mFqrsMasterList.get(i) - ApplicationUtils.mFqrsMasterList.get(i - 8));
                int aFDiff = ApplicationUtils.mFqrsMasterList.get(i) - ApplicationUtils.mFqrsMasterList.get(i - 1);
                try {
                    Thread.sleep(aFDiff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                PlotCallback.getInstance().sendFetalHeartRateData((float) ApplicationUtils.mFqrsMasterList.get(i), (float) aFHR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
