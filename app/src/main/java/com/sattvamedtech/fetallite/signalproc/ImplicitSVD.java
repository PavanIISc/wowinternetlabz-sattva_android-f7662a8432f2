package com.sattvamedtech.fetallite.signalproc;
//package hb;
import java.util.Arrays;

public class ImplicitSVD {
	MatrixFunctions mMatrixFunctions = new MatrixFunctions();

	public Object[] impcitsvd(double[][] iA) throws Exception {

		int aRow = iA.length;
		int aCol = iA[0].length;

		// Initialize sigma, beta, U, y
		double aNorm, aBeta;
		double[] aLeftHouseholderVector = new double[aRow];

		// initialize Q;
		double[][] aLeftQR = new double[aRow][aRow];
		for (int i = 0; i < aRow; i++) {
			aLeftQR[i][i] = 1;
		}

		double[] aQRtempCol = new double[aCol];
		double[] aQRtempRow = new double[aRow];

		// initialize for right householder
		double[] aRightHouseholderVector = new double[aCol];
		double[][] aRightQR = new double[aCol][aCol];

		for (int i = 0; i < aCol; i++) {
			aRightQR[i][i] = 1;
		}
		// initialize R
		double[][] aBiDiagonal = new double[aCol][aCol];

		/**
		 * 
		 * Aimplicit = QL(1:col,:)'*R*QR
		 * 
		 */
		// Start Bidiagonalization
		for (int k = 0; k < aCol; k++) {
			// start left householder

			aNorm = 0;
			for (int i = k; i < aRow; i++) {
				aNorm = aNorm + iA[i][k] * iA[i][k];
			}
			aNorm = Math.sqrt(aNorm);

			aBeta = 1 / (aNorm * (aNorm + Math.abs(iA[k][k])));

			aLeftHouseholderVector[k] = (iA[k][k] / Math.abs(iA[k][k])) * (aNorm + Math.abs(iA[k][k]));

			for (int i = k + 1; i < aRow; i++) {
				aLeftHouseholderVector[i] = iA[i][k];
			}

			iA = mMatrixFunctions.r_QRtransL(aBeta, aLeftHouseholderVector, iA, aQRtempCol, aRow, aCol, k);

			aLeftQR = mMatrixFunctions.q_QRtransL(aBeta, aLeftHouseholderVector, aLeftQR, aQRtempRow, aRow, k);

			// end left house holger

			// Start Right householder
			if (k < aCol - 2) {
				aNorm = 0;
				for (int i = k + 1; i < aCol; i++) {
					aNorm = aNorm + iA[k][i] * iA[k][i];
				}
				aNorm = Math.sqrt(aNorm);

				aBeta = 1 / (aNorm * (aNorm + Math.abs(iA[k][k + 1])));

				aRightHouseholderVector[k + 1] = (iA[k][k + 1] / Math.abs(iA[k][k + 1]))
						* (aNorm + Math.abs(iA[k][k + 1]));

				for (int i = k + 2; i < aCol; i++) {
					aRightHouseholderVector[i] = iA[k][i];
				}

				iA = mMatrixFunctions.r_QRtransR(aBeta, aRightHouseholderVector, iA, aQRtempRow, aRow, aCol, k);

				aRightQR = mMatrixFunctions.q_QRtransR(aBeta, aRightHouseholderVector, aRightQR, aQRtempCol, aCol, k);

			} // end right householder

		} // end Bidiagonalization

		// Extract biDiagonal from A

		for (int i = 0; i < aCol; i++) {
			for (int j = 0; j < aCol; j++) {
				aBiDiagonal[j][i] = iA[j][i];
			}
		}

		double[][] aLeftSingularMatrix = mMatrixFunctions.submatrix(mMatrixFunctions.transpose(aLeftQR), 0, aRow - 1, 0,
				aCol - 1);
		aRightQR = mMatrixFunctions.transpose(aRightQR);

		// DIagonalization of bidiagonal form
		int aInitialIndexCheck = 0;
		int aFinalIndexCheck = 0;
		double aEPS = -1;
		double[][] aBiDiagonalSubmatrix = new double[2][2];

		if (aEPS < 0) {
			aEPS = 1;
			while (aEPS + 1 > 1) {
				aEPS = aEPS * 0.5;
			}
			aEPS = aEPS * 64;
		}
		// Algorithm 1b. Golub-Reinsh SVD step 2;
		while (aFinalIndexCheck < aCol - 1) {

			for (int i = 0; i < aCol - 1; i++) {
				if (Math.abs(aBiDiagonal[i][i + 1]) <= aEPS
						* (Math.abs(aBiDiagonal[i][i]) + Math.abs(aBiDiagonal[i + 1][i + 1]))) {
					aBiDiagonal[i][i + 1] = 0;
				}
			}

			double aMaxDiagonal = 0;
			for (int i = 0; i < aCol; i++) {
				if (aMaxDiagonal < aBiDiagonal[i][i]) {
					aMaxDiagonal = aBiDiagonal[i][i];
				}
			} // end for

			while ((aInitialIndexCheck < aCol - 1)
					&& (Math.abs(aBiDiagonal[aInitialIndexCheck][aInitialIndexCheck + 1]) <= aEPS * aMaxDiagonal)) {
				aInitialIndexCheck = aInitialIndexCheck + 1;
			}
			if (aInitialIndexCheck == aCol - 1) {
				break;
			}
			int aInterIndexCheck = aInitialIndexCheck + 1;
			while (aInterIndexCheck < aCol
					&& Math.abs(aBiDiagonal[aInterIndexCheck - 1][aInterIndexCheck]) > aEPS * aMaxDiagonal) {
				aInterIndexCheck = aInterIndexCheck + 1;
			}
			aFinalIndexCheck = aCol - aInterIndexCheck;
			if (aFinalIndexCheck == aCol - 1) {
				break;
			}

			// Algo 1b - step : d

			// obtain B22 submatrix

			aBiDiagonalSubmatrix[0][0] = aBiDiagonal[(aCol - aFinalIndexCheck - 1) - 1][(aCol - aFinalIndexCheck - 1)
					- 1];
			aBiDiagonalSubmatrix[0][1] = aBiDiagonal[(aCol - aFinalIndexCheck - 1) - 1][(aCol - aFinalIndexCheck - 1)];
			aBiDiagonalSubmatrix[1][0] = aBiDiagonal[(aCol - aFinalIndexCheck - 1)][(aCol - aFinalIndexCheck - 1) - 1];
			aBiDiagonalSubmatrix[1][1] = aBiDiagonal[(aCol - aFinalIndexCheck - 1)][(aCol - aFinalIndexCheck - 1)];

			aBiDiagonalSubmatrix = mMatrixFunctions.multi(mMatrixFunctions.transpose(aBiDiagonalSubmatrix),
					aBiDiagonalSubmatrix);
			// Algo 1c Step 2 ends
			double aAlpha = 0;
			aBeta = 0;

			// calculate mu

			double b = -(aBiDiagonalSubmatrix[0][0] + aBiDiagonalSubmatrix[1][1]) / 2;
			double c = aBiDiagonalSubmatrix[0][0] * aBiDiagonalSubmatrix[1][1]
					- aBiDiagonalSubmatrix[0][1] * aBiDiagonalSubmatrix[1][0];
			double d = 0;

			if (b * b - c > 0) {
				d = Math.sqrt(b * b - c);
			} else {
				b = (aBiDiagonalSubmatrix[0][0] - aBiDiagonalSubmatrix[1][1]) / 2;
				c = -aBiDiagonalSubmatrix[0][1] * aBiDiagonalSubmatrix[1][0];
				if (b * b - c > 0) {
					d = Math.sqrt(b * b - c);
				}
			}

			double aLambda1 = -b + d;
			double aLambda2 = -b - d;

			double d1 = Math.abs(aLambda1 - aBiDiagonalSubmatrix[1][1]);

			double d2 = Math.abs(aLambda2 - aBiDiagonalSubmatrix[1][1]);

			double aMU = aLambda2;
			if (d1 < d2) {
				aMU = aLambda1;
			}
			// Algo 1c. Step 3 ends

			aAlpha = aBiDiagonal[aInitialIndexCheck][aInitialIndexCheck]
					* aBiDiagonal[aInitialIndexCheck][aInitialIndexCheck] - aMU;
			aBeta = aBiDiagonal[aInitialIndexCheck][aInitialIndexCheck]
					* aBiDiagonal[aInitialIndexCheck][aInitialIndexCheck + 1];
			// Algo 1c. Step 4 ends

			// end mu

			// start givens rotation
			// to obtain diagonal from bi-diagonal
			for (int k = aInitialIndexCheck; k <= aCol - aFinalIndexCheck - 2; k++) {

				aBiDiagonal = mMatrixFunctions.givensR(aBiDiagonal, aCol, k, aAlpha, aBeta);
				aRightQR = mMatrixFunctions.givensR(aRightQR, aCol, k, aAlpha, aBeta);

				aAlpha = aBiDiagonal[k][k];
				aBeta = aBiDiagonal[k + 1][k];

				aBiDiagonal = mMatrixFunctions.givensL(aBiDiagonal, aCol, k, aAlpha, aBeta);
				aLeftSingularMatrix = mMatrixFunctions.givensR(aLeftSingularMatrix, aRow, k, aAlpha, aBeta);
				if (k < aCol - aFinalIndexCheck - 2) {
					aAlpha = aBiDiagonal[k][k + 1];
					aBeta = aBiDiagonal[k][k + 2];
				}

			} // end givens rotation

		} // end while loop

		double[] aDiagonal = new double[aCol];

		double[] aSingularValues = new double[aCol];
		for (int i = 0; i < aCol; i++) {
			aDiagonal[i] = (aBiDiagonal[i][i]);
		}

		int[] aInd = ArrayUtils.argsort(aDiagonal);
		Arrays.sort(aDiagonal);
		for (int i = 0; i < aCol; i++) {
			aSingularValues[i] = aDiagonal[aCol - 1 - i];
		}

		double[][] aLeftSingular = new double[aRow][aCol];
		double[][] aRightSingular = new double[aCol][aCol];

		for (int i = 0; i < aInd.length; i++) {
			for (int j = 0; j < aRow; j++) {
				aLeftSingular[j][aInd.length - 1 - i] = aLeftSingularMatrix[j][aInd[i]];
			}
		}

		for (int i = 0; i < aInd.length; i++) {
			for (int j = 0; j < aCol; j++) {
				aRightSingular[j][aInd.length - 1 - i] = aRightQR[j][aInd[i]];
			}
		}

		return new Object[] { aSingularValues, aLeftSingular, aRightSingular };

	} // end main

} // end classs
