package com.sattvamedtech.fetallite.signalproc;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

//package hb;
public class qrsFSelectionQueue {
	int temp1 = 0;
	int temp2 = 0;
	int temp3 = 0;
	double diff = 0;
	double rrDiff0, rrDiff1, rrDiff2;

	public int[] qrs(int[] qrs, int startIndex, int[] qrsM) {

		/**
		 * Final Selection of QRS
		 */

		int decrement = 0;
		int iter = 0;
		int increment = 0;
		;
		int lengthQRS = qrs.length;

		LinkedList<Integer> qrsFinal = new LinkedList<Integer>();
		Queue<Integer> missF = new LinkedList<Integer>();
		Deque<Integer> missB = new LinkedList<Integer>();

		int forward_Iteration = 0;
		int countF = 0;
		int count0 = 0;

		int missFlag = 0;
		int countMiss = 0;
		int findFlag = 0;
		if (startIndex > -1) {
			forward_Iteration = startIndex;
			qrsFinal.addLast(qrs[forward_Iteration]);
			qrsFinal.addLast(qrs[forward_Iteration + 1]);

			forward_Iteration = forward_Iteration + 1;
			countF = 2;

			while (forward_Iteration < lengthQRS - 2) {
				count0 = countF;
				if (missFlag == 0) {
					if (countF < Constants.QRSF_INITIAL_RR_COUNT) {
						rrDiff0 = 0;
						for (int j = 0; j < countF - 1; j++) {
							rrDiff0 = rrDiff0 + (qrsFinal.get(j + 1) - qrsFinal.get(j));
						}
						rrDiff0 = rrDiff0 / (countF - 1);
					} else {
						rrDiff0 = 0;
						for (int j = countF - Constants.QRSF_INITIAL_RR_COUNT; j < countF - 1; j++) {
							rrDiff0 = rrDiff0 + (qrsFinal.get(j + 1) - qrsFinal.get(j));
						}
						rrDiff0 = rrDiff0 / Constants.QRSF_RR_COUNT;

					}
				} else {
					rrDiff0 = 0;
					for (int j = 1; j <= countMiss; j++) {
						rrDiff0 = rrDiff0 + (qrsFinal.get(countF - j) - qrsFinal.get(countF - j - 1));
					}
					rrDiff0 = rrDiff0 / countMiss;
				}

				rrDiff1 = qrs[forward_Iteration + 1] - qrsFinal.getLast();
				rrDiff2 = qrs[forward_Iteration + 2] - qrsFinal.getLast();

				if ((rrDiff1 > Constants.QRSF_RR_THRESHOLD) || (rrDiff2 > Constants.QRSF_RR_THRESHOLD)) {
					if ((rrDiff1 > rrDiff0 * Constants.QRS_RRLOW_PERC)
							&& (rrDiff1 < Constants.QRS_RRHIGH_PERC * rrDiff0)) {
						qrsFinal.addLast(qrs[forward_Iteration + 1]);
						forward_Iteration = forward_Iteration + 1;
						countF = countF + 1;
					} else if ((rrDiff2 > rrDiff0 * Constants.QRS_RRLOW_PERC)
							&& (rrDiff2 < Constants.QRS_RRHIGH_PERC * rrDiff0)) {
						qrsFinal.addLast(qrs[forward_Iteration + 2]);
						forward_Iteration = forward_Iteration + 2;
						countF = countF + 1;
					} else {
						findFlag = 0;
						increment = 0;
						while (findFlag == 0) {
							if ((qrs[forward_Iteration + 2 + increment]
									- qrs[forward_Iteration + 1 + increment]) > rrDiff0 * Constants.QRS_RRLOW_PERC
									&& (qrs[forward_Iteration + 2 + increment]
											- qrs[forward_Iteration + 1 + increment]) < rrDiff0
													* Constants.QRS_RRHIGH_PERC) {
								qrsFinal.addLast(qrs[forward_Iteration + 1 + increment]);
								qrsFinal.addLast(qrs[forward_Iteration + 2 + increment]);
								forward_Iteration = forward_Iteration + 2 + increment;
								countF = countF + 2;
								findFlag = 1;
							} else {
								// findFlag = 0;
								increment = increment + 1;
								if ((forward_Iteration + increment + 2) == lengthQRS) {
									forward_Iteration = forward_Iteration + 2 + increment;
									findFlag = 1;
								}
							}
						}
					}
				} else {
					forward_Iteration = forward_Iteration + 1;
				}

				if (count0 < countF) {
					if (getDiff(qrsFinal) > Constants.QRSF_RR_MISS_PERCENT * rrDiff0) {
						missF.add(countF - 2);
						missFlag = 1;
						countMiss = 1;
					} else {
						if (missFlag == 1) {
							countMiss = countMiss + 1;
							if (countMiss == 10) {
								missFlag = 0;
							}
						}
					}
				}
			}
		}

		// Add the last 1-2 peaks
		System.out.println("Forward tracking is over");
		if ((forward_Iteration + 1) == lengthQRS) {
			iter = 1;
		} else if ((forward_Iteration + 2) == lengthQRS) {
			iter = 2;
		}

		for (int backward_Iteration = 1; backward_Iteration < iter; backward_Iteration++) {
			count0 = countF;
			if (missFlag == 0) {
				if (countF < Constants.QRSF_INITIAL_RR_COUNT) {
					rrDiff0 = 0;
					for (int j = 0; j < countF - 1; j++) {
						rrDiff0 = rrDiff0 + (qrsFinal.get(j + 1) - qrsFinal.get(j));
					}
					rrDiff0 = rrDiff0 / (countF - 1);
				} else {
					rrDiff0 = 0;
					for (int j = countF - Constants.QRSF_INITIAL_RR_COUNT; j < countF - 1; j++) {
						rrDiff0 = rrDiff0 + (qrsFinal.get(j + 1) - qrsFinal.get(j));
					}
					rrDiff0 = rrDiff0 / Constants.QRSF_RR_COUNT;

				}
			} else {
				rrDiff0 = 0;
				for (int j = 1; j <= countMiss; j++) {
					rrDiff0 = rrDiff0 + (qrsFinal.get(countF - j) - qrsFinal.get(countF - j - 1));
				}
				rrDiff0 = rrDiff0 / countMiss;

			}

			if ((qrs[forward_Iteration + backward_Iteration] - qrsFinal.getLast()) > rrDiff0 * Constants.QRS_RRLOW_PERC
					&& (qrs[forward_Iteration + backward_Iteration] - qrsFinal.getLast()) < Constants.QRS_RRHIGH_PERC
							* rrDiff0) {
				qrsFinal.addLast(qrs[forward_Iteration + backward_Iteration]);
				countF = countF + 1;
			}

			if (count0 < countF) {
				if (getDiff(qrsFinal) > Constants.QRSF_RR_MISS_PERCENT * rrDiff0) {
					missF.add(countF - 2);
					missFlag = 1;
					countMiss = 1;
				} else {
					if (missFlag == 1) {
						countMiss = countMiss + 1;
						if (countMiss == 10) {
							missFlag = 0;
						}

					}
				}
			}

		}
		System.out.println("Adding last 1-2 peaks is over");
		/**
		 * Add missed peaks.
		 */
		int lenMissF = missF.size();
		int indMissF = -1;

		int factor = 0;
		int overlapFlag = 0;
		int qrsInter;
		int elementadded = 0;
		double diffMiss;
		double diffDenominator;

		if (lenMissF > 0) {

			int flag = 0;
			for (int i = 0; i < lenMissF; i++) {
				indMissF = missF.remove() + elementadded;
				flag = 0;
				diffMiss = (qrsFinal.get(indMissF) - qrsFinal.get(indMissF - 1));
				diffDenominator = (qrsFinal.get(indMissF - 1) - qrsFinal.get(indMissF - 2));
				factor = (int) Math.round(diffMiss / diffDenominator);
				overlapFlag = 0;
				if (factor == 2) {
					qrsInter = FindOverlapMqrsLoc(qrsM, qrsFinal.get(indMissF - 1), qrsFinal.get(indMissF));
					if (qrsInter > 0) {
						if (indMissF < Constants.QRSF_INITIAL_RR_COUNT) {
							rrDiff0 = 0;
							for (int j = 1; j < indMissF; j++) {
								rrDiff0 = rrDiff0 + (qrsFinal.get(j) - qrsFinal.get(j - 1));
							}
							rrDiff0 = rrDiff0 / (indMissF - 1);
						} else {
							rrDiff0 = 0;
							for (int j = 1; j < Constants.QRSF_INITIAL_RR_COUNT; j++) {
								rrDiff0 = rrDiff0 + (qrsFinal.get(indMissF - j) - qrsFinal.get(indMissF - j - 1));
							}
							rrDiff0 = rrDiff0 / Constants.QRSF_RR_COUNT;
						}
					}
					rrDiff1 = qrsInter - qrsFinal.get(indMissF - 1) + 1;
					if ((rrDiff1 > Constants.QRS_RRLOW_PERC * rrDiff0)
							&& (rrDiff1 < Constants.QRS_RRHIGH_PERC * rrDiff0)) {
						qrsFinal.add(indMissF, qrsInter + 1);
						overlapFlag = 1;
						flag = 1;
					}
				}

				if (overlapFlag == 0) {
					for (int f = factor - 1; f >= 1; f--) {
						qrsInter = (int) (qrsFinal.get(indMissF - 1) + diffMiss * f / factor);
						qrsFinal.add(indMissF, qrsInter);
						flag = 1;
					}
				}
				if (flag == 1) {
					elementadded = elementadded + (factor - 1);
				}
			}
		}

		/**
		 * Backtrack initial peaks
		 */

		int countB = 0;
		missFlag = 0;
		int backward_Iteration;
		countMiss = 0;
		if (startIndex > 0) {
			backward_Iteration = startIndex;
			while (backward_Iteration > 1) {
				if (missFlag == 0) {
					if (countF > Constants.QRSF_RR_COUNT) {
						rrDiff0 = 0;
						for (int j = 1; j < Constants.QRSF_INITIAL_RR_COUNT; j++) {
							rrDiff0 = rrDiff0 + (qrsFinal.get(j) - qrsFinal.get(j - 1));
						}
						rrDiff0 = rrDiff0 / Constants.QRSF_RR_COUNT;

					} else {
						rrDiff0 = 0;
						for (int j = 1; j < countF; j++) {
							rrDiff0 = rrDiff0 + (qrsFinal.get(j) - qrsFinal.get(j - 1));
						}
						rrDiff0 = rrDiff0 / countF;
					}
				} else {
					if (countMiss > 0) {
						rrDiff0 = 0;
						for (int j = 1; j < countMiss; j++) {
							rrDiff0 = rrDiff0 + (qrsFinal.get(j) - qrsFinal.get(j - 1));
						}
						rrDiff0 = rrDiff0 / countMiss;
					}
				}

				rrDiff1 = qrsFinal.getFirst() - qrs[backward_Iteration - 1];
				rrDiff2 = qrsFinal.getFirst() - qrs[backward_Iteration - 2];

				if ((rrDiff1 > Constants.QRSF_RR_THRESHOLD) || (rrDiff2 > Constants.QRSF_RR_THRESHOLD)) {
					if ((rrDiff1 > rrDiff0 * Constants.QRS_RRLOW_PERC)
							&& (rrDiff1 < Constants.QRS_RRHIGH_PERC * rrDiff0)) {
						qrsFinal.addFirst(qrs[backward_Iteration - 1]);
						backward_Iteration = backward_Iteration - 1;
						countB = countB + 1;
					} else if ((rrDiff2 > rrDiff0 * Constants.QRS_RRLOW_PERC)
							&& (rrDiff2 < Constants.QRS_RRHIGH_PERC * rrDiff0)) {
						qrsFinal.addFirst(qrs[backward_Iteration - 2]);
						backward_Iteration = backward_Iteration - 2;
						countB = countB + 1;
					} else {
						findFlag = 0;
						decrement = 0;
						while (findFlag == 0) {
							if ((qrs[backward_Iteration - 1 - decrement]
									- qrs[backward_Iteration - 2 - decrement]) > rrDiff0 * Constants.QRS_RRLOW_PERC
									&& (qrs[backward_Iteration - 1 - decrement]
											- qrs[backward_Iteration - 2 - decrement]) < rrDiff0
													* Constants.QRS_RRHIGH_PERC) {
								qrsFinal.addFirst(qrs[backward_Iteration - 1 - decrement]);
								qrsFinal.addFirst(qrs[backward_Iteration - 2 - decrement]);
								backward_Iteration = backward_Iteration - 2 - decrement;
								countB = countB + 2;
								findFlag = 1;
							} else {
								// findFlag = 0;
								decrement = decrement + 1;
								if ((backward_Iteration - decrement - 2) < 0) {
									backward_Iteration = backward_Iteration - 2 - decrement;
									findFlag = 1;
								}
							}
						}
					}

				} else {
					backward_Iteration = backward_Iteration - 1;
				}

				// Find loc of missed peaks

				if ((qrsFinal.get(2) - qrsFinal.get(1)) > Constants.QRSF_RR_MISS_PERCENT * rrDiff0) {
					missB.addFirst(countB);
					missFlag = 1;
					countMiss = 1;
				} else {
					if (missFlag == 1) {
						countMiss = countMiss + 1;
						if (countMiss == 10) {
							missFlag = 0;
						}

					}
				}
			}

			for (int it = backward_Iteration; it > 0; it--) {
				if ((qrsFinal.getFirst() - qrs[it - 1]) > Constants.QRSF_RR_THRESHOLD) {
					qrsFinal.addFirst(qrs[it - 1]);
					countB = countB + 1;
				}
			}

		}

		/**
		 * Add missed peaks
		 */
		int lenMissB = missB.size();
		int indMissB = -1;
		factor = 0;
		overlapFlag = 0;
		elementadded = 0;
		if (lenMissB > 0) {

			int flag = 0;
			for (int i = 0; i < lenMissB; i++) {
				flag = 0;
				indMissB = (countB - missB.removeFirst() + 2) + elementadded;
				diffMiss = (qrsFinal.get(indMissB) - qrsFinal.get(indMissB - 1));
				diffDenominator = (qrsFinal.get(indMissB - 1) - qrsFinal.get(indMissB - 2));
				factor = (int) Math.round(diffMiss / diffDenominator);
				overlapFlag = 0;
				if (factor == 2) {
					qrsInter = FindOverlapMqrsLoc(qrsM, qrsFinal.get(indMissB - 1), qrsFinal.get(indMissB));
					if (qrsInter > 0) {
						if (indMissB < Constants.QRSF_INITIAL_RR_COUNT) {
							rrDiff0 = 0;
							for (int j = 1; j < indMissB; j++) {
								rrDiff0 = rrDiff0 + (qrsFinal.get(j) - qrsFinal.get(j - 1));
							}
							rrDiff0 = rrDiff0 / (indMissB - 1);
						} else {
							rrDiff0 = 0;
							for (int j = 1; j < Constants.QRSF_INITIAL_RR_COUNT; j++) {
								rrDiff0 = rrDiff0 + (qrsFinal.get(indMissB - j) - qrsFinal.get(indMissB - j - 1));
							}
							rrDiff0 = rrDiff0 / Constants.QRSF_RR_COUNT;
						}
					}
					rrDiff1 = qrsInter - qrsFinal.get(indMissB - 1) + 1;
					if ((rrDiff1 > Constants.QRS_RRLOW_PERC * rrDiff0)
							&& (rrDiff1 < Constants.QRS_RRHIGH_PERC * rrDiff0)) {
						qrsFinal.add(indMissB, qrsInter + 1);
						overlapFlag = 1;
						flag = 1;
					}
				}

				if (overlapFlag == 0) {
					for (int f = factor - 1; f >= 1; f--) {
						qrsInter = (int) (qrsFinal.get(indMissB - 1) + diffMiss * f / factor);
						qrsFinal.add(indMissB, qrsInter);
						flag = 1;
					}
				}

				if (flag == 1) {
					elementadded = elementadded + (factor - 1);
				}

			}
		}

		int lengthQRSF = qrsFinal.size();
		int[] qrsF;
		if (lengthQRSF > 0) {
			qrsF = new int[lengthQRSF];
			for (int i = 0; i < lengthQRSF; i++) {
				qrsF[i] = qrsFinal.removeFirst();
			}
		} else {
			qrsF = new int[] {};
		}
		return qrsF;
	}

	private int FindOverlapMqrsLoc(int[] qrsM, int a, int b) {
		// TODO Auto-generated method stub
		int lenM = qrsM.length;
		for (int k = 0; k < lenM - 1; k++) {
			if (qrsM[k] > a) {
				if (qrsM[k] < b && qrsM[k + 1] < b) {
					return -1;
				} else {
					return qrsM[k];
				}
			}
		}
		return -1;
	}

	private double getDiff(Deque<Integer> qrsFinal) {
		// TODO Auto-generated method stub

		temp3 = qrsFinal.removeLast();
		temp2 = qrsFinal.removeLast();
		temp1 = qrsFinal.removeLast();
		diff = temp2 - temp1;
		qrsFinal.addLast(temp1);
		qrsFinal.addLast(temp2);
		qrsFinal.addLast(temp3);

		return diff;
	}
}