package com.example.duoduopin.activity.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.duoduopin.R;
import com.example.duoduopin.tool.GlideEngine;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.duoduopin.activity.MainActivity.headPath;
import static com.example.duoduopin.activity.MainActivity.idContent;
import static com.example.duoduopin.activity.MainActivity.tokenContent;
import static com.example.duoduopin.handler.GeneralMsgHandler.ERROR;
import static com.example.duoduopin.handler.GeneralMsgHandler.SUCCESS;
import static com.example.duoduopin.tool.Constants.API_KEY_TO_BED;
import static com.example.duoduopin.tool.Constants.getHeadUploadToServer;
import static com.example.duoduopin.tool.Constants.uploadToBedUrl;

public class EditHeadActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient.Builder().build();
    private ImageView ivHead;
    private String imageType = "";
    private String imagePath = "";

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_head);

        ImageView ivBackEditHead = findViewById(R.id.iv_back_edit_head);
        ivBackEditHead.setOnClickListener(v -> finish());

        ivHead = findViewById(R.id.iv_head);

        Button btnModifyHead = findViewById(R.id.btn_modify_head);
        btnModifyHead.setOnClickListener(v -> {
            selectImage(v.getContext());
        });

        Button btnCommitHead = findViewById(R.id.btn_commit_head);
        btnCommitHead.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @SuppressLint("HandlerLeak")
                final Handler commitHeadHandler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        if (msg.what == SUCCESS) {
                            Toast.makeText(v.getContext(), "头像上传成功！", Toast.LENGTH_SHORT).show();
                        } else {
                            String imageUrl = (String) msg.obj;
                            Log.e("headUpload", "failed: imageUrl = " + imageUrl);
                        }

                    }
                };

                @Override
                public void run() {
                    try {
                        if (!imagePath.isEmpty()) {
                            String TAG = "uploadToServer";
                            Message message = new Message();
                            String imageUrl = uploadToBed(imageType, imagePath);
                            Log.e(TAG, "imageUrl: " + imageUrl);
                            int res;
                            File file = new File(headPath);
                            if (!imageUrl.isEmpty()) {
                                if (file.exists()) {
                                    Log.e(TAG, "update");
                                    res = uploadHead(true, imageUrl);
                                } else {
                                    Log.e(TAG, "upload");
                                    res = uploadHead(false, imageUrl);
                                }
                            } else {
                                res = ERROR;
                                Log.e(TAG, "imageUrl: " + imageUrl);
                            }
                            message.what = res;
                            message.obj = imageUrl;
                            commitHeadHandler.sendMessage(message);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });
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
                            ivHead.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onCancel() {
                        imageType = "";
                        imagePath = "";
                    }
                });
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
            Log.e(TAG, "picLink: " + picLink);
        } else {
            Log.e(TAG, Objects.requireNonNull(response.body()).string());
        }

        return picLink;
    }

    private int uploadHead(boolean isUpdate, String imageUrl) throws IOException, JSONException {
        final String TAG = "uploadHead";
        int res;

        String uploadUrl = getHeadUploadToServer(isUpdate);

        RequestBody body = new FormBody.Builder()
                .add("path", imageUrl)
                .build();

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("token", idContent + "_" + tokenContent)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String responseString = response.body().string();
        Log.e("uploadHead", responseString);

        if (response.code() == 200) {
            res = SUCCESS;
        } else {
            res = ERROR;
        }

        return res;
    }
}
