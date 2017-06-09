package com.sattvamedtech.fetallite.signalproc;

import java.util.Arrays;

public class MatrixFunctions {
	/*
	 * Some comments about the class Matrix Matrix operations are performed on
	 * 2D arrays
	 *
	 */

	/**
	 * Copies the matrix into a different 2D - array
	 * 
	 * @param iInput
	 *            -- 2D array
	 * @param iOutput
	 *            -- Copy of the input
	 */
	public void copy(double[][] iInput, double[][] iOutput) {
		int aLength = iInput.length;
		int aWidth = iInput[0].length;

		for (int i = 0; i < aLength; i++) {
			for (int j = 0; j < aWidth; j++) {
				iOutput[i][j] = iInput[i][j];
			}
		}
	}

	/**
	 * Find median of the given Input array.
	 * 
	 * @param iInput
	 * @return returns median
	 */
	public double findMedian(double[] iInput) {

		Arrays.sort(iInput);
		int aLen = iInput.length;
		double aMedian = 0;
		if (aLen % 2 == 1) {
			aMedian = iInput[aLen / 2];
		} else {
			aMedian = (iInput[aLen / 2] + iInput[aLen / 2 - 1]) / 2;
		}
		return aMedian;

	}

	/**
	 * 
	 * @param iInput
	 * @param iPrecentile
	 * @return value of sorted iInput at the location of iPercentile
	 */
	public double findPercentileValue(double[] iInput, int iPrecentile) {
		Arrays.sort(iInput);
		int aFinalIndex = iInput.length - (iInput.length * iPrecentile / 100);
		return iInput[aFinalIndex - 1];
	}

	/**
	 * Multiply 2 matrices A and B and return in new matrix C.
	 * 
	 * @param iA
	 * @param iB
	 * @return Product of iA and iB
	 * @throws Exception
	 */
	public double[][] multi(double[][] iA, double[][] iB) throws Exception {

		int aColA = iA[0].length;
		int aRowA = iA.length;
		int aColB = iB[0].length;
		int aRowB = iB.length;
		double aC[][] = new double[aRowA][aColB];
		if (aColA == aRowB) // Matrix multiplication condition mxn and nxp
							// produces mxp;
		{

			for (int i = 0; i < aRowA; i++) {
				for (int j = 0; j < aColB; j++) {
					for (int k = 0; k < aRowB; k++) {
						aC[i][j] = aC[i][j] + iA[i][k] * iB[k][j];
					}
				}
			}
			return aC;
		} else {
			throw new Exception("Enter a valid Matrix");
		}

	}

	/**
	 * USED IN JADE
	 * 
	 * @param iInput
	 */
	public void subtractMeanColumn(double[][] iInput) {

		int aRow = iInput.length;
		int aCol = iInput[0].length;

		for (int i = 0; i < aCol; i++) {
			double aMean = 0;
			for (int j = 0; j < aRow; j++) {
				aMean = aMean + iInput[j][i];
			}
			aMean = aMean / aRow;
			for (int j = 0; j < aRow; j++) {
				iInput[j][i] = iInput[j][i] - aMean;
			}
		}
	}

	/**
	 * USED IN JADE DO :: C = (aT * a)/T
	 * 
	 * @param iInput
	 * @return
	 */
	public double[][] setEigenCovarianceMatrix(double[][] iInput) {
		int aRow = iInput.length;
		int aCol = iInput[0].length;
		double[][] aCoVariance = new double[aCol][aCol];
		double aSum;

		for (int i = 0; i < aCol; i++) {
			for (int j = 0; j < aCol; j++) {
				aSum = 0;
				for (int k = 0; k < aRow; k++) {
					aSum = aSum + iInput[k][i] * iInput[k][j];
				}
				aCoVariance[i][j] = aSum / aRow;
			}
		}

		return aCoVariance;
	}

	/**
	 * USED IN JADE Multiply matrices 'a' and 'bT' and update the matrix 'a'
	 * with the result
	 * 
	 * @param iA
	 * @param iB
	 * @throws Exception
	 */

	public void multiply_ABtranspose(double[][] iA, double[][] iB) throws Exception {
		int aRowA = iA.length;
		int aColA = iA[0].length;

		int aRowB = iB.length;
		int aColB = iB[0].length;

		double[] aTemp = new double[aColA];
		double aSum;
		if (aColA == aColB) {
			for (int i = 0; i < aRowA; i++) {
				for (int j = 0; j < aColA; j++) {
					aTemp[j] = iA[i][j];
				}
				for (int j = 0; j < aColA; j++) {
					aSum = 0;
					for (int k = 0; k < aRowB; k++) {
						aSum = aSum + aTemp[k] * iB[j][k];
					}
					iA[i][j] = aSum;
				}
			}
		} else {
			throw new Exception("Invalid Matrices for multiplication.");
			// return exception and break
		}

	}

	/**
	 * USED IN JADE
	 * 
	 * @param iIm
	 *            -- Extract from X(:,im)
	 * @param iInput
	 *            -- input for finding CM
	 * @param iJm
	 *            -- subtracting element for identity matrix extract
	 * @return
	 */
	public double[][] findCumulantMatrixEntries(double[][] iInput, int iIm, int iJm, double iScale) {
		int aLength = iInput.length;
		int aSize = iInput[0].length;
		double[][] aTempCM = new double[aSize][aSize];

		double[] aTempColSquare = new double[aLength];
		for (int i = 0; i < aLength; i++) {
			aTempColSquare[i] = iInput[i][iIm] * iInput[i][iJm];
		}

		double aSum;
		for (int i = 0; i < aSize; i++) {
			for (int j = 0; j < aSize; j++) {
				aSum = 0;
				for (int k = 0; k < aLength; k++) {
					aSum = aSum + aTempColSquare[k] * iInput[k][i] * iInput[k][j];
				}
				aTempCM[i][j] = iScale * aSum / aLength;
			}
		}

		aTempCM[iIm][iJm] = aTempCM[iIm][iJm] - iScale;
		aTempCM[iJm][iIm] = aTempCM[iJm][iIm] - iScale;
		if (iIm == iJm) {
			for (int i = 0; i < aSize; i++) {
				aTempCM[i][i] = aTempCM[i][i] - iScale;
			}
		}

		return aTempCM;
	}

