package com.example.duoduopin.fragment.ordertab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duoduopin.R;

import org.jetbrains.annotations.NotNull;

public class OrderMainFragment extends Fragment {
    private String imageUrlString;
    private String priceString;
    private String titleStrng;
    private String descriptionString;

    private String headPath;
    private String nicknameString;
    private String creditString;

    private OrderMainFragment() {
    }

    public static OrderMainFragment newInstance(Bundle bundle) {
        OrderMainFragment orderMainFragment = new OrderMainFragment();
        orderMainFragment.setArguments(bundle);
        return orderMainFragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (getArguments() != null) {
            imageUrlString = arguments.getString("imageUrl");
            priceString = arguments.getString("price");
            titleStrng = arguments.getString("title");
            descriptionString = arguments.getString("description");
            headPath = arguments.getString("headPath");
            nicknameString = arguments.getString("nickname");
            creditString = arguments.getString("credit");
        }
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.one_order_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(titleStrng);

        TextView tvDescription = view.findViewById(R.id.tv_description);
        tvDescription.setText(descriptionString);

        TextView tvPrice = view.findViewById(R.id.tv_price);
        tvPrice.setText(priceString);

        ImageView ivOrderHead = view.findViewById(R.id.iv_order_head);
        // TODO: setImageRes with headPath

        TextView tvOrderNickname = view.findViewById(R.id.tv_order_nickname);
        tvOrderNickname.setText(nicknameString);

        TextView tvOrderCredit = view.findViewById(R.id.tv_order_credit);
        tvOrderCredit.setText(creditString);
    }
}
