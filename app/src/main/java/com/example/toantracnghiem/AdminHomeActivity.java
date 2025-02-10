package com.example.toantracnghiem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView quizRecyclerView;
    private QuizAdapter quizAdapter;
    private List<Quiz> quizList;
    private Button addQuizButton, logoutButton; // Thêm biến cho nút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        db = FirebaseFirestore.getInstance();
        quizRecyclerView = findViewById(R.id.exam_set_recycler_view);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addQuizButton = findViewById(R.id.add_exam_set_button); // Tham chiếu tới nút thêm bài kiểm tra
        TextView userIcon = findViewById(R.id.user_icon);
        userIcon.setText(getIntent().getStringExtra("NAME").toUpperCase().substring(0,1));
        userIcon.setOnClickListener(this::showUserMenu);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        quizList = new ArrayList<>();
        quizAdapter = new QuizAdapter(quizList, this::onQuizClicked);
        quizRecyclerView.setAdapter(quizAdapter);

        // Tải danh sách bài kiểm tra
        loadQuizzes();

        // Xử lý khi nhấn vào nút "Thêm bài kiểm tra mới"
        addQuizButton.setOnClickListener(v -> openAddQuizActivity());
    }

    private void openAddQuizActivity() {
        // Chuyển sang trang AddQuestionActivity để tạo bài kiểm tra mới
        Intent intent = new Intent(this, CreateQuizActivity.class);
        startActivity(intent);
    }

    private void loadQuizzes() {
        db.collection("quizzes").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Quiz quiz = doc.toObject(Quiz.class);
                        quiz.setId(doc.getId()); // Đặt ID của quiz từ Firestore
                        quiz.setQuizTitle(doc.getString("title"));
                        quizList.add(quiz);
                    }
                    quizAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải danh sách bài kiểm tra", Toast.LENGTH_SHORT).show();
                });
    }

    private void onQuizClicked(Quiz quiz) {
        // Tạo Intent để mở EditQuizActivity
        Intent intent = new Intent(this, EditQuizActivity.class);

        Toast.makeText(this, quiz.getQuizId(), Toast.LENGTH_LONG).show();

        // Truyền quizId qua Intent để EditQuizActivity có thể lấy bài kiểm tra từ Firestore
        intent.putExtra("QUIZ_ID", quiz.getQuizId()); // quizId là ID của bài kiểm tra

        // Khởi động EditQuizActivity
        startActivity(intent);
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
}
