package com.sattvamedtech.fetallite.process;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.ExceptionHandling;
import com.sattvamedtech.fetallite.helper.FileLogger;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.miniTestModule.InitialDataCheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class DataSocketIntentService extends IntentService {

    private final IBinder mBinder = new LocalBinder();

    private ServerSocket mServerSocket;
    private Socket mClientConnection;
    public static final int SERVERPORT = 8080;
    private BufferedReader mBufferedInputReader;
    private boolean toReadData;

    private static final int MINI_TEST_NOT_DONE = 0;
    private static final int MINI_TEST_PROCESSING = 1;
    private static final int MINI_TEST_DONE = 2;
    private int miniTestStatus = 0;

    private static final String RESPONSE_BATTERY_OK_WAIT = "+OK+";
    private static final String REQUEST_START_DATA_STREAM = "+b+";
    private static final String REQUEST_STOP_DATA_STREAM = "+s+";

    private DataSocketCallback mDataSocketCallback;
    private ConversionHelper mConversionHelper;

    public DataSocketIntentService() {
        super("DataSocketIntentService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startSocketServer();
    }

    private void startSocketServer() {
        try {
            Logger.logDebug("DataSocketIntentService", "Starting socket server");
            mServerSocket = new ServerSocket(SERVERPORT);
            mClientConnection = mServerSocket.accept();
            mClientConnection.setTcpNoDelay(true);
            InputStreamReader aInputStreamReader = new InputStreamReader(mClientConnection.getInputStream());
            mBufferedInputReader = new BufferedReader(aInputStreamReader);
            waitForInitResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForInitResponse() {
        Logger.logDebug("DataSocketIntentService", "Waiting for initial response from device");
        toReadData = true;
        new ReadMessageOverSocket(true).execute();
    }

    public void startDataStream() {
        Logger.logDebug("DataSocketIntentService", "Start data stream from device");
        new SendMessageOverSocket(REQUEST_START_DATA_STREAM).execute();
    }

    private void readDataStream() {
        Logger.logDebug("DataSocketIntentService", "Handling data stream");
        toReadData = true;
        ApplicationUtils.mStartMS = System.currentTimeMillis();
        new ReadMessageOverSocket(false).execute();
    }

    public void stopDataStream() {
        Logger.logDebug("DataSocketIntentService", "Stop data stream from device");
        toReadData = false;
        miniTestStatus = MINI_TEST_NOT_DONE;
        new SendMessageOverSocket(REQUEST_STOP_DATA_STREAM).execute();
    }

    private void handleDataStream(String iMessage) {
        FileLogger.logData(iMessage, "input", FLApplication.mFileTimeStamp);
        String[] aSplitMessage = iMessage.split("\\+");
        ApplicationUtils.mSampleMasterList.addAll(Arrays.asList(aSplitMessage).subList(1, aSplitMessage.length));
        if (miniTestStatus == MINI_TEST_DONE)
            handleData();
        else if (miniTestStatus == MINI_TEST_NOT_DONE)
            handleDataForMiniTest();
    }

    private synchronized void handleData() {
        if (miniTestStatus == MINI_TEST_DONE && ApplicationUtils.mSampleMasterList.size() >= 15000 && ApplicationUtils.mConversionFlag == ApplicationUtils.IDLE) { // as soon as 15000 samples are found go inside
            ApplicationUtils.mConversionFlag = ApplicationUtils.PROCESSING;
            Logger.logInfo("HandleData", ApplicationUtils.bufferLength + " samples read within: " + (System.currentTimeMillis() - ApplicationUtils.mStartMS));
            ApplicationUtils.algoProcessStartCount++;
            Logger.logInfo("HandleData", "Iteration " + ApplicationUtils.algoProcessStartCount + " started");
            Logger.logDebug("ApplicationUtils.mSampleMasterList", ApplicationUtils.mSampleMasterList.size() + "");
            mConversionHelper = new ConversionHelper(ApplicationUtils.mSampleMasterList.subList(0, 15000), FLApplication.mFileTimeStamp);
            mConversionHelper.convert();
        }
    }

    private synchronized void handleDataForMiniTest() {
        if (miniTestStatus == MINI_TEST_NOT_DONE && ApplicationUtils.mSampleMasterList.size() >= 5000) {
            miniTestStatus = MINI_TEST_PROCESSING;
            InitialDataCheck aInitialDataCheck = new InitialDataCheck();
            boolean isDataValid = aInitialDataCheck.isValid(ApplicationUtils.mSampleMasterList.subList(0, 5000), FLApplication.mFileTimeStamp);
            Logger.logInfo("HandleDataForMiniTest", "isDataValid: " + isDataValid);
            miniTestStatus = MINI_TEST_DONE;
            if (!isDataValid) {
                mDataSocketCallback.onInvalidData();
            }
        }
    }

    public void registerCallback(Activity iActivity) {
        mDataSocketCallback = (DataSocketCallback) iActivity;
    }

    @Override
    public void onDestroy() {
        try {
            Logger.logInfo("DataSocketIntentService", "close mBufferedInputReader");
            if (mBufferedInputReader != null)
                mBufferedInputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            Logger.logInfo("DataSocketIntentService", "close mOutput");
//            mOutput.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            Logger.logInfo("DataSocketIntentService", "close mClientConnection");
            if (mClientConnection != null)
                mClientConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Logger.logInfo("DataSocketIntentService", "close mServerSocket");
            if (mServerSocket != null)
                mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public class ReadMessageOverSocket extends AsyncTask<Void, Void, Void> {

        boolean toWaitForDevice;

        public ReadMessageOverSocket(boolean toWaitForDevice) {
            this.toWaitForDevice = toWaitForDevice;
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            int count = 0;
            if (mClientConnection != null) {
                while ((toWaitForDevice && toReadData) || (toReadData && mClientConnection.isConnected() && !mClientConnection.isClosed())) {
                    try {
                        String aMessage = mBufferedInputReader.readLine();
                        if (!TextUtils.isEmpty(aMessage)) {
                            if (toWaitForDevice && !TextUtils.isEmpty(aMessage) && aMessage.equals(RESPONSE_BATTERY_OK_WAIT)) {
                                Logger.logInfo("DataSocketIntentService", "waiting: " + aMessage);
                                mDataSocketCallback.onClientConnected();
                                break;
                            } else if (!aMessage.equals(RESPONSE_BATTERY_OK_WAIT)) {
//                        count++;
//                        Logger.logInfo("DataSocketIntentService", "aMessage: " + count);
//                        Logger.logInfo("DataSocketIntentService", aMessage);
                                handleDataStream(aMessage);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            toReadData = false;
            return null;
        }
    }

    public class SendMessageOverSocket extends AsyncTask<Void, Void, Void> {

        String mMessage;

        public SendMessageOverSocket(String iMessage) {
            mMessage = iMessage;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Logger.logDebug("DataSocketIntentService", "Sending message to device. Message: " + mMessage);
            try {
                BufferedWriter aBufferedWriter = new BufferedWriter(new OutputStreamWriter(mClientConnection.getOutputStream()));
                aBufferedWriter.write(mMessage);
                aBufferedWriter.newLine();
                aBufferedWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
                if (ExceptionHandling.getInstance().getExceptionListener() != null) {
                    ExceptionHandling.getInstance().getExceptionListener().onException(new Exception("Issue with connection. Please restart app"));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mMessage.equals(REQUEST_START_DATA_STREAM)) {
                readDataStream();
                mDataSocketCallback.onDataStreamStarted();
            } else {
                mDataSocketCallback.onDataStreamStopped();
            }
        }
    }

    public class LocalBinder extends Binder {
        public DataSocketIntentService getSocketIntentService() {
            return DataSocketIntentService.this;
        }
    }

    public interface DataSocketCallback {
        void onClientConnected();

        void onDataStreamStarted();

        void onDataStreamStopped();

        void onInvalidData();
    }
}
