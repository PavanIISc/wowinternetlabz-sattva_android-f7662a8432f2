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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<User> mUserList;
    private UserClickListener mUserClickListener;

    public UserAdapter(Context iContext, ArrayList<User> iUserList, UserClickListener iUserClickListener) {
        mContext = iContext;
        mInflater = LayoutInflater.from(iContext);
        mUserList = iUserList;
        mUserClickListener = iUserClickListener;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(mInflater.inflate(R.layout.item_user_doctor, parent, false));
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        holder.mTvName.setText(mUserList.get(position).username);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder {

        TextView mTvName;
        ImageView mIvEdit, mIvDelete;

        public UserHolder(View itemView) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tvName);
            mIvEdit = (ImageView) itemView.findViewById(R.id.ivEdit);
            mIvDelete = (ImageView) itemView.findViewById(R.id.ivDelete);
            mIvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserClickListener.onEditClick(getAdapterPosition());
                }
            });
            mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserClickListener.onDeleteClick(getAdapterPosition());
                }
            });
        }
    }

    public interface UserClickListener {
        void onEditClick(int iPosition);

        void onDeleteClick(int iPosition);
    }
}
