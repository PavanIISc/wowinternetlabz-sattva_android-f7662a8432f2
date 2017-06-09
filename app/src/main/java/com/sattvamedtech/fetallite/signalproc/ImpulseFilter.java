package com.sattvamedtech.fetallite.signalproc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImpulseFilter {
	/**
	 * Input = N x 4 signal
	 */
	static boolean i0Flag = false;
	static boolean i1Flag = false;
	static boolean i2Flag = false;
	static boolean i3Flag = false;

	MatrixFunctions mMatrixFunctions = new MatrixFunctions();
	double[][] mInputBoundryCheck = new double[Constants.NO_OF_SAMPLES][Constants.NO_OF_CHANNELS];
	int mWindowMedian = (int) Math.floor((Constants.IMPULSE_WINDOW_PERCENT * Constants.FS));
	double[][] mImpulseRemoved = new double[Constants.NO_OF_SAMPLES][Constants.NO_OF_CHANNELS];

	public double[][] impulseFilterParallel(double[][] input) {
		// creating a copy of input array to avoid errors due to java copy by
		// reference.
		System.out.println("i0 Flag :" + i0Flag);
		System.out.println("i1 Flag :" + i1Flag);
		System.out.println("i2 Flag :" + i2Flag);
		System.out.println("i3 Flag :" + i3Flag);

		mMatrixFunctions.copy(input, mInputBoundryCheck);

		setBoundryCondition();

		ExecutorService aExec = Executors.newFixedThreadPool(Constants.NO_OF_CHANNELS);
		try {
			for (int aCols = 0; aCols < Constants.NO_OF_CHANNELS; aCols++) {
				final double[][] aFinalInputBoundryCheck = mInputBoundryCheck;
				final int aFinalCols = aCols;
				final double[] aChannelInputElimitation = new double[Constants.NO_OF_SAMPLES];

				aExec.submit(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < Constants.NO_OF_SAMPLES; i++) {
							aChannelInputElimitation[i] = aFinalInputBoundryCheck[i][aFinalCols];
						}

						impulseElimination(aChannelInputElimitation, Constants.IMPULSE_THRESHOLD, mWindowMedian,
								Constants.IMPULSE_PERCENTILE);

						for (int i = 0; i < Constants.NO_OF_SAMPLES; i++) {
							mImpulseRemoved[i][aFinalCols] = aChannelInputElimitation[i];
						}

						if (aFinalCols == 0)
							i0Flag = true;
						else if (aFinalCols == 1)
							i1Flag = true;
						else if (aFinalCols == 2)
							i2Flag = true;
						else if (aFinalCols == 3)
							i3Flag = true;
					}
				});

			}
			while (true) {
				Thread.sleep(10);
				if (i0Flag && i1Flag && i2Flag && i3Flag) {
					i0Flag = false;
					i1Flag = false;
					i2Flag = false;
					i3Flag = false;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			aExec.shutdown();
		}

		return mImpulseRemoved;
	}
	// main ends

	/**
	 * Set the first ten samples to the median of the following three
	 */
	private void setBoundryCondition() {
		for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
			double[] aTempBoundryArray = new double[Constants.IMPULSE_INITIAL_MEDIAN_SIZE];

			int aIndex = 0;
			for (int j = Constants.IMPULSE_NO_INITIAL_SAMPLES; j < Constants.IMPULSE_NO_INITIAL_SAMPLES
					+ Constants.IMPULSE_INITIAL_MEDIAN_SIZE; j++) {
				aTempBoundryArray[aIndex] = mInputBoundryCheck[j][i];
				++aIndex;
			}

			double aMedianTempArray = mMatrixFunctions.findMedian(aTempBoundryArray);

			for (int j = 0; j < Constants.IMPULSE_NO_INITIAL_SAMPLES; j++) {
				mInputBoundryCheck[j][i] = aMedianTempArray;
			}
		}
	}

	/**
	 * Impulse Elimantion fucntion
	 * 
	 */

	private void impulseElimination(double[] iInput, int iThreshold, int iWindow, int iPercentile) {
		int aLengthInput = iInput.length;
		if (iWindow % 2 == 0) {
			iWindow++;
		}
		double aMedianInput[] = medianFilter1D(iInput, iWindow);
		double[] aAbsoluteMedianInput = new double[aLengthInput];

		// Finding absolute value of the difference of median and input.Helps to
		// find the threshold for the filter.
		for (int i = 0; i < aLengthInput; i++) {
			aAbsoluteMedianInput[i] = Math.abs(iInput[i] - aMedianInput[i]);
		}

		int[] aIndex = mMatrixFunctions.findingPositiveElementsIndex(aAbsoluteMedianInput);

		double[] aTempAbsoluteMedian = new double[aIndex.length];

		for (int r = 0; r < aIndex.length; r++) {
			aTempAbsoluteMedian[r] = aAbsoluteMedianInput[aIndex[r]];
		}

		double aMaxAbsoluteMedian = mMatrixFunctions.findPercentileValue(aTempAbsoluteMedian, iPercentile);

		double aThresholeAbsolute = iThreshold * aMaxAbsoluteMedian;

		double[] aMedianThresholdedArray = new double[aAbsoluteMedianInput.length];

		for (int i = 0; i < aAbsoluteMedianInput.length; i++) {
			aMedianThresholdedArray[i] = (aAbsoluteMedianInput[i] - (aThresholeAbsolute));
		}
		int aIndexThresholded[] = mMatrixFunctions.findingPositiveElementsIndex(aMedianThresholdedArray);

		if (aIndexThresholded != null) {
			int i = 0;
			while (i < aIndexThresholded.length) {
				int aIndexInitial = aIndexThresholded[i];
				while ((i < aIndexThresholded.length - 1) && (aIndexThresholded[i + 1] == aIndexThresholded[i] + 1)) {
					i = i + 1;
				}
				double aIndexFinal = aIndexThresholded[i];
				double aTempMedian = (iInput[(int) Math.max(aIndexInitial - 1, 1)]
						+ iInput[(int) (Math.min(aIndexFinal + 1, aLengthInput))]) / 2;

				for (int ind = aIndexInitial; ind <= aIndexFinal; ind++) {
					iInput[ind - 1] = aTempMedian;
				}
				i = i + 1;
			}
		}

	}

	/**
	 * Find median across the signal and update
	 */
	private double[] medianFilter1D(double[] iInputMedian1D, int iWindowIn) {

		int aLengthInput = iInputMedian1D.length;
		int aLengthExt = iWindowIn / 2;

		aLengthExt = Math.min(aLengthExt, aLengthInput);

		double[] aExtensionInitial = new double[aLengthExt];
		double[] aExtensionFinal = new double[aLengthExt];
		for (int i = 0; i < aLengthExt; i++) {
			aExtensionInitial[i] = iInputMedian1D[i];
			aExtensionFinal[i] = iInputMedian1D[aLengthInput - (aLengthExt - i)];
		}

		double aMedianExtensionInitial = mMatrixFunctions.findMedian(aExtensionInitial);
		double aMedianExtensionFinal = mMatrixFunctions.findMedian(aExtensionFinal);

		int aLengthBoundryExtended = aLengthInput + 2 * aLengthExt;
		double[] aInputBoundryExtended = new double[aLengthBoundryExtended];

		for (int i = 0; i < aLengthExt; i++) {
			aInputBoundryExtended[i] = aMedianExtensionInitial;
			aInputBoundryExtended[aLengthInput + aLengthExt + i] = aMedianExtensionFinal;
		}

		for (int i = 0; i < aLengthInput; i++) {
			aInputBoundryExtended[aLengthExt + i] = iInputMedian1D[i];
		}

		// median1D function starts here

		int aWindow;
		if (iWindowIn % 2 == 0) { // if even
			aWindow = iWindowIn / 2;
		} else {
			aWindow = (iWindowIn - 1) / 2;
		}

		double aInputFinalExtension[] = new double[2 * aWindow + aLengthBoundryExtended];
		for (int i = 0; i < aLengthBoundryExtended; i++) {
			aInputFinalExtension[i + aWindow] = aInputBoundryExtended[i];
		}

		double aMeidanOutExtended[] = new double[aLengthBoundryExtended];
		double[] aMedianArray = new double[iWindowIn];

		for (int i = 0; i < aLengthBoundryExtended; i++) {
			int index = 0;
			for (int k = i; k < i + iWindowIn; k++) {
				aMedianArray[index] = aInputFinalExtension[k];
				index = index + 1;
			}
			aMeidanOutExtended[i] = mMatrixFunctions.findMedian(aMedianArray);
		}

		double[] aMedianOutput = new double[aLengthInput];

		for (int i = 0; i < aLengthInput; i++) {
			aMedianOutput[i] = aMeidanOutExtended[i + aLengthExt];
		}

		return aMedianOutput;

	} // end function

}