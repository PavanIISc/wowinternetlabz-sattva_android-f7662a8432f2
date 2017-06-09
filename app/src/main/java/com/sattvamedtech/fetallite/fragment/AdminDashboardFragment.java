package com.sattvamedtech.fetallite.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.activity.AdminDashboardActivity;
import com.sattvamedtech.fetallite.activity.HomeActivity;
import com.sattvamedtech.fetallite.activity.LoginActivity;
import com.sattvamedtech.fetallite.activity.TutorialsActivity;
import com.sattvamedtech.fetallite.adapter.HospitalAdapter;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.helper.GsonHelper;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.Hospital;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.storage.FLPreferences;

import java.util.ArrayList;

public class AdminDashboardFragment extends Fragment implements View.OnClickListener {

    private Button mBNext;
    private TextView mTvLoginId, mTvPhone, mTvEmail;
    private RelativeLayout mRlAddHospital;
    private RecyclerView mRvHospitalList;
    private ArrayList<Hospital> mHospitalList = new ArrayList<>();
    private HospitalAdapter mAdapter;
    private HospitalAdapter.HospitalClickListener mHospitalClickListener = new HospitalAdapter.HospitalClickListener() {
        @Override
        public void onHospitalClick(int iPosition) {
            if (iPosition > -1 && iPosition < mHospitalList.size() && mHospitalList.get(iPosition) != null) {
                HospitalDetailsFragment aHospitalDetailsFragment = new HospitalDetailsFragment();
                Bundle aBundle = new Bundle();
                aBundle.putSerializable(Constants.EXTRA_HOSPITAL, mHospitalList.get(iPosition));
                aHospitalDetailsFragment.setArguments(aBundle);
                ((AdminDashboardActivity) getActivity()).addFragment(aHospitalDetailsFragment, true);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListeners();
        setViewData();
    }

    private void initView(View iView) {
        mBNext = (Button) iView.findViewById(R.id.bNext);
        mTvLoginId = (TextView) iView.findViewById(R.id.tvLoginId);
        mTvPhone = (TextView) iView.findViewById(R.id.tvPhone);
        mTvEmail = (TextView) iView.findViewById(R.id.tvEmail);

        mRlAddHospital = (RelativeLayout) iView.findViewById(R.id.rlAddHospital);

        mRvHospitalList = (RecyclerView) iView.findViewById(R.id.rvHospitalList);
        mRvHospitalList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvHospitalList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mAdapter = new HospitalAdapter(getActivity(), mHospitalList, mHospitalClickListener);
        mRvHospitalList.setAdapter(mAdapter);

        fetchHospitals();
    }

    private void initListeners() {
        mBNext.setOnClickListener(this);
        mRlAddHospital.setOnClickListener(this);
    }

    private void setViewData() {
        mBNext.setText(FLPreferences.getInstance(FLApplication.getInstance()).getTutorialSeen() ? getString(R.string.action_go_to_home) : getString(R.string.action_go_to_tutorials));
        User aUser = (User) GsonHelper.getGson(FLPreferences.getInstance(FLApplication.getInstance()).getAdminUser(), User.class);
        if (aUser != null) {
            mTvLoginId.setText(aUser.username);
            mTvPhone.setText(aUser.phoneNumber);
            mTvEmail.setText(aUser.email);
        }
    }

    public void fetchHospitals() {
        mHospitalList.clear();
        mHospitalList.addAll(DatabaseHelper.getInstance(getActivity().getApplicationContext()).getAllHospital());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bNext) {
            if (allHospitalsHaveUsersAndDoctors()) {
                if (!FLPreferences.getInstance(FLApplication.getInstance()).getInitialProfile()) {
                    FLPreferences.getInstance(FLApplication.getInstance()).setInitialProfile(true);
                    Intent aIntent;
                    if (!FLPreferences.getInstance(FLApplication.getInstance()).getTutorialSeen()) {
                        aIntent = new Intent(getActivity(), TutorialsActivity.class);
                    } else if (TextUtils.isEmpty(FLPreferences.getInstance(FLApplication.getInstance()).getLoginSessionUserJson())) {
                        aIntent = new Intent(getActivity(), LoginActivity.class);
                    } else {
                        aIntent = new Intent(getActivity(), HomeActivity.class);
                    }
                    getActivity().finish();
                    startActivity(aIntent);
                } else {
                    getActivity().finish();
                }
            } else {
                new MessageHelper(getActivity()).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_no_user_doctor), getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        } else if (view.getId() == R.id.rlAddHospital) {
            ((AdminDashboardActivity) getActivity()).showAddHospitalDialog();
        }
    }

    private boolean allHospitalsHaveUsersAndDoctors() {
        for (Hospital aHospital : mHospitalList) {
            if (DatabaseHelper.getInstance(FLApplication.getInstance()).getAllUsersCount(aHospital) < 1)
                return false;
            if (DatabaseHelper.getInstance(FLApplication.getInstance()).getAllDoctorsCount(aHospital) < 1)
                return false;
        }
        return true;
    }

    public void openLatestHospitalDetails() {
        mHospitalClickListener.onHospitalClick(mHospitalList.size() - 1);
    }

}
