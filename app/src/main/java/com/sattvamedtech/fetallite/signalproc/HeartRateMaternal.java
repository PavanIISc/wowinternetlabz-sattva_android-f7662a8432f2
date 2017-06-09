package com.sattvamedtech.fetallite.signalproc;

import java.util.LinkedList;

public class HeartRateMaternal {
    public void heartRate(LinkedList<Integer> iQRS) {

        if (Constants.CURRENT_ITERATION > 0 && Constants.NoDetectionFlagMaternal == 0) {
            int aDiff = iQRS.getFirst() - Constants.QRS_MATERNAL_LOCATION.getLast();
            if (aDiff < 60) {
                iQRS.removeFirst();
            }

            double aRRMean = (Constants.QRS_MATERNAL_LOCATION.getLast() - Constants.QRS_MATERNAL_LOCATION
                    .get((Constants.QRS_MATERNAL_LOCATION.size() - (int) Constants.QRS_NO_RR_MEAN - 1)))
                    / Constants.QRS_NO_RR_MEAN;

            if (aDiff > Constants.QRSF_RR_MISS_PERCENT * aRRMean) {
                int aFactor = (int) Math.round(aDiff / aRRMean);
                if (aFactor == 2) {
                    int temp = Constants.QRS_MATERNAL_LOCATION.getLast();
                    iQRS.addFirst(temp + (int) Math.round(aDiff / aFactor));

                }
            }
        }

        if (Constants.CURRENT_ITERATION == 0 || Constants.NoDetectionFlagMaternal == 1) {
            for (int f = (int) Constants.QRS_NO_RR_MEAN; f < iQRS.size(); f++) {
                Constants.QRS_MATERNAL_LOCATION.add(iQRS.get(f));
                Constants.HR_MATERNAL.add((float) (60 * Constants.FS * Constants.QRS_NO_RR_MEAN
                        / (iQRS.get(f) - iQRS.get((int) (f - Constants.QRS_NO_RR_MEAN)))));

            }
        } else {
            int aCounter = Constants.QRS_MATERNAL_LOCATION.size();
            for (int f = 0; f < iQRS.size(); f++) {
                Constants.QRS_MATERNAL_LOCATION.add(iQRS.get(f));
                Constants.HR_MATERNAL
                        .add((float) (60 * Constants.FS * Constants.QRS_NO_RR_MEAN / (Constants.QRS_MATERNAL_LOCATION.getLast()
                                - Constants.QRS_MATERNAL_LOCATION.get((int) (aCounter - Constants.QRS_NO_RR_MEAN)))));
                aCounter++;

            }
        }

    }

}
