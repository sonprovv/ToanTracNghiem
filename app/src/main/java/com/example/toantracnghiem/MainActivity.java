package com.example.toantracnghiem;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Button startButton, joinButton;
    private Spinner levelSpinner, questionCountSpinner, timeSpinner;

    String level;
    int questionCount;
    int timeLimit;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        startButton = findViewById(R.id.start_button);
        joinButton = findViewById(R.id.exam_button);
        levelSpinner = findViewById(R.id.level_spinner);
        questionCountSpinner = findViewById(R.id.question_count_spinner);
        timeSpinner = findViewById(R.id.time_spinner);
        TextView userIcon = findViewById(R.id.users_icon);
        userIcon.setText(getIntent().getStringExtra("NAME").toUpperCase().substring(0, 1));
        userIcon.setOnClickListener(this::showUserMenu);

        joinButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UserHomeActivity.class);
            intent.putExtra("fromCode", true);
            startActivity(intent);
        });

        setupSpinners();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải câu hỏi...");
        progressDialog.setCancelable(false);
        startButton.setOnClickListener(view -> {
            // Lấy giá trị từ Spinner
            level = levelSpinner.getSelectedItem().toString();
            String questionCountStr = questionCountSpinner.getSelectedItem().toString();
            String timeLimitStr = timeSpinner.getSelectedItem().toString();

            // Kiểm tra nếu người dùng chưa chọn một số lượng câu hỏi hoặc thời gian hợp lệ
            if (questionCountStr.equals("Chọn số câu hỏi")) {
                Toast.makeText(MainActivity.this, "Vui lòng chọn số câu hỏi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (timeLimitStr.equals("Chọn thời gian")) {
                Toast.makeText(MainActivity.this, "Vui lòng chọn thời gian!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển đổi chuỗi thành số nguyên
            questionCount = Integer.parseInt(questionCountStr);
            Log.d("HAHAHA", "onCreate: số ques" + questionCount);// Chuyển đổi thành số nguyên
            timeLimit = Integer.parseInt(timeLimitStr.split(" ")[0]);
            // Truyền dữ liệu sang QuizActivity
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            intent.putExtra("LEVEL", level);
            intent.putExtra("QUESTION_COUNT", questionCount);
            intent.putExtra("TIME_LIMIT", timeLimit);
            intent.putExtra("fromCode", false);  // Không phải từ mã
            generateNewQuiz(level, questionCount, timeLimit);
        });
    }

    private void showUserMenu(View view) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

        // Xử lý sự kiện khi chọn menu
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_view_info) {
                // Chuyển đến màn hình Xem thông tin
                Toast.makeText(this, "Xem thông tin người dùng", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, UserInfoActivity.class));
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                // Đăng xuất và chuyển về màn hình đăng nhập
                Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this, R.array.level_options, android.R.layout.simple_spinner_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);

        ArrayAdapter<CharSequence> questionCountAdapter = ArrayAdapter.createFromResource(this, R.array.question_count_options, android.R.layout.simple_spinner_item);
        questionCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionCountSpinner.setAdapter(questionCountAdapter);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
    }

    private void generateNewQuiz(String level, int questionCount, int timeLimit) {
        CollectionReference quizzesRef = db.collection("quizzes");

        // Lấy email người dùng (đảm bảo người dùng đã đăng nhập)
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị ProgressDialog khi bắt đầu tải dữ liệu
        progressDialog.show();

        // Lọc các bài kiểm tra theo mức độ (level)
        Query query = quizzesRef.whereEqualTo("level", level);
        query.get().addOnCompleteListener(task -> {
            progressDialog.dismiss(); // Ẩn ProgressDialog khi hoàn tất truy vấn

            if (task.isSuccessful()) {
                List<Question> questionList = new ArrayList<>();

                // Duyệt qua các tài liệu tìm thấy trong truy vấn
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Lấy danh sách câu hỏi từ mỗi bài kiểm tra
                    List<Map<String, Object>> questions = (List<Map<String, Object>>) document.get("questions");

                    if (questions != null) {
                        // Duyệt qua từng câu hỏi và thêm vào questionList
                        for (Map<String, Object> questionData : questions) {
                            String url = (String) questionData.get("imageUrl");
                            String questionText = (String) questionData.get("questionText");
                            String option1 = (String) questionData.get("optionA");
                            String option2 = (String) questionData.get("optionB");
                            String option3 = (String) questionData.get("optionC");
                            String option4 = (String) questionData.get("optionD");
                            String correctAnswer = (String) questionData.get("correctAnswer");

                            // Thêm câu hỏi vào danh sách
                            questionList.add(new Question(url, questionText, option1, option2, option3, option4, correctAnswer));

                            // Kiểm tra nếu đã đủ câu hỏi yêu cầu
                            if (questionList.size() >= questionCount) {
                                break;
                            }
                        }
                    }

                    // Kiểm tra nếu đã đủ câu hỏi yêu cầu
                    if (questionList.size() >= questionCount) {
                        break;
                    }
                }

                // Nếu có đủ số câu hỏi
                if (questionList.size() >= questionCount) {
                    // Trộn ngẫu nhiên danh sách câu hỏi và chỉ lấy số lượng yêu cầu
                    Collections.shuffle(questionList);
                    List<Question> selectedQuestions = questionList.subList(0, questionCount);

                    // Tạo dữ liệu bài kiểm tra mới
                    String quizId = "quiz_" + System.currentTimeMillis();  // ID duy nhất dựa trên thời gian
                    Map<String, Object> quizData = new HashMap<>();
                    quizData.put("level", level);
                    quizData.put("timeLimit", timeLimit);
                    quizData.put("questions", selectedQuestions);  // Lưu danh sách câu hỏi đã chọn

                    // Tham chiếu đến bài kiểm tra của người dùng
                    DocumentReference userQuizRef = db.collection("users")
                            .document(userEmail)
                            .collection("quizzes")
                            .document(quizId);

                    // Lưu bài kiểm tra vào Firestore
                    progressDialog.show();
                    userQuizRef.set(quizData)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Quiz created and saved successfully!", Toast.LENGTH_SHORT).show();

                                // Điều hướng sang QuizActivity
                                Intent intent = new Intent(this, QuizActivity.class);
                                intent.putExtra("EMAIL", userEmail);
                                intent.putExtra("ID", quizId);
                                intent.putExtra("fromCode", false);
                                intent.putExtra("TIME", timeLimit);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Failed to save quiz data.", Toast.LENGTH_SHORT).show();
                            });

                } else {
                    Toast.makeText(this, "Not enough questions available for this level.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load questions.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}