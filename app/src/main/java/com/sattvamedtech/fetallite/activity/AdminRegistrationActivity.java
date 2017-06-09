package com.sattvamedtech.fetallite.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.FLBaseActivity;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.helper.EncryptDecryptHelper;
import com.sattvamedtech.fetallite.helper.GsonHelper;
import com.sattvamedtech.fetallite.helper.MessageHelper;
import com.sattvamedtech.fetallite.helper.SMSHelper;
import com.sattvamedtech.fetallite.model.User;
import com.sattvamedtech.fetallite.storage.FLPreferences;

import java.util.UUID;

public class AdminRegistrationActivity extends FLBaseActivity implements View.OnClickListener {

    private TextInputEditText mEtLoginId, mEtPassword, mEtConfirmPassword, mEtPhoneNumber, mEtEmail;
    private Button mBNext;
    private String mSmsMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_registration);
        initToolbar();
        initView();
        initListeners();
    }

    private void initToolbar() {
        Toolbar aToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(aToolbar);
    }

    private void initView() {
        mEtLoginId = (TextInputEditText) findViewById(R.id.etLoginId);
        mEtPassword = (TextInputEditText) findViewById(R.id.etPassword);
        mEtConfirmPassword = (TextInputEditText) findViewById(R.id.etConfirmPassword);
        mEtPhoneNumber = (TextInputEditText) findViewById(R.id.etPhoneNumber);
        mEtEmail = (TextInputEditText) findViewById(R.id.etEmail);
        mBNext = (Button) findViewById(R.id.bNext);
    }

    private void initListeners() {
        mBNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bNext) {
            if (validParams()) {
                String aEncryptedPassword = EncryptDecryptHelper.encryptIt(mEtPassword.getText().toString().trim());
                User aUser = new User(mEtLoginId.getText().toString().trim(), aEncryptedPassword, mEtPhoneNumber.getText().toString().trim(), mEtEmail.getText().toString().trim(), User.TYPE_USER);
                FLPreferences.getInstance(FLApplication.getInstance()).setAdminUser(GsonHelper.toUserJson(aUser));
                FLPreferences.getInstance(FLApplication.getInstance()).setAdminPassword(aEncryptedPassword);
                String aDeviceId = "deviceId-" + UUID.randomUUID().toString();
                FLPreferences.getInstance(FLApplication.getInstance()).setDeviceId(aDeviceId);
                long aTimeStamp = System.currentTimeMillis();
                FLPreferences.getInstance(FLApplication.getInstance()).setRegistrationDate(aTimeStamp);
                FLPreferences.getInstance(FLApplication.getInstance()).setLastServiceDate(aTimeStamp);
                prepareForSms();
                showRegistrationConfirmation();
            }
        }
    }

    private boolean validParams() {
        if (TextUtils.isEmpty(mEtLoginId.getText().toString().trim())) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_login_id_required), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtLoginId.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtPassword.getText().toString().trim())) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_password_required), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPassword.requestFocus();
                }
            });
            return false;
        } else if (mEtPassword.getText().toString().trim().length() < Constants.MIN_PASSWD_LENGTH) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_password_min_length, Constants.MIN_PASSWD_LENGTH), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPassword.requestFocus();
                }
            });
            return false;
        } else if (TextUtils.isEmpty(mEtConfirmPassword.getText().toString().trim())) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_confirm_password_required), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtConfirmPassword.requestFocus();
                }
            });
            return false;
        } else if (mEtConfirmPassword.getText().toString().trim().length() < Constants.MIN_PASSWD_LENGTH) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_confirm_password_min_length, Constants.MIN_PASSWD_LENGTH), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtConfirmPassword.requestFocus();
                }
            });
            return false;
        } else if (!mEtPassword.getText().toString().equals(mEtConfirmPassword.getText().toString())) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_password_mismatch), "", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mEtPassword.requestFocus();
                }
            });
            return false;
        } else if (!TextUtils.isEmpty(mEtEmail.getText().toString().trim()) && !Patterns.EMAIL_ADDRESS.matcher(mEtEmail.getText().toString().trim()).matches()) {
            new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.error_invalid_data), getString(R.string.error_invalid_email), "", new DialogInterface.OnClickListener() {
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

    private void prepareForSms() {
        mSmsMessage = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSmsPermission()) {
                SMSHelper.sendSMS(AdminRegistrationActivity.this, Constants.SMS_PHONE_NUMBER, mSmsMessage, false);
            }
        } else {
            SMSHelper.sendSMS(AdminRegistrationActivity.this, Constants.SMS_PHONE_NUMBER, mSmsMessage, false);
        }
    }

    private boolean checkSmsPermission() {
        if (ActivityCompat.checkSelfPermission(AdminRegistrationActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(AdminRegistrationActivity.this, new String[]{Manifest.permission.SEND_SMS}, Constants.RC_SEND_SMS);
            }
            return false;
        }
        return true;
    }

    private void showRegistrationConfirmation() {
        new MessageHelper(AdminRegistrationActivity.this).showTitleAlertOk(getString(R.string.success_registration), getString(R.string.success_thank_you, ""), getString(R.string.action_setup_admin_dash), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAdminDashboard(dialogInterface);
            }
        });
    }

    private void openAdminDashboard(DialogInterface iDialogInterface) {
        iDialogInterface.dismiss();
        Intent aIntent = new Intent(AdminRegistrationActivity.this, AdminDashboardActivity.class);
        finish();
        startActivity(aIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.RC_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SMSHelper.sendSMS(AdminRegistrationActivity.this, Constants.SMS_PHONE_NUMBER, mSmsMessage, false);
            }
        }
    }
}