	/**
	 * USED IN JADE
	 * 
	 * @param size
	 * @return
	 */
	public double findGivensTheta(double[][] iA) {

		int aRow = iA.length;
		int aCol = iA[0].length;

		double aTon = 0;
		double aToff = 0;
		for (int i = 0; i < aRow; i++) {
			for (int j = 0; j < aRow; j++) {
				if (i == j) {
					for (int k = 0; k < aCol; k++) {
						aTon = aTon + Math.pow(-1, i) * iA[i][k] * iA[j][k];
					}
				} else {
					for (int k = 0; k < aCol; k++) {
						aToff = aToff + iA[i][k] * iA[j][k];
					}
				}
			}
		}
		return 0.5 * Math.atan2(aToff, aTon + Math.sqrt(aTon * aTon + aToff * aToff));

	}

	/**
	 * Create Identity matrix of size
	 * 
	 * @param iSize
	 * @return
	 */
	public double[][] identity(int iSize) {

		double[][] aIdentity = new double[iSize][iSize];
		for (int i = 0; i < iSize; i++) {
			aIdentity[i][i] = 1.0;
		}

		return aIdentity;
	}

	/**
	 * Element wise multiplication of A.*B
	 * 
	 * @param iA
	 * @param iB
	 * @return
	 * @throws Exception
	 */
	public double[][] ElementWiseMult(double[][] iA, double[][] iB) throws Exception {
		int aColA = iA[0].length;
		int aRowA = iA.length;
		int aColB = iB[0].length;
		int aRowB = iB.length;
		double[][] aOut = new double[aRowA][aColA];
		if (aColA == aColB && aRowA == aRowB) // The dimensions of Matrix A and
												// B
												// must match
		{
			for (int i = 0; i < aRowA; i++) {
				for (int j = 0; j < aColA; j++) {
					aOut[i][j] = iA[i][j] * iB[i][j];
				}
			}
			return aOut;
		} else {
			throw new Exception("Enter matrices with valid dimension. ");
		}
	}

	/**
	 * Element wise divide of A./B
	 * 
	 * @param iA
	 * @param iB
	 * @return
	 * @throws Exception
	 */
	public double[][] ElementWiseDivide(double[][] iA, double[][] iB) throws Exception {
		int aColA = iA[0].length;
		int aRowA = iA.length;
		int aColB = iB[0].length;
		int aRowB = iB.length;
		double[][] aOut = new double[aRowA][aColA];
		if (aColA == aColB && aRowA == aRowB) // The dimensions of Matrix A and
												// B
												// must match
		{
			// System.out.println("The element wise mult matrix is: ");
			for (int i = 0; i < aRowA; i++) {
				for (int j = 0; j < aColA; j++) {
					aOut[i][j] = iA[i][j] / iB[i][j];
					// System.out.print(dot[i][j]+" ");
				}
				// System.out.println();
			}
			return aOut;
		} else {
			throw new Exception("Enter matrices with valid dimension. ");
		}
	}

	/**
	 * Find transpose of Matrix A
	 * 
	 * @param iA
	 * @return
	 */
	public double[][] transpose(double[][] iA) {
		int aRow = iA.length;
		int aCol = iA[0].length;
		double[][] aTranspose = new double[aCol][aRow];
		for (int i = 0; i < aRow; i++) {
			for (int j = 0; j < aCol; j++) {
				aTranspose[j][i] = iA[i][j];
			}
		}

		return aTranspose;
	}

	/**
	 * 
	 * @param iA
	 * @param iRowI
	 * @param iRowF
	 * @param iColI
	 * @param iColF
	 * @return
	 * @throws Exception
	 */
	public double[][] submatrix(double[][] iA, int iRowI, int iRowF, int iColI, int iColF) throws Exception {
		int aRow = iA.length;
		int aCol = iA[0].length;

		double[][] aSubMatrix = new double[(iRowF - iRowI) + 1][(iColF - iColI) + 1];
		// size of the sub matrix
		if (((iRowI >= 0 && iRowI <= iRowF) && (iRowF < aRow && iRowF >= iRowI))
				&& ((iColI >= 0 && iColI <= iColF) && (iColF < aCol && iColF >= iColI)))
		// The boundary conditions must lie within the size of the Matrix
		{
			for (int i = 0; i < iRowF - iRowI + 1; i++) {
				for (int j = 0; j < iColF - iColI + 1; j++) {
					aSubMatrix[i][j] = iA[iRowI + i][iColI + j];
				}
			}
			return aSubMatrix;
		} else {
			throw new Exception("Enter valid matrix dimention");
		}

	}

	/**
	 * 
	 * @param iA
	 * @param iB
	 * @return
	 */
	public double[][] verticalConcat(double[][] iA, double[][] iB) throws Exception {

		if (iA.length > 0 && iB.length == 0) {
			return iA;
		} else if (iA.length == 0 && iB.length > 0) {
			return iB;
		}
		if ((iA.length == 0 && iB.length == 0)) {
			throw new Exception("Enter non empty matrix");
		}
		if (iA[0].length != iB[0].length) {
			throw new Exception("Colums of both matrices must be same.");
		} else {
			double[][] iC = new double[iA.length + iB.length][iA[0].length];
			for (int i = 0; i < iA.length + iB.length; i++) {
				for (int j = 0; j < iA[0].length; j++) {
					if (i < iA.length) {
						iC[i][j] = iA[i][j];
					} else {
						iC[i][j] = iB[i - iA.length][j];
					}
				}
			}

			return iC;
		}

	}

