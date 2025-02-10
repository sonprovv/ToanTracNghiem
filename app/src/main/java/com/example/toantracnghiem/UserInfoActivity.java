package com.example.toantracnghiem;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class UserInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Khởi tạo các thành phần giao diện
        ImageView userAvatar = findViewById(R.id.user_avatar);
        TextView userName = findViewById(R.id.user_name);
        TextView userEmail = findViewById(R.id.user_email);
        TextView userAge = findViewById(R.id.user_age);
        TextView userGender = findViewById(R.id.user_gender);

        // Dữ liệu mẫu (hoặc lấy từ Firestore/Intent)
        String avatarUrl = "https://example.com/avatar.jpg"; // URL ảnh đại diện
        String name = "Nguyễn Văn A";
        String email = "example@gmail.com";
        int age = 25;
        String gender = "Nam";

        // Hiển thị thông tin
        Picasso.get().load(avatarUrl).placeholder(R.drawable.ic_user_placeholder).into(userAvatar);
        userName.setText("Họ tên: " + name);
        userEmail.setText("Email: " + email);
        userAge.setText("Tuổi: " + age);
        userGender.setText("Giới tính: " + gender);
    }
}
