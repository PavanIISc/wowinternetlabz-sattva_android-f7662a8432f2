package com.sattvamedtech.fetallite.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sattvamedtech.fetallite.R;
import com.sattvamedtech.fetallite.model.User;

import java.util.ArrayList;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.UserHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<User> mUserList;
    private DoctorClickListener mDoctorClickListener;
    private boolean toShowEdit;

    public DoctorAdapter(Context iContext, ArrayList<User> iDoctorList, DoctorClickListener iDoctorClickListener, boolean toShowEdit) {
        mContext = iContext;
        mInflater = LayoutInflater.from(iContext);
        mUserList = iDoctorList;
        mDoctorClickListener = iDoctorClickListener;
        this.toShowEdit = toShowEdit;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(mInflater.inflate(R.layout.item_user_doctor, parent, false));
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        holder.mTvName.setText(mUserList.get(position).username);
        holder.mTvPhone.setText(mUserList.get(position).phoneNumber);
        holder.mTvPhone.setVisibility(View.VISIBLE);
        holder.mIvEdit.setVisibility(toShowEdit ? View.VISIBLE : View.GONE);
        holder.mIvDelete.setVisibility(toShowEdit ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder {

        TextView mTvName, mTvPhone;
        ImageView mIvEdit, mIvDelete;

        public UserHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tvName);
            mTvPhone = (TextView) itemView.findViewById(R.id.tvPhone);
            mIvEdit = (ImageView) itemView.findViewById(R.id.ivEdit);
            mIvDelete = (ImageView) itemView.findViewById(R.id.ivDelete);

            mIvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDoctorClickListener.onEditClick(getAdapterPosition());
                }
            });
            mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDoctorClickListener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }

    public interface DoctorClickListener {
        void onEditClick(int iPosition);

        void onDeleteClick(int iPosition);
    }
}
