package com.sattvamedtech.fetallite.signalproc;

public class DecimatedWT {
    public double[] Dwt(double[] Input) {

//		double[] Input = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        int Length_input = Input.length;
        /**
         * LPF and HPF for wavelet transform
         */
//		double[] lpf = {Math.sqrt(0.5), Math.sqrt(0.5)};
        double[] lpf = {0.0273330683450780, 0.0295194909257746, -0.0391342493023831, 0.199397533977394, 0.723407690402421, 0.633978963458212, 0.0166021057645223, -0.175328089908450, -0.0211018340247589, 0.0195388827352867};


        int lengthlpf = lpf.length;

        double[] hpf = new double[lengthlpf];
        for (int i = 0; i <= lengthlpf - 1; i++) {
            hpf[i] = Math.pow(-1, (i + 1)) * lpf[lengthlpf - i - 1];
        }

        /**
         *  WT  :: Level 1
         */

        double[] Decomp1 = analyse(Input, lpf, hpf);
        int z = 0;
        z = z + 1;
        /**
         * Extract L and H from Decomp1 to pass for next level.
         */
        int Len2 = Length_input / 2;
        double L[] = new double[Len2];
        double H[] = new double[Len2];
        for (int i = 0; i < Length_input; i++) {
            if (i < Len2) {
                L[i] = Decomp1[i];
            } else {
                H[i - Len2] = Decomp1[i];
            }
        }

        /**
         *  WT  :: Level 2
         */
        double[] DecompL2 = analyse(L, lpf, hpf);
        double[] DecompH2 = analyse(H, lpf, hpf);
        /**
         * Extract LL, LH, HL and HH from DecompL2 and DecompH2 to pass for next level.
         */
        int Len3 = Len2 / 2;
        double LL[] = new double[Len3];
        double LH[] = new double[Len3];
        double HL[] = new double[Len3];
        double HH[] = new double[Len3];


        for (int i = 0; i < Len2; i++) {
            if (i < Len3) {
                LL[i] = DecompL2[i];
                HL[i] = DecompH2[i];
            } else {
                LH[i - Len3] = DecompL2[i];
                HH[i - Len3] = DecompH2[i];
            }
        }


        /**
         *  WT  :: Level 3
         */
        double[] DecompLL3 = analyse(LL, lpf, hpf);
        double[] DecompLH3 = analyse(LH, lpf, hpf);
        double[] DecompHL3 = analyse(HL, lpf, hpf);
        double[] DecompHH3 = analyse(HH, lpf, hpf);

        double sumLL = 0;
        double sumLH = 0;
        double sumHL = 0;
        double sumHH = 0;

        for (int i = 0; i < Len3; i++) {
            sumLL = sumLL + DecompLL3[i] * DecompLL3[i];
            sumLH = sumLH + DecompLH3[i] * DecompLH3[i];
            sumHL = sumHL + DecompHL3[i] * DecompHL3[i];
            sumHH = sumHH + DecompHH3[i] * DecompHH3[i];
        }

        double[] Perc_Ener = new double[2];

        Perc_Ener[0] = (sumLL + sumLH) / (sumLL + sumLH + sumHL + sumHH);
        Perc_Ener[1] = sumLL + sumLH;


        return Perc_Ener;


    }

    private static double[] analyse(double[] input, double[] lpf, double[] hpf) {
        // TODO Auto-generated method stub
        int lenF = lpf.length;
        int len = input.length;

        double[] input_ext = new double[len + lenF];

        for (int i = 0; i < len + lenF; i++) {
            if (i >= 0 && i < len) {
                input_ext[i] = input[i];
            } else if (i >= len && i < len + lenF) {
                input_ext[i] = input[i - len];
            }
        }


        double[] conv_ext = new double[len + 2 * lenF - 1];
        for (int i = 0; i < len + lenF; i++) {
            conv_ext[i + lenF - 1] = input_ext[i];
        }

        double[] LL = new double[len + lenF - 1];
        double[] HH = new double[len + lenF - 1];

        int j = 0;
        double sumL, sumH;
        while (j < len + lenF - 1) {
            sumL = 0;
            sumH = 0;
            for (int i = 0; i < lenF; i++) {
                sumL = sumL + lpf[i] * conv_ext[i + j];
                sumH = sumH + hpf[i] * conv_ext[i + j];
            }
            LL[j] = sumL;
            HH[j] = sumH;
            j = j + 1;
        }

        int len_down;
        int len_out;
        if (len % 2 == 0) {
            len_down = len / 2;
            len_out = len;
        } else {
            len_down = len / 2 + 1;
            len_out = len + 1;
        }


        double[] LL_down = new double[len_down];
        double[] HH_down = new double[len_down];


        j = 0;
        int i = lenF - 1;
        while (j < len_down && i < lenF + len - 1) {
            LL_down[j] = LL[i];
            HH_down[j] = HH[i];
            j++;
            i = i + 2;
        }

        double Out[] = new double[len_out];
        for (i = 0; i < len_out; i++) {
            if (i < len_down) {
                Out[i] = LL_down[i];
            } else {
                Out[i] = HH_down[i - len_down];
            }
        }

        return Out;
    }
}
