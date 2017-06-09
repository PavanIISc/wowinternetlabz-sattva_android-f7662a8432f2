package com.sattvamedtech.fetallite.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.activity.HomeActivity;
import com.sattvamedtech.fetallite.helper.ApplicationUtils;
import com.sattvamedtech.fetallite.helper.DateUtils;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.model.Patient;
import com.sattvamedtech.fetallite.model.Test;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.storage.FLPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class NewTestDialog extends Dialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Context mContext;

    private Toolbar mToolbar;
    private EditText mEtPatientId, mEtFirstName, mEtLastName, mEtDob;
    private Spinner mSRiskFactor, mSGravidity, mSParity, mSDoctor;
    private ArrayAdapter<CharSequence> mRskFactorAdapter, mGravidityAdapter, mParityAdapter;
    private ArrayList<User> mDoctorList = new ArrayList<>();
    private ArrayAdapter<User> mDoctorAdapter;
    private TextView mTvTestDuration;
    private SeekBar mSbTestDuration;
    private Button mBCancel;
    private Button mBStartTest;
    private NumberPicker mNpWeek, mNpDay;
    private DatePickerDialog mDatePicker;
    private Calendar mFileTimestampCalendar;
    private Calendar mDoBCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int date) {
            mDoBCalendar.set(year, month, date);
            resetCalendarTime(null);
            mEtDob.setText(DateUtils.convertDateToShortHumanReadable(mDoBCalendar.getTimeInMillis()));
        }
    };

    private static final int STEP_SIZE = 15;

    public NewTestDialog(Context context, Calendar iCalendar) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_test);
        mFileTimestampCalendar = iCalendar;
        if (getWindow() != null)
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        initView();
        setSpinnerData();
        setNumberPickers();
        initListeners();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        mToolbar.setTitle(mContext.getString(R.string.label_new_test));

        mEtPatientId = (EditText) findViewById(R.id.etPatientId);
        mEtDob = (EditText) findViewById(R.id.etDob);
        mEtFirstName = (EditText) findViewById(R.id.etFirstName);
        mEtLastName = (EditText) findViewById(R.id.etLastName);

        mSRiskFactor = (Spinner) findViewById(R.id.sRiskFactor);
        mSGravidity = (Spinner) findViewById(R.id.sGravidity);
        mSParity = (Spinner) findViewById(R.id.sParity);
        mSDoctor = (Spinner) findViewById(R.id.sDoctor);

        mTvTestDuration = (TextView) findViewById(R.id.tvTestDuration);
        mSbTestDuration = (SeekBar) findViewById(R.id.sbTestDuration);
        mSbTestDuration.setProgress(30);
        mTvTestDuration.setText(mContext.getString(R.string.label_test_duration, mSbTestDuration.getProgress(), 0));

        mNpWeek = (NumberPicker) findViewById(R.id.npWeek);
        mNpDay = (NumberPicker) findViewById(R.id.npDay);

        mBCancel = (Button) findViewById(R.id.bCancel);
        mBStartTest = (Button) findViewById(R.id.bStartTest);

        resetCalendarTime(null);
        mDatePicker = new DatePickerDialog(mContext, mDateSetListener, mDoBCalendar.get(Calendar.YEAR), mDoBCalendar.get(Calendar.MONTH), mDoBCalendar.get(Calendar.DATE));
    }

    private void setSpinnerData() {
        mRskFactorAdapter = ArrayAdapter.createFromResource(mContext, R.array.list_risk_factors, R.layout.item_spinner);
        mSRiskFactor.setAdapter(mRskFactorAdapter);

        mGravidityAdapter = ArrayAdapter.createFromResource(mContext, R.array.list_gravidity, R.layout.item_spinner);
        mSGravidity.setAdapter(mGravidityAdapter);

        mParityAdapter = ArrayAdapter.createFromResource(mContext, R.array.list_parity, R.layout.item_spinner);
        mSParity.setAdapter(mParityAdapter);

        mDoctorList.clear();
        mDoctorList.add(new User("Doctor", "", "", "", User.TYPE_DOCTOR));
        mDoctorList.addAll(DatabaseHelper.getInstance(FLApplication.getInstance()).getAllDoctors(FLPreferences.getInstance(FLApplication.getInstance()).getSessionHospital()));
        mDoctorAdapter = new ArrayAdapter<>(mContext, R.layout.item_spinner, mDoctorList);
        mSDoctor.setAdapter(mDoctorAdapter);
    }

    private void setNumberPickers() {
        mNpWeek.setMinValue(35);
        mNpWeek.setMaxValue(40);
        mNpWeek.setWrapSelectorWheel(true);

        mNpDay.setMinValue(0);
        mNpDay.setMaxValue(6);
        mNpDay.setWrapSelectorWheel(true);
    }

    private void initListeners() {
        mEtDob.setOnClickListener(this);
        mBCancel.setOnClickListener(this);
        mBStartTest.setOnClickListener(this);
        mSbTestDuration.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.etDob) {
            mDatePicker.show();
        } else if (view.getId() == R.id.bCancel) {
            Logger.logInfo("NewTestDialog", "Cancel test dialog");
            NewTestDialog.this.dismiss();
            ((HomeActivity) mContext).stopDataStream();
        } else if (view.getId() == R.id.bStartTest) {
            if (validParams()) {
                checkPatientInDbAndProceed();
            }
        }
    }

    private boolean validParams() {
        if (TextUtils.isEmpty(mEtPatientId.getText().toString().trim()) && TextUtils.isEmpty(mEtDob.getText().toString().trim()) && TextUtils.isEmpty(mEtFirstName.getText().toString().trim()) && TextUtils.isEmpty(mEtLastName.getText().toString().trim())) {
            return false;
        }
        return true;
    }

    private void checkPatientInDbAndProceed() {
        Patient aPatient = DatabaseHelper.getInstance(FLApplication.getInstance()).getPatientByIdNameDob(mEtPatientId.getText().toString().trim(), TextUtils.isEmpty(mEtDob.getText().toString().trim()) ? 0 : mDoBCalendar.getTimeInMillis(), mEtFirstName.getText().toString().trim(), mEtLastName.getText().toString().trim());
        if (aPatient == null) {
            aPatient = new Patient();
            aPatient.id = TextUtils.isEmpty(mEtPatientId.getText().toString().trim()) ? UUID.randomUUID().toString() : mEtPatientId.getText().toString().trim();
        }
        aPatient.firstName = TextUtils.isEmpty(mEtFirstName.getText().toString().trim()) ? (TextUtils.isEmpty(aPatient.firstName) ? "" : aPatient.firstName) : mEtFirstName.getText().toString().trim();
        aPatient.lastName = TextUtils.isEmpty(mEtLastName.getText().toString().trim()) ? (TextUtils.isEmpty(aPatient.lastName) ? "" : aPatient.lastName) : mEtLastName.getText().toString().trim();
        aPatient.dob = TextUtils.isEmpty(mEtDob.getText().toString().trim()) ? (aPatient.dob > 0 ? aPatient.dob : 0) : mDoBCalendar.getTimeInMillis();
        aPatient.gestationalWeeks = mNpWeek.getValue();
        aPatient.gestationalDays = mNpDay.getValue();
        aPatient.riskFactor = mSRiskFactor.getSelectedItemPosition() > 0 ? mSRiskFactor.getSelectedItem().toString() : "";
        aPatient.gravidity = mSGravidity.getSelectedItemPosition() > 0 ? mSGravidity.getSelectedItem().toString() : "";
        aPatient.parity = mSParity.getSelectedItemPosition() > 0 ? mSParity.getSelectedItem().toString() : "";
        aPatient.doctor = mSDoctor.getSelectedItemPosition() > 0 ? (User) mSDoctor.getSelectedItem() : null;
        DatabaseHelper.getInstance(FLApplication.getInstance()).addPatient(aPatient);
        FLApplication.mPatientId = aPatient.id;
        Test aTest = new Test();
        aTest.id = FLApplication.mTestId;
        aTest.testDurationInMinutes = mSbTestDuration.getProgress();
        aTest.timeStamp = mFileTimestampCalendar.getTimeInMillis();
        aTest.inputFileName = aTest.id + FLApplication.mFileTimeStamp;
        aTest.patient = aPatient;
        aTest.user = FLPreferences.getInstance(FLApplication.getInstance()).getLoginSessionUserObject();
        aTest.hospital = FLPreferences.getInstance(FLApplication.getInstance()).getSessionHospital();
        DatabaseHelper.getInstance(FLApplication.getInstance()).addTest(aTest);
        ApplicationUtils.algoProcessEndCount = -1;
        ((HomeActivity) mContext).openTestScreen(aPatient, mSbTestDuration.getProgress());
        NewTestDialog.this.dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        i = Math.round((float) i / STEP_SIZE) * STEP_SIZE;
        setSeekBarProgress(seekBar, i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setSeekBarProgress(SeekBar iSeekBar, int iProgress) {
        iSeekBar.setOnSeekBarChangeListener(null);
        iSeekBar.setProgress(iProgress);
        mTvTestDuration.setText(mContext.getString(R.string.label_test_duration, iProgress, 0));
        iSeekBar.setOnSeekBarChangeListener(this);
    }

    private void resetCalendarTime(Calendar iCalendar) {
        if (iCalendar != null) {
            iCalendar.set(Calendar.HOUR_OF_DAY, 0);
            iCalendar.set(Calendar.MINUTE, 0);
            iCalendar.set(Calendar.SECOND, 0);
            iCalendar.set(Calendar.MILLISECOND, 0);
        } else {
            mDoBCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mDoBCalendar.set(Calendar.MINUTE, 0);
            mDoBCalendar.set(Calendar.SECOND, 0);
            mDoBCalendar.set(Calendar.MILLISECOND, 0);
        }
    }
}
