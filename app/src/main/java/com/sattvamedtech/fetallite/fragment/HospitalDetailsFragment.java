package com.sattvamedtech.fetallite.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.activity.AdminDashboardActivity;
import com.sattvamedtech.fetallite.adapter.DoctorAdapter;
import com.sattvamedtech.fetallite.adapter.UserAdapter;
import com.sattvamedtech.fetallite.dialog.AddDoctorDialog;
import com.sattvamedtech.fetallite.dialog.AddUserDialog;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.Hospital;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;

import java.util.ArrayList;

public class HospitalDetailsFragment extends Fragment implements View.OnClickListener {

    private TextView mTvName, mTvPhone, mTvEmail, mTvAddress;
    private ImageView mIvClose;
    private ImageButton mIbDeleteHospital, mIbEditHospital;
    private Button mBAddUser, mBAddDoctor;
    private RecyclerView mRvUserList, mRvDoctorList;
    private Hospital mHospital;

    private AddUserDialog mAddUserDialog;
    private AddDoctorDialog mAddDoctorDialog;

    private UserAdapter mUserAdapter;
    private DoctorAdapter mDoctorAdapter;

    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<User> mDoctorList = new ArrayList<>();

    private UserAdapter.UserClickListener mUserClickListener = new UserAdapter.UserClickListener() {
        @Override
        public void onEditClick(int iPosition) {
            if (iPosition > -1 && iPosition < mUserList.size()) {
                initAddUserDialog();
                mAddUserDialog.showEdit(mUserList.get(iPosition));
            }
        }

        @Override
        public void onDeleteClick(int iPosition) {
            if (iPosition > -1 && iPosition < mUserList.size())
                confirmDeleteUser(iPosition);
        }
    };

    private DoctorAdapter.DoctorClickListener mDoctorClickListener = new DoctorAdapter.DoctorClickListener() {
        @Override
        public void onEditClick(int iPosition) {
            if (iPosition > -1 && iPosition < mDoctorList.size()) {
                initAddDoctorDialog();
                mAddDoctorDialog.showEdit(mDoctorList.get(iPosition));
            }
        }

        @Override
        public void onDeleteClick(int iPosition) {
            if (iPosition > -1 && iPosition < mDoctorList.size())
                confirmDeleteDoctor(iPosition);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hospital_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getExtraArguments();
        initView(view);
        initListeners();
        setView();
    }

    private void getExtraArguments() {
        if (getArguments() != null) {
            mHospital = (Hospital) getArguments().getSerializable(Constants.EXTRA_HOSPITAL);
        }
    }

    public void setHospital() {
        mHospital = DatabaseHelper.getInstance(FLApplication.getInstance()).getHospitalById(mHospital.hospitalId);
        setView();
    }

    private void initView(View iView) {
        mTvName = (TextView) iView.findViewById(R.id.tvTitle);
        mTvPhone = (TextView) iView.findViewById(R.id.tvPhone);
        mTvEmail = (TextView) iView.findViewById(R.id.tvEmail);
        mTvAddress = (TextView) iView.findViewById(R.id.tvAddress);

        mIvClose = (ImageView) iView.findViewById(R.id.ivOpenClose);
        mIvClose.setPadding(0, 0, 0, 0);
        mIvClose.setImageResource(R.drawable.close);

        mIbDeleteHospital = (ImageButton) iView.findViewById(R.id.ibDeleteHospital);
        mIbEditHospital = (ImageButton) iView.findViewById(R.id.ibEditHospital);

        mBAddUser = (Button) iView.findViewById(R.id.bAddUser);
        mBAddDoctor = (Button) iView.findViewById(R.id.bAddDoctor);

        mRvUserList = (RecyclerView) iView.findViewById(R.id.rvUserList);
        mUserAdapter = new UserAdapter(getActivity(), mUserList, mUserClickListener);
        mRvUserList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvUserList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRvUserList.setAdapter(mUserAdapter);

        mRvDoctorList = (RecyclerView) iView.findViewById(R.id.rvDoctorList);
        mDoctorAdapter = new DoctorAdapter(getActivity(), mDoctorList, mDoctorClickListener, true);
        mRvDoctorList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvDoctorList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRvDoctorList.setAdapter(mDoctorAdapter);

        fetchUsers();
        fetchDoctors();
    }

    private void initListeners() {
        mIvClose.setOnClickListener(this);
        mIbEditHospital.setOnClickListener(this);
        mIbDeleteHospital.setOnClickListener(this);
        mBAddUser.setOnClickListener(this);
        mBAddDoctor.setOnClickListener(this);
    }

    private void setView() {
        if (mHospital != null) {
            mTvName.setText(mHospital.name);
            mTvPhone.setText(mHospital.phoneNumber);
            mTvEmail.setText(mHospital.email);
            mTvAddress.setText(mHospital.address);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivOpenClose) {
            getActivity().onBackPressed();
        } else if (view.getId() == R.id.ibEditHospital) {
            ((AdminDashboardActivity) getActivity()).showEditHospitalDialog(mHospital);
        } else if (view.getId() == R.id.ibDeleteHospital) {
            confirmDeleteHospital();
        } else if (view.getId() == R.id.bAddUser) {
            showAddUserDialog();
        } else if (view.getId() == R.id.bAddDoctor) {
            showAddDoctorDialog();
        }
    }

    private void confirmDeleteHospital() {
        new MessageHelper(getActivity()).showTitleAlertOkCancel(getString(R.string.label_are_you_sure), getString(R.string.label_delete_hospital_confirm, mHospital.name), "", "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                DatabaseHelper.getInstance(FLApplication.getInstance()).deleteAllOfHospital(mHospital);
                DatabaseHelper.getInstance(FLApplication.getInstance()).deleteHospital(mHospital.hospitalId);
                getActivity().onBackPressed();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void initAddUserDialog() {
        if (mAddUserDialog == null) {
            mAddUserDialog = new AddUserDialog(getActivity(), mHospital);
            mAddUserDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mAddUserDialog.clearFields();
                    fetchUsers();
                }
            });
        }
    }