	/**
	 * USED IN IMPULSE FILTER
	 *
	 */
	public int[] findingPositiveElementsIndex(double inp[]) {

		int output[] = new int[inp.length];
		int t = 0;
		int count = 0;

		for (int y = 0; y < inp.length; y++) {
			if (inp[y] > 0) {
				output[t] = y;
				++t;
				if (y == 0) {
					count = count + 1;
				}
			}
		}

		if (count > 0) {
			for (int i = 1; i < output.length; i++) {
				if (output[i] > 0) {
					count = count + 1;
				} else {
					break;
				}
			}
		} else if (count == 0) {
			for (int i = 0; i < output.length; i++) {
				if (output[i] > 0) {
					count = count + 1;
				} else {
					break;
				}
			}
		}
		if (count > 0) {
			int[] out = new int[count];
			for (int i = 0; i < count; i++) {
				out[i] = output[i];
			}
			return out;
		} else {
			return null;
		}

	}

	// FOR SVD functions

	public double[][] r_QRtransR(double beta, double[] rightHouseholderVector, double[][] A, double[] qrTempRow,
			int row, int col, int k) {

		// compute yr = beta*A*ur

		for (int l = 0; l < row; l++) {
			qrTempRow[l] = 0;
			for (int j = k + 1; j < col; j++) {
				qrTempRow[l] = qrTempRow[l] + beta * rightHouseholderVector[j] * A[l][j];
			}
		}
		// compute A = (A - yr*ur);

		for (int i = k; i < row; i++) {
			for (int j = k + 1; j < col; j++) {
				A[i][j] = A[i][j] - rightHouseholderVector[j] * qrTempRow[i];
			}
		}
		return A;
	}

	public double[][] q_QRtransR(double beta, double[] rightHouseholderVector, double[][] QR, double[] qrTempCol,
			int col, int k) {
		// compute yqr^T = beta * ur^T * Qtilde

		for (int l = 0; l < col; l++) {
			qrTempCol[l] = 0;
			for (int j = k + 1; j < col; j++) {
				qrTempCol[l] = qrTempCol[l] + beta * rightHouseholderVector[j] * QR[j][l];
			}
		}

		// compute Qtilde = Qtilde - ur*yqr^T

		for (int i = 0; i < col; i++) {
			for (int j = k + 1; j < col; j++) {
				QR[j][i] = QR[j][i] - rightHouseholderVector[j] * qrTempCol[i];
			}
		}
		return QR;
	}

	/**
	 * Find R matrix of QR transform.
	 * 
	 * @param iBeta
	 * @param iLeftHouseholderVector
	 * @param iA
	 * @param iQRtempCol
	 * @param iRow
	 * @param iCol
	 * @param iIter
	 * @return
	 */
	public double[][] r_QRtransL(double iBeta, double[] iLeftHouseholderVector, double[][] iA, double[] iQRtempCol,
			int iRow, int iCol, int iIter) {

		// compute y^T = u^T * A
		for (int l = 0; l < iCol; l++) {
			iQRtempCol[l] = 0;
			for (int j = iIter; j < iRow; j++) {
				iQRtempCol[l] = iQRtempCol[l] + iBeta * iLeftHouseholderVector[j] * iA[j][l];
			}
		}

		// compute A = (A - u*y^T);

		for (int i = iIter; i < iCol; i++) {
			for (int j = iIter; j < iRow; j++) {
				iA[j][i] = iA[j][i] - iLeftHouseholderVector[j] * iQRtempCol[i];
			}
		}

		return iA;
	}

	/**
	 * Find Q matrix of QR transform
	 * 
	 * @param iBeta
	 * @param iLeftHouseholderVector
	 * @param iLeftQR
	 * @param iQRtempRow
	 * @param iRow
	 * @param iIter
	 * @return
	 */
	public double[][] q_QRtransL(double iBeta, double[] iLeftHouseholderVector, double[][] iLeftQR, double[] iQRtempRow,
			int iRow, int iIter) {
		// compute yq = beta*Q*u
		for (int l = 0; l < iRow; l++) {
			iQRtempRow[l] = 0;
			for (int j = iIter; j < iRow; j++) {
				iQRtempRow[l] = iQRtempRow[l] + iBeta * iLeftHouseholderVector[j] * iLeftQR[j][l];
			}
		}
		// compute Q = Q - yql * u^T;

		for (int i = 0; i < iRow; i++) {
			for (int j = iIter; j < iRow; j++) {
				iLeftQR[j][i] = iLeftQR[j][i] - iQRtempRow[i] * iLeftHouseholderVector[j];
			}
		}

		return iLeftQR;
	}

	public double[][] givensL(double[][] B, int n, int k, double a, double b) {

		double r = Math.sqrt(a * a + b * b);
		double c = a / r;
		double s = -b / r;

		double S0, S1;

		for (int i = 0; i < n; i++) {
			S0 = B[k + 0][i];
			S1 = B[k + 1][i];

			B[k][i] = c * S0 - s * S1;
			B[k + 1][i] = s * S0 + c * S1;

		}

		return B;
	} // end givensL

	public double[][] givensR(double[][] B, int n, int k, double a, double b) {

		double r = Math.sqrt(a * a + b * b);
		double c = a / r;
		double s = -b / r;

		double S0, S1;
		for (int i = 0; i < n; i++) {
			S0 = B[i][k];
			S1 = B[i][k + 1];
			B[i][k] = c * S0 - s * S1; // check sign of s
			B[i][k + 1] = s * S0 + c * S1; // -ve in this or above line

		}
		return B;
	} // end givensL

	/**
	 * USED IN MQRS CANCEL
	 */

