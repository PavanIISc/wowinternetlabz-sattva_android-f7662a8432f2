package com.sattvamedtech.fetallite.signalproc;

public class UcAlgo {
    public double[] UcAlgoDwt(double[] RawInput) {

        /**
         * Input is 15000 array
         */
        double[] Input = new double[10000];
        int nIt = 2;
        double[] UcPerc = new double[nIt];
        double[] UcEnergy = new double[nIt];

        double ahigh[] = {1, -0.997489878867098};
        double bhigh[] = {0.998744939433549, -0.998744939433549};
        double zhigh = -0.998744939433532;

        double alow[] = {1, -0.975177876180649};
        double blow[] = {0.0124110619096754, 0.0124110619096754};
        double zlow = 0.987588938090325;

        double[] input_dwt = new double[100];

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < nIt; i++) {
            for (int k = 0; k < 10000; k++) {
                Input[k] = RawInput[k + 5000 * i];
            }

            FilterLoHi(Input, ahigh, bhigh, zhigh);

            FilterLoHi(Input, alow, blow, zlow);

            for (int k = 0; k < 100; k++) {
                input_dwt[k] = Input[k * 100];
            }
//			double[] input_dwt = CSV.csvDwt();
            DecimatedWT UcDwt = new DecimatedWT();
            double[] UC_Perc_Energy = UcDwt.Dwt(input_dwt);

            UcPerc[i] = UC_Perc_Energy[0];
            UcEnergy[i] = UC_Perc_Energy[1];
        }
        long EndTime = System.currentTimeMillis();

        System.out.println("Takes " + (EndTime - startTime) + " ms to execute " + nIt + " iterations.");

        return UcEnergy;

    }

    private static void FilterLoHi(double[] channel, double[] a, double[] b, double z) {

        int length = channel.length;
        int nfact = 3;
        int Len_ext = 2 * nfact + length;
        double Mirror[] = new double[Len_ext];

        MirrorInput(channel, Mirror, nfact);
        Filter2(Mirror, b, a, Mirror[0] * z, Len_ext);
        Reverse(Mirror, Len_ext);
        Filter2(Mirror, b, a, Mirror[0] * z, Len_ext);
        Reverse(Mirror, Len_ext);

        for (int i = 0; i < length; i++) {
            channel[i] = Mirror[nfact + i];
        }

    }

    private static void Filter2(double[] Inp, double[] b, double[] a, double z, int len) {

        double tempI, tempF;
        tempI = Inp[0];

        Inp[0] = b[0] * Inp[0] + z;

        for (int i = 1; i < len; i++) {
            tempF = Inp[i];
            Inp[i] = b[0] * Inp[i] + b[1] * tempI - a[1] * Inp[i - 1];
            tempI = tempF;
        }
    }

    private static void MirrorInput(double[] Inp, double[] Inp_ext, int nfact) {

        int length = Inp.length;
        int nIt = length + 2 * (nfact);
        int nIf = length + (nfact - 1);
        int nShift = 2 * length + nfact - 2;
        for (int i = 0; i < nIt; i++) {
            if (i < nfact) {
                Inp_ext[i] = 2 * Inp[0] - Inp[nfact - i];
            } else if (i > nIf) {
                Inp_ext[i] = 2 * Inp[length - 1] - Inp[nShift - i];
            } else {
                Inp_ext[i] = Inp[i - nfact];
            }
        }


    }

    private static void Reverse(double[] Inp, int len) {

        double temp = 0;
        for (int i = 0; i < len / 2; i++) {
            temp = Inp[i];
            Inp[i] = Inp[len - i - 1];
            Inp[len - i - 1] = temp;
        }

    }

}
