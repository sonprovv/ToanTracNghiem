package com.example.toantracnghiem;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import okhttp3.*;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class CloudinaryUploader {
//    private Cloudinary cloudinary;

    private static final String CLOUD_NAME = "dhblvmcb8";
    private static final String API_KEY = "637662972287348";
    private static final String UPLOAD_PRESET = "sonpham";
    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private Context context;
    private OkHttpClient client;

    public CloudinaryUploader(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void uploadImage(Uri imageUri, UploadCallback callback) {
        new UploadTask(imageUri, callback).execute();
    }

    private class UploadTask extends AsyncTask<Void, Void, String> {
        private Uri imageUri;
        private UploadCallback callback;

        public UploadTask(Uri imageUri, UploadCallback callback) {
            this.imageUri = imageUri;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Mở InputStream từ Uri bằng ContentResolver
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

                if (inputStream == null) {
                    return "Không thể đọc tệp từ Uri.";
                }

                // Đọc nội dung của ảnh thành byte array
                byte[] imageData = getBytesFromInputStream(inputStream);

                // Tạo RequestBody từ byte array để gửi ảnh lên Cloudinary
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg",
                                RequestBody.create(MediaType.parse("image/*"), imageData)) // imageData là byte array của ảnh
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .addFormDataPart("api_key", API_KEY)
                        .build();

                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    return jsonObject.getString("secure_url"); // Trả về URL của ảnh
                } else {
                    return "Lỗi phản hồi từ Cloudinary: " + response.message();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Lỗi ngoại lệ: " + e.getMessage();
            }
        }

        // Phương thức để chuyển InputStream thành byte array
        private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }


        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.startsWith("http")) {
                callback.onSuccess(result); // URL ảnh trả về thành công
            } else {
                callback.onFailure(result);
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }
}
