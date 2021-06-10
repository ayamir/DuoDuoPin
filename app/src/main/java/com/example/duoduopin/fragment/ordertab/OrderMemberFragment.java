package com.example.duoduopin.fragment.ordertab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duoduopin.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderMemberFragment extends Fragment {
    private final ArrayList<HashMap<String, String>> cases = new ArrayList<>();
    private final String nicknameKey = "nickname";
    private final String creditKey = "credit";
    private List<String> memberNicknameList;
    private List<String> memberCreditList;
    private ListView lvOrderMember;

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
        return (ViewGroup) inflater.inflate(R.layout.one_order_member, container, false);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            memberNicknameList = arguments.getStringArrayList("nicknameList");
            memberCreditList = arguments.getStringArrayList("creditList");

            for (int i = 0; i < memberNicknameList.size(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put(nicknameKey, memberNicknameList.get(i));
                map.put(creditKey, memberCreditList.get(i));
                cases.add(map);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvOrderMember = view.findViewById(R.id.lv_order_member);
        SimpleAdapter adapter = new SimpleAdapter(view.getContext(), cases, R.layout.person_item,
                new String[]{nicknameKey, creditKey},
                new int[]{R.id.tv_member_nickname, R.id.tv_member_credit});
        lvOrderMember.setAdapter(adapter);
    }
}
