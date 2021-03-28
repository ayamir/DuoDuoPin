package com.example.duoduopin.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.duoduopin.R;
import com.example.duoduopin.activity.AssistantLocationActivity;
import com.example.duoduopin.activity.OrderCaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private String searchIdString;
    private EditText searchId;

    private TextView timeStart;
    private TextView timeEnd;
    private EditText minPrice;
    private EditText maxPrice;
    private EditText description;
    private EditText tude;
    private TimePickerView pvTimeStart;
    private TimePickerView pvTimeEnd;

    private String typeString, distanceString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_home, null);
        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton carBtn = getActivity().findViewById(R.id.carBtn);
        carBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "等待进一步开发...", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton orderBtn = getActivity().findViewById(R.id.orderBtn);
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "等待进一步开发...", Toast.LENGTH_SHORT).show();
            }
        });

        EditText searchBar = getActivity().findViewById(R.id.searchBar);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "等待进一步开发...", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView selectLogo = getActivity().findViewById(R.id.selectLogo);
        selectLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "请从屏幕左边缘向右滑动", Toast.LENGTH_SHORT).show();
            }
        });

        TextView select = getActivity().findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "请从屏幕左边缘向右滑动", Toast.LENGTH_SHORT).show();
            }
        });

        searchId = getActivity().findViewById(R.id.searchId);

        Button searchByUserId = getActivity().findViewById(R.id.searchByUserId);
        searchByUserId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIdString = searchId.getText().toString();
                if (searchIdString.isEmpty()) {
                    Toast.makeText(getActivity(), "请输入用户id", Toast.LENGTH_SHORT).show();
                } else {
                    Intent toIntent = new Intent(v.getContext(), OrderCaseActivity.class);
                    toIntent.putExtra("from", "userId");
                    toIntent.putExtra("userId", searchIdString);
                    toIntent.putExtra("type", "all");
                    startActivity(toIntent);
                }
            }
        });
        Button searchByOrderId = getActivity().findViewById(R.id.searchByOrderId);
        searchByOrderId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIdString = searchId.getText().toString();
                if (searchIdString.isEmpty()) {
                    Toast.makeText(getActivity(), "请输入商品id", Toast.LENGTH_SHORT).show();
                } else {
                    Intent toIntent = new Intent(v.getContext(), OrderCaseActivity.class);
                    toIntent.putExtra("from", "orderId");
                    toIntent.putExtra("orderId", searchIdString);
                    toIntent.putExtra("type", "all");
                    startActivity(toIntent);
                }
            }
        });

        Spinner typeSpinner = getActivity().findViewById(R.id.type);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.itemType, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("拼车")) {
                    typeString = "CAR";
                } else if (selected.equals("拼单")) {
                    typeString = "BILL";
                } else {
                    typeString = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                typeString = null;
            }
        });

        timeStart = getActivity().findViewById(R.id.timeStart);
        timeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTimeStart.show();
            }
        });

        timeEnd = getActivity().findViewById(R.id.timeEnd);
        timeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTimeEnd.show();
            }
        });
        initTimeStartPicker();
        initTimeEndPicker();

        Spinner distanceSpinner = Objects.requireNonNull(getActivity()).findViewById(R.id.distance);
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.distanceType, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);
        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                switch (selected) {
                    case "不限制":
                        distanceString = "";
                        break;
                    case "500米":
                        distanceString = "M500";
                        break;
                    case "1千米":
                        distanceString = "KM1";
                        break;
                    case "2千米":
                        distanceString = "KM2";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                distanceString = null;
            }
        });

        Button location = getActivity().findViewById(R.id.locationSearch);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AssistantLocationActivity.class);
                startActivity(intent);
            }
        });

        minPrice = getActivity().findViewById(R.id.minPrice);
        maxPrice = getActivity().findViewById(R.id.maxPrice);
        description = getActivity().findViewById(R.id.description);
        tude = getActivity().findViewById(R.id.tudeSearch);

        Button submitSearch = getActivity().findViewById(R.id.submitSearch);
        submitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeStartString = timeStart.getText().toString();
                String timeEndString = timeEnd.getText().toString();
                String minPriceString = minPrice.getText().toString();
                String maxPriceString = maxPrice.getText().toString();
                String descriptionString = description.getText().toString();
                String tudeString = tude.getText().toString();
                if (tudeString.isEmpty()) {
                    Toast.makeText(getActivity(), "请输入经纬度", Toast.LENGTH_SHORT).show();
                } else {
                    String[] tudeArray = tudeString.split(",");
                    if (tudeArray.length == 1) {
                        Toast.makeText(getActivity(), "请用,分隔经纬度", Toast.LENGTH_SHORT).show();
                    } else {
                        String longitudeString = tudeArray[0];
                        String latitudeString = tudeArray[1];
                        Intent toIntent = new Intent(getActivity(), OrderCaseActivity.class);
                        toIntent.putExtra("from", "info");
                        toIntent.putExtra("type", "all");
                        toIntent.putExtra("timeStart", timeStartString.replace(' ', 'T'));
                        toIntent.putExtra("timeEnd", timeEndString.replace(' ', 'T'));
                        toIntent.putExtra("minPrice", minPriceString);
                        toIntent.putExtra("maxPrice", maxPriceString);
                        toIntent.putExtra("description", descriptionString);
                        toIntent.putExtra("orderType", typeString);
                        toIntent.putExtra("distance", distanceString);
                        toIntent.putExtra("longitude", longitudeString);
                        toIntent.putExtra("latitude", latitudeString);
                        Log.d("toOrderActivity", toIntent.toString());
                        startActivity(toIntent);
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private void initTimeStartPicker() {//Dialog 模式下，在底部弹出
        pvTimeStart = new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTimeSelect(Date date, View v) {
                Toast.makeText(getActivity(), getTime(date), Toast.LENGTH_SHORT).show();
                Log.i("pvTime", "onTimeSelect");
                timeStart.setText(getTime(date));
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                        Log.i("pvTime", "onTimeSelectChanged");
                    }
                })
                .setType(new boolean[]{true, true, true, true, true, true})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .setLineSpacingMultiplier(2.0f)
                .build();

        Dialog mDialog = pvTimeStart.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTimeStart.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f);
            }
        }
    }

    private void initTimeEndPicker() {//Dialog 模式下，在底部弹出
        pvTimeEnd = new TimePickerBuilder(getActivity(), new OnTimeSelectListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTimeSelect(Date date, View v) {
                Toast.makeText(getActivity(), getTime(date), Toast.LENGTH_SHORT).show();
                Log.i("pvTime", "onTimeSelect");
                timeEnd.setText(getTime(date));
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                        Log.i("pvTime", "onTimeSelectChanged");
                    }
                })
                .setType(new boolean[]{true, true, true, true, true, true})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .setLineSpacingMultiplier(2.0f)
                .build();

        Dialog mDialog = pvTimeEnd.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTimeEnd.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f);
            }
        }
    }
}
