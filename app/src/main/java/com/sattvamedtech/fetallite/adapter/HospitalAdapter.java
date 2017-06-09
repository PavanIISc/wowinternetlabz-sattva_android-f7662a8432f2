package com.sattvamedtech.fetallite.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.model.Hospital;

import java.util.ArrayList;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Hospital> mHospitalList;
    private HospitalClickListener mHospitalClickListener;

    public HospitalAdapter(Context iContext, ArrayList<Hospital> iHospitalList, HospitalClickListener iHospitalClickListener) {
        mContext = iContext;
        mInflater = LayoutInflater.from(iContext);
        mHospitalList = iHospitalList;
        mHospitalClickListener = iHospitalClickListener;
    }

    @Override
    public HospitalHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HospitalHolder(mInflater.inflate(R.layout.item_hospital_list, parent, false));
    }

    @Override
    public void onBindViewHolder(HospitalHolder holder, int position) {
        holder.mTvTitle.setText(mHospitalList.get(position).name);
    }

    @Override
    public int getItemCount() {
        return mHospitalList.size();
    }

    public class HospitalHolder extends RecyclerView.ViewHolder {

        LinearLayout mLlRoot;
        TextView mTvTitle;

        public HospitalHolder(View itemView) {
            super(itemView);
            mLlRoot = (LinearLayout) itemView.findViewById(R.id.llRoot);
            mTvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            mLlRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHospitalClickListener.onHospitalClick(getAdapterPosition());
                }
            });
        }
    }

    public interface HospitalClickListener {
        void onHospitalClick(int iPosition);
    }
}
