package com.sattvamedtech.fetallite.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.activity.HomeActivity;
import com.sattvamedtech.fetallite.dialog.PatientDetailsDialog;
import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.helper.FileLogger;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.helper.SessionHelper;
import com.sattvamedtech.fetallite.interfaces.PlotCallback;
import com.sattvamedtech.fetallite.interfaces.PlotInterface;
import com.sattvamedtech.fetallite.interfaces.PrintCallback;
import com.sattvamedtech.fetallite.model.Patient;
import com.sattvamedtech.fetallite.model.Test;

import java.io.File;
import java.io.FileReader;

import au.com.bytecode.opencsv.CSVReader;

public class TestFragment extends Fragment implements View.OnClickListener, OnChartValueSelectedListener, PlotInterface {

    private static final int SEC_IN_MILLIS = 1000;
    private static final int MIN_IN_SEC = 60;
    private static final int MIN_IN_MILLIS = MIN_IN_SEC * SEC_IN_MILLIS;
    private static final int GRAPH_VISIBLE_RANGE = 10;
    private static final int GRAPH_LABEL_COUNT = 20;
    private static int COLOR_PRIMARY;

    private Patient mPatient;
    private int mTestDuration;
    private Test mTest;

    private LinearLayout mLlFragmentTestRoot, mLlFhr, mLlButtonControls;
    private TextView mTvFhr, mTvFhrLabel, mTvMhr, mTvMhrLabel, mTvGestationalAge, mTvAccelerations, mTvDecelerations, mTvName, mTvPatientId, mTvTestTime, mTvMinSecLabel, mTvTestTimeLabel, mTvStopTest, mTvSendSmsLabel;
    private Button mBPatientDetails, mBPrint, mBExit;
    private ImageButton mIbStopTest, mIbSendSms;
    private DonutProgress mDpTestDuration;
    private LineChart mFetalChart, mUcChart;
    private int mChartLineColor = Color.parseColor("#FF7F7F7F");

    private CountDownTimer mCountDownTimer;

    private static final int FILE_TYPE_HR = 1;
    private static final int FILE_TYPE_UC = 2;

