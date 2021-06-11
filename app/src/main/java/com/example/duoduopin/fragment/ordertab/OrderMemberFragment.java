package com.example.duoduopin.fragment.ordertab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duoduopin.R;
import com.example.duoduopin.adapter.BriefMemberInfoAdapter;
import com.example.duoduopin.bean.BriefMemberInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OrderMemberFragment extends Fragment {
    private ArrayList<BriefMemberInfo> memberInfoList;

    private OrderMemberFragment() {
    }

    public static OrderMemberFragment newInstance(Bundle bundle) {
        OrderMemberFragment memberFragment = new OrderMemberFragment();
        memberFragment.setArguments(bundle);
        return memberFragment;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.one_order_member, container, false);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            memberInfoList = arguments.getParcelableArrayList("memberInfo");
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvOrderMember = view.findViewById(R.id.rv_order_member);
        BriefMemberInfoAdapter adapter = new BriefMemberInfoAdapter(memberInfoList);
        rvOrderMember.setAdapter(adapter);
    }
}