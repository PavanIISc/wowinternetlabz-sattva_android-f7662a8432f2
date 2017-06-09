package com.sattvamedtech.fetallite.signalproc;

import java.util.LinkedList;

public class Constants {

    public static final int NO_OF_SAMPLES = 15000;
    public static final int NO_OF_CHANNELS = 4;
    public static final int FS = 1000;


    /**
     * Constants for computing HR plotting and print
     */

    public static final int HR_DECIMAL_MAX = 584;
    public static final int HR_DECIMAL_MIN = 1;
    public static final int HR_VALUE_MAX = 240;
    public static final int HR_VALUE_MIN = 30;


    public static final double HR_PRINT_RANGE = 240 - 30;
    public static final double HR_DECIMAL_PRINT_RANGE = 1 - 584;
    public static final int NO_OF_PRINT_VALUES = 20;
    public static final int DIFFERENCE_SAMPLES = 500;


    public static int CURRENT_ITERATION = 0;
    public static LinkedList<Integer> QRS_FETAL_LOCATION = new LinkedList<Integer>();
    public static LinkedList<Float> HR_FETAL = new LinkedList<Float>();
    public static LinkedList<Integer> QRS_MATERNAL_LOCATION = new LinkedList<Integer>();
    public static LinkedList<Float> HR_MATERNAL = new LinkedList<Float>();
    public static int LastFetalPlotIndex = 1;
    public static int LastMaternalPlotIndex = 1;

    public static int LastQRSMIteration = -1;
    public static int NoDetectionFlagMaternal = 0;
    public static double LastRRMeanMaternal = 0;
    public static int LastQRSMaternal = 0;
    public static int InterpolatedLengthMaternal = 0;

    public static int LastQRSFIteration = -1;
    public static int NoDetectionFlagFetal = 0;
    public static double LastRRMeanFetal = 0;
    public static int LastQRSFetal = 0;
    public static int InterpolatedLengthFetal = 0;

    /**
     * Inpulse Filter Constant Values
     */
    public static final int IMPULSE_NO_INITIAL_SAMPLES = 10;
    public static final int IMPULSE_INITIAL_MEDIAN_SIZE = 3;
    public static final int IMPULSE_THRESHOLD = 4;
    public static final int IMPULSE_PERCENTILE = 2; // ALWAYS < 100
    public static final double IMPULSE_WINDOW_PERCENT = 0.06;

    /**
     * Filtering Lo-Hi-Notch Constant Values
     */
    public static final double FILTER_ZHIGH = -0.98453370859689782;
    public static final double FILTER_BHIGH0 = 0.984533708596897;
    public static final double FILTER_BHIGH_SUM = -1.9690674171937941;
    public static final double FILTER_AHIGH_SUM = -1.969067417193793;

    public static final double FILTER_ALOW[] = {1.0, -0.324919696232906};
    public static final double FILTER_BLOW[] = {0.337540151883547, 0.337540151883547};
    public static final double FILTER_ZLOW = 0.662459848116453;

    public static final int FILTER_NFACT2 = 3;

    public static final double FILTER_ANOTCH[] = {1.0, -1.8329786119774709, 0.927307768331003};
    public static final double FILTER_BNOTCH[] = {0.963653884165502, -1.8329786119774709, 0.963653884165502};
    public static final double FILTER_ZNOTCH1 = 0.036346115834507829;
    public static final double FILTER_ZNOTCH2 = 0.036346115834507829 + -1.8013368574543165E-14;
    public static final int FILTER_NFACT3 = 6;

    /**
     * Maternal QRS detection
     */
    public static final double[] QRS_DERIVATIVE = {-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10};
    public static final int QRS_DERIVATIVE_SCALE = 64;

    public static final double QRS_INTEGRTOR_MAX = 0.9;
    public static final double QRS_INTEGRATOR_MIN = 0.1;
    public static final double MQRS_INTEGRATOR_THRESHOLD_SCALE = 10;
    public static final double FQRS_INTEGRATOR_THRESHOLD_SCALE = 20;

