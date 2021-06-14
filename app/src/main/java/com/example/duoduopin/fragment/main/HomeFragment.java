package com.example.duoduopin.fragment.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.duoduopin.R;
import com.example.duoduopin.activity.order.LocateActivity;
import com.example.duoduopin.activity.order.OrderCaseActivity;
import com.example.duoduopin.adapter.BriefOrderContentAdapter;
import com.example.duoduopin.pojo.OrderContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static com.example.duoduopin.activity.MainActivity.isLoaded;
import static com.example.duoduopin.activity.MainActivity.recBriefOrderContentList;
import static com.example.duoduopin.activity.MainActivity.recOrderContentList;
import static com.example.duoduopin.tool.Constants.brief_order_content_load_signal;

public class HomeFragment extends Fragment {

    private final ArrayList<OrderContent> recKeywordContentList = new ArrayList<>();
    private final ArrayList<OrderContent> recBillContentList = new ArrayList<>();
    private final ArrayList<OrderContent> recCarContentList = new ArrayList<>();
    private String searchIdString;
    private EditText searchId;
    private TextView timeStart;
    private TextView timeEnd;
    private EditText minPrice;
    private EditText maxPrice;
    private EditText description;
    private EditText etTude;
    private TimePickerView pvTimeStart;
    private TimePickerView pvTimeEnd;
    private String typeString, distanceString;
    private SwipeRefreshLayout srlHomeContent;
    private RecyclerView rvContentList;
    private BriefOrderContentReceiver briefOrderContentReceiver;
    private BriefOrderContentAdapter briefOrderContentAdapter;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bindMainItems();
        bindMenuItems();

