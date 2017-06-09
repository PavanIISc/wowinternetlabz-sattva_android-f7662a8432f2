package com.sattvamedtech.fetallite.process;


import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ReadData {

    private String[] nextLine;
    //    private CSVReader mReader;
    private int mCount;
    private String[] mTempString;
    private String mTempInput;

    private ConversionHelper aConversionHelper1, aConversionHelper2;

    private Calendar mCalendar = Calendar.getInstance();
    private SimpleDateFormat mSDF = new SimpleDateFormat("MM-dd-HH-mm-ss");
    private String mStartDate = mSDF.format(mCalendar.getTime());
    private String mFileName = "m-sattva-" + mStartDate;

    public void readInput(int sample_set) {

//        if (IotaHelper.getInstance().getRunType() == IotaHelper.RUN_CSV) {
//            try {
//                mReader = new CSVReader(new FileReader("sdcard/Download/inp_28.csv"));
//                for (int s_int = 0; s_int < sample_set; s_int++) {
//                    feedTestInputArray();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                InputStream aInpuStream = FLApplication.getInstance().getResources().openRawResource(R.raw.uc_file_sample);
//                DataInputStream aDIS = new DataInputStream(aInpuStream);
//                Logger.logInfo("ReadData", "Started reading file");
//                while ((mTempInput = aDIS.readLine()) != null) {
//                    mTempString = mTempInput.split("\\+");
//                    ApplicationUtils.mSampleMasterList.addAll(Arrays.asList(mTempString).subList(1, mTempString.length));
////                    handleData();
//                    new FileLogger(mTempString[1], mFileName).execute();
//                }
//                Logger.logInfo("ReadData", "Ended reading file");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private synchronized void handleData() {
        if (ApplicationUtils.mSampleMasterList.size() >= ApplicationUtils.bufferLength && ApplicationUtils.mConversionFlag == ApplicationUtils.IDLE) { // as soon as 15000 samples are found go inside
            ApplicationUtils.mConversionFlag = ApplicationUtils.PROCESSING;
            Logger.logInfo("HandleData", ApplicationUtils.bufferLength + " samples read within: " + (System.currentTimeMillis() - ApplicationUtils.mStartMS));
            ApplicationUtils.algoProcessStartCount++;
            aConversionHelper1 = new ConversionHelper(ApplicationUtils.mSampleMasterList.subList(0, 15000), "");
            aConversionHelper1.execute();
//            ApplicationUtils.lastBufferIndex += ApplicationUtils.bufferLength;
//            ApplicationUtils.bufferLength = 10000;
        }
    }

//Ravi version with 15000 mod.
    /*
    public double[][] readInput(int start, int end)
    {
        double[][] input = new double[15000][4];
        CSVReader mReader;
        try
        {
            mReader = new CSVReader(new FileReader("sdcard/Download/inp_28.readInput"));
            int mCount =0;
            String [] nextLine;
            while (((nextLine = mReader.readNext()) != null))
            {
                if(mCount >= start && mCount < end)
                {
                    for (int i = 0; i<4 ;i++)
                    {
                        input[mCount][i] = Double.parseDouble(nextLine[i]) ;
                    }
                }
                else if(mCount > end)
                    break;

                mCount = mCount+1;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return input;
    }

    */


//Kishore mod version

//    public double[][] readInput(int iStart, int iEnd) {
//        double[][] aDoubleInput = new double[15000][4];
//        CSVReader aReader;
//        try {
//            aReader = new CSVReader(new FileReader("sdcard/Download/inp_28.csv"));
//            //mReader = new CSVReader(new FileReader("D:\\Test\\HB\\hb\\inp_28.readInput"), '\t', '\'', 13);
//            int aCount = 0;
//            String[] aNextLine = null;
//
//            System.out.println("start : " + iStart);
//            System.out.println("end : " + iEnd);
//
//            for (int i = 0; i < iStart; i++) {
////			System.mOut.println("iter : "+i);
//                aNextLine = aReader.readNext();
////			System.mOut.println(nextLine);
//            }
//            for (int k = iStart; k <= iEnd; k++) {
//                aNextLine = aReader.readNext();
////			System.mOut.println(nextLine);
////			System.mOut.println(nextLine);
//                {
//                    for (int i = 0; i < 4; i++) {
////					System.mOut.println(" "+Double.parseDouble(nextLine[i]));
//                        aDoubleInput[aCount][i] = Double.parseDouble(aNextLine[i]);
//                    }
//                }
//
//                aCount = aCount + 1;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return aDoubleInput;
//    }


//    public void feedTestInputArray() {
//        mCount = 0;
//        try {
//            while (((nextLine = mReader.readNext()) != null) && mCount < 15000) {
//                // nextLine[] is an array of values from the line
//                for (int i = 0; i < 4; i++) {
//                    ApplicationUtils.mTestInputArray[mCount][i] = Double.parseDouble(nextLine[i]);
//                    if (i == 0) {
//                        ApplicationUtils.mInputArrayUc[mCount] = ApplicationUtils.mTestInputArray[mCount][i];
//                    }
//                }
//                mCount++;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}