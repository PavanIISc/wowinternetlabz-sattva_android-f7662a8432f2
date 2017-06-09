package com.sattvamedtech.fetallite.signalprocjade;

import com.sattvamedtech.fetallite.signalproc.ArrayUtils;
import com.sattvamedtech.fetallite.signalproc.Constants;
import com.sattvamedtech.fetallite.signalproc.EigenvalueDecomposition;
import com.sattvamedtech.fetallite.signalproc.MatrixFunctions;

import java.util.Arrays;

//import java.util.Arrays;

public class JadeMainFuction {
	MatrixFunctions mMatrixFunctions = new MatrixFunctions();

	public double[][] jade(double[][] iInput) throws Exception {
		
		double[][] aInput = new double[iInput.length][iInput[0].length];
		mMatrixFunctions.copy(iInput, aInput);
		/**
		 * 1. Mean removal 2. Whitening & projection onto signal subspace
		 */

		mMatrixFunctions.subtractMeanColumn(aInput);

		// Co-cariance matrix :: cosTheta = (X*X') / T
		double[][] aCoVarianceMatrix = mMatrixFunctions.setEigenCovarianceMatrix(aInput);

		EigenvalueDecomposition aEigDecomposition = new EigenvalueDecomposition(aCoVarianceMatrix);

		double[] aEigenValues = aEigDecomposition.getRealEigenvalues();
		double[][] aEigenVectorM = aEigDecomposition.getV();

		if (aEigenValues[0] <= 0 || aEigenValues[1] <= 0 || aEigenValues[2] <= 0 || aEigenValues[3] <= 0) {
			throw new Exception("Invalid eigen values after eigen value decomposition");
		}

		int[] aSortInd = ArrayUtils.argsort(aEigenValues);
		Arrays.sort(aEigenValues);

		// scaling
		double[][] aSpheringMatrix = new double[Constants.NO_OF_CHANNELS][Constants.NO_OF_CHANNELS];
		for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
			for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
				aSpheringMatrix[j][i] = aEigenVectorM[i][aSortInd[Constants.NO_OF_CHANNELS - 1 - j]] * 1
						/ Math.sqrt(aEigenValues[Constants.NO_OF_CHANNELS - 1 - j]);
			}
		}
		// Sphering
		mMatrixFunctions.multiply_ABtranspose(aInput, aSpheringMatrix);

		/**
		 * Estimation of the cumulant matrices.
		 */
		int aNumberCM = (Constants.NO_OF_CHANNELS * (Constants.NO_OF_CHANNELS + 1)) / 2; // dimension
																						// of
																						// the
																						// space
																						// of
																						// real
																						// symm
																						// matrices
		// Number of cumulative matrices
		int aSizeCM = Constants.NO_OF_CHANNELS * aNumberCM;
		double[][] aCumulantMatrix = new double[Constants.NO_OF_CHANNELS][aSizeCM]; // storage
																					// for
																					// cummulative
																					// atrices

		// Qij calculation
		// int duplicate_i=-1;
		/**
		 * Estimate CM matrix CM - cumulant matirx
		 */
		int aIncrement = 0;
		double aTemp_CM[][];
		for (int i = 0; i < Constants.NO_OF_CHANNELS; i++)// change value of
															// i!!!
		{
			aTemp_CM = mMatrixFunctions.findCumulantMatrixEntries(aInput, i, i, 1);
			for (int u = 0; u < Constants.NO_OF_CHANNELS; u++)
				for (int v = 0; v < Constants.NO_OF_CHANNELS; v++) {
					aCumulantMatrix[u][aIncrement + v] = aTemp_CM[u][v];
				}
			aIncrement = aIncrement + Constants.NO_OF_CHANNELS;

			for (int jm = 0; jm < i; jm++)// change value of jm!!!!
			{
				aTemp_CM = mMatrixFunctions.findCumulantMatrixEntries(aInput, jm, i, Math.sqrt(2));
				for (int u = 0; u < Constants.NO_OF_CHANNELS; u++) {
					for (int v = 0; v < Constants.NO_OF_CHANNELS; v++) {
						aCumulantMatrix[u][aIncrement + v] = aTemp_CM[u][v];
					}
				}
				aIncrement = aIncrement + Constants.NO_OF_CHANNELS;
			} // end of jm loop
		} // end of for i loop

		/**
		 * End of CM matrix generation
		 */

		/**
		 * Initialize Values for Final Jade
		 */

		double[][] aGivensRotationM = mMatrixFunctions.identity(Constants.NO_OF_CHANNELS);
		double[][] aGivensRotationMExtract = new double[Constants.NO_OF_CHANNELS][2];
		double[][] aGivensRotationPutback;
		

		double aSmallAngleTh = Math.pow(10, -6) / Math.sqrt(Constants.NO_OF_SAMPLES); // A
																						// statistically
																						// scaled
																						// threshold
																						// on
																						// `small'
																						// angles
		int aEnCore = 1;
		int aSweep = 0; // % sweep number
		int aUpdates = 0; // % Total number of rotations
		int aUpdateSweep = 0; // % Number of rotations in a given sweep

		double[][] aGivensCM = new double[2][aNumberCM];
		double[][] aCumulantMatrixExtract = new double[2][aSizeCM];
		double[][] aCumulantMatrixTemp = new double[2][aSizeCM];

		double[][] aGivensMatrix = new double[2][2];

		double aTheta = 0;

		int aIP[] = new int[aNumberCM];
		int aIQ[] = new int[aNumberCM];

		double aCosTheta, aSinTheta;
		/**
		 * Start JADE
		 */

		while (aEnCore == 1) {
			aEnCore = 0;
//			System.out.println("Jade-> sweep " + aSweep);
			aSweep = aSweep + 1;
			aUpdateSweep = 0;

			for (int p = 0; p < Constants.NO_OF_CHANNELS - 1; p++) {
				for (int q = p + 1; q < Constants.NO_OF_CHANNELS; q++) {

					for (int k = 0; k < aNumberCM; k++) {
						aIP[k] = p + k * Constants.NO_OF_CHANNELS;
						aIQ[k] = q + k * Constants.NO_OF_CHANNELS;
					}

					// computation of Givens angle
					for (int i = 0; i < 2; i++) {
						for (int k = 0; k < aNumberCM; k++) {
							if (i < 1) {
								aGivensCM[i][k] = aCumulantMatrix[p][aIP[k]] - aCumulantMatrix[q][aIQ[k]];
							} else {
								aGivensCM[i][k] = aCumulantMatrix[p][aIQ[k]] + aCumulantMatrix[q][aIP[k]];
							}
						}
					}

					aTheta = mMatrixFunctions.findGivensTheta(aGivensCM);

					if (Math.abs(aTheta) > aSmallAngleTh) {
						aEnCore = 1;
						aUpdateSweep = aUpdateSweep + 1;

						aCosTheta = Math.cos(aTheta);
						aSinTheta = Math.sin(aTheta);
						aGivensMatrix[0][0] = aCosTheta;
						aGivensMatrix[0][1] = -aSinTheta;
						aGivensMatrix[1][0] = aSinTheta;
						aGivensMatrix[1][1] = aCosTheta;
						// V1(:,pair) extract the 2 columns
						for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
							aGivensRotationMExtract[i][0] = aGivensRotationM[i][p];
							aGivensRotationMExtract[i][1] = aGivensRotationM[i][q];
						}

						aGivensRotationPutback = mMatrixFunctions.multi(aGivensRotationMExtract, aGivensMatrix);
						// V(:,pair) put back the output in V
						for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
							aGivensRotationM[i][p] = aGivensRotationPutback[i][0];
							aGivensRotationM[i][q] = aGivensRotationPutback[i][1];
						}

						for (int i = 0; i < aSizeCM; i++) {
							aCumulantMatrixExtract[0][i] = aCumulantMatrix[p][i];
							aCumulantMatrixExtract[1][i] = aCumulantMatrix[q][i];
						}

						aCumulantMatrixTemp = mMatrixFunctions.multi(mMatrixFunctions.transpose(aGivensMatrix), aCumulantMatrixExtract);

						for (int i = 0; i < aSizeCM; i++) {
							aCumulantMatrix[p][i] = aCumulantMatrixTemp[0][i];
							aCumulantMatrix[q][i] = aCumulantMatrixTemp[1][i];
						}
						for (int i = 0; i < aNumberCM; i++) {
							for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
								double z1 = aCosTheta * aCumulantMatrix[j][aIP[i]] + aSinTheta * aCumulantMatrix[j][aIQ[i]];
								double z = -aSinTheta * aCumulantMatrix[j][aIP[i]] + aCosTheta * aCumulantMatrix[j][aIQ[i]];
								aCumulantMatrix[j][aIP[i]] = z1;
								aCumulantMatrix[j][aIQ[i]] = z;
							}
						}

					} // end if 'theta'

				} // end for loop 'q'
			} // end for loop 'p'

			aUpdates = aUpdates + aUpdateSweep;

		} // end while (encore)

		/**
		 * Permut the rows of the separating matrix B to get the most energetic
		 * components first. Here the **signals** are normalized to unit
		 * variance. Therefore, the sort is according to the norm of the columns
		 * of A = pinv(B)
		 */

		double[][] aWhiteningMatrix = mMatrixFunctions.multi(mMatrixFunctions.transpose(aGivensRotationM), aSpheringMatrix);

		Matrix aDeWhiteningMatrixM = new Matrix(aWhiteningMatrix);
		double aDeWhiteningMatrix[][] = aDeWhiteningMatrixM.inverse().getArray();

		double[] aSum = new double[Constants.NO_OF_CHANNELS];
		for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
			for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
				aSum[i] = aSum[i] + aDeWhiteningMatrix[j][i] * aDeWhiteningMatrix[j][i];
			}
		}

		int aInd[] = ArrayUtils.argsort(aSum);

		double[][] aWhiteningMatrixSort = new double[Constants.NO_OF_CHANNELS][Constants.NO_OF_CHANNELS];
		for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
			for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
				aWhiteningMatrixSort[i][j] = aWhiteningMatrix[aInd[i]][j];
			}
		}

		double[][] aWhiteningMatrixSortReverse = new double[Constants.NO_OF_CHANNELS][Constants.NO_OF_CHANNELS];
		for (int i = 0; i < Constants.NO_OF_CHANNELS; i++) {
			for (int j = 0; j < Constants.NO_OF_CHANNELS; j++) {
				aWhiteningMatrixSortReverse[i][j] = aWhiteningMatrixSort[Constants.NO_OF_CHANNELS - 1 - i][j];
			}
		}
		// Signs are fixed by forcing the first column of B to have non-negative
		// entries.
		double[][] aDeMixingMatrix = new double[Constants.NO_OF_CHANNELS][Constants.NO_OF_CHANNELS];
		int aTemp = 0;

		for (int w = 0; w < Constants.NO_OF_CHANNELS; w++) {
			if (aWhiteningMatrixSortReverse[w][0] >= 0) {
				aTemp = 1;
			} else {
				aTemp = -1;
			}
			for (int v = 0; v < Constants.NO_OF_CHANNELS; v++) {
				aDeMixingMatrix[w][v] = aWhiteningMatrixSortReverse[w][v] * aTemp;
			}
			aTemp = 0;
		}

		return aDeMixingMatrix;
	} // end main

}// class ends