    public static final double MQRS_BHIGH0 = 0.997493021323278;
    public static final double MQRS_BHIGH_SUM = -1.994986042646556;
    public static final double MQRS_AHIGH_SUM = -1.994986042646556;
    public static final double MQRS_ZHIGH = -0.997493021323276;

    public static final double[] MQRS_BLOW = {0.009337054753656, 0.009337054753656};
    public static final double MQRS_ALOW_SUM = -1.981325890492688;
    public static final double MQRS_ZLOW = 0.99066294524634591;

    public static final int MQRS_WINDOW = 50;
    public static final int MQRS_VARIANCE_THRESHOLD = 40;
    public static final int MQRS_RR_LOW_TH = 400;
    public static final int MQRS_RR_HIGH_TH = 1200;
    // MQRS selection threshold values
    public static final int QRSM_HIGH_RR_THRESHOLD = 400;
    public static final int QRSM_LOW_RR_THRESHOLD = 200;

    public static final double QRSM_RR_MISS_PERC = 0.4;

    public static final int QRSM_INITIAL_RR_COUNT = 10;
    public static final int QRSM_RR_COUNT = 8;

    /**
     * Fetal QRS Detection
     */
    // Use derivative and integrator mentioned in maternal QRS detection
    public static final double[] FQRS_AHIGH = {1.000000000000000, -0.987511929907314};
    public static final double FQRS_BHIGH0 = 0.993755964953657;
    public static final double FQRS_BHIGH_SUM = -1.9875119299073141;
    public static final double FQRS_ZHIGH = -0.99375596495365437;

    public static final double[] FQRS_ALOW = {1.000000000000000, -0.978247159730251};
    public static final double[] FQRS_BLOW = {0.010876420134875, 0.010876420134875};
    public static final double FQRS_ZLOW = 0.98912357986517108;

    public static final int FQRS_WINDOW = 75;
    public static final int FQRS_VARIANCE_THRESHOLD = 60;
    public static final int FQRS_RR_LOW_TH = 300;
    public static final int FQRS_RR_HIGH_TH = 600;

    public static final int QRSF_INITIAL_RR_COUNT = 9;
    public static final int QRSF_RR_COUNT = 8;

    // low and high percentage is found in maternal QRS detection, same is used
    // for this also
    public static final double QRS_NO_RR_MEAN = 4.0;
    //	public static final int QRS_VAR_RR_TH = 60;
    public static final double QRS_RRLOW_PERC = 0.8;//0.763128816516429;
    public static final double QRS_RRHIGH_PERC = 1.2;//1.261883214824392;

    public static final double QRSF_RR_MISS_PERCENT = 1.66;
    public static final int QRSF_RR_THRESHOLD = 300;

    public static final int QRS_LENGTH_MAX_INTERPOLATE = 2000; // 2 secs of data
    public static final int QRS_LENGTH_END_INTERPOLATE = 12000; //
    public static final int QRS_LENGTH_START_INTERPOLATE = 2000; //
    /**
     * QRSM CANCELLATION
     */
    public static final double CANCEL_QRS_BEFORE_PERC = 0.2;
    public static final double CANCEL_QRS_AFTER_PERC = 0.8;
    public static final double CANCEL_QRS_AFTER_TH = 0.5;

    public static final double CANCEL_LASTQRS_TH_HIGH_PERC = 0.15;
    public static final double CANCEL_LASTQRS_TH_LOW_PERC = 0.1;
    public static final int CANCEL_NO_SAMPLES_END = 5;

    public static void reset() {
        CURRENT_ITERATION = 0;
        QRS_FETAL_LOCATION.clear();
        HR_FETAL.clear();
        QRS_MATERNAL_LOCATION.clear();
        HR_MATERNAL.clear();
        LastFetalPlotIndex = 1;
        LastMaternalPlotIndex = 1;

        LastQRSMIteration = -1;
        NoDetectionFlagMaternal = 0;
        LastRRMeanMaternal = 0;
        LastQRSMaternal = 0;
        InterpolatedLengthMaternal = 0;

        LastQRSFIteration = -1;
        NoDetectionFlagFetal = 0;
        LastRRMeanFetal = 0;
        LastQRSFetal = 0;
        InterpolatedLengthFetal = 0;
    }

}
