package com.sattvamedtech.fetallite.process;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.ExceptionHandling;
import com.sattvamedtech.fetallite.helper.FileLogger;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.interfaces.PrintCallback;
import com.sattvamedtech.fetallite.signalproc.AlgorithmMain;
import com.sattvamedtech.fetallite.signalproc.UcAlgo;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ConversionHelper extends AsyncTask<Void, Void, Void> {

    private Queue<String> mSampleQueue = new LinkedList<>();
    private double mVref = 4.5;
    private double mCheck = Math.pow(2, 23);
    private double mCheckDivide = 2 * mCheck;
    private double mGain = 24;
    //    private ArrayList<String> mBackupToRemove = new ArrayList<>();
//    private double mOutArray[][] = new double[15000][4];
    Object aFinal[];
    private long mTotalTime, mStartTime = 0;
    private String mFileDateStamp;
    private int mInterpolationCount = 0;
    private int mCurrentSampleIndex = -1;

    public ConversionHelper(List<String> iSampleList, String iFileDateStamp) {
        Logger.logInfo("ConversionHelper", "constructor initialised");
        mFileDateStamp = iFileDateStamp;
//        FileLogger.logData("Iteration " + ApplicationUtils.algoProcessStartCount, "ConversionSampleList", mFileDateStamp);
//        for (String aString : iSampleList) {
//            FileLogger.logData(aString, "ConversionSampleList", mFileDateStamp);
//        }
        mSampleQueue.addAll(iSampleList);
    }

    public void convert() {
//        FileLogger.logData("Iteration " + ApplicationUtils.algoProcessStartCount, "ConversionInput", mFileDateStamp);
//        FileLogger.logData("Iteration " + ApplicationUtils.algoProcessStartCount, "FeedInput", mFileDateStamp);

        Logger.logInfo("ConversionHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + " Population started");
        populateInputArray();
        Logger.logInfo("ConversionHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + " Population completed");

//        FileLogger.logData("Iteration count: " + ApplicationUtils.algoProcessStartCount, "AlgorithmInput", mFileDateStamp);
//        for (int i = 0; i < ApplicationUtils.mInputArray.length; i++) {
//            FileLogger.logData(ApplicationUtils.mInputArray[i][0] + "," + ApplicationUtils.mInputArray[i][1] + "," + ApplicationUtils.mInputArray[i][2] + "," + ApplicationUtils.mInputArray[i][3], "AlgorithmInput", mFileDateStamp);
//        }

//        Logger.logInfo("ConversionHelper", "mOutArray: row 1: " + mOutArray[0][0] + ", " + mOutArray[0][1] + ", " + mOutArray[0][2] + ", " + mOutArray[0][3]);
//        Logger.logInfo("ConversionHelper", "mOutArray: row 1000: " + mOutArray[999][0] + ", " + mOutArray[999][1] + ", " + mOutArray[999][2] + ", " + mOutArray[999][3]);
//        Logger.logInfo("ConversionHelper", "mOutArray: row 10000: " + mOutArray[9999][0] + ", " + mOutArray[9999][1] + ", " + mOutArray[9999][2] + ", " + mOutArray[9999][3]);

        Logger.logInfo("ConversionHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + " Algorithm started");
        AlgorithmMain aAlgorithmMain = new AlgorithmMain();
        UcAlgo Uc = new UcAlgo();

        Logger.logInfo("ConversionHelper", "Starting Algorithm after " + (System.currentTimeMillis() - ApplicationUtils.mStartMS) + "ms");

        double[] aUcFinal;
        try {
            mStartTime = System.currentTimeMillis();
            aFinal = aAlgorithmMain.algoStart(ApplicationUtils.mInputArray, ApplicationUtils.algoProcessStartCount);
            mTotalTime = System.currentTimeMillis() - mStartTime;
            Logger.logDebug("Algo Main", mTotalTime + "");
            mStartTime = System.currentTimeMillis();
            aUcFinal = Uc.UcAlgoDwt(ApplicationUtils.mInputArrayUc);
            mTotalTime = System.currentTimeMillis() - mStartTime;
            Logger.logDebug("UcAlgoDwt", mTotalTime + "");
            Logger.logInfo("ConversionHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + " Algorithm completed");

            PrintCallback.getInstance().savePrintData((String) aFinal[3]);

            while (true) {
                if (ApplicationUtils.mHrPlottingFlag == ApplicationUtils.IDLE && ApplicationUtils.mUcPlottingFlag == ApplicationUtils.IDLE) {
                    ApplicationUtils.algoProcessEndCount++;
                    ApplicationUtils.mHrPlottingFlag = ApplicationUtils.PROCESSING;
                    ApplicationUtils.mUcPlottingFlag = ApplicationUtils.PROCESSING;

                    Logger.logInfo("ConversionHelper", "Iteration " + ApplicationUtils.algoProcessEndCount + " Plotting started");
                    Logger.logInfo("ConversionHelper", "Plot processing");
//                    Logger.logInfo("ConversionHelper", "fhr: " + (FLApplication.isFetalEnabled ? ((int[]) aFinal[1]).length : 0) + "mhr: " + (!FLApplication.isFetalEnabled ? ((int[]) aFinal[0]).length : 0));
                    Logger.logInfo("ConversionHelper", "Plotting after " + (System.currentTimeMillis() - ApplicationUtils.mStartMS) + "ms");

                    new PlottingTaskSimplified((int[]) aFinal[0], (int[]) aFinal[2], true).executeOnExecutor(THREAD_POOL_EXECUTOR);
                    new PlottingTaskSimplified((int[]) aFinal[0], (int[]) aFinal[1], false).executeOnExecutor(THREAD_POOL_EXECUTOR);
                    new PlottingTaskSimplified(aUcFinal).executeOnExecutor(THREAD_POOL_EXECUTOR);

                    for (int i = 0; i < (10000 - mInterpolationCount); i++) {
                        ApplicationUtils.mSampleMasterList.remove(0);
//                ApplicationUtils.lastBufferIndex--;
                    }
                    ApplicationUtils.mConversionFlag = ApplicationUtils.IDLE;
                    break;
                }
                Logger.logInfo("ConversionHelper", "Waiting for previous plot cycle");
                Thread.sleep(50);
            }
            Logger.logInfo("ConversionHelper", "Iteration " + ApplicationUtils.algoProcessStartCount + " Plotting completed");
        } catch (Exception e) {
            e.printStackTrace();
            if (ExceptionHandling.getInstance().getExceptionListener() != null) {
                ExceptionHandling.getInstance().getExceptionListener().onException(e);
            }
        }
    }

    private void populateInputArray() {
        int aInputArrayCounter = 0;
        Logger.logDebug("ConversionHelper", "populate started");
        String aPreviousSample = "";
        String aSample = getNextValidSample();
        Logger.logDebug("ConversionHelper", "mTempTest: " + aSample);
        if (!TextUtils.isEmpty(aSample)) {
            int aLastIndex = Character.getNumericValue(aSample.charAt(0));
            Logger.logDebug("ConversionHelper", "inside if");
            feedInputArray(aSample, aInputArrayCounter);
            Logger.logDebug("ConversionHelper", "starting for");
            for (aInputArrayCounter++; aInputArrayCounter < ApplicationUtils.mInputArray.length; aInputArrayCounter++) {
                aSample = getNextValidSample();
                if (!TextUtils.isEmpty(aSample)) {
                    int aCurrentIndex = Character.getNumericValue(aSample.charAt(0));
                    int aIndexDiff = aCurrentIndex - aLastIndex;
                    if (aIndexDiff <= 0)
                        aIndexDiff += 10;
                    if (aIndexDiff == 1 || aIndexDiff == -9) {
                        feedInputArray(aSample, aInputArrayCounter);
                    } else {
                        Logger.logInfo("ConversionHelper", "sample: " + aSample + ", aPreviousSample: " + aPreviousSample + ", aCurrentIndex: " + aCurrentIndex + ", aLastIndex: " + aLastIndex);
                        aInputArrayCounter += aIndexDiff - 1;
                        feedInputArray(aSample, aInputArrayCounter);
                        interpolate(aInputArrayCounter, aInputArrayCounter - aIndexDiff);
                    }
                    aLastIndex = Character.getNumericValue(aSample.charAt(0));
                } else {
                    Logger.logDebug("ConversionHelper", "empty sample inside for");
                }
                aPreviousSample = aSample;
            }
        } else
            Logger.logDebug("ConversionHelper", "empty sample");
    }

    private String getNextValidSample() {
        String aValidSample = "";
        if (mSampleQueue.size() > 0) {
            do { // find next valid sample
                aValidSample = mSampleQueue.remove();
                mCurrentSampleIndex++;
//                FileLogger.logData(aValidSample, "ConversionInput", mFileDateStamp);
            }
            while (aValidSample != null && aValidSample.length() != 25 && mSampleQueue.size() > 0);
        }
        return aValidSample;
    }

    private void interpolate(int iCurrentInputIndex, int iStartIndex) {
        Logger.logInfo("ConversionHelper", "Interpolating...");
        Logger.logInfo("ConversionHelper", "current_input_index: " + iCurrentInputIndex);
        Logger.logInfo("ConversionHelper", "mStartIndex: " + iStartIndex);
        FileLogger.logData("Iteration count: " + ApplicationUtils.algoProcessStartCount, "Interpolation", mFileDateStamp);
        FileLogger.logData("current_input_index: " + iCurrentInputIndex + "\n" + "mStartIndex: " + iStartIndex, "Interpolation", mFileDateStamp);
        for (int k = iStartIndex + 1; k < iCurrentInputIndex; k++) {
            for (int aInputChannelIndex = 0; aInputChannelIndex < 4; aInputChannelIndex++) {
                ApplicationUtils.mInputArray[k][aInputChannelIndex] = ApplicationUtils.mInputArray[iStartIndex][aInputChannelIndex] + (ApplicationUtils.mInputArray[iCurrentInputIndex][aInputChannelIndex] - ApplicationUtils.mInputArray[iStartIndex][aInputChannelIndex]) / (iCurrentInputIndex - iStartIndex) * (k - iStartIndex); // crashed here on 2nd time
            }
            mInterpolationCount++;
        }
    }


    private void feedInputArray(String iInputString, int iInputIndex) {
//        FileLogger.logData(iInputString, "FeedInput", mFileDateStamp);
        for (int aInputChannelIndex = 0; aInputChannelIndex < 4; aInputChannelIndex++) {
            ApplicationUtils.mInputArray[iInputIndex][aInputChannelIndex] = stringToDouble(iInputString.substring(6 * aInputChannelIndex + 1, 6 * aInputChannelIndex + 7), iInputIndex, aInputChannelIndex);
            if (aInputChannelIndex == 0) {
                ApplicationUtils.mInputArrayUc[iInputIndex] = ApplicationUtils.mInputArray[iInputIndex][aInputChannelIndex];
            }
        }
    }

    private double doubleConvDouble(double iInput, String iInputString, int iArrayCount, int iChannelCount) {
        double aOut;
        if (iInput >= mCheck) {
            aOut = (iInput - mCheckDivide) * mVref / (mCheck - 1) / mGain;
//            mOutArray[iArrayCount][iChannelCount] = (iInput - mCheckDivide);
        } else {
            aOut = iInput / (mCheck - 1) / mGain * mVref;
//            mOutArray[iArrayCount][iChannelCount] = iInput;
        }
        return aOut;
    }

    private double stringToDouble(String iInput, int iArrayCount, int iChannelCount) {
        return doubleConvDouble(new BigInteger(iInput, 16).doubleValue(), iInput, iArrayCount, iChannelCount);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Logger.logInfo("ConversionHelper", "background task started");
        convert();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
//        ApplicationUtils.lastBufferIndex += ApplicationUtils.bufferLength;
        ApplicationUtils.bufferLength = 10000;
        Logger.logInfo("ConversionHelper", "algoProcessStartCount: " + ApplicationUtils.algoProcessStartCount + " algoProcessEndCount: " + ApplicationUtils.algoProcessEndCount);
    }
}
