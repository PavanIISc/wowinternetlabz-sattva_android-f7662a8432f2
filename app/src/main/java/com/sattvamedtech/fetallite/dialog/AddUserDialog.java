package com.sattvamedtech.fetallite.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.helper.EncryptDecryptHelper;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.Hospital;
import com.sattvamedtech.fetallite.model.User;

public class AddUserDialog extends Dialog implements View.OnClickListener {

    private Context mContext;

    private Toolbar mToolbar;
    private EditText mEtUsername, mEtPassword, mEtConfirmPassword;
    private Button mBCancel, mBSave;
    private Hospital mHospital;

    private User mUser;
    private boolean isEdit;

    public AddUserDialog(Context context, Hospital iHospital) {
        super(context);
        mContext = context;
        mHospital = iHospital;
    }

    public AddUserDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AddUserDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_user);
        initView();
        initListeners();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        mToolbar.setTitle(mContext.getString(R.string.label_add_hospital));

        mEtUsername = (EditText) findViewById(R.id.etUsername);
        mEtPassword = (EditText) findViewById(R.id.etPassword);
        mEtConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);

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
            AddUserDialog.this.dismiss();
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
        if (TextUtils.isEmpty(mEtUsername.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_username_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtUsername.requestFocus();
                }
            });
            return false;
        } else if (!isEdit && DatabaseHelper.getInstance(FLApplication.getInstance()).usernameExists(mEtUsername.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_username_exists), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtUsername.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtPassword.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_password_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPassword.requestFocus();
                }
            });
            return false;
        } else if (mEtPassword.getText().toString().trim().length() < Constants.MIN_PASSWD_LENGTH) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_password_min_length, Constants.MIN_PASSWD_LENGTH), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPassword.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtConfirmPassword.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_email_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtConfirmPassword.requestFocus();
                }
            });
            return false;
        } else if (mEtConfirmPassword.getText().toString().trim().length() < Constants.MIN_PASSWD_LENGTH) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_confirm_password_min_length, Constants.MIN_PASSWD_LENGTH), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtConfirmPassword.requestFocus();
                }
            });
            return false;
        } else if (!mEtPassword.getText().toString().equals(mEtConfirmPassword.getText().toString())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_password_mismatch), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPassword.requestFocus();
                }
            });
            return false;
        }
        return true;
    }

    private void save() {
        String aPassword = EncryptDecryptHelper.encryptIt(mEtPassword.getText().toString().trim());
        if (mUser == null) {
            mUser = new User(mEtUsername.getText().toString().trim(),
                    aPassword,
                    null,
                    null,
                    User.TYPE_USER,
                    mHospital);
        } else {
            mUser.password = aPassword;
        }

        DatabaseHelper.getInstance(mContext.getApplicationContext()).addUserDoctor(mUser);

        new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.success_registration), isEdit ? mContext.getString(R.string.success_user_edit) : mContext.getString(R.string.success_user_add), mContext.getString(R.string.action_ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                isEdit = false;
                AddUserDialog.this.dismiss();
            }
        });
    }

    public void clearFields() {
        mEtUsername.getText().clear();
        mEtPassword.getText().clear();
        mEtConfirmPassword.getText().clear();
        mEtUsername.requestFocus();
        mUser = null;
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
            mEtUsername.setText(mUser.username);
            mEtUsername.setEnabled(!isEdit);
        }
    }
}
