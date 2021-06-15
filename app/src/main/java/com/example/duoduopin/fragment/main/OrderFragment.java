package com.example.duoduopin.fragment.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.example.duoduopin.R;
import com.example.duoduopin.activity.order.LocateActivity;
import com.example.duoduopin.activity.order.OneOrderCaseActivity;
import com.example.duoduopin.tool.GlideEngine;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.duoduopin.activity.LoginActivity.JSON;
import static com.example.duoduopin.activity.MainActivity.client;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.nicknameContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.API_KEY_TO_BED;
import static com.example.duoduopin.tool.Constants.createOrderUrl;
import static com.example.duoduopin.tool.Constants.getImageUploadToServerUrl;
import static com.example.duoduopin.tool.Constants.notifyMessage;
import static com.example.duoduopin.tool.Constants.uploadToBedUrl;

public class OrderFragment extends Fragment {
    private EditText etTitle;
    private EditText etDescription;
    private EditText etCurPeople;
    private EditText etMaxPeople;
    private EditText etPrice;
    private EditText etTude;

    private ImageView ivUploadPic;

    private TextView tvUploadPic;
    private TextView tvAddress;
    private TextView tvTime;
    private TimePickerView pvTime;

    private String typeString;
    private String orderId;
    private String imageType;
    private String imagePath = "";
    private String imageUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_order, null);
        return view;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private String uploadToBed(String imageType, String imagePath) throws IOException, JSONException {
        final String TAG = "uploadPic";
        String picLink = "";

        File imageFile = new File(imagePath);
        RequestBody imageBody = RequestBody.create(MediaType.parse(imageType), imageFile);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("smfile", imageFile.getName(), imageBody)
                .build();

        final Request request = new Request.Builder()
                .url(uploadToBedUrl)
                .header("Authorization", API_KEY_TO_BED)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            JSONObject dataJSON = new JSONObject(responseJSON.getString("data"));
            picLink = dataJSON.getString("url");
        } else {
            Log.e(TAG, Objects.requireNonNull(response.body()).string());
        }

        return picLink;
    }

    private void selectImage(Context context) {
        final String TAG = "SelectUploadImage";
        EasyPhotos.createAlbum((Activity) context, false, false, GlideEngine.getInstance())
                .start(new SelectCallback() {
                    @Override
                    public void onResult(ArrayList<Photo> photos, boolean isOriginal) {
                        if (photos.size() == 1) {
                            Photo photo = photos.get(0);
                            imageType = photo.type;
                            imagePath = photo.path;
                            Log.e(TAG, "imagePath = " + imagePath);
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            ivUploadPic.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onCancel() {
                        imageType = "";
                        imagePath = "";
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null) {
                String address = data.getStringExtra("address");
                tvAddress.setText(address);
                boolean isChosen = data.getBooleanExtra("isChosen", false);
                if (isChosen) {
                    String tude = data.getStringExtra("tude");
                    etTude.setText(tude);
                } else {
                    // TODO: clipboard
                    etTude.setText("");
                    etTude.setHint("请从剪切板粘贴经纬度~");
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout llLocate = view.findViewById(R.id.ll_locate);
        llLocate.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), LocateActivity.class);
            intent.putExtra("address", "");
            intent.putExtra("isChosen", false);
            intent.putExtra("tude", "");
            startActivityForResult(intent, 0);
        });

        ivUploadPic = view.findViewById(R.id.iv_upload_pic);
        ivUploadPic.setOnClickListener(v -> {
            selectImage(v.getContext());
        });
        tvUploadPic = view.findViewById(R.id.tv_upload_pic);
        tvUploadPic.setOnClickListener(v -> {
            selectImage(v.getContext());
        });
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        tvAddress = view.findViewById(R.id.tv_address);
        tvTime = view.findViewById(R.id.tv_time);
        etCurPeople = view.findViewById(R.id.et_cur_people);
        etMaxPeople = view.findViewById(R.id.et_max_people);
        etPrice = view.findViewById(R.id.et_price);
        etTude = view.findViewById(R.id.et_tude);

        tvTime = view.findViewById(R.id.tv_time);
        tvTime.setOnClickListener(v -> pvTime.show(v));
        initTimePicker();

        Spinner spinner = view.findViewById(R.id.typeInput);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.newOrderItemType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("拼车")) {
                    typeString = "CAR";
                    ivUploadPic.setVisibility(View.INVISIBLE);
                    tvUploadPic.setVisibility(View.INVISIBLE);
                } else if (selected.equals("拼单")) {
                    typeString = "BILL";
                    ivUploadPic.setVisibility(View.VISIBLE);
                    tvUploadPic.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                typeString = "";
            }
        });

        Button submitOrder = view.findViewById(R.id.submitOrderButton);
        submitOrder.setOnClickListener(v -> {
            boolean canPost = true;

            final String titleString = etTitle.getText().toString();
            final String descString = etDescription.getText().toString();
            final String addrString = tvAddress.getText().toString();
            final String timeString = tvTime.getText().toString();
            final String curPeopleString = etCurPeople.getText().toString();
            final String maxPeopleString = etMaxPeople.getText().toString();
            final String priceString = etPrice.getText().toString();
            String latitudeString = "", longitudeString = "";
            if (!etTude.getText().toString().isEmpty()) {
                String[] tudeString = etTude.getText().toString().split(",");
                longitudeString = tudeString[0];
                latitudeString = tudeString[1];
            }

            if (titleString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入标题", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (descString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入描述", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (addrString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入地址", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (timeString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入时间", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (maxPeopleString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入最大人数", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (longitudeString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入经度", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (latitudeString.isEmpty()) {
                Toast.makeText(v.getContext(), "请输入纬度", Toast.LENGTH_SHORT).show();
                canPost = false;
            } else if (imagePath.isEmpty() && typeString.equals("BILL")) {
                Toast.makeText(v.getContext(), "请上传图片", Toast.LENGTH_SHORT).show();
                canPost = false;
            }
            if (canPost) {
                boolean isBill = typeString.equals("BILL");
                if (isBill) {
                    new Thread(new Runnable() {
                        @SuppressLint("HandlerLeak")
                        final Handler uploadOrderImageHandler = new Handler() {
                            @Override
                            public void handleMessage(@NonNull Message msg) {
                                if (msg.what == SUCCESS) {
                                    imageUrl = (String) msg.obj;
                                    Log.e("uploadToBed", imageUrl);
                                    Toast.makeText(v.getContext(), "图片上传成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    imageUrl = (String) msg.obj;
                                    Log.e("headUpload", "failed: imageUrl = " + imageUrl);
                                }

                            }
                        };

                        @Override
                        public void run() {
                            try {
                                Message message = new Message();
                                String url = uploadToBed(imageType, imagePath);
                                Log.e("uploadToBed", "picLink = " + url);
                                if (url.isEmpty()) {
                                    message.what = ERROR;
                                } else {
                                    message.what = SUCCESS;
                                    message.obj = url;
                                }
                                uploadOrderImageHandler.sendMessage(message);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                } else {
                    imageUrl = "";
                }
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("title", titleString);
                    jsonObject.put("type", typeString);
                    jsonObject.put("description", descString);
                    jsonObject.put("address", addrString);
                    jsonObject.put("time", timeString.replace(' ', 'T') + ".0");
                    jsonObject.put("curPeople", curPeopleString);
                    jsonObject.put("maxPeople", maxPeopleString);
                    jsonObject.put("price", priceString);
                    jsonObject.put("longitude", longitudeString);
                    jsonObject.put("latitude", latitudeString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("JSONBuild", jsonObject.toString());
                @SuppressLint("HandlerLeak") final Handler newOrderHandler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == SUCCESS) {
                            new Thread(new Runnable() {
                                @SuppressLint("HandlerLeak")
                                final Handler uploadImageToServerHandler = new Handler() {
                                    @Override
                                    public void handleMessage(@NonNull Message msg) {
                                        if (msg.what == SUCCESS) {
                                            Log.e("uploadHeadToServer", "上传到服务器成功");
                                        } else {
                                            Log.e("uploadHeadToServer", "上传到服务器失败");
                                        }
                                        if (msg.arg1 != SUCCESS) {
                                            Log.e("notifyMessage", "failed");
                                        }
                                    }
                                };

                                @Override
                                public void run() {
                                    try {
                                        Message message = new Message();
                                        if (imageUrl != null) {
                                            message.what = postUploadImage(imageUrl);
                                        }
                                        message.arg1 = postNotifyMessage();
                                        uploadImageToServerHandler.sendMessage(message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            Toast.makeText(view.getContext(), "创建成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(view.getContext(), OneOrderCaseActivity.class);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("userId", idContent);
                            intent.putExtra("nickname", nicknameContent);
                            intent.putExtra("type", typeString);
                            intent.putExtra("price", priceString);
                            intent.putExtra("address", addrString);
                            intent.putExtra("curPeople", curPeopleString);
                            intent.putExtra("maxPeople", maxPeopleString);
                            intent.putExtra("time", timeString);
                            intent.putExtra("description", descString);
                            intent.putExtra("title", titleString);
                            if (isBill) {
                                intent.putExtra("imageUrl", imageUrl);
                            }
                            startActivity(intent);
                        } else {
                            Toast.makeText(view.getContext(), "创建失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                new Thread(() -> {
                    try {
                        Message message = new Message();
                        message.what = putRequest(jsonObject.toString());
                        newOrderHandler.sendMessage(message);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private int postNotifyMessage() throws IOException {
        int res;

        RequestBody body = new FormBody.Builder()
                .add("BillId", orderId)
                .build();

        Request request = new Request.Builder()
                .url(notifyMessage)
                .header("token", idContent + "_" + tokenContent)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            res = SUCCESS;
        } else {
            res = ERROR;
        }

        return res;
    }

    private int postUploadImage(String imageUrl) throws IOException {
        int res;

        RequestBody body = new FormBody.Builder()
                .add("path", imageUrl)
                .build();

        Request request = new Request.Builder()
                .url(getImageUploadToServerUrl(orderId))
                .header("token", idContent + "_" + tokenContent)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            res = SUCCESS;
        } else {
            res = ERROR;
        }

        return res;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int putRequest(String jsonBody) throws IOException, JSONException {
        final String TAG = "createOrder";
        final String contentFromServer = "content";
        final String orderIdFromServer = "id";

        int ret;

        RequestBody body = RequestBody.create(jsonBody, JSON);

        final Request request = new Request.Builder()
                .url(createOrderUrl)
                .header("token", idContent + "_" + tokenContent)
                .put(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if (response.code() == 200) {
            JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
            JSONObject contentJson = new JSONObject(responseJson.getString(contentFromServer));
            orderId = contentJson.optString(orderIdFromServer);
            ret = SUCCESS;
        } else {
            Log.d(TAG, Objects.requireNonNull(response.body()).string());
            Log.d(TAG, response.toString());
            ret = ERROR;
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTime(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initTimePicker() {//Dialog 模式下，在底部弹出
        pvTime = new TimePickerBuilder(getActivity(), (date, v) -> {
            Toast.makeText(v.getContext(), getTime(date), Toast.LENGTH_SHORT).show();
            Log.i("pvTime", "onTimeSelect");
            tvTime.setText(getTime(date));
        })
                .setTimeSelectChangeListener(date -> Log.i("pvTime", "onTimeSelectChanged"))
                .setType(new boolean[]{true, true, true, true, true, true})
                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .setLineSpacingMultiplier(2.0f)
                .build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
                dialogWindow.setDimAmount(0.3f);
            }
        }
    }
}