	public double[][] weightFunction(int nSamplesBeforeQRS, int nSamplesAfterQRS, int fs) {
		int nSamplesBefore1 = fs * 6 / 100;
		int nSamplesAfter1 = fs * 6 / 100;
		int nSamplesBefore2 = fs * 8 / 100;
		int nSamplesAfter2 = Math.min(fs * 2 / 100, (nSamplesAfterQRS - nSamplesAfter1));

		int iend1 = nSamplesBeforeQRS - nSamplesBefore1 - nSamplesBefore2;
		int iend2 = iend1 + nSamplesBefore2;
		int istart3 = iend2 + 1;
		int iend3 = iend2 + nSamplesBefore1 + nSamplesAfter1 + 1;
		int iend4 = iend3 + nSamplesAfter2;
		int istart5 = iend4 + 1;
		int iend5 = nSamplesBeforeQRS + nSamplesAfterQRS + 1;
		double wwg[][] = new double[iend5][1];
		int flag = 0;

		double constantValue = 0.2;
		double slopeValue = 0.8;
		for (int i = 0; i < iend1; i++) {
			wwg[i][0] = constantValue;
			flag = i;
		}
		int k = 0;
		while (flag < iend2 && k <= nSamplesBefore2) {
			flag = flag + 1;
			k = k + 1;
			wwg[flag][0] = constantValue + ((slopeValue * (k)) / nSamplesBefore2);
		}

		for (int i = istart3 - 1; i < iend3; i++) {
			wwg[i][0] = 1;
			flag = i;// 12
		}

		k = 1;
		while (flag < iend4 && k <= nSamplesAfter2) {
			flag = flag + 1;
			wwg[flag][0] = (1 - ((slopeValue * (k)) / nSamplesAfter2));
			k = k + 1;
		}

		for (int i = istart5 - 1; i < iend5; i++) {
			wwg[i][0] = constantValue;
			flag = i;
		}

		return wwg;

	}

	/**
	 * 
	 * @param iInputArr
	 *            -- 1xN array
	 * @param iRow
	 *            -- No of times to replicate.
	 * @return
	 */
	public double[][] repmat(double[][] iInputArr, int iRow) {
		// replicate a row vector to many rows.
		double[][] aExt = new double[iRow][iInputArr[0].length];
		for (int i = 0; i < iRow; i++) {
			for (int j = 0; j < iInputArr[0].length; j++) {
				aExt[i][j] = iInputArr[0][j];
			}
		}
		return aExt;
	}

	/**
	 * Find mean of iInpArr between iPercI and iPercF
	 * 
	 * @param iInpArr
	 * @param iPercI
	 * @param iPercF
	 * @return
	 */
	public double findMeanBetweenDistributionTails(double[] iInpArr, int iPercI, int iPercF) {
		int aLen = iInpArr.length;
		double[] aArr = new double[aLen];
		for (int i = 0; i < aLen; i++) {
			aArr[i] = iInpArr[i];
		}
		Arrays.sort(aArr);
		int aInitIndex = 1 + aLen * iPercI / 100;
		int aFinalIndex = aLen - aLen * iPercF / 100;
		double aSum = 0;
		for (int i = aInitIndex - 1; i < aFinalIndex; i++) {
			aSum = aSum + aArr[i];
		}

		return aSum / (aFinalIndex - aInitIndex + 1);
	}

	/**
	 * FILTERING CODES
	 */

	public void filterLoHi(double[] iChannel, double[] iA, double[] iB, double iZ) {

		int aLength = iChannel.length;

		int aLengthExt = 2 * Constants.FILTER_NFACT2 + aLength;
		double aMirrorExtension[] = new double[aLengthExt];

		mirrorInput(iChannel, aMirrorExtension, Constants.FILTER_NFACT2);
		filter2(aMirrorExtension, iB, iA, aMirrorExtension[0] * iZ);
		reverse(aMirrorExtension, aLengthExt);
		filter2(aMirrorExtension, iB, iA, aMirrorExtension[0] * iZ);
		reverse(aMirrorExtension, aLengthExt);

		for (int i = 0; i < aLength; i++) {
			iChannel[i] = aMirrorExtension[Constants.FILTER_NFACT2 + i];
		}

	}

	public void filterNotch(double[] iChannel, double[] iA, double[] iB, double iZ1, double iZ2) {
		int aLength = iChannel.length;
		int aLengthExt = 2 * Constants.FILTER_NFACT3 + aLength;
		double[] aMirrorExtension = new double[aLengthExt];

		mirrorInput(iChannel, aMirrorExtension, Constants.FILTER_NFACT3);
		aMirrorExtension = filter3N(aMirrorExtension, iB, iA, aMirrorExtension[0] * iZ1, aMirrorExtension[0] * iZ2);
		reverse(aMirrorExtension, aLengthExt);
		aMirrorExtension = filter3N(aMirrorExtension, iB, iA, aMirrorExtension[0] * iZ1, aMirrorExtension[0] * iZ2);
		reverse(aMirrorExtension, aLengthExt);

		for (int i = 0; i < aLength; i++) {
			iChannel[i] = aMirrorExtension[Constants.FILTER_NFACT3 + i];
		}

	}

	/**
	 * filtering of signals with filters of length 2 and one delay element
	 */
	public void filter2(double[] iInput, double[] iB, double[] iA, double iZ) {
		int aLength = iInput.length;
		double aTempI, aTempF;
		aTempI = iInput[0];

		iInput[0] = iB[0] * iInput[0] + iZ;

		for (int i = 1; i < aLength; i++) {
			aTempF = iInput[i];
			iInput[i] = iB[0] * iInput[i] + iB[1] * aTempI - iA[1] * iInput[i - 1];
			aTempI = aTempF;
		}
	}

