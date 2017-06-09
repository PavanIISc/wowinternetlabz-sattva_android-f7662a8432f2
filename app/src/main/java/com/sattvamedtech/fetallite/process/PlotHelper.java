package com.sattvamedtech.fetallite.process;

import android.os.Environment;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.FileLogger;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.interfaces.PlotCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;


public class PlotHelper {

    public static void plotData(int[] iFQrs, int[] iMQrs, String iFileDateStamp) {
        if (FLApplication.isFetalEnabled) {
            Logger.logInfo("PlotHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + "iFQrs.length: " + iFQrs.length);
            if (iFQrs.length > 25) {
                for (int aFQrs : iFQrs) {
                    if (aFQrs >= 2000 && aFQrs <= 12000) {
                        ApplicationUtils.mFqrsMasterList.add(aFQrs + (ApplicationUtils.algoProcessStartCount * 10000));
                    }
                }
            } else {
                ApplicationUtils.mFqrsMasterList.add(2200 + (ApplicationUtils.algoProcessStartCount * 10000));
                ApplicationUtils.mFqrsMasterList.add(11800 + (ApplicationUtils.algoProcessStartCount * 10000));
            }
            fetalPlot(!(iFQrs.length > 25), iFileDateStamp);
            if (iFQrs.length > 25)
                ApplicationUtils.lastFetalPlotIndex = ApplicationUtils.mFqrsMasterList.size();
            else
                ApplicationUtils.lastFetalPlotIndex = ApplicationUtils.mFqrsMasterList.size() + 8;
        } else {
            Logger.logInfo("PlotHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + "iMQrs.length: " + iMQrs.length);
            if (iMQrs.length > 15) {
                for (int aMQrs : iMQrs) {
                    if (aMQrs >= 2000 && aMQrs <= 12000) {
                        ApplicationUtils.mMaternalMasterList.add(aMQrs + (ApplicationUtils.algoProcessStartCount * 10000));
                    }
                }
            } else {
                ApplicationUtils.mMaternalMasterList.add(2200 + (ApplicationUtils.algoProcessStartCount * 10000));
                ApplicationUtils.mMaternalMasterList.add(11800 + (ApplicationUtils.algoProcessStartCount * 10000));
            }
            maternalPlot(!(iMQrs.length > 15), iFileDateStamp);
            if (iMQrs.length > 15)
                ApplicationUtils.lastMaternalPlotIndex = ApplicationUtils.mMaternalMasterList.size();
            else
                ApplicationUtils.lastMaternalPlotIndex = ApplicationUtils.mMaternalMasterList.size() + 8;
        }

//        Thread aFetalThread = new Thread(new FetalPlotThread(!(iFQrs.length > 25)));
//        Thread aMaternalThread = new Thread(new UcPlotThread(!(iMQrs.length > 15)));

//        aFetalThread.start();
//        aMaternalThread.start();


        Logger.logInfo("PlotHelper", "Plotting started for FQrs & MQrs");

//        while (aFetalThread.isAlive() || aMaternalThread.isAlive()) {
//            try {
//                Thread.sleep(200);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }


//        int aFValueCounter;
//        for (aFValueCounter = 8; aFValueCounter < iFQrs.length - 1; aFValueCounter++) {
//            double aFHR = (60.0 * 1000.0 * 8) / (iFQrs[aFValueCounter] - iFQrs[aFValueCounter - 8]);
//            int aFDiff = iFQrs[aFValueCounter] - iFQrs[aFValueCounter - 1];
//            try {
//                Thread.sleep(aFDiff);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            try {
//                PlotCallback.getInstance().sendFetalHeartRateData((ApplicationUtils.mXEntryDiff + (float) ApplicationUtils.FQRS[aFValueCounter]), (float) aFHR);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        ApplicationUtils.mXEntryDiff += (float) iFQrs[aFValueCounter - 1];
        Logger.logInfo("PlotHelper", "Plotting for 15k done at " + (System.currentTimeMillis() - ApplicationUtils.mStartMS));
    }

    public static void plotUcData(double[] iUcData, String iFileDateStamp) {
        sleep(15000 / 3);

        PlotCallback.getInstance().sendUcData((float) ((ApplicationUtils.algoProcessStartCount * 10000) + 5000), (float) iUcData[0]);
        FileLogger.logData(String.valueOf((float) ((ApplicationUtils.algoProcessStartCount * 10000) + 5000)) + "," + String.valueOf((float) iUcData[0]), "UC", iFileDateStamp);

        sleep(15000 / 3);

        PlotCallback.getInstance().sendUcData((float) ((ApplicationUtils.algoProcessStartCount * 10000) + 10000), (float) iUcData[1]);
        FileLogger.logData(String.valueOf((float) ((ApplicationUtils.algoProcessStartCount * 10000) + 10000)) + "," + String.valueOf((float) iUcData[1]), "UC", iFileDateStamp);

        sleep(15000 / 3);
    }

    static class FetalPlotThread implements Runnable {

        boolean isFetalDummy;

        FetalPlotThread(boolean isFetalDummy) {
            FetalPlotThread.this.isFetalDummy = isFetalDummy;
        }

        @Override
        public void run() {
            fetalPlot(isFetalDummy, "");
        }
    }

    static class UcPlotThread implements Runnable {

        boolean isUcDummy;

        UcPlotThread(boolean isUcDummy) {
            UcPlotThread.this.isUcDummy = isUcDummy;
        }

        @Override
        public void run() {
            maternalPlot(isUcDummy, "");
        }
    }

    private static void fetalPlot(boolean isFetalDummy, String iFileDateStamp) {
        for (int i = ApplicationUtils.lastFetalPlotIndex; i < ApplicationUtils.mFqrsMasterList.size(); i++) {
            double aFHR = 110;
            if (!isFetalDummy) {
                aFHR = (60.0 * 1000.0 * 8) / (ApplicationUtils.mFqrsMasterList.get(i) - ApplicationUtils.mFqrsMasterList.get(i - 8));
                float aFDiff = 15000 / (ApplicationUtils.mFqrsMasterList.size() - ApplicationUtils.lastFetalPlotIndex);
                aFDiff = Math.abs(aFDiff);
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

    private static void maternalPlot(boolean isMaternalDummy, String iFileDateStamp) {
        for (int i = ApplicationUtils.lastMaternalPlotIndex; i < ApplicationUtils.mMaternalMasterList.size(); i++) {
            double aMHR = 110;
            if (!isMaternalDummy) {
                aMHR = (60.0 * 1000.0 * 8) / (ApplicationUtils.mMaternalMasterList.get(i) - ApplicationUtils.mMaternalMasterList.get(i - 8));
                float aFDiff = 15000 / (ApplicationUtils.mMaternalMasterList.size() - ApplicationUtils.lastMaternalPlotIndex);
                aFDiff = Math.abs(aFDiff);
                sleep((int) aFDiff);
            }
            try {
                PlotCallback.getInstance().sendFetalHeartRateData((float) ApplicationUtils.mMaternalMasterList.get(i), (float) aMHR);
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

    private static void logDataOld(String input, String iLogType, String iLogFileName) {
        try {
            File root = new File(Environment.getExternalStorageDirectory().getPath() + "/sattva");
            File ActivityLog = new File(Environment.getExternalStorageDirectory().getPath() + "/sattva", iLogType + "-" + FLApplication.mPatientId + iLogFileName + ".txt");

            if (root.isDirectory()) {
                // Logger.logDebug("root exists","root exists");
                if (ActivityLog.exists()) {

                    FileWriter outFile = new FileWriter(ActivityLog, true);
                    // Logger.logDebug("writing","writing " +
                    // ActivityLog.getAbsolutePath());
                    PrintWriter out = new PrintWriter(outFile);
                    out.print(input);
                    out.flush();
                    out.close();
                } else {
                    // Logger.logDebug("writing2","writing2");
                    if (ActivityLog.createNewFile()) {
                        FileWriter outFile = new FileWriter(ActivityLog, true);
                        PrintWriter out = new PrintWriter(outFile);
                        out.print(input);
                        out.flush();
                        out.close();
                    }
                }
            } else {
                if (root.mkdir()) {
                    if (ActivityLog.createNewFile()) {

                        FileWriter outFile = new FileWriter(ActivityLog, true);

                        PrintWriter out = new PrintWriter(outFile);
                        out.println(input);
                        out.flush();
                        // Logger.logDebug("writing3","writing3");
                        out.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
