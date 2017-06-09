package com.sattvamedtech.fetallite.signalproc;

public class qrsfSelection {
    public int[] qrs(int[] qrs1, int[] qrs2, int[] qrs3, int[] qrs4) {
        /**
         * Channel selection part
         */

        int len1 = qrs1.length;
        int len2 = qrs2.length;
        int len3 = qrs3.length;
        int len4 = qrs4.length;

        double ind1 = 0;
        double ind2 = 0;
        double ind3 = 0;
        double ind4 = 0;
        // to get the start index in each channel
        int strtInd1 = -1;
        int strtInd2 = -1;
        int strtInd3 = -1;
        int strtInd4 = -1;
        if (len1 > 3) {
            int l = len1 - 3;
            double var1[] = new double[l];
            double t1, t2, t3, mean;
            for (int i = 0; i < l; i++) {
                t1 = qrs1[i + 1] - qrs1[i];
                t2 = qrs1[i + 2] - qrs1[i + 1];
                t3 = qrs1[i + 3] - qrs1[i + 2];

                mean = (t1 + t2 + t3) / 3;

                var1[i] = Math.sqrt(((t1 - mean) * (t1 - mean) + (t2 - mean) * (t2 - mean) + (t3 - mean) * (t3 - mean)) / 3);
                if (var1[i] < 50) {
                    ind1 = ind1 + 1;
                    if (strtInd1 == -1) {
                        strtInd1 = i;
                    }
                }
            }
            ind1 = ind1 / l;
        }

        if (len2 > 3) {
            int l = len2 - 3;
            double var2[] = new double[l];
            double t1, t2, t3, mean;
            for (int i = 0; i < l; i++) {
                t1 = qrs2[i + 1] - qrs2[i];
                t2 = qrs2[i + 2] - qrs2[i + 1];
                t3 = qrs2[i + 3] - qrs2[i + 2];

                mean = (t1 + t2 + t3) / 3;

                var2[i] = Math.sqrt(((t1 - mean) * (t1 - mean) + (t2 - mean) * (t2 - mean) + (t3 - mean) * (t3 - mean)) / 3);
                if (var2[i] < 50) {
                    ind2 = ind2 + 1;
                    if (strtInd2 == -1) {
                        strtInd2 = i;
                    }
                }
            }
            ind2 = ind2 / l;
        }

        if (len3 > 3) {
            int l = len3 - 3;
            double var3[] = new double[l];
            double t1, t2, t3, mean;
            for (int i = 0; i < l; i++) {
                t1 = qrs3[i + 1] - qrs3[i];
                t2 = qrs3[i + 2] - qrs3[i + 1];
                t3 = qrs3[i + 3] - qrs3[i + 2];

                mean = (t1 + t2 + t3) / 3;

                var3[i] = Math.sqrt(((t1 - mean) * (t1 - mean) + (t2 - mean) * (t2 - mean) + (t3 - mean) * (t3 - mean)) / 3);
                if (var3[i] < 50) {
                    ind3 = ind3 + 1;
                    if (strtInd3 == -1) {
                        strtInd3 = i;
                    }
                }
            }
            ind3 = ind3 / l;
        }

        if (len4 > 3) {
            int l = len4 - 3;
            double var4[] = new double[l];
            double t1, t2, t3, mean;
            for (int i = 0; i < l; i++) {
                t1 = qrs4[i + 1] - qrs4[i];
                t2 = qrs4[i + 2] - qrs4[i + 1];
                t3 = qrs4[i + 3] - qrs4[i + 2];

                mean = (t1 + t2 + t3) / 3;

                var4[i] = Math.sqrt(((t1 - mean) * (t1 - mean) + (t2 - mean) * (t2 - mean) + (t3 - mean) * (t3 - mean)) / 3);
                if (var4[i] < 50) {
                    ind4 = ind4 + 1;
                    if (strtInd4 == -1) {
                        strtInd4 = i;
                    }
                }
            }
            ind4 = ind4 / l;
        }
        // FInd the maximum value of 'ind'
        // Have to add mean RR value also to this computation to get better estimate of 'ch'
        double ind = ind1;
        int len = len1;
        int ch = 1;

        if (ind2 > ind) {
            ind = ind2;
            ch = 2;
            len = len2;

        } else if (ind3 > ind) {
            ind = ind3;
            ch = 3;
            len = len3;
        } else if (ind4 > ind) {
            ind = ind4;
            ch = 4;
            len = len4;
        }
        /**
         * Get the start Index and qrs values to find the final QRS.
         */
        int[] QRS = new int[len];
        int StartIndex = -1;
        if (ch == 1) {
            StartIndex = strtInd1;

            for (int i = 0; i < len; i++) {
                QRS[i] = qrs1[i];
            }
        } else if (ch == 2) {
            StartIndex = strtInd2;
            for (int i = 0; i < len; i++) {
                QRS[i] = qrs2[i];
            }
        } else if (ch == 3) {
            StartIndex = strtInd3;
            for (int i = 0; i < len; i++) {
                QRS[i] = qrs3[i];
            }
        } else if (ch == 4) {
            StartIndex = strtInd4;
            for (int i = 0; i < len; i++) {
                QRS[i] = qrs4[i];
            }
        }


        /**
         * Final Selection of QRS
         */


        int dec = 0;
        int iter = 0;
        int inc = 0;
        ;
        int lengthQRS = QRS.length;
        int QRSFinal[] = new int[lengthQRS];
        int lenM = 0;
        int start = 0;


        if (StartIndex >= 0) {

            QRSFinal[StartIndex] = QRS[StartIndex];
            QRSFinal[StartIndex + 1] = QRS[StartIndex + 1];

            int i = StartIndex + 1;
            int count = StartIndex + 2;
            double RRdiff0 = 0;
            double RRdiff1 = 0;
            double RRdiff2 = 0;


            while (i < lengthQRS - 2) {
                if (count < 10) {
                    RRdiff0 = 0;
                    for (int j = StartIndex + 1; j < count; j++) {
                        RRdiff0 = RRdiff0 + QRSFinal[i] - QRSFinal[i - 1];
                    }
                    RRdiff0 = RRdiff0 / (count - 1);

                } else {
                    RRdiff0 = 0;
                    for (int j = count - 8; j < count; j++) {
                        RRdiff0 = RRdiff0 + QRSFinal[i] - QRSFinal[i - 1];
                    }
                    RRdiff0 = RRdiff0 / 8;
                }

                RRdiff1 = QRS[i + 1] - QRSFinal[count - 1];
                RRdiff2 = QRS[i + 2] - QRSFinal[count - 1];

                if (RRdiff1 > 300 || RRdiff2 > 300) {
                    if ((RRdiff1 > RRdiff0 * 0.9) && (RRdiff1 < 1.1 * RRdiff0)) {
                        QRSFinal[count] = QRS[i + 1];
                        i = i + 1;
                        count = count + 1;
                    } else if ((RRdiff2 > RRdiff0 * 0.9) && (RRdiff2 < 1.1 * RRdiff0)) {
                        QRSFinal[count] = QRS[i + 2];
                        count = count + 1;
                        i = i + 2;
                    } else if (((QRS[i + 2] - QRS[i + 1]) > 0.4 * RRdiff0) || (QRS[i + 2] - QRS[i + 1] > 120)) {
                        QRSFinal[count] = QRS[i + 1];

                        i = i + 1;
                        count = count + 1;
                    } else {
                        iter = 0;
                        inc = 1;
                        while (iter == 0) {
                            if ((QRS[i + 2 + inc] - QRS[i + 1] > 0.4 * RRdiff0) || (QRS[i + 2 + inc] - QRS[i + 1]) > 120) {
                                QRSFinal[count] = QRS[i + 1];
                                i = i + 1 + inc;
                                count = count + 1;
                                iter = 1;
                            } else {
                                inc = inc + 1;
                            }
                        }
                    }


                } else {
                    if ((RRdiff2 > 300)) {
                        QRSFinal[count] = QRS[i + 2];
                        i = i + 2;
                        count = count + 1;
                    } else {
                        iter = 0;
                        inc = 1;
                        while (iter == 0) {
                            if ((QRS[i + 3 + inc] - QRS[i + 2] > 0.4 * RRdiff0) || (QRS[i + 2] - QRS[i + 1]) > 120) {
                                QRSFinal[count] = QRS[i + 2];
                                i = i + 2 + inc;
                                count = count + 1;
                                iter = 1;
                            } else {
                                inc = inc + 1;
                            }
                        }
                    }
                }

            }

            // FInd how many peaks are left after the above set of iterations
            for (i = count; i < lengthQRS; i++) {
                if (QRSFinal[count - 1] == QRS[i]) {
                    iter = i;
                    break;
                }
            }
            // Find out how many are QRS peaks and add them to the array
            for (i = 0; i < iter; i++) {
                if ((QRS[i + 1] - QRSFinal[count - 1]) > 300) {
                    QRSFinal[count] = QRS[i + 1];
                    count = count + 1;
                    i = i + 1;
                } else {
                    i = i + 1;
                }
            }

            int fin = count - 1;
            // backtrack initial peaks

            count = StartIndex - 1;

            if (StartIndex > 0) {
                i = StartIndex;
                while (i > 1) {
                    RRdiff0 = 0;
                    for (int j = count + 1; j < count + 9; j++) {
                        RRdiff0 = RRdiff0 + QRSFinal[j + 1] - QRSFinal[j];
                    }
                    RRdiff0 = RRdiff0 / 8;

                    RRdiff1 = QRSFinal[i] - QRS[i - 1];
                    RRdiff2 = QRSFinal[i] - QRS[i - 2];

                    if (RRdiff1 > 300) {
                        if ((RRdiff1 > RRdiff0 * 0.9) && (RRdiff1 < 1.1 * RRdiff0)) {
                            QRSFinal[count] = QRS[i - 1];
                            i = i - 1;
                            count = count - 1;
                        } else if ((RRdiff2 > RRdiff0 * 0.9) && (RRdiff2 < 1.1 * RRdiff0)) {
                            QRSFinal[count] = QRS[i - 2];
                            count = count - 1;
                            i = i - 2;

                        } else if (((QRS[i - 1] - QRS[i - 2]) > 0.4 * RRdiff0) || ((QRS[i - 1] - QRS[i - 2]) > 120)) {
                            QRSFinal[count] = QRS[i - 1];
                            count = count - 1;
                            i = i - 1;
                        } else {
                            iter = 0;
                            dec = 1;
                            while ((iter == 0) || (i - 2 - dec) > 0) {
                                if (((QRS[i - 2 - dec] - QRS[i - 1]) > 0.4 * RRdiff0) || ((QRS[i - 2 - dec] - QRS[i - 1]) > 120)) {
                                    QRSFinal[count] = QRS[i - 1];
                                    i = i - 1 - dec;
                                    count = count - 1;
                                    iter = 1;
                                } else {
                                    dec = dec + 1;
                                }
                            }
                        }

                    } else {
                        if (RRdiff2 > 300) {
                            QRSFinal[count] = QRS[i - 2];
                            i = i - 2;
                            count = count - 1;
                        } else {
                            iter = 0;
                            inc = 1;
                            while (iter == 0) {
                                if (((QRS[i - 3 - inc] - QRS[i - 2]) > 0.4 * RRdiff0) || ((QRS[i - 2] - QRS[i - 1]) > 120)) {
                                    QRSFinal[count] = QRS[i - 2];
                                    i = i - 2 - inc;
                                    count = count - 1;
                                    iter = 1;
                                } else {
                                    inc = inc + 1;
                                }
                            }
                        }
                    }

                } // end while(i>2)

                // obtain first 1-2 peaks

                for (int j = i; i > 1; i--) {
                    if (QRSFinal[count - 1] - QRS[j - 1] > 350) {
                        QRSFinal[count] = QRS[j - 1];
                        count = count - 1;
                    }
                }

            }


            start = count + 1;


            // start gives the first index of QRS in QRSFinal.
            // Final gives the last index of QRS in QRSFinal.

            lenM = fin - start + 1;


        }
        int QRSF[] = new int[lenM];
        for (int i = 0; i < lenM; i++) {
            QRSF[i] = QRSFinal[i + start];
        }


        return QRSF;
    }
}