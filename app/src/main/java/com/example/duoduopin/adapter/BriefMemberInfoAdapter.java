package com.example.duoduopin.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.pojo.BriefMemberInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BriefMemberInfoAdapter extends RecyclerView.Adapter<BriefMemberInfoAdapter.MemberInfoViewHolder> {
    private final ArrayList<BriefMemberInfo> memberInfoList;

    public BriefMemberInfoAdapter(ArrayList<BriefMemberInfo> memberInfoList) {
        this.memberInfoList = memberInfoList;
    }

    @NonNull
    @NotNull
    @Override
    public MemberInfoViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tip_item_person, parent, false);
        return new MemberInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MemberInfoViewHolder holder, int position) {
        BriefMemberInfo briefMemberInfo = memberInfoList.get(position);

        String headpath = briefMemberInfo.getHeadpath();
        String nickname = briefMemberInfo.getNickname();
        String userId = briefMemberInfo.getUserId();
        String credit = briefMemberInfo.getCredit();

        holder.tvMemberNickname.setText(nickname);
        holder.tvMemberCredit.setText(credit);
        Bitmap head = BitmapFactory.decodeFile(headpath);
        holder.ivMemberHead.setImageBitmap(head);

        holder.clPersonItem.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("headPath", headpath);
            intent.putExtra("nickname", nickname);
            intent.putExtra("userId", userId);
            intent.putExtra("credit", credit);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return memberInfoList.size();
    }

    public static class MemberInfoViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout clPersonItem;
        ImageView ivMemberHead;
        TextView tvMemberNickname;
        TextView tvMemberCredit;

        public MemberInfoViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            clPersonItem = itemView.findViewById(R.id.cl_person_item);
            ivMemberHead = itemView.findViewById(R.id.iv_member_head);
            tvMemberNickname = itemView.findViewById(R.id.tv_member_nickname);
            tvMemberCredit = itemView.findViewById(R.id.tv_member_credit);
        }
    }
}
