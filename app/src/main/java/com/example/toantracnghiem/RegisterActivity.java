package com.example.toantracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword;
    private Button registerButton, loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);

        registerButton.setOnClickListener(view -> {
            String email = registerEmail.getText().toString().trim();
            String password = registerPassword.getText().toString().trim();

            // Kiểm tra trường nhập liệu
            if (TextUtils.isEmpty(email)) {
                registerEmail.setError("Vui lòng nhập email");
                registerEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                registerPassword.setError("Vui lòng nhập mật khẩu");
                registerPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                registerPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                registerPassword.requestFocus();
                return;
            }

            // Gọi hàm đăng ký người dùng
            registerUser(email, password);
        });

        // Xử lý khi nhấn vào nút "Đăng nhập"
        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng RegisterActivity
        });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
