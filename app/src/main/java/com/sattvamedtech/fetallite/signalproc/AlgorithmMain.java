package com.sattvamedtech.fetallite.signalproc;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.helper.FileLogger;
import com.sattvamedtech.fetallite.signalprocjade.JadeMainFuction;

import java.util.LinkedList;

public class AlgorithmMain {

    /**
     * @param input - N x 4 input.
     * @return
     */

    public Object[] algoStart(double[][] input, int iCurrentIteration) throws Exception {
        MatrixFunctions aMatrixFunctions = new MatrixFunctions();
        Constants.CURRENT_ITERATION = iCurrentIteration;
        if (iCurrentIteration == 0) {
            Constants.QRS_FETAL_LOCATION.add(0);
            Constants.HR_FETAL.add(0f);
            Constants.QRS_MATERNAL_LOCATION.add(0);
            Constants.HR_MATERNAL.add(0f);
        }
        LinkedList<Integer> aQrsF = new LinkedList<Integer>();
        LinkedList<Integer> aQrsM = new LinkedList<Integer>();
        /**
         * NUll input check
         */
        int aInputZeroCount = 0;
        for (int i = 0; i < Constants.NO_OF_SAMPLES; i++) {
            for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
                if (input[i][j] == 0) {
                    aInputZeroCount++;
                }
            }
        }
        if (aInputZeroCount > (15000 * 4 * 0.2)) {
            throw new Exception("Invalid input");
        }

        /**
         * Impulse filtering
         */
        ImpulseFilter aImpulseFilter = new ImpulseFilter();
        double[][] aEcgImpulse = aImpulseFilter.impulseFilterParallel(input);

        /**
         * NUll input check
         */
        int aImpulseZeroCount = 0;
        for (int i = 0; i < Constants.NO_OF_SAMPLES; i++) {
            for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
                if (aEcgImpulse[i][j] == 0) {
                    aImpulseZeroCount++;
                }
            }
        }
        if (aImpulseZeroCount > (15000 * 4 * 0.2)) {
            throw new Exception("Invalid Impulse Filter output");
        }

        /**
         * /** Filtering : Low, high, notch
         */
        FilterLoHiNotch aFilter = new FilterLoHiNotch();
        double[][] aEcgFilter = aFilter.filterParallel(aEcgImpulse);

        /**
         * Perform ICA on filtered data
         */
        JadeMainFuction aJade = new JadeMainFuction();
        double[][] aWeightedMatrix1 = aJade.jade(aEcgFilter);
        double[][] aEcgIca1 = aMatrixFunctions.multi(aEcgFilter, aMatrixFunctions.transpose(aWeightedMatrix1));

        /**
         * Estimate Maternal QRS
         */
        MQRSDetection aMqrsDetection = new MQRSDetection();
        int[] aQRSM = aMqrsDetection.mQRS(aEcgIca1);

