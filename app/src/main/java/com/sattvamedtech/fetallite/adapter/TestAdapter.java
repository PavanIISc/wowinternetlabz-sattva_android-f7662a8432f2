package com.sattvamedtech.fetallite.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.helper.DateUtils;
import com.sattvamedtech.fetallite.model.Test;

import java.util.ArrayList;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Test> mTestList;
    private TestClickListener mTestClickListener;

    public TestAdapter(Context iContext, ArrayList<Test> iTestList, TestClickListener iTestClickListener) {
        mContext = iContext;
        mInflater = LayoutInflater.from(iContext);
        mTestList = iTestList;
        mTestClickListener = iTestClickListener;
    }

    @Override
    public TestHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TestHolder(mInflater.inflate(R.layout.item_test_list, parent, false));
    }

    @Override
    public void onBindViewHolder(TestHolder holder, int position) {
        holder.mTvTestId.setText(mTestList.get(position).id);
        holder.mTvPatientNameId.setText(mTestList.get(position).patient.firstName + " " + mTestList.get(position).patient.lastName + " | " + mTestList.get(position).patient.id);
        holder.mTvDoB.setText(DateUtils.convertDateToLongHumanReadable(mTestList.get(position).patient.dob));
        holder.mTvTestResult.setText(mContext.getString(R.string.label_test_result_format, "123", "45", "67"));
        holder.mTvDate.setText(DateUtils.convertDateToLongHumanReadable(mTestList.get(position).timeStamp));
        holder.mTvTime.setText(DateUtils.convertTimeToHumanReadable(mTestList.get(position).timeStamp));
    }

    @Override
    public int getItemCount() {
        return mTestList.size();
    }

    public class TestHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLlRoot;
        private TextView mTvTestId, mTvPatientNameId, mTvDoB, mTvTestResult, mTvDate, mTvTime;

        public TestHolder(View itemView) {
            super(itemView);
            mLlRoot = (LinearLayout) itemView.findViewById(R.id.llRoot);
            mTvTestId = (TextView) itemView.findViewById(R.id.tvTestId);
            mTvPatientNameId = (TextView) itemView.findViewById(R.id.tvPatientNameId);
            mTvDoB = (TextView) itemView.findViewById(R.id.tvDoB);
            mTvTestResult = (TextView) itemView.findViewById(R.id.tvTestResult);
            mTvDate = (TextView) itemView.findViewById(R.id.tvDate);
            mTvTime = (TextView) itemView.findViewById(R.id.tvTime);
            mLlRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTestClickListener.onTestClick(getAdapterPosition());
                }
            });
        }
    }

    public interface TestClickListener {
        void onTestClick(int iPosition);
    }
}
