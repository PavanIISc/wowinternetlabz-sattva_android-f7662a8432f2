package com.sattvamedtech.fetallite.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.sattvamedtech.fetallite.FLBaseActivity;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.dialog.AddHospitalDialog;
import com.sattvamedtech.fetallite.fragment.AdminDashboardFragment;
import com.sattvamedtech.fetallite.fragment.HospitalDetailsFragment;
import com.sattvamedtech.fetallite.model.Hospital;

public class AdminDashboardActivity extends FLBaseActivity implements FragmentManager.OnBackStackChangedListener {

    private AddHospitalDialog mAddHospitalDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        initToolbar();
        mAddHospitalDialog = new AddHospitalDialog(this);
        mAddHospitalDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mAddHospitalDialog.clearFields();
                refreshAdminDashboardFragment();
                refreshHospitalDetailsFragment();
                if (getCurrentFragment() instanceof AdminDashboardFragment)
                    ((AdminDashboardFragment) getCurrentFragment()).openLatestHospitalDetails();
            }
        });
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        addFragment(new AdminDashboardFragment(), false);
    }

    private void initToolbar() {
        Toolbar aToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(aToolbar);
    }

    public void addFragment(Fragment iFragment, boolean iAddToBackStack) {
        FragmentTransaction aTransaction = getSupportFragmentManager().beginTransaction();
        if (iAddToBackStack) {
            aTransaction.add(R.id.flFragmentContainer, iFragment);
            aTransaction.addToBackStack(null);
        } else {
            aTransaction.replace(R.id.flFragmentContainer, iFragment);
        }
        aTransaction.commit();
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.flFragmentContainer);
    }

    public void showAddHospitalDialog() {
        mAddHospitalDialog.show();
    }

    public void showEditHospitalDialog(Hospital iHospital) {
        mAddHospitalDialog.showEdit(iHospital);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void refreshAdminDashboardFragment() {
        if (getCurrentFragment() instanceof AdminDashboardFragment) {
            ((AdminDashboardFragment) getCurrentFragment()).fetchHospitals();
        }
    }

    private void refreshHospitalDetailsFragment() {
        if (getCurrentFragment() instanceof HospitalDetailsFragment) {
            ((HospitalDetailsFragment) getCurrentFragment()).setHospital();
        }
    }

    @Override
    public void onBackStackChanged() {
        refreshAdminDashboardFragment();
    }
}
