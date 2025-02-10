package com.example.toantracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateQuizActivity extends AppCompatActivity {

    private EditText quizTitleInput, questionCountInput;
    private Spinner levelSpinner, timeSpinner;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        quizTitleInput = findViewById(R.id.quiz_title_input);
        questionCountInput = findViewById(R.id.question_count_input);
        levelSpinner = findViewById(R.id.level_spinner);
        timeSpinner = findViewById(R.id.time_spinner);
        continueButton = findViewById(R.id.continue_button);

        continueButton.setOnClickListener(view -> {
            String title = quizTitleInput.getText().toString().trim();
            String questionCountText = questionCountInput.getText().toString().trim();
            String level = levelSpinner.getSelectedItem().toString();
            String timeLimit = timeSpinner.getSelectedItem().toString().split(" ")[0];

            if (title.isEmpty() || questionCountText.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int questionCount = Integer.parseInt(questionCountText);

            Intent intent = new Intent(CreateQuizActivity.this, AddQuestionActivity.class);
            intent.putExtra("QUIZ_TITLE", title);
            intent.putExtra("QUESTION_COUNT", questionCount);
            intent.putExtra("LEVEL", level);
            intent.putExtra("TIME_LIMIT", Integer.parseInt(timeLimit));
            startActivity(intent);
        });
    }
}
