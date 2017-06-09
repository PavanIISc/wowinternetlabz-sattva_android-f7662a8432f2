package com.sattvamedtech.fetallite.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.helper.EncryptDecryptHelper;
import com.sattvamedtech.fetallite.helper.GsonHelper;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.DatabaseHelper;
import com.sattvamedtech.fetallite.storage.FLPreferences;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtUsername, mEtPassword;
    private TextView mTvForgotPassword;
    private Button mBLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListeners();
    }

    private void initView() {
        mEtUsername = (EditText) findViewById(R.id.etUsername);
        mEtPassword = (EditText) findViewById(R.id.etPassword);
        mTvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        mBLogin = (Button) findViewById(R.id.bLogin);
    }

    private void initListeners() {
        mTvForgotPassword.setOnClickListener(this);
        mBLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tvForgotPassword) {

        } else if (view.getId() == R.id.bLogin) {
            if (validParams()) {
                loginAndProceed();
            }
        }
    }

    private boolean validParams() {
        if (TextUtils.isEmpty(mEtUsername.getText().toString().trim())) {
            new MessageHelper(LoginActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_login_id_required), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtUsername.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtPassword.getText().toString().trim())) {
            new MessageHelper(LoginActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_password_required), "", new DialogInterface.OnClickListener() {
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

    private void loginAndProceed() {
        String aEncryptedPassword = EncryptDecryptHelper.encryptIt(mEtPassword.getText().toString().trim());
        User aUser = DatabaseHelper.getInstance(FLApplication.getInstance()).validUserCredentials(mEtUsername.getText().toString().trim(), aEncryptedPassword);

        if (aUser == null) {
            new MessageHelper(LoginActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_invalid_credentials), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtUsername.requestFocus();
                }
            });
        } else {
            FLPreferences.getInstance(FLApplication.getInstance()).setLoginSessionTimestamp(System.currentTimeMillis());
            FLPreferences.getInstance(FLApplication.getInstance()).setLoginSessionUser(GsonHelper.toUserJson(aUser));
            Intent aIntent = new Intent(LoginActivity.this, HomeActivity.class);
            finish();
            startActivity(aIntent);
        }
    }
}
