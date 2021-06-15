package com.example.duoduopin.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class CreditAdapter extends RecyclerView.Adapter<CreditAdapter.CreditItemViewHolder> {
    private final ArrayList<String> creditNicknameList;
    private final ArrayList<String> creditUserIdList;
    private final HashMap<String, Integer> creditMap = new HashMap<>();

    public CreditAdapter(ArrayList<String> creditNicknameList, ArrayList<String> creditUserIdList) {
        this.creditNicknameList = creditNicknameList;
        this.creditUserIdList = creditUserIdList;
    }

    public HashMap<String, Integer> getCreditMap() {
        return creditMap;
    }

    @NonNull
    @NotNull
    @Override
    public CreditItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tip_item_credit, parent, false);
        return new CreditItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CreditItemViewHolder holder, int position) {
        String TAG = "CreditAdapter";
        Log.e(TAG, "nicknameList size: " + creditNicknameList.size() + ", userIdList size: " + creditUserIdList.size());
        String nickname = creditNicknameList.get(position);
        holder.tvCreditNickname.setText(nickname);

        holder.rbCredit.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                creditMap.put(creditUserIdList.get(position), (int) (rating * 10));
            }
        });
    }

    @Override
    public int getItemCount() {
        return creditNicknameList.size();
    }

    public static class CreditItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvCreditNickname;
        RatingBar rbCredit;

        public CreditItemViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvCreditNickname = itemView.findViewById(R.id.tv_credit_nickname);
            rbCredit = itemView.findViewById(R.id.rb_credit);
        }
    }
}
