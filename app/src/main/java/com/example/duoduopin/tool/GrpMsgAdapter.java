package com.example.duoduopin.tool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.bean.GrpMsgDisplay;

import java.util.List;

public class GrpMsgAdapter extends RecyclerView.Adapter<GrpMsgAdapter.GrpMsgViewHolder> {
    private List<GrpMsgDisplay> grpMsgList;

    public GrpMsgAdapter(List<GrpMsgDisplay> grpMsgList) {
        this.grpMsgList = grpMsgList;
    }

    public static class GrpMsgViewHolder extends RecyclerView.ViewHolder {
        ImageView chatPic;
        TextView msgContent;
        public GrpMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            chatPic = itemView.findViewById(R.id.chat_pic_right);
            msgContent = itemView.findViewById(R.id.tv_my_msg);
        }
    }

    @NonNull
    @Override
    public GrpMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_right, parent, false);
        final GrpMsgViewHolder holder = new GrpMsgViewHolder(view);
        holder.msgContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "点击自己的消息", Toast.LENGTH_SHORT).show();
            }
        });
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull GrpMsgViewHolder holder, int position) {
        GrpMsgDisplay grpMsg = grpMsgList.get(position);
        holder.chatPic.setImageResource(grpMsg.getPicId());
        holder.msgContent.setText(grpMsg.getContent());
    }

    @Override
    public int getItemCount() {
        return grpMsgList.size();
    }
}
