package com.sattvamedtech.fetallite.fragment;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.activity.HomeActivity;
import com.sattvamedtech.fetallite.adapter.TestAdapter;
import com.sattvamedtech.fetallite.helper.CompressionHelper;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.helper.DateUtils;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.helper.SessionHelper;
import com.sattvamedtech.fetallite.model.Test;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.storage.FLPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class PatientTestDataFragment extends Fragment implements View.OnClickListener {

    private EditText mEtPatientDetails, mEtTestDate;
    private Button mBBack, mBSearch;
    private RecyclerView mRvTestList;
    private ArrayList<Test> mTestList = new ArrayList<>();
    private TestAdapter mAdapter;
    private DatePickerDialog mDatePicker;
    private Calendar mCalendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int date) {
            mCalendar.set(year, month, date);
            resetCalendarTime(null);
            mEtTestDate.setText(DateUtils.convertDateToShortHumanReadable(mCalendar.getTimeInMillis()));
        }
    };

    private TestAdapter.TestClickListener mTestClickListener = new TestAdapter.TestClickListener() {
        @Override
        public void onTestClick(int iPosition) {
            if (SessionHelper.isLoginSessionValid()) {
                ((HomeActivity) getActivity()).mProgressDialog.show();
                File aZip = new File(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + mTestList.get(iPosition).inputFileName + ".zip");
                File aDir = new File(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + mTestList.get(iPosition).inputFileName);
                if (!aDir.isDirectory()) {
                    if (aZip.exists()) {
                        CompressionHelper.unzipFile(aZip, aDir);
                    } else {
                        new MessageHelper(getActivity()).showTitleAlertOk(getString(R.string.label_info), getString(R.string.error_empty_invalid_files), getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        return;
                    }
                }
                TestFragment aTestFragment = new TestFragment();
                Bundle aBundle = new Bundle();
                aBundle.putSerializable(Constants.EXTRA_TEST, mTestList.get(iPosition));
                aTestFragment.setArguments(aBundle);
                ((HomeActivity) getActivity()).addReplaceFragment(aTestFragment, true);
            } else {
                ((HomeActivity) getActivity()).invalidSession();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_test_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListeners();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbar();
        search();
    }

    public void setToolbar() {
        ((HomeActivity) getActivity()).setTitle(getString(R.string.label_patient_test_data));
        ((HomeActivity) getActivity()).showMenuIcon(true);
    }

    private void initView(View iView) {
        mEtPatientDetails = (EditText) iView.findViewById(R.id.etPatientDetails);
        mEtTestDate = (EditText) iView.findViewById(R.id.etTestDate);
        mBBack = (Button) iView.findViewById(R.id.bBack);
        mBSearch = (Button) iView.findViewById(R.id.bSearch);
        mRvTestList = (RecyclerView) iView.findViewById(R.id.rvTestList);
        mAdapter = new TestAdapter(getActivity(), mTestList, mTestClickListener);
        mRvTestList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvTestList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRvTestList.setAdapter(mAdapter);

        mDatePicker = new DatePickerDialog(getActivity(), mDateSetListener, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DATE));
    }

    private void initListeners() {
        mEtTestDate.setOnClickListener(this);
        mBBack.setOnClickListener(this);
        mBSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (SessionHelper.isLoginSessionValid()) {
            if (v.getId() == R.id.etTestDate) {
                mDatePicker.show();
            } else if (v.getId() == R.id.bBack) {
                getActivity().onBackPressed();
            } else if (v.getId() == R.id.bSearch) {
                search();
            }
        } else {
            ((HomeActivity) getActivity()).invalidSession();
        }
    }

    private void search() {
        mTestList.clear();
        if (TextUtils.isEmpty(mEtPatientDetails.getText().toString().trim()) && TextUtils.isEmpty(mEtTestDate.getText().toString().trim()))
            mTestList.addAll(DatabaseHelper.getInstance(FLApplication.getInstance()).getAllTestByHospital(FLPreferences.getInstance(FLApplication.getInstance()).getSessionHospital()));
        else
            mTestList.addAll(DatabaseHelper.getInstance(FLApplication.getInstance()).getAllTestByPatient(mEtPatientDetails.getText().toString().trim(), !TextUtils.isEmpty(mEtTestDate.getText().toString().trim()) ? mCalendar.getTimeInMillis() : 0, FLPreferences.getInstance(FLApplication.getInstance()).getSessionHospital()));
        mAdapter.notifyDataSetChanged();
        if (mTestList.size() == 0) {
            handleEmptySearchResults();
        }
    }

    private void handleEmptySearchResults() {
        new MessageHelper(getActivity()).showTitleAlertOk(getString(R.string.label_info), getString(R.string.error_empty_test_results), getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void resetCalendarTime(Calendar iCalendar) {
        if (iCalendar != null) {
            iCalendar.set(Calendar.HOUR_OF_DAY, 0);
            iCalendar.set(Calendar.MINUTE, 0);
            iCalendar.set(Calendar.SECOND, 0);
            iCalendar.set(Calendar.MILLISECOND, 0);
        } else {
            mCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mCalendar.set(Calendar.MILLISECOND, 0);
        }
    }
}