	/**
	 * filtering of signals with filters of length 3 and different delays used
	 * for notch filter
	 */
	public double[] filter3N(double[] iInput, double[] iB, double[] iA, double iDelayN1, double iDelayN2) {

		int aLength = iInput.length;

		// filter operation
		double[] aFilteredOutput = new double[aLength];
		aFilteredOutput[0] = iB[0] * iInput[0] + iDelayN1;
		aFilteredOutput[1] = iB[0] * iInput[1] + iB[1] * iInput[0] - iA[1] * aFilteredOutput[0] + iDelayN2;

		for (int n = 2; n < aLength; n++) {
			aFilteredOutput[n] = (iB[0] * iInput[n]) + (iB[1] * iInput[n - 1]) + (iB[2] * iInput[n - 2])
					- (iA[1] * aFilteredOutput[n - 1]) - (iA[2] * aFilteredOutput[n - 2]);
		}

		return aFilteredOutput;

	}

	public void mirrorInput(double[] iInput, double[] iMirrorExtension, int iNfact) {

		int aLength = iInput.length;
		int aNoIteration = aLength + 2 * (iNfact);
		int aNfactEnd = aLength + (iNfact - 1);
		int aNoShift = 2 * aLength + iNfact - 2;
		for (int i = 0; i < aNoIteration; i++) {
			if (i < iNfact) {
				iMirrorExtension[i] = 2 * iInput[0] - iInput[iNfact - i];
			} else if (i > aNfactEnd) {
				iMirrorExtension[i] = 2 * iInput[aLength - 1] - iInput[aNoShift - i];
			} else {
				iMirrorExtension[i] = iInput[i - iNfact];
			}
		}

	}

	public void reverse(double[] iInput, int iLength) {

		double aTemp = 0;
		for (int i = 0; i < iLength / 2; i++) {
			aTemp = iInput[i];
			iInput[i] = iInput[iLength - i - 1];
			iInput[iLength - i - 1] = aTemp;
		}

	}

	/**
	 * QRS DETECTION fucntions
	 */

	/**
	 * Finds derivative and then squares the output
	 * 
	 * @param iInput
	 * @param iFilter
	 */

	public void convolutionQRSDetection(double[] iInput, double[] iFilter) {
		int aLengthInput = iInput.length;
		int aLengthFilter = iFilter.length;
		int aLengthExtension = aLengthInput + aLengthFilter - 1;

		double aExtension[] = new double[aLengthExtension];

		for (int i = 0; i < aLengthExtension; i++) {
			if (i >= aLengthFilter / 2 && i < aLengthFilter / 2 + aLengthInput)
				aExtension[i] = iInput[i - aLengthFilter / 2];
			else
				aExtension[i] = 0;
		}

		double aSum;
		for (int i = 0; i < aLengthInput; i++) {
			aSum = 0;
			for (int j = 0; j < aLengthFilter; j++) {
				aSum = aSum + iFilter[aLengthFilter - 1 - j] * aExtension[j + i] / Constants.QRS_DERIVATIVE_SCALE;
			}
			iInput[i] = aSum * aSum;
		}
	}

	/**
	 * Find the threshold for integrator.
	 * 
	 * @param iIntegrator
	 * @return
	 */
	public double setIntegratorThreshold(double[] iIntegrator, double scale) {
		int aLength = iIntegrator.length;
		double aIntegratorSort[] = new double[aLength];

		for (int i = 0; i < aLength; i++) {
			aIntegratorSort[i] = iIntegrator[i];
		}
		Arrays.sort(aIntegratorSort);

		int aMaxLoc = (int) Math.ceil(aLength * Constants.QRS_INTEGRTOR_MAX);
		int aMinLoc = (int) Math.ceil(aLength * Constants.QRS_INTEGRATOR_MIN);

		double aMaxVal = aIntegratorSort[aMaxLoc];
		double aMinVal = aIntegratorSort[aMinLoc];

		double aThreshold = (aMaxVal - aMinVal) / scale;

		for (int i = 0; i < aLength; i++) {
			if (iIntegrator[i] < aThreshold) {
				iIntegrator[i] = 0;
			}
		}

		return aThreshold;
	}

	/**
	 * Peak detection for array with minimum difference of delta.
	 * 
	 * @param iInput
	 * @param iDelta
	 * @return If no peak detected, wil return an EMPTY Array.
	 */
	public int[] peakDetection(double[] iInput, double iDelta) {
		double aMinimum = 100000, aMaximum = -100000;
		double aMaxPos = 0;
		double aLookformax = 1;
		double aThisVar = 0;
		int aCountMax = 0;
		int aCountMin = 0;
		double[] aPeakLoc = new double[iInput.length];

		for (int ind = 0; ind < iInput.length; ind++) {
			aThisVar = iInput[ind];
			// check max and min are greater and lesser to x[y][0] respectively
			if (aThisVar > aMaximum) {
				aMaximum = aThisVar;
				aMaxPos = ind;
			}
			if (aThisVar < aMinimum) {
				aMinimum = aThisVar;
			}

			if (aLookformax == 1) {
				if (aThisVar < (aMaximum - iDelta)) {
					aPeakLoc[aCountMax] = aMaxPos; // first col has positions
					aCountMax = aCountMax + 1; // next row
					aMinimum = aThisVar;
					aLookformax = 0;
				}
			} else if (aLookformax == 0) {
				if (aThisVar > (aMinimum + iDelta)) {
					aCountMin = aCountMin + 1;
					aMaximum = aThisVar;
					aMaxPos = ind;
					aLookformax = 1;
				}
			}
		}

		int aCount = 0;
		if (aPeakLoc[0] >= 0 && aPeakLoc[1] > 0) {
			aCount = aCount + 1;
		}
		for (int i = 1; i < aPeakLoc.length; i++) {
			if (aPeakLoc[i] > 0) {
				aCount = aCount + 1;
			} else {
				break;
			}
		}

		int[] aPeakLocFinal = new int[aCount];
		for (int i = 0; i < aCount; i++) {
			aPeakLocFinal[i] = (int) (Math.floor(aPeakLoc[i])); // in case , we
																// get
																// decimal.
		}
		return aPeakLocFinal;
	}

