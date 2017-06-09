package com.sattvamedtech.fetallite.process;

import android.os.AsyncTask;

import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.FileLogger;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.interfaces.PlotCallback;
import com.sattvamedtech.fetallite.signalproc.Constants;

import java.util.ArrayList;


public class PlottingTask extends AsyncTask<Void, Void, Void> {

    private int[] mFQrs, mMQrs;
    private double[] mUcData;
    private boolean isPlotHr;
    private String mFileDateStamp;
    private boolean isFetal;
    private ArrayList<Integer> mFqrsList = new ArrayList<>();

    public PlottingTask(int[] iQrs, boolean isFetal, String iFileDateStamp) {
        this.isFetal = isFetal;
        if (isFetal)
            mFQrs = iQrs;
        else
            mMQrs = iQrs;
        isPlotHr = true;
        mFileDateStamp = iFileDateStamp;
    }

    public PlottingTask(double[] iUcData, String iFileDateStamp) {
        mUcData = iUcData;
        isPlotHr = false;
        mFileDateStamp = iFileDateStamp;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (isPlotHr)
            plotData();
        else
            plotUcData();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (isPlotHr)
            ApplicationUtils.mHrPlottingFlag = ApplicationUtils.IDLE;
        else
            ApplicationUtils.mUcPlottingFlag = ApplicationUtils.IDLE;
    }

    private void plotData() {
        if (isFetal) {
            Logger.logInfo("PlottingTask", "Iteration " + ApplicationUtils.algoProcessEndCount + "mFQrs.length: " + mFQrs.length);
            if (mFQrs.length > 25) {
                for (int aFQrs : mFQrs) {
                    if (aFQrs >= 2000 && aFQrs <= 12000) {
                        mFqrsList.add(aFQrs + (ApplicationUtils.algoProcessEndCount * 10000));
                    }
                }
            } else {
                mFqrsList.add(2200 + (ApplicationUtils.algoProcessEndCount * 10000));
                mFqrsList.add(11800 + (ApplicationUtils.algoProcessEndCount * 10000));
            }
            fetalPlot(!(mFQrs.length > 25), mFileDateStamp);
            if (mFQrs.length > 25)
                ApplicationUtils.lastFetalPlotIndex = ApplicationUtils.mFqrsMasterList.size();
            else
                ApplicationUtils.lastFetalPlotIndex = ApplicationUtils.mFqrsMasterList.size() + ApplicationUtils.SKIP_COUNT_FOR_PLOT;
        } else {
            Logger.logInfo("PlottingTask", "Iteration " + ApplicationUtils.algoProcessEndCount + "mMQrs.length: " + mMQrs.length);
            if (mMQrs.length > 15) {
                for (int aMQrs : mMQrs) {
                    if (aMQrs >= 2000 && aMQrs <= 12000) {
                        ApplicationUtils.mMaternalMasterList.add(aMQrs + (ApplicationUtils.algoProcessEndCount * 10000));
                    }
                }
            } else {
                ApplicationUtils.mMaternalMasterList.add(2200 + (ApplicationUtils.algoProcessEndCount * 10000));
                ApplicationUtils.mMaternalMasterList.add(11800 + (ApplicationUtils.algoProcessEndCount * 10000));
            }
            maternalPlot(!(mMQrs.length > 15), mFileDateStamp);
            if (mMQrs.length > 15)
                ApplicationUtils.lastMaternalPlotIndex = ApplicationUtils.mMaternalMasterList.size();
            else
                ApplicationUtils.lastMaternalPlotIndex = ApplicationUtils.mMaternalMasterList.size() + ApplicationUtils.SKIP_COUNT_FOR_PLOT;
        }

        Logger.logInfo("PlottingTask", "Plotting started for FQrs & MQrs");

        Logger.logInfo("PlottingTask", "Plotting for 15k done at " + (System.currentTimeMillis() - ApplicationUtils.mStartMS));
    }

    private void plotUcData() {
        sleep(10000 / 3);

        PlotCallback.getInstance().sendUcData((float) ((ApplicationUtils.algoProcessEndCount * 10000) + 5000), (float) mUcData[0] * (float) Math.pow(10, 8));
        FileLogger.logData(String.valueOf((float) ((ApplicationUtils.algoProcessEndCount * 10000) + 5000)) + "," + String.valueOf((float) mUcData[0] * (float) Math.pow(10, 8)), "UC", mFileDateStamp);

        sleep(10000 / 3);

        PlotCallback.getInstance().sendUcData((float) ((ApplicationUtils.algoProcessEndCount * 10000) + 10000), (float) mUcData[1] * (float) Math.pow(10, 8));
        FileLogger.logData(String.valueOf((float) ((ApplicationUtils.algoProcessEndCount * 10000) + 10000)) + "," + String.valueOf((float) mUcData[1] * (float) Math.pow(10, 8)), "UC", mFileDateStamp);

        sleep(10000 / 3);
    }

    class FetalPlotThread implements Runnable {

        boolean isFetalDummy;

        FetalPlotThread(boolean isFetalDummy) {
            FetalPlotThread.this.isFetalDummy = isFetalDummy;
        }

        @Override
        public void run() {
            fetalPlot(isFetalDummy, "");
        }
    }

    class UcPlotThread implements Runnable {

        boolean isUcDummy;

        UcPlotThread(boolean isUcDummy) {
            UcPlotThread.this.isUcDummy = isUcDummy;
        }

