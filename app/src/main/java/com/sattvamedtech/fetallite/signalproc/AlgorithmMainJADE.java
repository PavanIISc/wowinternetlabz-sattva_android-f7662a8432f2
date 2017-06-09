package com.sattvamedtech.fetallite.signalproc;//package com.sattva.signalproc;
//
//import com.sattvamedtech.fetallite.helper.Logger;
//
//import com.sattva.signalprocjade.JadeMainFuction;
//
//public class AlgorithmMainJADE {
//
//    /**
//     * @param input - N x 4 input.
//     * @return
//     */
//
//    double[][] ica1;
//    double[][] ica2;
//
//    public Object[] AlgoStart(double[][] input) {
//        MatrixFunctions M = new MatrixFunctions();
//
//        Logger.logError("AlgorithmMainJADE", "inside algoStart  " + input[0][0] + ", " + input[14999][3]);
//        Logger.logInfo("AlgorithmMainJADE", "input: row 1: " + input[0][0] + ", " + input[0][1] + ", " + input[0][2] + ", " + input[0][3]);
//        Logger.logInfo("AlgorithmMainJADE", "input: row 1000: " + input[999][0] + ", " + input[999][1] + ", " + input[999][2] + ", " + input[999][3]);
//        Logger.logInfo("AlgorithmMainJADE", "input: row 10000: " + input[9999][0] + ", " + input[9999][1] + ", " + input[9999][2] + ", " + input[9999][3]);
//
//        int Fs = 1000;
//        /**
//         * Impulse filtering
//         */
//        long startTime = System.currentTimeMillis();
//
//        ImpulseFilter ImpFilt = new ImpulseFilter();
//        double[][] Ecg_imp = ImpFilt.ImpulseParallel(input, Fs);
//        //double[][] Ecg_imp = ImpFilt.Impulse(input, Fs);
//
//
//        Logger.logError("AlgorithmMainJADE", "impulse filter output  " + Ecg_imp[0][0] + ", " + Ecg_imp[14999][3]);
//
//
//        long stopTime1 = System.currentTimeMillis();
//
//
//        /**
//         * Filtering : Low, high, notch
//         */
//        FilterDecimal filt = new FilterDecimal();
//        //double[][] Ecg_filt = filt.filter(Ecg_imp);
//        double[][] Ecg_filt = filt.filterParallel(Ecg_imp);
//        long stopTime2 = System.currentTimeMillis();
//
//        Logger.logInfo("AlgorithmMainJADE", "filter output: row 1: " + Ecg_filt[0][0] + ", " + Ecg_filt[0][1] + ", " + Ecg_filt[0][2] + ", " + Ecg_filt[0][3]);
//        Logger.logInfo("AlgorithmMainJADE", "filter output: row 1000: " + Ecg_filt[999][0] + ", " + Ecg_filt[999][1] + ", " + Ecg_filt[999][2] + ", " + Ecg_filt[999][3]);
//        Logger.logInfo("AlgorithmMainJADE", "filter output: row 10000: " + Ecg_filt[9999][0] + ", " + Ecg_filt[9999][1] + ", " + Ecg_filt[9999][2] + ", " + Ecg_filt[9999][3]);
//
//        Logger.logError("AlgorithmMainJADE", "LoH filter output  " + Ecg_filt[0][0] + ", " + Ecg_filt[14999][3]);
//
//        /**
//         * Perform ICA on filtered data
//         */
//
//
//        //FastICA fpica = new FastICA();
//        //ica1 = fpica.ICA(Ecg_filt);
//
//        //System.out.println("ICA 1 out >>>>>>  "+ica1[0][0]+ " "+ica1[14999][3]);
//
//        JadeMainFuction jade = new JadeMainFuction();
//        double[][] B = jade.JadeR(Ecg_filt);
//
//        ica1 = M.multi(Ecg_filt, M.transpose(B));
//
//        Logger.logInfo("AlgorithmMainJADE", "ica1 output: row 1: " + ica1[0][0] + ", " + ica1[0][1] + ", " + ica1[0][2] + ", " + ica1[0][3]);
//        Logger.logInfo("AlgorithmMainJADE", "ica1 output: row 1000: " + ica1[999][0] + ", " + ica1[999][1] + ", " + ica1[999][2] + ", " + ica1[999][3]);
//        Logger.logInfo("AlgorithmMainJADE", "ica1 output: row 10000: " + ica1[9999][0] + ", " + ica1[9999][1] + ", " + ica1[9999][2] + ", " + ica1[9999][3]);
//
//        long stopTime3 = System.currentTimeMillis();
//
//        /**
//         * Estimate Maternal QRS
//         */
//
//        MQRSDetection mqrsDet = new MQRSDetection();
//        int[] qrsM = mqrsDet.mQRS(ica1, Fs);
//
//        Logger.logError("AlgorithmMainJADE", "qrsM length = " + qrsM.length);
////        System.out.println("Maternal QRS locations >>>>>>  " + qrsM[0] + " " + qrsM[1] + " " + qrsM[2]);
//
//        int count = 0;
//        int count1 = 0;
//        for (int i = 0; i < qrsM.length; i++) {
//            if (qrsM[i] < 120) {
//                count = count + 1;
//            }
//            if (qrsM[i] > 14850) {
//                count1 = count1 + 1;
//            }
//        }
//        int[] qrsM1 = new int[qrsM.length - count - count1];
//        int it = 0;
//        for (int i = count; i < (qrsM.length - count1); i++) {
//            qrsM1[it] = qrsM[i];
//            it = it + 1;
//        }
//
//        long stopTime4 = System.currentTimeMillis();
//        /**
//         * Maternal QRS cancellation
//         */
////        MQRScancelIParallel canc = new MQRScancelIParallel();
//        MQRSCancelI canc = new MQRSCancelI();
//
//        double[][] fetalSig = canc.cancel(Ecg_filt, qrsM1);
//
//        Logger.logInfo("AlgorithmMainJADE", "fetalSig output: row 1: " + fetalSig[0][0] + ", " + fetalSig[0][1] + ", " + fetalSig[0][2] + ", " + fetalSig[0][3]);
//        Logger.logInfo("AlgorithmMainJADE", "fetalSig output: row 1000: " + fetalSig[999][0] + ", " + fetalSig[999][1] + ", " + fetalSig[999][2] + ", " + fetalSig[999][3]);
//        Logger.logInfo("AlgorithmMainJADE", "fetalSig output: row 10000: " + fetalSig[9999][0] + ", " + fetalSig[9999][1] + ", " + fetalSig[9999][2] + ", " + fetalSig[9999][3]);
//
//        long stopTime5 = System.currentTimeMillis();
//
//
//        /**
//         * Perform ICA on residue
//         */
//
//
//        //ica2 = fpica.ICA(fetalSig);
//        //System.out.println("ica2 out >>>>>>  "+ica2[0][0]+ " "+ica2[14999][3]);
//
//        double[][] B2 = jade.JadeR(fetalSig);
//        ica2 = M.multi(fetalSig, M.transpose(B2));
//
//        long stopTime6 = System.currentTimeMillis();
//
//        /**
//         * Estimate Fetal QRS
//         */
//
//        FQRSDetection fqrsDet = new FQRSDetection();
//        int[] qrsF = fqrsDet.fQRS(ica2, Fs, qrsM);
//
//        Logger.logError("AlgorithmMainJADE", "qrsF length = " + qrsF.length);
//
//        System.out.println("Fetl QRS Locations >>>>>>>>>  " + qrsF[0] + " " + qrsF[1]);
//        long stopTime7 = System.currentTimeMillis();
//
//
//        System.out.println((stopTime1 - startTime) + " milliseconds for Impulse artifact cancellation");
//        System.out.println((stopTime2 - stopTime1) + " milliseconds for Filtering Hi/Lo/No ");
//        System.out.println((stopTime3 - stopTime2) + " milliseconds for Jade1 execution");
//        System.out.println((stopTime4 - stopTime3) + " milliseconds for MQRS execution");
//        System.out.println((stopTime5 - stopTime4) + " milliseconds for MQRS cancellation");
//        System.out.println((stopTime6 - stopTime5) + " milliseconds for Jade2 execution");
//        System.out.println((stopTime7 - stopTime6) + " milliseconds for FQRS execution");
//        System.out.println(stopTime7 - startTime + " milliseconds for total execution");
//
//
//        return new Object[]{qrsM, qrsF};
//    }
//}