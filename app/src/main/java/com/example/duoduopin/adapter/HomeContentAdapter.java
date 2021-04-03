package com.example.duoduopin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.bean.HomeContent;

import java.util.ArrayList;

public class HomeContentAdapter extends RecyclerView.Adapter<HomeContentAdapter.HomeContentViewHolder> {
    private final ArrayList<HomeContent> contentList;

    public HomeContentAdapter(ArrayList<HomeContent> contentList) {
        this.contentList = contentList;
    }

    public static class HomeContentViewHolder extends RecyclerView.ViewHolder {
        TextView tvNickname;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvCurrentNumber;

        public HomeContentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCurrentNumber = itemView.findViewById(R.id.tv_current_number);
        }
    }

    @NonNull
    @Override
    public HomeContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_item, parent, false);
        final HomeContentViewHolder holder = new HomeContentViewHolder(view);
        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeContentViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
