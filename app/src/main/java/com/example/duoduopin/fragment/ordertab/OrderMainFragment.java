package com.example.duoduopin.fragment.ordertab;

import android.graphics.BitmapFactory;
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
import com.liulishuo.filedownloader.FileDownloader;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.example.duoduopin.activity.MainActivity.basePath;

public class OrderMainFragment extends Fragment {
    private String userIdString;
    private String typeString;
    private String orderIdString;
    private String imageUrlString = "";
    private String imagePath = "";
    private String priceString;
    private String titleString;
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
            userIdString = arguments.getString("userId");
            typeString = arguments.getString("type");
            orderIdString = arguments.getString("orderIdString");
            imageUrlString = arguments.getString("imageUrl");
            imagePath = downloadImage(imageUrlString);
            priceString = arguments.getString("price");
            titleString = arguments.getString("title");
            descriptionString = arguments.getString("description");
            nicknameString = arguments.getString("nickname");
            creditString = arguments.getString("credit");
        }
    }

    private String downloadImage(String imageUrl) {
        String filepath = "";
        if (imageUrl != null) {
            if (!imageUrl.isEmpty()) {
                String format = imageUrl.substring(imageUrl.lastIndexOf('.'));
                filepath = basePath + File.separator + orderIdString + "_order" + format;
                FileDownloader.getImpl().create(imageUrl).setPath(filepath).start();
            }
        }
        return filepath;
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_order_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivImage = view.findViewById(R.id.iv_image);
        if (typeString.equals("拼车")) {
            ivImage.setVisibility(View.INVISIBLE);
        } else {
            ivImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        }

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(titleString);

        TextView tvDescription = view.findViewById(R.id.tv_description);
        tvDescription.setText(descriptionString);

        TextView tvPrice = view.findViewById(R.id.tv_price);
        tvPrice.setText(priceString);

        TextView tvOrderNickname = view.findViewById(R.id.tv_order_nickname);
        tvOrderNickname.setText(nicknameString);

        TextView tvOrderCredit = view.findViewById(R.id.tv_order_credit);
        tvOrderCredit.setText(creditString);
    }
}