//		if (aQRSM.length < 5) // 15000/3000 = 20 fhr
//		{
//			throw new Exception("Maternal Heart rate too low.");
//		}
//		if (aQRSM.length > 38) // 15000/400 = 150 fhr
//		{
//			throw new Exception("Maternal Heart rate too high.");
//		}

        int aCount1 = 0;
        int aCount2 = 0;
        for (int i = 0; i < aQRSM.length; i++) {
            if (aQRSM[i] < 120) {
                aCount1 = aCount1 + 1;
            }
            if (aQRSM[i] > 14850) {
                aCount2 = aCount2 + 1;
            }
        }
        int[] aQRSM1;


        /**
         * Maternal QRS cancellation
         */
        int aQRSF[];
        if (aQRSM.length >= 5 && aQRSM.length <= 38) {


            aQRSM1 = new int[aQRSM.length - aCount1 - aCount2];
            int aCount3 = 0;
            for (int i = aCount1; i < (aQRSM.length - aCount2); i++) {
                aQRSM1[aCount3] = aQRSM[i];
                aCount3 = aCount3 + 1;
            }
            Constants.LastQRSMIteration = iCurrentIteration;

            MQRSCancelI aCancel = new MQRSCancelI();

            double[][] aFetalSig = aCancel.cancel(aEcgFilter, aQRSM1);

            /**
             * Perform ICA on residue
             */
            double[][] aWeightedMatrix2 = aJade.jade(aFetalSig);
            double[][] aEcgIca2 = aMatrixFunctions.multi(aFetalSig, aMatrixFunctions.transpose(aWeightedMatrix2));

            /**
             * Estimate Fetal QRS
             */
            FQRSDetection aFqrsDetection = new FQRSDetection();
            Object[] aQrsSelected = aFqrsDetection.fQRS(aEcgIca2, aQRSM, Constants.InterpolatedLengthFetal,
                    Constants.LastQRSFetal, Constants.LastRRMeanFetal, Constants.NoDetectionFlagFetal);
            aQRSF = (int[]) aQrsSelected[0];

//			if (aQRSF.length < 13) // 15000/1200 = 50 fhr
//			{
//				throw new Exception("Fetal Heart rate too low.");
//			}
//			if (aQRSF.length > 55) // 15000/280 = 214 fhr
//			{
//				throw new Exception("Fetal Heart rate too high.");
//			}

            Constants.InterpolatedLengthFetal = (int) aQrsSelected[1];
            Constants.NoDetectionFlagFetal = (int) aQrsSelected[2];

            // Obtain the last QRS and last RR mean from this iteration for use
            // in Next iteration

            if (aQRSF.length >= 13 && aQRSF.length <= 55) {

                Constants.LastQRSFIteration = iCurrentIteration;
                int i = aQRSF.length - 1;
                while (aQRSF[i] > Constants.QRS_LENGTH_END_INTERPOLATE) {
                    i = i - 1;
                }
                Constants.LastQRSFetal = aQRSF[i] - 10000;
                Constants.LastRRMeanFetal = (aQRSF[i] - aQRSF[(int) (i - Constants.QRS_NO_RR_MEAN)])
                        / Constants.QRS_NO_RR_MEAN;

            } else {
                aQRSF = new int[]{};
                Constants.LastQRSFetal = 0;
                Constants.LastRRMeanFetal = 0;
                if (iCurrentIteration - Constants.LastQRSFIteration >= 3) {
                    new Throwable("No Fetal Heart Rate Detected");
                }
            }

        } else {
            aQRSF = new int[]{};
            Constants.LastQRSFetal = 0;
            Constants.LastRRMeanFetal = 0;
            aQRSM1 = new int[]{};
            if (iCurrentIteration - Constants.LastQRSMIteration >= 3) {
                new Throwable("No Maternal Heart Rate Detected");
            }

        }

        int[] aFinalQRSM;
        float[] aFinalHRM;
        int[] aFinalQrsmHrPlot = new int[Constants.NO_OF_PRINT_VALUES];
        String[] aHRmPrint = new String[Constants.NO_OF_PRINT_VALUES];

        for (int i = 0; i < aQRSM1.length; i++) {
            if (aQRSM1[i] >= 2000 && aQRSM1[i] <= 12000) {
                aQrsM.add(aQRSM1[i] + 10000 * Constants.CURRENT_ITERATION);
            }
        }
        HeartRateMaternal aMHR = new HeartRateMaternal();
        aMHR.heartRate(aQrsM);

        if (Constants.QRS_MATERNAL_LOCATION.size() > Constants.LastMaternalPlotIndex) {
            aFinalQRSM = new int[Constants.QRS_MATERNAL_LOCATION.size() - Constants.LastMaternalPlotIndex + 1];
            aFinalHRM = new float[Constants.QRS_MATERNAL_LOCATION.size() - Constants.LastMaternalPlotIndex + 1];
            if (Constants.NoDetectionFlagMaternal == 0 && Constants.CURRENT_ITERATION > 0) {
                aFinalHRM[0] = Constants.HR_MATERNAL.get(Constants.LastMaternalPlotIndex - 1);
                aFinalQRSM[0] = Constants.QRS_MATERNAL_LOCATION.get(Constants.LastMaternalPlotIndex - 1);
            } else {
                aFinalHRM[0] = 0;
                aFinalQRSM[0] = 10000 * Constants.CURRENT_ITERATION + 2000;
            }

            for (int i = Constants.LastMaternalPlotIndex; i < Constants.QRS_MATERNAL_LOCATION.size(); i++) {
                aFinalHRM[i - Constants.LastMaternalPlotIndex + 1] = Constants.HR_MATERNAL.get(i);
                aFinalQRSM[i - Constants.LastMaternalPlotIndex + 1] = Constants.QRS_MATERNAL_LOCATION.get(i);
            }
            Constants.LastMaternalPlotIndex = Constants.QRS_MATERNAL_LOCATION.size();

            for (int i = 0; i < aFinalHRM.length; i++) {
                FileLogger.logData(aFinalHRM[i] + "," + aFinalQRSM[i], "raw-mhr", FLApplication.mFileTimeStamp);
            }

            aHRmPrint = ConvertHR2MilliSec(aFinalQrsmHrPlot, aFinalHRM, aFinalQRSM);

        } else {
            aFinalQRSM = new int[]{};
            aFinalHRM = new float[]{};
            for (int i = 0; i < Constants.NO_OF_PRINT_VALUES; i++) {
                aFinalQrsmHrPlot[i] = 1;
                aHRmPrint[i] = "0001";
            }
        }

        int[] aFinalQRSF;
        float[] aFinalHRF;
        int[] aFinalQrsfHrPlot = new int[Constants.NO_OF_PRINT_VALUES];
        String[] aHRfPrint = new String[Constants.NO_OF_PRINT_VALUES];

        for (int j = 0; j < aQRSF.length; j++) {
            if (aQRSF[j] >= 2000 && aQRSF[j] <= 12000) {
                aQrsF.add(aQRSF[j] + 10000 * Constants.CURRENT_ITERATION);
            }
        }

        HeartRateFetal aFHR = new HeartRateFetal();
        aFHR.heartRate(aQrsF);

        if (Constants.QRS_FETAL_LOCATION.size() > Constants.LastFetalPlotIndex) {
            aFinalQRSF = new int[Constants.QRS_FETAL_LOCATION.size() - Constants.LastFetalPlotIndex + 1];
            aFinalHRF = new float[Constants.QRS_FETAL_LOCATION.size() - Constants.LastFetalPlotIndex + 1];

            if (Constants.NoDetectionFlagFetal == 0 && Constants.CURRENT_ITERATION > 0) {
                aFinalHRF[0] = Constants.HR_FETAL.get(Constants.LastFetalPlotIndex - 1);
                aFinalQRSF[0] = Constants.QRS_FETAL_LOCATION.get(Constants.LastFetalPlotIndex - 1);
            } else {
                aFinalHRF[0] = 0;
                aFinalQRSF[0] = 10000 * Constants.CURRENT_ITERATION + 2000;
            }

            for (int i = Constants.LastFetalPlotIndex; i < Constants.QRS_FETAL_LOCATION.size(); i++) {
                aFinalHRF[i - Constants.LastFetalPlotIndex + 1] = Constants.HR_FETAL.get(i);
                aFinalQRSF[i - Constants.LastFetalPlotIndex + 1] = Constants.QRS_FETAL_LOCATION.get(i);
            }

            Constants.LastFetalPlotIndex = Constants.QRS_FETAL_LOCATION.size();

            for (int i = 0; i < aFinalHRF.length; i++) {
                FileLogger.logData(aFinalHRF[i] + "," + aFinalQRSF[i], "raw-fhr", FLApplication.mFileTimeStamp);
            }

            aHRfPrint = ConvertHR2MilliSec(aFinalQrsfHrPlot, aFinalHRF, aFinalQRSF);

        } else {
            aFinalQRSF = new int[]{};
            aFinalHRF = new float[]{};

            for (int i = 0; i < Constants.NO_OF_PRINT_VALUES; i++) {
                aFinalQrsfHrPlot[i] = 1;
                aHRfPrint[i] = "0001";
            }
        }

        String aHrPrint = "";
        for (int i = 0; i < Constants.NO_OF_PRINT_VALUES; i++) {
            aHrPrint = aHrPrint + aHRfPrint[i];
            aHrPrint = aHrPrint + aHRmPrint[i];
        }
        int[] aLocation = new int[Constants.NO_OF_PRINT_VALUES];
        for (int i = 0; i < Constants.NO_OF_PRINT_VALUES; i++) {
            aLocation[i] = 2000 + Constants.DIFFERENCE_SAMPLES * i;
        }

        return new Object[]{aLocation, aFinalQrsmHrPlot, aFinalQrsfHrPlot, aHrPrint};
    }

    private String[] ConvertHR2MilliSec(int[] iFinalQrsHrPlot, float[] iFinalHR, int[] iFinalQRS) {


        /**
         * 1. First QRS is below 2000+1000*CurrentIter .
         *
         * 2. First QRS is == 2000+1000*CurrentIter && First HR = 0; 3. The
         * Points are from [2, 2.5, 3, ... , 10, 10.5, 11, 11.5] = 20 points.
         * Hence, the last point HR is equal to the heart rate in the last point
         * even if the QRS location is less than 11.5 , else it will be taken
         * accordingly.
         */
//		double aHrRange = 240 - 30;
//		double aDecimalRange = 1 - 584;

        int[] aHrDecimalPrint = new int[Constants.NO_OF_PRINT_VALUES];

        int aQrsScale = 10000 * Constants.CURRENT_ITERATION;
        int aInitialQrsThreshold = aQrsScale + 2000;
        int aQRS1, aQRS2;
        int aCurrentIndex, aNextIndex;
        int aPreviousQRSIndex = 0;
        if (iFinalHR[0] == 0 && iFinalQRS[0] == aInitialQrsThreshold) {
            // Check for each pair of values , btw which they lie.
            aPreviousQRSIndex = 1;
        }

        aQRS1 = (iFinalQRS[aPreviousQRSIndex] - aQrsScale);
        if (aQRS1 > 1500) {
            aCurrentIndex = aQRS1 / Constants.DIFFERENCE_SAMPLES + 1 - 4;
        } else {
            aCurrentIndex = aQRS1 / Constants.DIFFERENCE_SAMPLES + 1 - 3;
            // IF the QRS value is below 1500, i.e for maternal can go be 14xx
            // That time we need to subtract it by 3.
        }
        // 1. 4 is the minimum scale. Even if its 2500, consider from the next
        // sample. Here we consider {.} ( least integer function).
        // 2. aCurrentIndex cannot go negative, but later have to write for that
        // exception case also.

        for (int i = aPreviousQRSIndex + 1; i < iFinalQRS.length; i++) {

            aQRS2 = iFinalQRS[i] - aQrsScale;
            aNextIndex = aQRS2 / Constants.DIFFERENCE_SAMPLES - 4; // No +1, because it should be till
            // the [.] (greatest Integer
            // function)
            if (aNextIndex >= aCurrentIndex && aCurrentIndex < Constants.NO_OF_PRINT_VALUES && aNextIndex < Constants.NO_OF_PRINT_VALUES) {
                for (int it = aCurrentIndex; it <= aNextIndex; it++) {
                    // a = y1 + (y2-y1) * (ax - x1)/(x2-x1) ;
                    iFinalQrsHrPlot[it] = (int) (iFinalHR[aPreviousQRSIndex]
                            + (iFinalHR[i] - iFinalHR[aPreviousQRSIndex]) * (aCurrentIndex * Constants.DIFFERENCE_SAMPLES + 2000 - aQRS1)
                            / (aQRS2 - aQRS1));
                    aHrDecimalPrint[it] = (int) (Constants.HR_DECIMAL_MAX + Constants.HR_DECIMAL_PRINT_RANGE / Constants.HR_PRINT_RANGE * (iFinalQrsHrPlot[it] - Constants.HR_VALUE_MIN));
                }
                aPreviousQRSIndex = i;
                aCurrentIndex = aNextIndex + 1;
                aQRS1 = aQRS2;
            } else {
                // If the next value lies within the 500 limit, then
                // use this value for next interpolation as its closer to the
                // next 500 limit.
                aPreviousQRSIndex = i;
                aCurrentIndex = aNextIndex + 1;
                aQRS1 = aQRS2;
            }

        }
        // This is if the last QRS is less than 11500, then do this.
        // If more than 1 HR is empty in the last, then do it for the last qrs
        // and leave remaining to 0 ( as error in Algorithm ).
        if (aCurrentIndex < 19) {
            iFinalQrsHrPlot[aCurrentIndex + 1] = (int) (iFinalHR[aPreviousQRSIndex]);
            aHrDecimalPrint[aCurrentIndex
                    + 1] = (int) (Constants.HR_DECIMAL_MAX + Constants.HR_DECIMAL_PRINT_RANGE / Constants.HR_PRINT_RANGE * (iFinalQrsHrPlot[aCurrentIndex + 1] - Constants.HR_VALUE_MIN));
        }

        /**
         * Convert Decimal HR to Hexadecimal.
         */

        String[] aHrPrint = new String[Constants.NO_OF_PRINT_VALUES];
        int aInputVal;
        int aQuotient;
        int aScale;
        for (int j = 0; j < Constants.NO_OF_PRINT_VALUES; j++) {
            aHrPrint[j] = "";
            aInputVal = aHrDecimalPrint[j];
            if (aInputVal > 0) {
                for (int i = 3; i >= 0; i--) {
                    aScale = (int) Math.pow(16, i);
                    aQuotient = aInputVal / aScale;
                    aInputVal = aInputVal - aQuotient * aScale;
                    if (aQuotient == 10) {
                        aHrPrint[j] = aHrPrint[j] + "A";
                    } else if (aQuotient == 11) {
                        aHrPrint[j] = aHrPrint[j] + "B";
                    } else if (aQuotient == 12) {
                        aHrPrint[j] = aHrPrint[j] + "C";
                    } else if (aQuotient == 13) {
                        aHrPrint[j] = aHrPrint[j] + "D";
                    } else if (aQuotient == 14) {
                        aHrPrint[j] = aHrPrint[j] + "E";
                    } else if (aQuotient == 15) {
                        aHrPrint[j] = aHrPrint[j] + "F";
                    } else {
                        aHrPrint[j] = aHrPrint[j] + aQuotient;
                    }
                }
            } else {
                aHrPrint[j] = aHrPrint[j] + "0001";
            }
        }

        return aHrPrint;
    } // end Function to convert to milliSec and Hex values.

} // End Class