    private void initAddDoctorDialog() {
        if (mAddDoctorDialog == null) {
            mAddDoctorDialog = new AddDoctorDialog(getActivity(), mHospital);
            mAddDoctorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mAddDoctorDialog.clearFields();
                    fetchDoctors();
                }
            });
        }
    }

    private void showAddUserDialog() {
        initAddUserDialog();
        mAddUserDialog.show();
    }

    private void showAddDoctorDialog() {
        initAddDoctorDialog();
        mAddDoctorDialog.show();
    }

    private void fetchUsers() {
        mUserList.clear();
        mUserList.addAll(DatabaseHelper.getInstance(FLApplication.getInstance()).getAllUsers(mHospital));
        mUserAdapter.notifyDataSetChanged();
    }

    private void fetchDoctors() {
        mDoctorList.clear();
        mDoctorList.addAll(DatabaseHelper.getInstance(FLApplication.getInstance()).getAllDoctors(mHospital));
        mDoctorAdapter.notifyDataSetChanged();
    }

    private void confirmDeleteUser(final int iPosition) {
        new MessageHelper(getActivity()).showTitleAlertOkCancel(getString(R.string.label_are_you_sure), getString(R.string.label_delete_user_confirm, mUserList.get(iPosition).username), "", "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                deleteUser(iPosition);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void deleteUser(int iPosition) {
        DatabaseHelper.getInstance(FLApplication.getInstance()).deleteUserDoctor(mUserList.get(iPosition).id);
        mUserList.remove(iPosition);
        mUserAdapter.notifyDataSetChanged();
    }

    private void confirmDeleteDoctor(final int iPosition) {
        new MessageHelper(getActivity()).showTitleAlertOkCancel(getString(R.string.label_are_you_sure), getString(R.string.label_delete_user_confirm, mDoctorList.get(iPosition).username), "", "", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                deleteDoctor(iPosition);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void deleteDoctor(int iPosition) {
        DatabaseHelper.getInstance(FLApplication.getInstance()).deleteUserDoctor(mDoctorList.get(iPosition).id);
        mDoctorList.remove(iPosition);
        mDoctorAdapter.notifyDataSetChanged();
    }
}
