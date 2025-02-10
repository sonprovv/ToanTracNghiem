package com.example.toantracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserHomeActivity extends AppCompatActivity {

    private EditText quizIdInput;
    private Button startQuizButton;
    private boolean fromCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        quizIdInput = findViewById(R.id.quiz_id_input);
        startQuizButton = findViewById(R.id.start_quiz_button);
        fromCode = getIntent().getBooleanExtra("fromCode", true);

        startQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quizId = quizIdInput.getText().toString().trim();

                if (!quizId.isEmpty()) {
                    // Chuyển đến QuizActivity với ID bài kiểm tra
                    Intent intent = new Intent(UserHomeActivity.this, QuizActivity.class);
                    intent.putExtra("QUIZ_ID", quizId);
                    intent.putExtra("fromCode", fromCode);
                    startActivity(intent);
                } else {
                    Toast.makeText(UserHomeActivity.this, "Vui lòng nhập ID bài kiểm tra", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