	/**
	 * Select the best channel out of the 4 possible QRS locations
	 * 
	 * @param iQRS1
	 * @param iQRS2
	 * @param iQRS3
	 * @param iQRS4
	 * @param iVarTh
	 * @param iRRlowTh
	 * @param RRhighTh
	 * @return
	 */
	public Object[] channelSelection(int[] iQRS1, int[] iQRS2, int[] iQRS3, int[] iQRS4, int iVarTh) {
		/**
		 * Channel selection part
		 */

		int aLen1 = iQRS1.length;
		int aLen2 = iQRS2.length;
		int aLen3 = iQRS3.length;
		int aLen4 = iQRS4.length;

		double aInd1 = 0;
		double aInd2 = 0;
		double aInd3 = 0;
		double aInd4 = 0;
		// to get the start index in each channel
		int aStartInd1 = -1;
		int aStartInd2 = -1;
		int aStartInd3 = -1;
		int aStartInd4 = -1;
		if (aLen1 > 3) {
			int aNIt = aLen1 - 3;
			double aVar1[] = new double[aNIt];
			double t1, t2, t3, aMean;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS1[i + 1] - iQRS1[i];
				t2 = iQRS1[i + 2] - iQRS1[i + 1];
				t3 = iQRS1[i + 3] - iQRS1[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar1[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar1[i] < iVarTh) {
					aInd1 = aInd1 + 1;
					if (aStartInd1 == -1) {
						aStartInd1 = i;
					}
				}
			}
			aInd1 = aInd1 / aNIt;
		}

		if (aLen2 > 3) {
			int aNIt = aLen2 - 3;
			double aVar2[] = new double[aNIt];
			double t1, t2, t3, aMean;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS2[i + 1] - iQRS2[i];
				t2 = iQRS2[i + 2] - iQRS2[i + 1];
				t3 = iQRS2[i + 3] - iQRS2[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar2[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar2[i] < iVarTh) {
					aInd2 = aInd2 + 1;
					if (aStartInd2 == -1) {
						aStartInd2 = i;
					}
				}
			}
			aInd2 = aInd2 / aNIt;
		}

		if (aLen3 > 3) {
			int aNIt = aLen3 - 3;
			double aVar3[] = new double[aNIt];
			double t1, t2, t3, aMean;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS3[i + 1] - iQRS3[i];
				t2 = iQRS3[i + 2] - iQRS3[i + 1];
				t3 = iQRS3[i + 3] - iQRS3[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar3[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar3[i] < iVarTh) {
					aInd3 = aInd3 + 1;
					if (aStartInd3 == -1) {
						aStartInd3 = i;
					}
				}
			}
			aInd3 = aInd3 / aNIt;
		}

		if (aLen4 > 3) {
			int nIt = aLen4 - 3;
			double var4[] = new double[nIt];
			double t1, t2, t3, mean;
			for (int i = 0; i < nIt; i++) {
				t1 = iQRS4[i + 1] - iQRS4[i];
				t2 = iQRS4[i + 2] - iQRS4[i + 1];
				t3 = iQRS4[i + 3] - iQRS4[i + 2];

				mean = (t1 + t2 + t3) / 3;

				var4[i] = Math
						.sqrt(((t1 - mean) * (t1 - mean) + (t2 - mean) * (t2 - mean) + (t3 - mean) * (t3 - mean)) / 2);
				if (var4[i] < iVarTh) {
					aInd4 = aInd4 + 1;
					if (aStartInd4 == -1) {
						aStartInd4 = i;
					}
				}
			}
			aInd4 = aInd4 / nIt;
		}
		// FInd the maximum value of 'ind'
		// Have to add mean RR value also to this computation to get better
		// estimate of 'ch'
		double ind = aInd1;
		int length_Final = aLen1;
		int ch = 1;
		double RRmean = 0;
		for (int i = 0; i < aLen1 - 1; i++) {
			RRmean = RRmean + iQRS1[i + 1] - iQRS1[i];
		}
		RRmean = RRmean / (aLen1 - 1);
		if (aInd2 == ind) {
			double RRmean2 = 0;
			for (int i = 0; i < aLen2 - 1; i++) {
				RRmean2 = RRmean2 + iQRS2[i + 1] - iQRS2[i];
			}
			RRmean2 = RRmean2 / (aLen2 - 1);
			if (RRmean < RRmean2) {
				ind = aInd2;
				ch = 2;
				length_Final = aLen2;
				RRmean = RRmean2;
			}
		} else if (aInd2 > ind) {
			ind = aInd2;
			ch = 2;
			length_Final = aLen2;
			double RRmean2 = 0;
			for (int i = 0; i < aLen2 - 1; i++) {
				RRmean2 = RRmean2 + iQRS2[i + 1] - iQRS2[i];
			}
			RRmean = RRmean2 / (aLen2 - 1);
		}
		if (aInd3 == ind) {
			double RRmean3 = 0;
			for (int i = 0; i < aLen3 - 1; i++) {
				RRmean3 = RRmean3 + iQRS3[i + 1] - iQRS3[i];
			}
			RRmean3 = RRmean3 / (aLen3 - 1);
			if (RRmean < RRmean3) {
				ind = aInd3;
				ch = 3;
				length_Final = aLen3;
				RRmean = RRmean3;
			}
		} else if (aInd3 > ind) {
			ind = aInd3;
			ch = 3;
			length_Final = aLen3;
			double RRmean3 = 0;
			for (int i = 0; i < aLen3 - 1; i++) {
				RRmean3 = RRmean3 + iQRS3[i + 1] - iQRS3[i];
			}
			RRmean = RRmean3 / (aLen3 - 1);
		}
		if (aInd4 > ind) {
			double RRmean4 = 0;
			for (int i = 0; i < aLen4 - 1; i++) {
				RRmean4 = RRmean4 + iQRS4[i + 1] - iQRS4[i];
			}
			RRmean4 = RRmean4 / (aLen4 - 1);
			if (RRmean < RRmean4) {
				ind = aInd4;
				ch = 4;
				length_Final = aLen4;
				RRmean = RRmean4;
			}
		} else if (aInd4 > ind) {
			ind = aInd4;
			ch = 4;
			length_Final = aLen4;
			double RRmean4 = 0;
			for (int i = 0; i < aLen4 - 1; i++) {
				RRmean4 = RRmean4 + iQRS4[i + 1] - iQRS4[i];
			}
			RRmean = RRmean4 / (aLen4 - 1);
		}
		/**
		 * Get the start Index and qrs values to find the final QRS.
		 */
		int[] qrs = new int[length_Final];
		int startIndex = -1;
		if (ch == 1) {
			startIndex = aStartInd1;

			for (int i = 0; i < length_Final; i++) {
				qrs[i] = iQRS1[i];
			}
		} else if (ch == 2) {
			startIndex = aStartInd2;
			for (int i = 0; i < length_Final; i++) {
				qrs[i] = iQRS2[i];
			}
		} else if (ch == 3) {
			startIndex = aStartInd3;
			for (int i = 0; i < length_Final; i++) {
				qrs[i] = iQRS3[i];
			}
		} else if (ch == 4) {
			startIndex = aStartInd4;
			for (int i = 0; i < length_Final; i++) {
				qrs[i] = iQRS4[i];
			}
		}

		return new Object[] { qrs, startIndex };
	}

