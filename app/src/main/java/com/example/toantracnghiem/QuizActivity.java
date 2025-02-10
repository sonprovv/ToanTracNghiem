package com.example.toantracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    private TextView questionText;
    private ImageView questionImage;
    private RadioGroup answerGroup;
    private RadioButton answer1, answer2, answer3, answer4;
    private Button submitButton;
    private FirebaseFirestore db;
    private List<Map<String, Object>> questionList;
    private List<Map<String, Object>> questionListfromuser;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String quizId;
    private String email;
    private String idquiz;
    private boolean fromCode;
    private ProgressBar progressBar;
    private TextView timerText;
    private ImageView progressIcon;


    private int timePerQuestion = 30; // Thời gian tối đa cho mỗi câu (giây)
    private CountDownTimer countDownTimer; // Sử dụng để đếm thời gian


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Khởi tạo view
        questionText = findViewById(R.id.question_text);
        questionImage = findViewById(R.id.question_image);
        answerGroup = findViewById(R.id.answer_group);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);
        answer4 = findViewById(R.id.answer4);
        submitButton = findViewById(R.id.submit_button);
        progressBar = findViewById(R.id.progress_bar);
        timerText = findViewById(R.id.timer_text);
        progressIcon = findViewById(R.id.progress_icon);


        db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu từ Intent
        fromCode = getIntent().getBooleanExtra("fromCode", false);

        if (fromCode) {
            // Nếu tham gia bằng mã
            quizId = getIntent().getStringExtra("QUIZ_ID");
            Log.d("LOV", "fromcode1" + fromCode + quizId);
            loadQuizDataFromCode();  // Tải dữ liệu bài kiểm tra bằng mã
        } else {
            timePerQuestion = getIntent().getIntExtra("TIME", 0) * 60;
            Log.d("LOV", "fromcode2" + fromCode);
            // Nếu chọn từ danh sách bộ đề
            email = getIntent().getStringExtra("EMAIL");
            idquiz = getIntent().getStringExtra("ID");
            loadQuizDataFromList();  // Tải câu hỏi dựa trên bộ đề
        }

        submitButton.setOnClickListener(v -> submitAnswer());
    }

    private void loadQuizDataFromCode() {
        db.collection("quizzes").document(quizId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        timePerQuestion = Integer.parseInt(documentSnapshot.get("timeLimit").toString()) * 60;
                        questionList = (List<Map<String, Object>>) documentSnapshot.get("questions");
                        if (questionList != null && !questionList.isEmpty()) {
                            displayQuestion();
                        } else {
                            Toast.makeText(this, "Không có câu hỏi trong bài kiểm tra.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy bài kiểm tra.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải bài kiểm tra.", Toast.LENGTH_SHORT).show());
    }

    private void loadQuizDataFromList() {
        db.collection("users").document(email).collection("quizzes").document(idquiz)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy danh sách các câu hỏi từ trường "questions" dưới dạng List<Map<String, Object>>
                        questionListfromuser = (List<Map<String, Object>>) documentSnapshot.get("questions");

                        if (questionListfromuser != null && !questionListfromuser.isEmpty()) {
                            // Hiển thị câu hỏi đầu tiên
                            displayQuestion();
                        } else {
                            Toast.makeText(this, "Không có câu hỏi trong bài kiểm tra này.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Bài kiểm tra không tồn tại.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải câu hỏi.", Toast.LENGTH_SHORT).show());
    }


    private void displayQuestion() {
        if (fromCode) {
            if (currentQuestionIndex < questionList.size()) {

                Map<String, Object> questionData = questionList.get(currentQuestionIndex);

                String questionTextStr = (String) questionData.get("questionText");
                String optionA = (String) questionData.get("optionA");
                String optionB = (String) questionData.get("optionB");
                String optionC = (String) questionData.get("optionC");
                String optionD = (String) questionData.get("optionD");
                String imageUrl = (String) questionData.get("imageUrl");

                questionText.setText(questionTextStr);
                answer1.setText(optionA);
                answer2.setText(optionB);
                answer3.setText(optionC);
                answer4.setText(optionD);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(questionImage);
                    questionImage.setVisibility(View.VISIBLE);
                } else {
                    questionImage.setVisibility(View.GONE);
                }

                answerGroup.clearCheck();
                startTimer();

                if (currentQuestionIndex == questionList.size() - 1) {
                    submitButton.setText("Hoàn thành");
                } else {
                    submitButton.setText("Tiếp tục");
                }
            } else {
                finishQuiz();
            }
        } else {
            if (currentQuestionIndex < questionListfromuser.size()) {
                Log.d("SIZE", "displayQuestion: " + questionListfromuser.size());
                Map<String, Object> questionData = questionListfromuser.get(currentQuestionIndex);
                String questionTextStr = (String) questionData.get("questionText");
                String optionA = (String) questionData.get("option1");
                String optionB = (String) questionData.get("option2");
                String optionC = (String) questionData.get("option3");
                String optionD = (String) questionData.get("option4");
                String imageUrl = (String) questionData.get("url");

                questionText.setText(questionTextStr);
                answer1.setText(optionA);
                answer2.setText(optionB);
                answer3.setText(optionC);
                answer4.setText(optionD);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(questionImage);
                    questionImage.setVisibility(View.VISIBLE);
                } else {
                    questionImage.setVisibility(View.GONE);
                }
                answerGroup.clearCheck();
                startTimer();

                if (currentQuestionIndex == questionListfromuser.size() - 1) {
                    submitButton.setText("Hoàn thành");
                } else {
                    submitButton.setText("Tiếp tục");
                }
            } else {
                finishQuiz();
            }
        }
    }


    private void submitAnswer() {
        int selectedAnswerId = answerGroup.getCheckedRadioButtonId();
        if (selectedAnswerId == -1) {
            Toast.makeText(this, "Vui lòng chọn câu trả lời.", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> currentQuestion;
        if (fromCode)
            currentQuestion = questionList.get(currentQuestionIndex);
        else currentQuestion = questionListfromuser.get(currentQuestionIndex);
        String correctAnswer = (String) currentQuestion.get("correctAnswer");

        // Kiểm tra câu trả lời đúng
        String selectedAnswer = "";
        if (selectedAnswerId == R.id.answer1) selectedAnswer = "A";
        else if (selectedAnswerId == R.id.answer2) selectedAnswer = "B";
        else if (selectedAnswerId == R.id.answer3) selectedAnswer = "C";
        else if (selectedAnswerId == R.id.answer4) selectedAnswer = "D";

        if (selectedAnswer.equals(correctAnswer)) {
            score++;
        }

        currentQuestionIndex++;
        if(fromCode) {
            if (currentQuestionIndex < questionList.size()) {
                displayQuestion();
            } else {
                finishQuiz();
            }
        }
        else {
            if (currentQuestionIndex < questionListfromuser.size()) {
                displayQuestion();
            } else {
                finishQuiz();
            }
        }
    }

    private void finishQuiz() {
        // Truyền điểm và số câu hỏi sang ResultActivity
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SCORE", score);
        if (fromCode)
        intent.putExtra("TOTAL_QUESTIONS", questionList.size());
        else intent.putExtra("TOTAL_QUESTIONS", questionListfromuser.size());
        startActivity(intent);
        finish();
    }
    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Hủy timer nếu đang chạy
        }

        // Đặt lại ProgressBar và TextView
        progressBar.setProgress(100);
        timerText.setText("Time Left: " + timePerQuestion + "s");

        // Bắt đầu CountDownTimer
        countDownTimer = new CountDownTimer(timePerQuestion * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished / (timePerQuestion * 10));
                progressBar.setProgress(progress);
                updateProgressIcon(progress);
                timerText.setText("Time: " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
//                timerText.setText("Time's up!");
                progressBar.setProgress(0);
                updateProgressIcon(0);

                // Chuyển sang câu hỏi tiếp theo nếu hết thời gian
                currentQuestionIndex++;
                displayQuestion();
            }
        };
        countDownTimer.start();
    }
    private void updateProgressIcon(int progress) {
        // Lấy chiều rộng của ProgressBar
        int progressBarWidth = progressBar.getWidth();

        // Tính toán vị trí mới cho icon dựa trên tiến trình
        int iconPosition = (progressBarWidth * (100 - progress)) / 100;

        // Cập nhật vị trí của icon
        progressIcon.setTranslationX(-iconPosition + (progressIcon.getWidth() / 2)); // Điều chỉnh để icon nằm giữa vạch
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}
