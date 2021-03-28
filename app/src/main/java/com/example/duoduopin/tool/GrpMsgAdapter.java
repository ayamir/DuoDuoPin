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
    private final List<GrpMsgDisplay> grpMsgList;

    public GrpMsgAdapter(List<GrpMsgDisplay> grpMsgList) {
        this.grpMsgList = grpMsgList;
    }

    public static class GrpMsgViewHolder extends RecyclerView.ViewHolder {
        ImageView chatPic;
        TextView msgContent;
        TextView msgTime;
        TextView msgNickname;

        public GrpMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            chatPic = itemView.findViewById(R.id.chat_pic_right);
            msgContent = itemView.findViewById(R.id.msg_right);
            msgTime = itemView.findViewById(R.id.msg_time_left);
            msgNickname = itemView.findViewById(R.id.msg_nickname_left);
        }
    }

    static class GrpMsgViewHolderLeft extends GrpMsgViewHolder {
        ImageView chatPic;
        TextView msgContent;
        TextView msgTime;
        TextView msgNickname;

        public GrpMsgViewHolderLeft(@NonNull View itemView) {
            super(itemView);
            chatPic = itemView.findViewById(R.id.chat_pic_left);
            msgContent = itemView.findViewById(R.id.msg_left);
            msgTime = itemView.findViewById(R.id.msg_time_left);
            msgNickname = itemView.findViewById(R.id.msg_nickname_left);
        }
    }

    static class GrpMsgViewHolderRight extends GrpMsgViewHolder {
        ImageView chatPic;
        TextView msgContent;
        TextView msgTime;
        TextView msgNickname;

        public GrpMsgViewHolderRight(@NonNull View itemView) {
            super(itemView);
            chatPic = itemView.findViewById(R.id.chat_pic_right);
            msgContent = itemView.findViewById(R.id.msg_right);
            msgTime = itemView.findViewById(R.id.msg_time_right);
            msgNickname = itemView.findViewById(R.id.msg_nickname_right);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!grpMsgList.get(position).isMine()) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public GrpMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_left, parent, false);
            final GrpMsgViewHolderLeft holder = new GrpMsgViewHolderLeft(view);
            holder.msgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "点击别人的消息", Toast.LENGTH_SHORT).show();
                }
            });
            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_right, parent, false);
            final GrpMsgViewHolderRight holder = new GrpMsgViewHolderRight(view);
            holder.msgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "点击自己的消息", Toast.LENGTH_SHORT).show();
                }
            });
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GrpMsgViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                GrpMsgViewHolderLeft grpMsgViewHolderLeft = (GrpMsgViewHolderLeft) holder;
                GrpMsgDisplay grpMsgLeft = grpMsgList.get(position);
                grpMsgViewHolderLeft.chatPic.setImageResource(grpMsgLeft.getPicId());
                grpMsgViewHolderLeft.msgContent.setText(grpMsgLeft.getContent());
                grpMsgViewHolderLeft.msgNickname.setText(grpMsgLeft.getNickname());
                grpMsgViewHolderLeft.msgTime.setText(grpMsgLeft.getTime());
                break;
            case 1:
                GrpMsgViewHolderRight grpMsgViewHolderRight = (GrpMsgViewHolderRight) holder;
                GrpMsgDisplay grpMsgRight = grpMsgList.get(position);
                grpMsgViewHolderRight.chatPic.setImageResource(grpMsgRight.getPicId());
                grpMsgViewHolderRight.msgContent.setText(grpMsgRight.getContent());
                grpMsgViewHolderRight.msgNickname.setText(grpMsgRight.getNickname());
                grpMsgViewHolderRight.msgTime.setText(grpMsgRight.getTime());
                break;

        }
    }

    @Override
    public int getItemCount() {
        return grpMsgList.size();
    }

    public void add(GrpMsgDisplay grpMsg) {
        grpMsgList.add(grpMsg);
        notifyItemInserted(grpMsgList.size() + 1);
    }

    public void addHistory(GrpMsgDisplay grpMsg) {
        grpMsgList.add(grpMsg);
        notifyItemInserted(grpMsgList.size() -1);
    }
}
