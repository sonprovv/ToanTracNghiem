package com.example.toantracnghiem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddQuestionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText questionText, option1, option2, option3, option4, correctAnswer;
    private Button addImageButton, nextQuestionButton;
    private ImageView questionImage;
    private Uri imageUri;
    private FirebaseFirestore db;
    private CloudinaryUploader cloudinaryUploader;

    private int questionCount; // Số câu hỏi cần nhập
    private int currentQuestionIndex = 0; // Đếm số câu hỏi đã nhập
    private String quizTitle, level; // Thông tin bài kiểm tra
    private int timeLimit;
    private List<Map<String, Object>> questions; // Lưu danh sách câu hỏi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        questionText = findViewById(R.id.question_text);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        correctAnswer = findViewById(R.id.correct_answer);
        questionImage = findViewById(R.id.question_image);
        addImageButton = findViewById(R.id.add_image_button);
        nextQuestionButton = findViewById(R.id.next_question_button);

        db = FirebaseFirestore.getInstance();
        cloudinaryUploader = new CloudinaryUploader(this);

        // Nhận thông tin từ Intent
        quizTitle = getIntent().getStringExtra("QUIZ_TITLE");
        level = getIntent().getStringExtra("LEVEL");
        timeLimit = getIntent().getIntExtra("TIME_LIMIT", 0);
        questionCount = getIntent().getIntExtra("QUESTION_COUNT", 0);

        questions = new ArrayList<>();

        addImageButton.setOnClickListener(v -> openImagePicker());

        nextQuestionButton.setOnClickListener(v -> {
            if (validateQuestionInput()) {
                if (imageUri != null) {
                    cloudinaryUploader.uploadImage(imageUri, new CloudinaryUploader.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            addQuestionToList(imageUrl);
                            processNextQuestion();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(AddQuestionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    addQuestionToList(null); // Không có ảnh
                    processNextQuestion();
                }
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            questionImage.setImageURI(imageUri); // Hiển thị ảnh đã chọn
        }
    }

    private void addQuestionToList(String imageUrl) {
        Map<String, Object> questionData = new HashMap<>();
        questionData.put("questionText", questionText.getText().toString());
        questionData.put("optionA", option1.getText().toString());
        questionData.put("optionB", option2.getText().toString());
        questionData.put("optionC", option3.getText().toString());
        questionData.put("optionD", option4.getText().toString());
        questionData.put("correctAnswer", correctAnswer.getText().toString());
        questionData.put("imageUrl", imageUrl); // URL ảnh từ Cloudinary

        questions.add(questionData); // Thêm câu hỏi vào danh sách
    }

    private void processNextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex >= questionCount) {
            // Nếu đã nhập đủ câu hỏi, lưu bài kiểm tra
            saveQuizToFirestore();
        } else {
            // Xóa các trường nhập để nhập câu hỏi tiếp theo
            clearQuestionFields();
        }
    }

    private void clearQuestionFields() {
        questionText.setText("");
        option1.setText("");
        option2.setText("");
        option3.setText("");
        option4.setText("");
        correctAnswer.setText("");
        questionImage.setImageDrawable(null);
        imageUri = null;
    }

    private void saveQuizToFirestore() {
        // Tạo ID cho bài kiểm tra mới
        String quizId = db.collection("quizzes").document().getId();

        // Tạo dữ liệu bài kiểm tra
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("id", quizId);
        quizData.put("title", quizTitle); // Tên bài kiểm tra
        quizData.put("level", level); // Mức độ
        quizData.put("timeLimit", timeLimit); // Thời gian
        quizData.put("questions", questions); // Danh sách câu hỏi

        // Lưu bài kiểm tra vào Firestore
        db.collection("quizzes").document(quizId).set(quizData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Bài kiểm tra đã được lưu!", Toast.LENGTH_SHORT).show();
                    // Sau khi lưu, quay lại AdminHomeActivity
                    Intent intent = new Intent(this, AdminHomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi lưu bài kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean validateQuestionInput() {
        return !questionText.getText().toString().isEmpty() &&
                !option1.getText().toString().isEmpty() &&
                !option2.getText().toString().isEmpty() &&
                !option3.getText().toString().isEmpty() &&
                !option4.getText().toString().isEmpty() &&
                !correctAnswer.getText().toString().isEmpty();
    }
}