    private boolean isViewDataAvailable = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getExtraArguments();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            COLOR_PRIMARY = getResources().getColor(R.color.colorAccent);
        else
            COLOR_PRIMARY = getActivity().getColor(R.color.colorAccent);
        initView(view);
        setToolbar();
        initProgress();
        initFetalChart(view);
        initUcChart(view);
        setView();
        initListeners();
        setTheme(false);
        if (mTest == null) {
            PlotCallback.getInstance().addPlotInterface(this);
            ((HomeActivity) getActivity()).mProgressDialog.show();
        } else {
            ((HomeActivity) getActivity()).clearPrintData();
            stoppedStateView();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    readFile(FILE_TYPE_HR);
                    readFile(FILE_TYPE_UC);
                    readPrintFile();
                    checkEmptyData();
                    ((HomeActivity) getActivity()).mProgressDialog.dismiss();
                }
            }, 500);
        }
    }

    private void getExtraArguments() {
        if (getArguments() != null) {
            mTest = (Test) getArguments().getSerializable(Constants.EXTRA_TEST);
            if (mTest != null) {
                mPatient = mTest.patient;
                mTestDuration = mTest.testDurationInMinutes;
            } else {
                mPatient = (Patient) getArguments().getSerializable(Constants.EXTRA_PATIENT);
                mTestDuration = getArguments().getInt(Constants.EXTRA_TEST_DURATION);
            }
        }
    }

    private void initProgress() {
        mDpTestDuration.setMax(mTestDuration * MIN_IN_MILLIS);
        mCountDownTimer = new CountDownTimer(mTestDuration * MIN_IN_MILLIS, SEC_IN_MILLIS) {
            @Override
            public void onTick(long l) {
                setTestTime(l);
            }

            @Override
            public void onFinish() {
                setTestTime(0);
                confirmStopTest(getString(R.string.label_duration_complete), getString(R.string.confirm_stop_test_duration));
            }
        };
    }

    private void initView(View iView) {
        mLlFragmentTestRoot = (LinearLayout) iView.findViewById(R.id.fragment_test_root);
        mLlFhr = (LinearLayout) iView.findViewById(R.id.llFhr);
        mTvFhr = (TextView) iView.findViewById(R.id.tvFhr);
        mTvFhrLabel = (TextView) iView.findViewById(R.id.tvFhrLabel);
        mTvMhr = (TextView) iView.findViewById(R.id.tvMhr);
        mTvMhrLabel = (TextView) iView.findViewById(R.id.tvMhrLabel);
        mTvGestationalAge = (TextView) iView.findViewById(R.id.tvGestationalAge);
        mTvAccelerations = (TextView) iView.findViewById(R.id.tvAccelerations);
        mTvDecelerations = (TextView) iView.findViewById(R.id.tvDecelerations);
        mTvName = (TextView) iView.findViewById(R.id.tvName);
        mTvPatientId = (TextView) iView.findViewById(R.id.tvPatientId);
        mTvTestTime = (TextView) iView.findViewById(R.id.tvTestTime);
        mTvMinSecLabel = (TextView) iView.findViewById(R.id.tvMinSecLabel);
        mTvTestTimeLabel = (TextView) iView.findViewById(R.id.tvTestTimeLabel);
        mBPatientDetails = (Button) iView.findViewById(R.id.bPatientDetails);
        mIbStopTest = (ImageButton) iView.findViewById(R.id.ibStopTest);
        mTvStopTest = (TextView) iView.findViewById(R.id.tvStopTest);
        mIbSendSms = (ImageButton) iView.findViewById(R.id.ibSendSms);
        mTvSendSmsLabel = (TextView) iView.findViewById(R.id.tvSendSmsLabel);
        mDpTestDuration = (DonutProgress) iView.findViewById(R.id.dpTestDuration);
        mLlButtonControls = (LinearLayout) iView.findViewById(R.id.llButtonControls);
        mBPrint = (Button) iView.findViewById(R.id.bPrint);
        mBExit = (Button) iView.findViewById(R.id.bExit);
    }

    public void setToolbar() {
        ((HomeActivity) getActivity()).setTitle(null);
        ((HomeActivity) getActivity()).showMenuIcon(true);
    }

    public void setTheme(boolean isThemeDark) {
        mLlFragmentTestRoot.setBackgroundColor(isThemeDark ? Color.BLACK : Color.WHITE);
        mTvFhr.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvFhrLabel.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvMhr.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvMhrLabel.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvGestationalAge.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvAccelerations.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvDecelerations.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvName.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvPatientId.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvTestTime.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvMinSecLabel.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvTestTimeLabel.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvStopTest.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
        mTvSendSmsLabel.setTextColor(isThemeDark ? Color.WHITE : Color.BLACK);
    }

    private void initFetalChart(View iView) {
        mFetalChart = (LineChart) iView.findViewById(R.id.chartFetal);
        mFetalChart.setOnChartValueSelectedListener(this);
        // enable description text
        mFetalChart.getDescription().setEnabled(false);
        // enable touch gestures
        mFetalChart.setTouchEnabled(true);
        // enable scaling and dragging
        mFetalChart.setDragEnabled(true);
        mFetalChart.setScaleEnabled(true);
        mFetalChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mFetalChart.setPinchZoom(true);
        // set an alternative background color
        mFetalChart.setBackgroundColor(Color.TRANSPARENT);
        LineData aLineData = new LineData();
        aLineData.setValueTextColor(Color.BLACK);
        // add empty data
        mFetalChart.setData(aLineData);
        // get the legend (only possible after setting data)
        mFetalChart.getLegend().setEnabled(false);
        XAxis aXAxis = mFetalChart.getXAxis();
        aXAxis.setTextColor(COLOR_PRIMARY);
        aXAxis.setDrawGridLines(true);
        aXAxis.setAvoidFirstLastClipping(true);
        aXAxis.setEnabled(true);
        aXAxis.setDrawLabels(true);
        aXAxis.setLabelCount(GRAPH_LABEL_COUNT);
        aXAxis.setAxisMinimum(0f);

        YAxis aLeftYAxis = mFetalChart.getAxisLeft();
        aLeftYAxis.setTextColor(COLOR_PRIMARY);
        if (FLApplication.isFetalEnabled) {
            aLeftYAxis.setAxisMaximum(200f);
            aLeftYAxis.setAxisMinimum(90f);
        } else {
            aLeftYAxis.setAxisMaximum(150f);
            aLeftYAxis.setAxisMinimum(40f);
        }
        aLeftYAxis.setDrawGridLines(true);

        YAxis aRightYAxis = mFetalChart.getAxisRight();
        aRightYAxis.setEnabled(false);

        addDummyFetalData();
    }

    private void addDummyFetalData() {
        addFetalEntry(10 * MIN_IN_MILLIS, 0);
        addFetalEntry(9 * MIN_IN_MILLIS, 0);
        addFetalEntry(8 * MIN_IN_MILLIS, 0);
        addFetalEntry(7 * MIN_IN_MILLIS, 0);
        addFetalEntry(6 * MIN_IN_MILLIS, 0);
        addFetalEntry(5 * MIN_IN_MILLIS, 0);
        addFetalEntry(4 * MIN_IN_MILLIS, 0);
        addFetalEntry(3 * MIN_IN_MILLIS, 0);
        addFetalEntry(2 * MIN_IN_MILLIS, 0);
        addFetalEntry(MIN_IN_MILLIS, 0);
        addFetalEntry(0, 0);
        addFetalEntry(-1 * MIN_IN_MILLIS, 0);
        addFetalEntry(-2 * MIN_IN_MILLIS, 0);
        addFetalEntry(-3 * MIN_IN_MILLIS, 0);
        addFetalEntry(-4 * MIN_IN_MILLIS, 0);
        addFetalEntry(-5 * MIN_IN_MILLIS, 0);
    }

    private void initUcChart(View iView) {
        mUcChart = (LineChart) iView.findViewById(R.id.chartMaternal);
        mUcChart.setOnChartValueSelectedListener(this);
        // enable description text
        mUcChart.getDescription().setEnabled(false);
        // enable touch gestures
        mUcChart.setTouchEnabled(true);
        // enable scaling and dragging
        mUcChart.setDragEnabled(true);
        mUcChart.setScaleEnabled(true);
        mUcChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mUcChart.setPinchZoom(true);
        // set an alternative background color
        mUcChart.setBackgroundColor(Color.TRANSPARENT);
        LineData aLineData = new LineData();
        aLineData.setValueTextColor(Color.BLACK);
        // add empty data
        mUcChart.setData(aLineData);
        // get the legend (only possible after setting data)
        mUcChart.getLegend().setEnabled(false);
        XAxis aXAxis = mUcChart.getXAxis();
        aXAxis.setTextColor(COLOR_PRIMARY);
        aXAxis.setDrawGridLines(true);
        aXAxis.setAvoidFirstLastClipping(true);
        aXAxis.setEnabled(true);
        aXAxis.setDrawLabels(true);
        aXAxis.setLabelCount(GRAPH_LABEL_COUNT);
        aXAxis.setAxisMinimum(0f);

        YAxis aLeftYAxis = mUcChart.getAxisLeft();
        aLeftYAxis.setTextColor(COLOR_PRIMARY);
        aLeftYAxis.setDrawGridLines(true);
        aLeftYAxis.setAxisMaximum(100f);
        aLeftYAxis.setAxisMinimum(0f);

        YAxis aRightYAxis = mUcChart.getAxisRight();
        aRightYAxis.setEnabled(false);

        addDummyUcData();
    }

    private void addDummyUcData() {
        addUcEntry(10 * MIN_IN_MILLIS, 0);
        addUcEntry(9 * MIN_IN_MILLIS, 0);
        addUcEntry(8 * MIN_IN_MILLIS, 0);
        addUcEntry(7 * MIN_IN_MILLIS, 0);
        addUcEntry(6 * MIN_IN_MILLIS, 0);
        addUcEntry(5 * MIN_IN_MILLIS, 0);
        addUcEntry(4 * MIN_IN_MILLIS, 0);
        addUcEntry(3 * MIN_IN_MILLIS, 0);
        addUcEntry(2 * MIN_IN_MILLIS, 0);
        addUcEntry(MIN_IN_MILLIS, 0);
        addUcEntry(0, 0);
        addUcEntry(-1 * MIN_IN_MILLIS, 0);
    }

    private void setView() {
        mTvGestationalAge.setText(getString(R.string.label_gestational_weeks_value, mPatient.gestationalWeeks));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mTvName.setText(Html.fromHtml(getString(R.string.label_name_value, mPatient.firstName, mPatient.lastName)));
            mTvPatientId.setText(Html.fromHtml(getString(R.string.label_patient_id_value, mPatient.id)));
        } else {
            mTvName.setText(Html.fromHtml(getString(R.string.label_name_value, mPatient.firstName, mPatient.lastName), Html.FROM_HTML_MODE_COMPACT));
            mTvPatientId.setText(Html.fromHtml(getString(R.string.label_patient_id_value, mPatient.id), Html.FROM_HTML_MODE_COMPACT));
        }
        setTestTime(mTestDuration * MIN_IN_MILLIS);
    }

    private void initListeners() {
        mIbStopTest.setOnClickListener(this);
        mIbSendSms.setOnClickListener(this);
        mBPatientDetails.setOnClickListener(this);
        mBPrint.setOnClickListener(this);
        mBExit.setOnClickListener(this);
    }

    private void setTestTime(long iTimeInMillis) {
        try {
            mDpTestDuration.setProgress((mTestDuration * MIN_IN_MILLIS) - iTimeInMillis);
            mTvTestTime.setText(getString(R.string.label_test_duration, iTimeInMillis / MIN_IN_MILLIS, (iTimeInMillis % MIN_IN_MILLIS) / SEC_IN_MILLIS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTest() {
        mCountDownTimer.start();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bPatientDetails) {
            new PatientDetailsDialog(getActivity(), mPatient).show();
        } else if (view.getId() == R.id.ibSendSms) {
            ((HomeActivity) getActivity()).prepareForSms(false, "Test Details");
        } else if (view.getId() == R.id.ibStopTest) {
            confirmStopTest(getString(R.string.label_are_you_sure), getString(R.string.confirm_stop_test_manual));
        } else if (view.getId() == R.id.bPrint) {
            ((HomeActivity) getActivity()).printData();
        } else if (view.getId() == R.id.bExit) {
            if (SessionHelper.isLoginSessionValid()) {
                if (((HomeActivity) getActivity()).mProgressDialog != null && ((HomeActivity) getActivity()).mProgressDialog.isShowing())
                    ((HomeActivity) getActivity()).mProgressDialog.dismiss();
                ((HomeActivity) getActivity()).restartDataSocketService();
                ((HomeActivity) getActivity()).restartPrintSocketService();
                ((HomeActivity) getActivity()).popFragment();
            } else {
                ((HomeActivity) getActivity()).invalidSession();
            }
        }
    }

    private void confirmStopTest(String iTitle, String iMessage) {
        new MessageHelper(getActivity()).showTitleAlertOkCancel(iTitle, iMessage, "", "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                stopTest(true);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    public void stopTest(boolean isFromTestFragment) {
        if (isFromTestFragment)
            ((HomeActivity) getActivity()).stopDataStream();
        mCountDownTimer.cancel();
        stoppedStateView();
    }

    private void stoppedStateView() {
        mIbStopTest.setEnabled(false);
        mIbStopTest.setImageResource(R.drawable.stopped);
        mTvStopTest.setText(getString(R.string.action_stopped));
        mLlButtonControls.setVisibility(View.VISIBLE);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(getActivity(), String.valueOf(e.getY()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void plotFetalHeartRate(final float iXValue, final float iYValue) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (((HomeActivity) getActivity()).mProgressDialog.isShowing()) {
                        ((HomeActivity) getActivity()).mProgressDialog.dismiss();
                        startTest();
                    }
                    if (FLApplication.isFetalEnabled) {
                        setFetalHeartText(iYValue);
                    } else {
                        setMaternalHeartText(iYValue);
                    }
                    addFetalEntry(iXValue, iYValue);
                    FileLogger.logData(String.valueOf(iXValue) + "," + String.valueOf(iYValue), "fhr", FLApplication.mFileTimeStamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void plotMaternalHeartRate(final float iXValue, final float iMhr) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (((HomeActivity) getActivity()).mProgressDialog.isShowing()) {
                        ((HomeActivity) getActivity()).mProgressDialog.dismiss();
                        startTest();
                    }
                    setMaternalHeartText(iMhr);
                    FileLogger.logData(String.valueOf(iXValue) + "," + String.valueOf(iMhr), "mhr", FLApplication.mFileTimeStamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void plotUc(final float iXValue, final float iYValue) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (((HomeActivity) getActivity()).mProgressDialog.isShowing()) {
                        ((HomeActivity) getActivity()).mProgressDialog.dismiss();
                        startTest();
                    }
                    addUcEntry(iXValue, iYValue);
                    FileLogger.logData(String.valueOf(iXValue) + "," + String.valueOf(iYValue), "UC", FLApplication.mFileTimeStamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setMaternalHeartText(float iMhr) {
        mTvMhr.setText(String.valueOf((int) iMhr));
    }

    private void setFetalHeartText(float iFhr) {
        mTvFhr.setText(String.valueOf((int) iFhr));
    }

    private void addFetalEntry(float iXEntry, float iYEntry) {
        LineData aLineData = mFetalChart.getData();
        if (aLineData != null) {
            ILineDataSet aILineDataSet = aLineData.getDataSetByIndex(0);
            // set.addFetalEntry(...); // can be called as well
            if (aILineDataSet == null) {
                aILineDataSet = fetalHeartRate();
                aLineData.addDataSet(aILineDataSet);
            }
            Logger.logInfo("MainActivity: Fetal", "x_entry = " + iXEntry + ", y_entry = " + iYEntry);
            aLineData.addEntry(new Entry((iXEntry / MIN_IN_MILLIS), iYEntry), 0);
            Logger.logInfo("TestFragment", "Iteration " + ApplicationUtils.algoProcessStartCount + " fetalEntryCount: " + aLineData.getEntryCount());
            aLineData.notifyDataChanged();
            mFetalChart.notifyDataSetChanged();
            mFetalChart.setVisibleXRangeMaximum(GRAPH_VISIBLE_RANGE);
            mFetalChart.setVisibleXRangeMinimum(0);
            mFetalChart.moveViewToX(aLineData.getEntryCount() - GRAPH_VISIBLE_RANGE);
        }
    }

    private void addUcEntry(float iXEntry, float iYEntry) {
        LineData aLineData = mUcChart.getData();
        if (aLineData != null) {
            ILineDataSet aILineDataSet = aLineData.getDataSetByIndex(0);
            // set.addFetalEntry(...); // can be called as well
            if (aILineDataSet == null) {
                aILineDataSet = ucRate();
                aLineData.addDataSet(aILineDataSet);
            }
            Logger.logInfo("MainActivity: UC", "x_entry = " + iXEntry + ", y_entry = " + iYEntry);
            aLineData.addEntry(new Entry((iXEntry / MIN_IN_MILLIS), iYEntry), 0);
            aLineData.notifyDataChanged();
            mUcChart.notifyDataSetChanged();
            mUcChart.setVisibleXRangeMaximum(GRAPH_VISIBLE_RANGE);
            mUcChart.setVisibleXRangeMinimum(0);
            mUcChart.moveViewToX(aLineData.getEntryCount() - GRAPH_VISIBLE_RANGE);
        }
    }

    private LineDataSet fetalHeartRate() {
        LineDataSet aLineDataSet = new LineDataSet(null, "Fetal Heart Rate");
        aLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        aLineDataSet.setColor(mChartLineColor);
        aLineDataSet.setCircleColor(mChartLineColor);
        aLineDataSet.setLineWidth(2f);
        aLineDataSet.setCircleRadius(1f);
        //aLineDataSet.setFillAlpha(65);
        aLineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        aLineDataSet.setHighLightColor(Color.TRANSPARENT);
        aLineDataSet.setDrawValues(false);
        return aLineDataSet;
    }

    private LineDataSet ucRate() {
        LineDataSet aLineDataSet = new LineDataSet(null, "Uterine Contraction (Kpa)");
        aLineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        aLineDataSet.setColor(Color.BLUE);
        aLineDataSet.setCircleColor(Color.BLUE);
        aLineDataSet.setLineWidth(2f);
        aLineDataSet.setCircleRadius(1f);
        //aLineDataSet.setFillAlpha(65);
        aLineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        aLineDataSet.setHighLightColor(Color.TRANSPARENT);
        aLineDataSet.setDrawValues(false);
        return aLineDataSet;
    }

    private void readFile(int iFileType) {
        String aFileType = iFileType == FILE_TYPE_HR ? "fhr" : "UC";
        aFileType += "-";
        String[] aNextLine, aMhr;
        try {
            CSVReader aReader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + mTest.inputFileName + File.separator + aFileType + mTest.id + mTest.inputFileName.substring(mTest.inputFileName.indexOf("-sattva-")) + ".txt"));
            CSVReader aMhrReader = null;
            if (iFileType == FILE_TYPE_HR) {
                aMhrReader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + mTest.inputFileName + File.separator + "mhr-" + mTest.id + mTest.inputFileName.substring(mTest.inputFileName.indexOf("-sattva-")) + ".txt"));
            }
            while (((aNextLine = aReader.readNext()) != null)) {
                // nextLine[] is an array of values from the line
                if (iFileType == FILE_TYPE_HR) {
                    addFetalEntry(Float.parseFloat(aNextLine[0]), Float.parseFloat(aNextLine[1]));
                    setFetalHeartText(Float.parseFloat(aNextLine[1]));
                    if (((aMhr = aMhrReader.readNext()) != null))
                        setMaternalHeartText(Float.parseFloat(aMhr[1]));
                    setTestTime((long) Float.parseFloat(aNextLine[0]));
                } else {
                    addUcEntry(Float.parseFloat(aNextLine[0]), Float.parseFloat(aNextLine[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isViewDataAvailable = false;
        }
    }

    private void readPrintFile() {
        String aFileType = "print-";
        String[] aNextLine;
        try {
            CSVReader aReader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + mTest.inputFileName + File.separator + aFileType + mTest.id + mTest.inputFileName.substring(mTest.inputFileName.indexOf("-sattva-")) + ".txt"));
            while (((aNextLine = aReader.readNext()) != null)) {
                // nextLine[] is an array of values from the line
                PrintCallback.getInstance().savePrintData(aNextLine[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            new MessageHelper(getActivity()).showTitleAlertOk(getString(R.string.label_info), getString(R.string.error_no_print_data), getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mBPrint.setVisibility(View.GONE);
                    dialogInterface.dismiss();
                }
            });
        }
    }

    private void checkEmptyData() {
        if (mFetalChart.getData().getEntryCount() == 0 || mUcChart.getData().getEntryCount() == 0 || !isViewDataAvailable) {
            new MessageHelper(getActivity()).showTitleAlertOk(getString(R.string.label_info), getString(R.string.error_test_was_improper), getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PlotCallback.getInstance().removePlotInterface(this);
    }
}
