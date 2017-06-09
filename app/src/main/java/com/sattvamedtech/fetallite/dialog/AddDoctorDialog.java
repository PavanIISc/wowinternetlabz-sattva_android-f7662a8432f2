package com.sattvamedtech.fetallite.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.Hospital;
import com.sattvamedtech.fetallite.model.User;

public class AddDoctorDialog extends Dialog implements View.OnClickListener {

    private Context mContext;

    private Toolbar mToolbar;
    private EditText mEtName, mEtPhoneNumber, mEtEmail;
    private Button mBCancel, mBSave;
    private Hospital mHospital;

    private User mUser;
    private boolean isEdit;

    public AddDoctorDialog(Context context, Hospital iHospital) {
        super(context);
        mContext = context;
        mHospital = iHospital;
    }

    public AddDoctorDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AddDoctorDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_doctor);
        initView();
        initListeners();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        mToolbar.setTitle(mContext.getString(R.string.label_add_hospital));

        mEtName = (EditText) findViewById(R.id.etName);
        mEtPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        mEtEmail = (EditText) findViewById(R.id.etEmail);

        mBCancel = (Button) findViewById(R.id.bCancel);
        mBSave = (Button) findViewById(R.id.bSave);
    }

    private void initListeners() {
        mBCancel.setOnClickListener(this);
        mBSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bCancel) {
            AddDoctorDialog.this.dismiss();
        } else if (view.getId() == R.id.bSave) {
            validateAndSave();
        }
    }

    private void validateAndSave() {
        if (validParams()) {
            save();
        }
    }

    private boolean validParams() {
        if (TextUtils.isEmpty(mEtName.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_hospital_name_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtName.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtPhoneNumber.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_phone_number_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPhoneNumber.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtEmail.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_email_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtEmail.requestFocus();
                }
            });
            return false;
        } else if (!TextUtils.isEmpty(mEtEmail.getText().toString().trim()) && !Patterns.EMAIL_ADDRESS.matcher(mEtEmail.getText().toString().trim()).matches()) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_invalid_email), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtEmail.requestFocus();
                }
            });
            return false;
        }
        return true;
    }

    private void save() {
        if (mUser == null) {
            mUser = new User(mEtName.getText().toString().trim(),
                    null,
                    mEtPhoneNumber.getText().toString().trim(),
                    mEtEmail.getText().toString().trim(),
                    User.TYPE_DOCTOR,
                    mHospital);
        } else {
            mUser.username = mEtName.getText().toString().trim();
            mUser.phoneNumber = mEtPhoneNumber.getText().toString().trim();
            mUser.email = mEtEmail.getText().toString().trim();
        }

        DatabaseHelper.getInstance(mContext.getApplicationContext()).addUserDoctor(mUser);

        new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.success_registration), isEdit ? mContext.getString(R.string.success_doctor_edit) : mContext.getString(R.string.success_doctor_add), mContext.getString(R.string.action_ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                isEdit = false;
                AddDoctorDialog.this.dismiss();
            }
        });
    }

    public void clearFields() {
        mEtName.getText().clear();
        mEtPhoneNumber.getText().clear();
        mEtEmail.getText().clear();
        mEtName.requestFocus();
    }

    public void showEdit(User iUser) {
        if (iUser != null) {
            isEdit = true;
            mUser = iUser;
            show();
            setView();
        }
    }

    private void setView() {
        if (mUser != null) {
            mEtName.setText(mUser.username);
            mEtPhoneNumber.setText(mUser.phoneNumber);
            mEtEmail.setText(mUser.email);
        }
    }
}
