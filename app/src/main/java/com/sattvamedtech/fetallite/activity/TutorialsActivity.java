package com.sattvamedtech.fetallite.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.sattvamedtech.fetallite.FLApplication;
import com.sattvamedtech.fetallite.FLBaseActivity;
import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.adapter.TutorialsAdapter;
import com.sattvamedtech.fetallite.dialog.CustomerCareDialog;
import com.sattvamedtech.fetallite.helper.Constants;
import com.sattvamedtech.fetallite.helper.SMSHelper;
import com.sattvamedtech.fetallite.model.Tutorial;
import com.sattvamedtech.fetallite.storage.FLPreferences;

import java.io.File;
import java.util.ArrayList;

public class TutorialsActivity extends FLBaseActivity implements View.OnClickListener {

    private Button mBPlayALl, mBContactCare, mBGoToHome;
    private RecyclerView mRvVideos, mRvGuides;
    private ArrayList<Tutorial> mVideoList = new ArrayList<>();
    private ArrayList<Tutorial> mGuideList = new ArrayList<>();
    private TutorialsAdapter mVideosAdapter, mGuidesAdapter;

    private TutorialsAdapter.TutorialItemClickListener mVideoTutorialItemClick = new TutorialsAdapter.TutorialItemClickListener() {
        @Override
        public void onTutorialItemClick(int iPosition) {
            openVideo(mVideoList.get(iPosition));
        }
    };

    private TutorialsAdapter.TutorialItemClickListener mGuideTutorialItemClick = new TutorialsAdapter.TutorialItemClickListener() {
        @Override
        public void onTutorialItemClick(int iPosition) {
            openPdf(mGuideList.get(iPosition));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials);
        initToolbar();
        initView();
        initListeners();
        initTutorialLists();
    }

    private void initToolbar() {
        Toolbar aToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(aToolbar);
    }

    private void initView() {
        mBPlayALl = (Button) findViewById(R.id.bPlayAll);

        mRvVideos = (RecyclerView) findViewById(R.id.rvVideos);
        mRvVideos.setLayoutManager(new LinearLayoutManager(TutorialsActivity.this));
        mRvVideos.addItemDecoration(new DividerItemDecoration(TutorialsActivity.this, DividerItemDecoration.VERTICAL));
        mVideosAdapter = new TutorialsAdapter(TutorialsActivity.this, mVideoList, mVideoTutorialItemClick);
        mRvVideos.setAdapter(mVideosAdapter);

        mRvGuides = (RecyclerView) findViewById(R.id.rvGuides);
        mRvGuides.setLayoutManager(new LinearLayoutManager(TutorialsActivity.this));
        mRvGuides.addItemDecoration(new DividerItemDecoration(TutorialsActivity.this, DividerItemDecoration.VERTICAL));
        mGuidesAdapter = new TutorialsAdapter(TutorialsActivity.this, mGuideList, mGuideTutorialItemClick);
        mRvGuides.setAdapter(mGuidesAdapter);

        mBContactCare = (Button) findViewById(R.id.bContactCare);
        mBGoToHome = (Button) findViewById(R.id.bGoToHome);
    }

    private void initListeners() {
        mBPlayALl.setOnClickListener(this);
        mBContactCare.setOnClickListener(this);
        mBGoToHome.setOnClickListener(this);
    }

    private void initTutorialLists() {
        initVideoTutorialList();
        initGuideTutorialList();
    }

    private void initVideoTutorialList() {
        mVideoList.clear();
        mVideoList.add(new Tutorial(1, "Setting up Sattva Fetal Lite", "lorem_ipsum.mp4", true));
        mVideoList.add(new Tutorial(2, "Attaching the electrodes", "lorem_ipsum.mp4", true));
        mVideoList.add(new Tutorial(3, "Placing the sensor unit on the mother", "lorem_ipsum.mp4", true));
        mVideosAdapter.notifyDataSetChanged();
    }

    private void initGuideTutorialList() {
        mGuideList.clear();
        mGuideList.add(new Tutorial(1, "Example guide topic one", "lorem_ipsum.pdf", false));
        mGuideList.add(new Tutorial(2, "Example guide topic two", "lorem_ipsum.pdf", false));
        mGuideList.add(new Tutorial(3, "Example guide topic three", "lorem_ipsum.pdf", false));
        mGuidesAdapter.notifyDataSetChanged();
    }

    private void openVideo(Tutorial iVideo) {
        File aVideo = new File(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + iVideo.fileName);
        Intent aIntent = new Intent(Intent.ACTION_VIEW);
        aIntent.setDataAndType(Uri.fromFile(aVideo), "video/*");
        aIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(aIntent);
    }

    private void openPdf(Tutorial iGuide) {
        File aPdf = new File(Environment.getExternalStorageDirectory().getPath() + "/sattva" + File.separator + iGuide.fileName);
        Intent aIntent = new Intent(Intent.ACTION_VIEW);
        aIntent.setDataAndType(Uri.fromFile(aPdf), "application/pdf");
        aIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(aIntent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bContactCare) {
            sendSmsForCustomerCare();
        } else if (view.getId() == R.id.bGoToHome) {
            if (!FLPreferences.getInstance(FLApplication.getInstance()).getTutorialSeen()) {
                FLPreferences.getInstance(FLApplication.getInstance()).setTutorialSeen(true);
                Intent aIntent;
                if (TextUtils.isEmpty(FLPreferences.getInstance(FLApplication.getInstance()).getLoginSessionUserJson())) {
                    aIntent = new Intent(TutorialsActivity.this, LoginActivity.class);
                } else {
                    aIntent = new Intent(TutorialsActivity.this, HomeActivity.class);
                }
                finish();
                startActivity(aIntent);
            } else {
                finish();
            }
        }
    }

    private void sendSmsForCustomerCare() {
        String aTicketNumber = String.valueOf(System.currentTimeMillis());
        new CustomerCareDialog(TutorialsActivity.this, aTicketNumber).show();
        SMSHelper.sendSMS(TutorialsActivity.this, Constants.CC_PHONE_NUMBER, aTicketNumber, false);
    }
}
