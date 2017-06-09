package com.sattvamedtech.fetallite.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.adapter.DoctorAdapter;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.Hospital;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.storage.FLPreferences;

import java.util.ArrayList;

public class DoctorDirectoryDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private Hospital mHospital;
    private Button mBDismiss;
    private RecyclerView mRvDoctorList;
    private DoctorAdapter mDoctorAdapter;
    private ArrayList<User> mDoctorList = new ArrayList<>();

    private DoctorAdapter.DoctorClickListener mDoctorClickListener = new DoctorAdapter.DoctorClickListener() {
        @Override
        public void onEditClick(int iPosition) {
        }

        @Override
        public void onDeleteClick(int iPosition) {
            if (iPosition > -1 && iPosition < mDoctorList.size())
                confirmDeleteDoctor(iPosition);
        }
    };

    public DoctorDirectoryDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public DoctorDirectoryDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected DoctorDirectoryDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_doctor_directory);
        mHospital = FLPreferences.getInstance(FLApplication.getInstance()).getSessionHospital();
        initView();
        initListeners();
    }

    private void initView() {
        mBDismiss = (Button) findViewById(R.id.bDismiss);
        mRvDoctorList = (RecyclerView) findViewById(R.id.rvDoctorList);
        mDoctorAdapter = new DoctorAdapter(mContext, mDoctorList, mDoctorClickListener, false);
        mRvDoctorList.setLayoutManager(new LinearLayoutManager(mContext));
        mRvDoctorList.setAdapter(mDoctorAdapter);
        fetchDoctors();
    }

    private void initListeners() {
        mBDismiss.setOnClickListener(this);
    }

    private void fetchDoctors() {
        mDoctorList.clear();
        mDoctorList.addAll(DatabaseHelper.getInstance(FLApplication.getInstance()).getAllDoctors(mHospital));
        mDoctorAdapter.notifyDataSetChanged();
    }

    private void confirmDeleteDoctor(final int iPosition) {
        new MessageHelper(mContext).showTitleAlertOkCancel(mContext.getString(R.string.label_are_you_sure), mContext.getString(R.string.label_delete_user_confirm, mDoctorList.get(iPosition).username), "", "", new DialogInterface.OnClickListener() {
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibClose || v.getId() == R.id.bDismiss) {
            DoctorDirectoryDialog.this.dismiss();
        }
    }
}
