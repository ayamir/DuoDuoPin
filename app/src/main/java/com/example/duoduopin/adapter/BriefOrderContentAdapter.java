package com.example.duoduopin.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.activity.order.OneOrderCaseActivity;
import com.example.duoduopin.pojo.OrderContent;

import java.util.ArrayList;

public class BriefOrderContentAdapter extends RecyclerView.Adapter<BriefOrderContentAdapter.HomeContentViewHolder> {
    private final ArrayList<OrderContent> orderContentList;

    public BriefOrderContentAdapter(ArrayList<OrderContent> orderContentList) {
        this.orderContentList = orderContentList;
    }

    @NonNull
    @Override
    public HomeContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tip_item_rec, parent, false);
        return new HomeContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeContentViewHolder holder, int position) {
        final OrderContent orderContent = orderContentList.get(position);
        holder.tvTitle.setText(orderContent.getTitle());
        holder.tvNickname.setText(orderContent.getNickname());
        holder.tvDescription.setText(orderContent.getDescription());
        holder.tvCurrentNumber.setText(orderContent.getCurPeople());

        holder.rlOneRecOrder.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OneOrderCaseActivity.class);
            intent.putExtra("orderId", orderContent.getBillId());
            intent.putExtra("userId", orderContent.getUserId());
            intent.putExtra("nickname", orderContent.getNickname());
            intent.putExtra("type", orderContent.getType());
            intent.putExtra("price", orderContent.getPrice());
            intent.putExtra("address", orderContent.getAddress());
            intent.putExtra("curPeople", orderContent.getCurPeople());
            intent.putExtra("maxPeople", orderContent.getMaxPeople());
            intent.putExtra("time", orderContent.getTime());
            intent.putExtra("description", orderContent.getDescription());
            intent.putExtra("title", orderContent.getTitle());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderContentList.size();
    }

    public static class HomeContentViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlOneRecOrder;
        TextView tvNickname;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvCurrentNumber;

        public HomeContentViewHolder(@NonNull View itemView) {
            super(itemView);
            rlOneRecOrder = itemView.findViewById(R.id.rl_one_rec_order);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCurrentNumber = itemView.findViewById(R.id.tv_current_number);
        }
    }
}
