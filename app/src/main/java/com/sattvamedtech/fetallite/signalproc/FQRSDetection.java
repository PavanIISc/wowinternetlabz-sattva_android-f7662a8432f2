package com.sattvamedtech.fetallite.signalproc;

public class FQRSDetection {
	private static int length;
	MatrixFunctions Matrix = new MatrixFunctions();

	/**
	 * 
	 * @param iInput
	 * @param iQrsM
	 * @param iInterpolatedLength
	 * @param iQRSLast
	 * @param iRRMeanLast
	 * @param iNoDetectionFlag
	 * @return Object[] { aQRSFinal, aInterpolatedLength, aNoDetectionFLag };
	 * @throws Exception
	 */
	public Object[] fQRS(double[][] iInput, int[] iQrsM, int iInterpolatedLength, int iQRSLast, double iRRMeanLast,
			int iNoDetectionFlag) throws Exception {

		length = iInput.length;

		double[] channel1 = new double[length];
		double[] channel2 = new double[length];
		double[] channel3 = new double[length];
		double[] channel4 = new double[length];

		for (int i = 0; i < length; i++) {
			channel1[i] = iInput[i][0];
			channel2[i] = iInput[i][1];
			channel3[i] = iInput[i][2];
			channel4[i] = iInput[i][3];
		}

		int qrs1[] = fetalQRS(channel1);
		int qrs2[] = fetalQRS(channel2);
		int qrs3[] = fetalQRS(channel3);
		int qrs4[] = fetalQRS(channel4);

		Object[] qrsSelectionInputs = Matrix.channelSelection_Feb17(qrs1, qrs2, qrs3, qrs4,
				Constants.FQRS_VARIANCE_THRESHOLD, Constants.FQRS_RR_LOW_TH, Constants.FQRS_RR_HIGH_TH);

		// Object[] qrsSelectionInputs2 = Matrix.channelSelection(qrs1, qrs2,
		// qrs3, qrs4, Constants.FQRS_VARIANCE_THRESHOLD);

		QRSSelection aQrsSelect = new QRSSelection();
		Object[] aQrsSelected = aQrsSelect.qrsSelection((int[]) qrsSelectionInputs[0], (int) qrsSelectionInputs[1],
				iQrsM, iInterpolatedLength, iQRSLast, iRRMeanLast, iNoDetectionFlag);

		// qrsFSelectionQueue qrsFetal = new qrsFSelectionQueue();
		// int[] qrsF = qrsFetal.qrs((int[]) qrsSelectionInputs2[0], (int)
		// qrsSelectionInputs[1], iQrsM);

		return aQrsSelected;
	}

	/**
	 * Change the filter [a,b] values for 2 filters
	 * 
	 */

	private int[] fetalQRS(double[] channel) {

		// differentiate and square
		Matrix.convolutionQRSDetection(channel, Constants.QRS_DERIVATIVE);

		/**
		 * FIltering 0.8- 3Hz
		 */

		double bhigh[] = new double[2];
		for (int i0 = 0; i0 < 2; i0++) {
			bhigh[i0] = Constants.FQRS_BHIGH0 + Constants.FQRS_BHIGH_SUM * (double) i0;
		}

		Matrix.filterLoHi(channel, Constants.FQRS_AHIGH, bhigh, Constants.FQRS_ZHIGH);

		// Have to add 6th order filter

		Matrix.filterLoHi(channel, Constants.FQRS_ALOW, Constants.FQRS_BLOW, Constants.FQRS_ZLOW);

		/**
		 * Integrator
		 */

		double[] integrator = new double[length];

		double sum = 0;

		for (int j = 0; j < Constants.FQRS_WINDOW; j++) {
			sum = sum + channel[Constants.FQRS_WINDOW - j - 1];
		}
		integrator[Constants.FQRS_WINDOW - 1] = sum / Constants.FQRS_WINDOW;

		for (int i = Constants.FQRS_WINDOW; i < length; i++) {
			integrator[i] = integrator[i - 1]
					+ (-channel[i - Constants.FQRS_WINDOW] + channel[i]) / Constants.FQRS_WINDOW;
		}
		/**
		 * Find the 90% and 10% value to find the threshold
		 */

		double threshold = Matrix.setIntegratorThreshold(integrator, Constants.FQRS_INTEGRATOR_THRESHOLD_SCALE);

		/**
		 * Peak Detection , not sure about return type have to change it Just
		 * return the first column of the Maxtab. No need the magnitudes.
		 */
		int peakLoc[] = Matrix.peakDetection(integrator, threshold);

		int delay = Constants.FQRS_WINDOW / 2;
		int peakLength = peakLoc.length;
		// Check the starting peak is greater than delay/2 or remove nIt
		int count = 0;
		for (int i = 0; i < peakLength; i++) {
			if (peakLoc[i] < delay + 2) {
				count = count + 1;
			}
		}

		int lenQrs = peakLength - count;
		int[] qrs = new int[lenQrs];
		for (int i = 0; i < lenQrs; i++) {
			qrs[i] = peakLoc[i + count] - delay;
		}

		return qrs;
	}

}