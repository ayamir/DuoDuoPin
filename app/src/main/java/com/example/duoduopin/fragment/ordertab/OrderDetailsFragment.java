package com.example.duoduopin.fragment.ordertab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duoduopin.R;

import org.jetbrains.annotations.NotNull;

public class OrderDetailsFragment extends Fragment {
    private String userIdString;
    private String typeString;
    private String orderIdString;
    private String priceString;
    private String curPeopleString;
    private String maxPeopleString;
    private String timeString;
    private String addressString;

    private OrderDetailsFragment() {
    }

    public static OrderDetailsFragment newInstance(Bundle bundle) {
        OrderDetailsFragment detailsFragment = new OrderDetailsFragment();
        detailsFragment.setArguments(bundle);
        return detailsFragment;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.tab_order_details, container, false);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            userIdString = arguments.getString("userId");
            typeString = arguments.getString("type");
            orderIdString = arguments.getString("orderId");
            priceString = arguments.getString("price");
            curPeopleString = arguments.getString("curPeople");
            maxPeopleString = arguments.getString("maxPeople");
            timeString = arguments.getString("time");
            addressString = arguments.getString("address");
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvUserId = view.findViewById(R.id.tv_order_user_id);
        tvUserId.setText(userIdString);

        TextView tvType = view.findViewById(R.id.tv_order_type);
        tvType.setText(typeString);

        TextView tvOrderId = view.findViewById(R.id.tv_order_id);
        tvOrderId.setText(orderIdString);

        TextView tvPrice = view.findViewById(R.id.tv_order_price);
        tvPrice.setText(priceString);

        TextView tvCurPeople = view.findViewById(R.id.tv_order_cur_people);
        tvCurPeople.setText(curPeopleString);

        TextView tvMaxPeople = view.findViewById(R.id.tv_order_max_people);
        tvMaxPeople.setText(maxPeopleString);

        TextView tvTime = view.findViewById(R.id.tv_order_time);
        tvTime.setText(timeString);

        TextView tvAddress = view.findViewById(R.id.tv_order_address);
        tvAddress.setText(addressString);
    }
}