        briefOrderContentReceiver = new BriefOrderContentReceiver();
        IntentFilter intentFilter = new IntentFilter(brief_order_content_load_signal);
        getActivity().registerReceiver(briefOrderContentReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(briefOrderContentReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String tude = data.getStringExtra("tude");
            boolean isChosen = data.getBooleanExtra("isChosen", false);
            if (isChosen) {
                etTude.setText(tude);
            } else {
                etTude.setText("");
                etTude.setHint("请将复制的经纬度粘贴到这里");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void bindMenuItems() {
        searchId = getActivity().findViewById(R.id.searchId);

        Button searchByUserId = getActivity().findViewById(R.id.searchByUserId);
        searchByUserId.setOnClickListener(v -> {
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
        });
        Button searchByOrderId = getActivity().findViewById(R.id.searchByOrderId);
        searchByOrderId.setOnClickListener(v -> {
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
        });
        Spinner typeSpinner = getActivity().findViewById(R.id.type);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.searchItemType, android.R.layout.simple_spinner_item);
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
        timeStart.setOnClickListener(v -> pvTimeStart.show());

        timeEnd = getActivity().findViewById(R.id.timeEnd);
        timeEnd.setOnClickListener(v -> pvTimeEnd.show());
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

        ImageView ivLocate = getActivity().findViewById(R.id.iv_locate);
        ivLocate.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), LocateActivity.class);
            intent.putExtra("address", "");
            intent.putExtra("tude", "");
            intent.putExtra("isChosen", false);
            startActivityForResult(intent, 0);
        });

        minPrice = getActivity().findViewById(R.id.minPrice);
        maxPrice = getActivity().findViewById(R.id.maxPrice);
        description = getActivity().findViewById(R.id.tv_description);
        etTude = getActivity().findViewById(R.id.et_menu_tude);

        Button submitSearch = getActivity().findViewById(R.id.submitSearch);
        submitSearch.setOnClickListener(v -> {
            String timeStartString = timeStart.getText().toString();
            String timeEndString = timeEnd.getText().toString();
            String minPriceString = minPrice.getText().toString();
            String maxPriceString = maxPrice.getText().toString();
            String descriptionString = description.getText().toString();
            String tudeString = etTude.getText().toString();
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
        });
    }

    private void bindMainItems() {
        FloatingActionButton fabOrder = getActivity().findViewById(R.id.fab_order);
        fabOrder.setOnClickListener(v -> displayBillOrCar(true));

        FloatingActionButton fabCar = getActivity().findViewById(R.id.fab_car);
        fabCar.setOnClickListener(v -> displayBillOrCar(false));

        EditText searchBar = getActivity().findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v -> {
            String keyword = searchBar.getText().toString().trim();
            filterByKeyword(keyword);
        });

        ImageView selectLogo = getActivity().findViewById(R.id.selectLogo);
        selectLogo.setOnClickListener(v -> Toast.makeText(getActivity(), "请从屏幕左边缘向右滑动", Toast.LENGTH_SHORT).show());

        TextView select = getActivity().findViewById(R.id.select);
        select.setOnClickListener(v -> Toast.makeText(getActivity(), "请从屏幕左边缘向右滑动", Toast.LENGTH_SHORT).show());

        srlHomeContent = getActivity().findViewById(R.id.srl_home_content);
        if (!isLoaded && recBriefOrderContentList.size() == 0) {
            srlHomeContent.setRefreshing(true);
        }
        srlHomeContent.setOnRefreshListener(() -> {
            if (isLoaded) {
                BriefOrderContentAdapter briefOrderContentAdapter = new BriefOrderContentAdapter(recOrderContentList);
                rvContentList.setAdapter(briefOrderContentAdapter);
                srlHomeContent.setRefreshing(false);
            }
        });

        rvContentList = getActivity().findViewById(R.id.rv_content_list);
        LinearLayoutManager homeContentLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rvContentList.setLayoutManager(homeContentLayoutManager);

        if (recOrderContentList != null) {
            briefOrderContentAdapter = new BriefOrderContentAdapter(recOrderContentList);
            rvContentList.setAdapter(briefOrderContentAdapter);
        }
    }

    private void filterByKeyword(String keyword) {
        if (isLoaded) {
            recKeywordContentList.clear();

            Pattern pn = Pattern.compile(".*" + keyword + ".*");
            for (int i = 0; i < recOrderContentList.size(); i++) {
                String title = recOrderContentList.get(i).getTitle();
                Matcher matcher = pn.matcher(title);
                if (matcher.find()) {
                    recKeywordContentList.add(recOrderContentList.get(i));
                } else {
                    Log.e("filter", "第" + i + "条内容没有被匹配，其标题为" + title);
                }
            }

            BriefOrderContentAdapter briefKeywordContentAdapter = new BriefOrderContentAdapter(recKeywordContentList);
            rvContentList.setAdapter(briefKeywordContentAdapter);
        }
    }

    private void displayBillOrCar(boolean isBill) {
        if (isLoaded) {
            recBillContentList.clear();
            recCarContentList.clear();
            for (int i = 0; i < recOrderContentList.size(); i++) {
                if (recOrderContentList.get(i).getType().equals("BILL")) {
                    recBillContentList.add(recOrderContentList.get(i));
                } else {
                    recCarContentList.add(recOrderContentList.get(i));
                }
            }
            if (isBill) {
                BriefOrderContentAdapter briefBillContentAdapter = new BriefOrderContentAdapter(recBillContentList);
                rvContentList.setAdapter(briefBillContentAdapter);
            } else {
                BriefOrderContentAdapter briefCarContentAdapter = new BriefOrderContentAdapter(recCarContentList);
                rvContentList.setAdapter(briefCarContentAdapter);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initTimeStartPicker() {//Dialog 模式下，在底部弹出
        pvTimeStart = new TimePickerBuilder(getActivity(), (date, v) -> {
            Toast.makeText(getActivity(), getTime(date), Toast.LENGTH_SHORT).show();
            Log.i("pvTime", "onTimeSelect");
            timeStart.setText(getTime(date));
        })
                .setTimeSelectChangeListener(date -> Log.i("pvTime", "onTimeSelectChanged"))
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initTimeEndPicker() {//Dialog 模式下，在底部弹出
        pvTimeEnd = new TimePickerBuilder(getActivity(), (date, v) -> {
            Toast.makeText(getActivity(), getTime(date), Toast.LENGTH_SHORT).show();
            Log.i("pvTime", "onTimeSelect");
            timeEnd.setText(getTime(date));
        })
                .setTimeSelectChangeListener(date -> Log.i("pvTime", "onTimeSelectChanged"))
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

    private class BriefOrderContentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            briefOrderContentAdapter = new BriefOrderContentAdapter(recOrderContentList);
            rvContentList.setAdapter(briefOrderContentAdapter);
            isLoaded = true;
            srlHomeContent.setRefreshing(false);
            Toast.makeText(context, "加载推荐成功！", Toast.LENGTH_SHORT).show();
        }
    }
}
