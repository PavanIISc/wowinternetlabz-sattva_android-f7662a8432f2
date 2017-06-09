package com.sattvamedtech.fetallite.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.activity.AdminDashboardActivity;
import com.sattvamedtech.fetallite.helper.EncryptDecryptHelper;
import com.sattvamedtech.fetallite.helper.GsonHelper;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.FLPreferences;

public class AdminLoginDialog extends Dialog implements View.OnClickListener {

    private Context mContext;

    private Toolbar mToolbar;
    private EditText mEtLoginId, mEtPassword;
    private Button mBCancel, mBLogin;

    public AdminLoginDialog(Context context) {
        super(context);
        mContext = context;
    }

    public AdminLoginDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected AdminLoginDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_admin_login);
        initView();
        initListeners();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        mToolbar.setTitle(mContext.getString(R.string.label_add_hospital));

        mEtLoginId = (EditText) findViewById(R.id.etLoginId);
        mEtPassword = (EditText) findViewById(R.id.etPassword);

        mBCancel = (Button) findViewById(R.id.bCancel);
        mBLogin = (Button) findViewById(R.id.bLogin);
    }

    private void initListeners() {
        mBCancel.setOnClickListener(this);
        mBLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bCancel) {
            AdminLoginDialog.this.dismiss();
        } else if (view.getId() == R.id.bLogin) {
            validateAndLogin();
        }
    }

    private void validateAndLogin() {
        if (validParams()) {
            login();
        }
    }

    private boolean validParams() {
        if (TextUtils.isEmpty(mEtLoginId.getText().toString().trim())) {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_login_id_required), "", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtLoginId.requestFocus();
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
        }
        return true;
    }

    private void login() {
        String aEncryptedPassword = EncryptDecryptHelper.encryptIt(mEtPassword.getText().toString().trim());
        User aAdminUser = (User) GsonHelper.getGson(FLPreferences.getInstance(FLApplication.getInstance()).getAdminUser(), User.class);

        if (mEtLoginId.getText().toString().trim().equals(aAdminUser.username) && aEncryptedPassword.equals(aAdminUser.password)) {
            AdminLoginDialog.this.dismiss();
            Intent aIntent = new Intent(mContext, AdminDashboardActivity.class);
            mContext.startActivity(aIntent);
        } else {
            new MessageHelper(mContext).showTitleAlertOk(mContext.getString(R.string.error_invalid_data), mContext.getString(R.string.error_invalid_credentials), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtLoginId.requestFocus();
                }
            });
        }

    }
}