        @Override
        public void run() {
            maternalPlot(isUcDummy, "");
        }
    }

    private void fetalPlot(boolean isFetalDummy, String iFileDateStamp) {
        if (ApplicationUtils.algoProcessEndCount > 0 && Constants.NoDetectionFlagFetal == 0) {
            int aDiff = mFqrsList.get(0) - ApplicationUtils.mFqrsMasterList.get(ApplicationUtils.mFqrsMasterList.size() - 1);
            if (aDiff < 60) {
                mFqrsList.remove(0);
            }

            aDiff = mFqrsList.get(0) - ApplicationUtils.mFqrsMasterList.get(ApplicationUtils.mFqrsMasterList.size() - 1);

            double aRrMean = (ApplicationUtils.mFqrsMasterList.get(ApplicationUtils.mFqrsMasterList.size() - 1) - ApplicationUtils.mFqrsMasterList.get(ApplicationUtils.mFqrsMasterList.size() - ApplicationUtils.SKIP_COUNT_FOR_PLOT - 1)) / ApplicationUtils.SKIP_COUNT_FOR_PLOT;

            if (aDiff > Constants.QRSF_RR_MISS_PERCENT * aRrMean) {
                int aFactor = (int) Math.round(aDiff / aRrMean);
                if (aFactor == 2) {
                    mFqrsList.add(0, (int) aRrMean + ApplicationUtils.mFqrsMasterList.get(ApplicationUtils.mFqrsMasterList.size() - 1));
                }
            }
        }

        ApplicationUtils.mFqrsMasterList.addAll(mFqrsList);

        for (int i = ApplicationUtils.lastFetalPlotIndex; i < ApplicationUtils.mFqrsMasterList.size(); i++) {
            double aFHR = 110;
            if (!isFetalDummy) {
                aFHR = (60.0 * Constants.FS * ApplicationUtils.SKIP_COUNT_FOR_PLOT) / (ApplicationUtils.mFqrsMasterList.get(i) - ApplicationUtils.mFqrsMasterList.get(i - ApplicationUtils.SKIP_COUNT_FOR_PLOT));
                float aFDiff = Math.abs(ApplicationUtils.mFqrsMasterList.get(i) - ApplicationUtils.mFqrsMasterList.get(i - 1));
                sleep((int) aFDiff);
            }
            try {
                PlotCallback.getInstance().sendFetalHeartRateData((float) ApplicationUtils.mFqrsMasterList.get(i), (float) aFHR);
                FileLogger.logData(String.valueOf((float) ApplicationUtils.mFqrsMasterList.get(i)) + "," + String.valueOf((float) aFHR), "fhr", iFileDateStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void maternalPlot(boolean isMaternalDummy, String iFileDateStamp) {
        if (ApplicationUtils.algoProcessEndCount > 0 && Constants.NoDetectionFlagFetal == 0) {
            int aDiff = ApplicationUtils.mMaternalMasterList.get(ApplicationUtils.lastMaternalPlotIndex + 1) - ApplicationUtils.mMaternalMasterList.get(ApplicationUtils.lastMaternalPlotIndex);
            if (aDiff < 60) {
                ApplicationUtils.mMaternalMasterList.remove(ApplicationUtils.lastMaternalPlotIndex + 1);
            }

            aDiff = ApplicationUtils.mMaternalMasterList.get(ApplicationUtils.lastMaternalPlotIndex + 1) - ApplicationUtils.mMaternalMasterList.get(ApplicationUtils.lastMaternalPlotIndex);

            double aRrMean = (ApplicationUtils.mMaternalMasterList.get(ApplicationUtils.lastMaternalPlotIndex) - ApplicationUtils.mMaternalMasterList.get(ApplicationUtils.lastMaternalPlotIndex - ApplicationUtils.SKIP_COUNT_FOR_PLOT)) / ApplicationUtils.SKIP_COUNT_FOR_PLOT;

            if (aDiff > Constants.QRSF_RR_MISS_PERCENT * aRrMean) {
                int aFactor = (int) Math.round(aDiff / aRrMean);
                if (aFactor == 2) {
                    ApplicationUtils.mMaternalMasterList.add(ApplicationUtils.lastMaternalPlotIndex + 1, (int) aRrMean + ((ApplicationUtils.algoProcessEndCount - 1) * 10000));
                }
            }
        }

        for (int i = ApplicationUtils.lastMaternalPlotIndex; i < ApplicationUtils.mMaternalMasterList.size(); i++) {
            double aMHR = 110;
            if (!isMaternalDummy) {
                aMHR = (60.0 * Constants.FS * ApplicationUtils.SKIP_COUNT_FOR_PLOT) / (ApplicationUtils.mMaternalMasterList.get(i) - ApplicationUtils.mMaternalMasterList.get(i - ApplicationUtils.SKIP_COUNT_FOR_PLOT));
                float aFDiff = Math.abs(ApplicationUtils.mMaternalMasterList.get(i) - ApplicationUtils.mMaternalMasterList.get(i - 1));
                sleep((int) aFDiff);
            }
            try {
                PlotCallback.getInstance().sendMaternalHeartRateData((float) ApplicationUtils.mMaternalMasterList.get(i), (float) aMHR);
                FileLogger.logData(String.valueOf((float) ApplicationUtils.mMaternalMasterList.get(i)) + "," + String.valueOf((float) aMHR), "mhr", iFileDateStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void sleep(int iDurationInMilliseconds) {
        try {
            Thread.sleep(iDurationInMilliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
