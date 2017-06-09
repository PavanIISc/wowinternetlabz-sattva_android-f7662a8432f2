package com.sattvamedtech.fetallite.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Vibhav on 16/11/16.
 */
public class ApplicationUtils {

    public static final int IDLE = 0;
    public static final int PROCESSING = 1;

    public static Queue<String> mDynamicDataStore = new LinkedList<String>();
    public static ArrayList<String> mSampleMasterList = new ArrayList<>();
    public static ArrayList<Integer> mFqrsMasterList = new ArrayList<>();
    public static ArrayList<Integer> mMaternalMasterList = new ArrayList<>();
    public static int algoProcessStartCount = -1;
    public static int algoProcessEndCount = -1;
    public static int bufferLength = 15000;
    //    public static int lastBufferIndex = 0;
    public static final int SKIP_COUNT_FOR_PLOT = 1;
    public static int lastFetalPlotIndex = SKIP_COUNT_FOR_PLOT;
    public static int lastMaternalPlotIndex = SKIP_COUNT_FOR_PLOT;
    public static int lastFetalPlotXValue = 0;
    public static int lastMaternalPlotXValue = 0;
    public static int lastUcPlotXValue = 0;
    public static int chan_select = 0;
    public static int mConversionFlag = IDLE;
    public static int mPlottingFlag = IDLE;
    public static int mHrPlottingFlag = IDLE;
    public static int mUcPlottingFlag = IDLE;
    public static double[][] mInputArray = new double[15000][4];
    public static double[] mInputArrayUc = new double[15000];
    public static final double[][] mTestInputArray = new double[15000][4];
    public static long mStartMS;
    public static int[] FQRS;
    public static int[] MQRS;
    public static int test_printer_flag = 1;

    public static float mXEntryDiff = 0;
    public static int sample_set = 0;

}