	/**
	 * EDIT on FEB 19th, 2017. added low and high threshold for RR values.
	 * 
	 * @param iQRS1
	 * @param iQRS2
	 * @param iQRS3
	 * @param iQRS4
	 * @param iVarTh
	 * @param iRRlowTh
	 * @param RRhighTh
	 * @return Object[] { qrs, startIndex } :: Will return the best QRS array
	 *         for Maternal/Fetal. If no channel is selected, will return
	 *         concatinated array for all QRS
	 * 
	 *         Edited on Feb 27th, Instead of choosing minimum RR value, choose
	 *         minimum Variance.
	 */
	public Object[] channelSelection_Feb17(int[] iQRS1, int[] iQRS2, int[] iQRS3, int[] iQRS4, int iVarTh, int iRRlowTh,
			int iRRhighTh) {
		/**
		 * Channel selection part
		 */
		
		int aLen1 = iQRS1.length;
		int aLen2 = iQRS2.length;
		int aLen3 = iQRS3.length;
		int aLen4 = iQRS4.length;

		double aInd1 = 0;
		double aInd2 = 0;
		double aInd3 = 0;
		double aInd4 = 0;
		// to get the start index in each channel
		int aStartInd1 = -1;
		int aStartInd2 = -1;
		int aStartInd3 = -1;
		int aStartInd4 = -1;
		// RR mean for each channel
		double aRRmean1 = 0;
		double aRRmean2 = 0;
		double aRRmean3 = 0;
		double aRRmean4 = 0;

		if (aLen1 > 3) {
			int aNIt = aLen1 - 3;
			double aVar1[] = new double[aNIt];
			double t1, t2, t3, aMean, aRRTemp, aVarMin;
			aVarMin = 1000;
			double counter = 0;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS1[i + 1] - iQRS1[i];
				t2 = iQRS1[i + 2] - iQRS1[i + 1];
				t3 = iQRS1[i + 3] - iQRS1[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar1[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar1[i] < iVarTh) {
					aRRTemp = iQRS1[i + 1] - iQRS1[i];
					if (aRRTemp > iRRlowTh && aRRTemp < iRRhighTh) {
						aRRmean1 = aRRmean1 + aRRTemp;
						counter = counter + 1;
						if (aVar1[i] < aVarMin) {
							aVarMin = aVar1[i];
							aStartInd1 = i;
						}
					}
				}
			}
			aRRmean1 = aRRmean1 / counter;
			aInd1 = counter / aNIt;
		}

		if (aLen2 > 3) {
			int aNIt = aLen2 - 3;
			double aVar2[] = new double[aNIt];
			double t1, t2, t3, aMean, aRRTemp, aVarMin;
			double counter = 0;
			aVarMin = 1000;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS2[i + 1] - iQRS2[i];
				t2 = iQRS2[i + 2] - iQRS2[i + 1];
				t3 = iQRS2[i + 3] - iQRS2[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar2[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar2[i] < iVarTh) {
					aRRTemp = iQRS2[i + 1] - iQRS2[i];
					if (aRRTemp > iRRlowTh && aRRTemp < iRRhighTh) {
						aRRmean2 = aRRmean2 + aRRTemp;
						counter = counter + 1;
						if (aVar2[i] < aVarMin) {
							aVarMin = aVar2[i];
							aStartInd2 = i;
						}
					}
				}
			}
			aRRmean2 = aRRmean2 / counter;

			aInd2 = counter / aNIt;
		}

		if (aLen3 > 3) {
			int aNIt = aLen3 - 3;
			double aVar3[] = new double[aNIt];
			double t1, t2, t3, aMean, aRRTemp, aVarMin;
			double counter = 0;
			aVarMin = 1000;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS3[i + 1] - iQRS3[i];
				t2 = iQRS3[i + 2] - iQRS3[i + 1];
				t3 = iQRS3[i + 3] - iQRS3[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar3[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar3[i] < iVarTh) {
					aRRTemp = iQRS3[i + 1] - iQRS3[i];
					if (aRRTemp > iRRlowTh && aRRTemp < iRRhighTh) {
						aRRmean3 = aRRmean3 + 1;
						counter = counter + 1;
						if (aVar3[i] < aVarMin) {
							aVarMin = aVar3[i];
							aStartInd3 = i;
						}
					}
				}
			}
			aRRmean3 = aRRmean3 / counter;
			aInd3 = counter / aNIt;
		}

		if (aLen4 > 3) {
			int aNIt = aLen4 - 3;
			double aVar4[] = new double[aNIt];
			double t1, t2, t3, aMean, aRRTemp, aVarMin;
			double counter = 0;
			aVarMin = 1000;
			for (int i = 0; i < aNIt; i++) {
				t1 = iQRS4[i + 1] - iQRS4[i];
				t2 = iQRS4[i + 2] - iQRS4[i + 1];
				t3 = iQRS4[i + 3] - iQRS4[i + 2];

				aMean = (t1 + t2 + t3) / 3;

				aVar4[i] = Math.sqrt(
						((t1 - aMean) * (t1 - aMean) + (t2 - aMean) * (t2 - aMean) + (t3 - aMean) * (t3 - aMean)) / 2);
				if (aVar4[i] < iVarTh) {
					aRRTemp = iQRS4[i + 1] - iQRS4[i];
					if (aRRTemp > iRRlowTh && aRRTemp < iRRhighTh) {
						aRRmean4 = aRRmean4 + 1;
						counter = counter + 1;
						if (aVar4[i] < aVarMin) {
							aVarMin = aVar4[i];
							aStartInd4 = i;
						}
					}
				}
			}
			aRRmean4 = aRRmean4 / counter;
			aInd4 = counter / aNIt;
		}
		// FInd the maximum value of 'ind'
		// Have to add mean RR value also to this computation to get better
		// estimate of 'ch'

		if (aInd1 == 0 && aInd2 == 0 && aInd3 == 0 && aInd4 == 0) {
			int qrs[] = new int[aLen1 + aLen2 + aLen3 + aLen4];
			for (int i = 0; i < aLen1; i++) {
				qrs[i] = iQRS1[i];
			}
			int shift = aLen1;
			for (int i = 0; i < aLen2; i++) {
				qrs[i + shift] = iQRS2[i];
			}
			shift = shift + aLen2;
			for (int i = 0; i < aLen3; i++) {
				qrs[i + shift] = iQRS3[i];
			}
			shift = shift + aLen3;
			for (int i = 0; i < aLen4; i++) {
				qrs[i + shift] = iQRS4[i];
			}
			Arrays.sort(qrs);
			return new Object[] { qrs, -1 };
		} else {
			double ind = aInd1;
			int length_Final = aLen1;
			int ch = 1;
			double RRmean = 0;
			for (int i = 0; i < aLen1 - 1; i++) {
				RRmean = RRmean + iQRS1[i + 1] - iQRS1[i];
			}
			RRmean = RRmean / (aLen1 - 1);
			if (aInd2 == ind) {
				double RRmean2 = 0;
				for (int i = 0; i < aLen2 - 1; i++) {
					RRmean2 = RRmean2 + iQRS2[i + 1] - iQRS2[i];
				}
				RRmean2 = RRmean2 / (aLen2 - 1);
				if (RRmean < RRmean2) {
					ind = aInd2;
					ch = 2;
					length_Final = aLen2;
					RRmean = RRmean2;
				}
			} else if (aInd2 > ind) {
				ind = aInd2;
				ch = 2;
				length_Final = aLen2;
				double RRmean2 = 0;
				for (int i = 0; i < aLen2 - 1; i++) {
					RRmean2 = RRmean2 + iQRS2[i + 1] - iQRS2[i];
				}
				RRmean = RRmean2 / (aLen2 - 1);
			}
			if (aInd3 == ind) {
				double RRmean3 = 0;
				for (int i = 0; i < aLen3 - 1; i++) {
					RRmean3 = RRmean3 + iQRS3[i + 1] - iQRS3[i];
				}
				RRmean3 = RRmean3 / (aLen3 - 1);
				if (RRmean < RRmean3) {
					ind = aInd3;
					ch = 3;
					length_Final = aLen3;
					RRmean = RRmean3;
				}
			} else if (aInd3 > ind) {
				ind = aInd3;
				ch = 3;
				length_Final = aLen3;
				double RRmean3 = 0;
				for (int i = 0; i < aLen3 - 1; i++) {
					RRmean3 = RRmean3 + iQRS3[i + 1] - iQRS3[i];
				}
				RRmean = RRmean3 / (aLen3 - 1);
			}
			if (aInd4 > ind) {
				double RRmean4 = 0;
				for (int i = 0; i < aLen4 - 1; i++) {
					RRmean4 = RRmean4 + iQRS4[i + 1] - iQRS4[i];
				}
				RRmean4 = RRmean4 / (aLen4 - 1);
				if (RRmean < RRmean4) {
					ind = aInd4;
					ch = 4;
					length_Final = aLen4;
					RRmean = RRmean4;
				}
			} else if (aInd4 > ind) {
				ind = aInd4;
				ch = 4;
				length_Final = aLen4;
				double RRmean4 = 0;
				for (int i = 0; i < aLen4 - 1; i++) {
					RRmean4 = RRmean4 + iQRS4[i + 1] - iQRS4[i];
				}
				RRmean = RRmean4 / (aLen4 - 1);
			}
			/**
			 * Get the start Index and qrs values to find the final QRS.
			 */
			int[] qrs = new int[length_Final];
			int startIndex = -1;
			if (ch == 1) {
				startIndex = aStartInd1;

				for (int i = 0; i < length_Final; i++) {
					qrs[i] = iQRS1[i];
				}
			} else if (ch == 2) {
				startIndex = aStartInd2;
				for (int i = 0; i < length_Final; i++) {
					qrs[i] = iQRS2[i];
				}
			} else if (ch == 3) {
				startIndex = aStartInd3;
				for (int i = 0; i < length_Final; i++) {
					qrs[i] = iQRS3[i];
				}
			} else if (ch == 4) {
				startIndex = aStartInd4;
				for (int i = 0; i < length_Final; i++) {
					qrs[i] = iQRS4[i];
				}
			}

			return new Object[] { qrs, startIndex };
		}
	}

}// close class
