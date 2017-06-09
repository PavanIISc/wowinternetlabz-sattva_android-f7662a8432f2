package com.sattvamedtech.fetallite.signalproc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilterDecimal {
    static boolean i0Flag = false;
    static boolean i1Flag = false;
    static boolean i2Flag = false;
    static boolean i3Flag = false;
    static double fXc[][] = null;

    public double[][] filter(double[][] input) {
        int length = input.length;

        double[] Channel1 = new double[length];
        double[] Channel2 = new double[length];
        double[] Channel3 = new double[length];
        double[] Channel4 = new double[length];


        for (int i = 0; i < length; i++) {
            Channel1[i] = input[i][0];
            Channel2[i] = input[i][1];
            Channel3[i] = input[i][2];
            Channel4[i] = input[i][3];
        }

        FilterLowHiNotch(Channel1, Channel2, Channel3, Channel4);

        double[][] Out = new double[length][4];
        for (int i = 0; i < length; i++) {
            Out[i][0] = Channel1[i];
            Out[i][1] = Channel2[i];
            Out[i][2] = Channel3[i];
            Out[i][3] = Channel4[i];
        }


        return Out;
    }

    public double[][] filterParallel(double[][] input) {
        final int length = input.length;
        final double[][] finput = input;
        int ns = input[0].length;
        final double[][] out = new double[length][ns];

        ExecutorService exec = Executors.newFixedThreadPool(ns);
        for (int j = 0; j < ns; j++) {
            final int fj = j;
            final double[] Channel = new double[length];
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < length; i++) {
                        Channel[i] = finput[i][fj];
                    }
                    FilterLowHiNotchParallel(Channel);

                    for (int i = 0; i < length; i++) {
                        out[i][fj] = Channel[i];
                    }
                    if (fj == 0)
                        i0Flag = true;
                    else if (fj == 1)
                        i1Flag = true;
                    else if (fj == 2)
                        i2Flag = true;
                    else if (fj == 3)
                        i3Flag = true;
                }
            });
        }
        System.out.println("***Waiting for process Filter Calculation to finish : " + (new java.text.SimpleDateFormat("H:mm:ss:SSS")).format(java.util.Calendar.getInstance().getTime()));
        try {
            while (true) {
                Thread.sleep(100);
                if (i0Flag && i1Flag && i2Flag && i3Flag) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            exec.shutdown();
        }
        return out;
    }

    private void FilterLowHiNotchParallel(double[] channel) {


        double ahigh[] = new double[2];
        double bhigh[] = new double[2];
        double zhigh = -0.98453370859689782;
        for (int i0 = 0; i0 < 2; i0++) {
            bhigh[i0] = 0.984533708596897 + -1.9690674171937941 * (double) i0;
            ahigh[i0] = 1.0 + -1.969067417193793 * (double) i0;
        }

        FilterLoHi(channel, ahigh, bhigh, zhigh);

        double alow[] = {1.0, -0.324919696232906};
        double blow[] = {0.337540151883547, 0.337540151883547};
        double zlow = 0.662459848116453;

        FilterLoHi(channel, alow, blow, zlow);

        double aN[] = {1.0, -1.8329786119774709, 0.927307768331003};
        double bN[] = {0.963653884165502, -1.8329786119774709, 0.963653884165502};
        int i0 = 0;
        double zN1 = (0.036346115834507829 + -1.8013368574543165E-14 * (double) i0);
        i0 = 1;
        double zN2 = (0.036346115834507829 + -1.8013368574543165E-14 * (double) i0);


        FilterNotch(channel, aN, bN, zN1, zN2);
    }

    private void FilterLowHiNotch(double[] channel1, double[] channel2, double[] channel3, double[] channel4) {


        double ahigh[] = new double[2];
        double bhigh[] = new double[2];
        double zhigh = -0.98453370859689782;
        for (int i0 = 0; i0 < 2; i0++) {
            bhigh[i0] = 0.984533708596897 + -1.9690674171937941 * (double) i0;
            ahigh[i0] = 1.0 + -1.969067417193793 * (double) i0;
        }

        FilterLoHi(channel1, ahigh, bhigh, zhigh);
        FilterLoHi(channel2, ahigh, bhigh, zhigh);
        FilterLoHi(channel3, ahigh, bhigh, zhigh);
        FilterLoHi(channel4, ahigh, bhigh, zhigh);

        double alow[] = {1.0, -0.324919696232906};
        double blow[] = {0.337540151883547, 0.337540151883547};
        double zlow = 0.662459848116453;


        FilterLoHi(channel1, alow, blow, zlow);
        FilterLoHi(channel2, alow, blow, zlow);
        FilterLoHi(channel3, alow, blow, zlow);
        FilterLoHi(channel4, alow, blow, zlow);

        double aN[] = {1.0, -1.8329786119774709, 0.927307768331003};
        double bN[] = {0.963653884165502, -1.8329786119774709, 0.963653884165502};
        int i0 = 0;
        double zN1 = (0.036346115834507829 + -1.8013368574543165E-14 * (double) i0);
        i0 = 1;
        double zN2 = (0.036346115834507829 + -1.8013368574543165E-14 * (double) i0);

        FilterNotch(channel1, aN, bN, zN1, zN2);
        FilterNotch(channel2, aN, bN, zN1, zN2);
        FilterNotch(channel3, aN, bN, zN1, zN2);
        FilterNotch(channel4, aN, bN, zN1, zN2);

    }

    private void FilterLoHi(double[] channel, double[] a, double[] b, double z) {

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

    private void Filter2(double[] Inp, double[] b, double[] a, double z, int len) {

        double tempI, tempF;
        tempI = Inp[0];

        Inp[0] = b[0] * Inp[0] + z;

        for (int i = 1; i < len; i++) {
            tempF = Inp[i];
            Inp[i] = b[0] * Inp[i] + b[1] * tempI - a[1] * Inp[i - 1];
            tempI = tempF;
        }
    }

    private void FilterNotch(double[] channel, double[] a, double[] b, double z1, double z2) {

        int nfact = 6;
        int length = channel.length;
        int Len_ext = 2 * nfact + length;
        double[] Mirror = new double[Len_ext];

        MirrorInput(channel, Mirror, nfact);
        Mirror = filter3N(Mirror, b, a, Mirror[0] * z1, Mirror[0] * z2);
        Reverse(Mirror, Len_ext);
        Mirror = filter3N(Mirror, b, a, Mirror[0] * z1, Mirror[0] * z2);
        Reverse(Mirror, Len_ext);

        for (int i = 0; i < length; i++) {
            channel[i] = Mirror[nfact + i];
        }


    }

    private void MirrorInput(double[] Inp, double[] Inp_ext, int nfact) {

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

    private void Reverse(double[] Inp, int len) {

        double temp = 0;
        for (int i = 0; i < len / 2; i++) {
            temp = Inp[i];
            Inp[i] = Inp[len - i - 1];
            Inp[len - i - 1] = temp;
        }

    }

    /**
     * filtering of signals with filters of length 3 and different delays used
     * for notch filter
     */
    public double[] filter3N(double[] inp_ext_N, double[] b, double[] a,
                             double delayN1, double delayN2) {

        int row = inp_ext_N.length;// System.out.println(row);

        // filter operation
        double[] filt = new double[row];
        filt[0] = b[0] * inp_ext_N[0] + delayN1;
        filt[1] = b[0] * inp_ext_N[1] + b[1] * inp_ext_N[0] - a[1] * filt[0]
                + delayN2;

        for (int n = 2; n < row; n++) {
            filt[n] = (b[0] * inp_ext_N[n]) + (b[1] * inp_ext_N[n - 1])
                    + (b[2] * inp_ext_N[n - 2]) - (a[1] * filt[n - 1])
                    - (a[2] * filt[n - 2]);
        }

        return filt;

    } // end filt3


}