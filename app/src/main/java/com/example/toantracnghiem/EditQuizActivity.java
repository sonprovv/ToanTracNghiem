package com.example.toantracnghiem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditQuizActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private LinearLayout questionsContainer;
    private Button saveQuizButton;
    private FirebaseFirestore db;
    private CloudinaryUploader cloudinaryUploader;
    private String quizId;
    private Map<Integer, Uri> imageUris = new HashMap<>(); // Stores image URIs for each question
    private List<Map<String, Object>> questionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quiz);

        quizId = getIntent().getStringExtra("QUIZ_ID");
        questionsContainer = findViewById(R.id.questions_container);
        saveQuizButton = findViewById(R.id.save_quiz_button);

        db = FirebaseFirestore.getInstance();
        cloudinaryUploader = new CloudinaryUploader(this);

        // Load all questions from Firestore
        loadQuizData();

        // Save all questions when Save button is clicked
        saveQuizButton.setOnClickListener(v -> saveAllQuestions());
    }

    private void loadQuizData() {
        db.collection("quizzes").document(quizId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> quizQuestions = (List<Map<String, Object>>) documentSnapshot.get("questions");
                        if (quizQuestions != null) {
                            questionsList = quizQuestions;
                            for (int i = 0; i < quizQuestions.size(); i++) {
                                addQuestionView(i, quizQuestions.get(i));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading quiz data", Toast.LENGTH_SHORT).show());
    }

    private void addQuestionView(int index, Map<String, Object> questionData) {
        View questionView = getLayoutInflater().inflate(R.layout.question_item, questionsContainer, false);

        EditText questionText = questionView.findViewById(R.id.question_text);
        EditText optionA = questionView.findViewById(R.id.option_a);
        EditText optionB = questionView.findViewById(R.id.option_b);
        EditText optionC = questionView.findViewById(R.id.option_c);
        EditText optionD = questionView.findViewById(R.id.option_d);
        Spinner correctAnswerSpinner = questionView.findViewById(R.id.correct_answer_spinner);
        ImageView questionImage = questionView.findViewById(R.id.question_image);
        Button changeImageButton = questionView.findViewById(R.id.change_image_button);

        // Populate fields with data
        questionText.setText((String) questionData.get("questionText"));
        optionA.setText((String) questionData.get("optionA"));
        optionB.setText((String) questionData.get("optionB"));
        optionC.setText((String) questionData.get("optionC"));
        optionD.setText((String) questionData.get("optionD"));

        // Set the correct answer in the spinner
        String correctAnswer = (String) questionData.get("correctAnswer");
        if (correctAnswer != null) {
            switch (correctAnswer) {
                case "A": correctAnswerSpinner.setSelection(0); break;
                case "B": correctAnswerSpinner.setSelection(1); break;
                case "C": correctAnswerSpinner.setSelection(2); break;
                case "D": correctAnswerSpinner.setSelection(3); break;
            }
        }

        // Load the image if available
        String imageUrl = (String) questionData.get("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(questionImage);
        }

        // Set up the Change Image button
        changeImageButton.setOnClickListener(v -> openImagePicker(index));

        questionsContainer.addView(questionView);
    }

    private void openImagePicker(int questionIndex) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST + questionIndex); // Unique request code for each question
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            int questionIndex = requestCode - PICK_IMAGE_REQUEST; // Get the question index
            Uri imageUri = data.getData();
            imageUris.put(questionIndex, imageUri);

            // Update the image view for this question immediately
            View questionView = questionsContainer.getChildAt(questionIndex);
            ImageView questionImage = questionView.findViewById(R.id.question_image);
            questionImage.setImageURI(imageUri); // Show selected image immediately

            // Upload the image to Cloudinary and update Firestore
            cloudinaryUploader.uploadImage(imageUri, new CloudinaryUploader.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    // Update the Firestore document with the new image URL
                    questionsList.get(questionIndex).put("imageUrl", imageUrl);
                    updateFirestoreWithNewImage(questionIndex, imageUrl);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(EditQuizActivity.this, "Error uploading image: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Save the updated image URL in Firestore
    private void updateFirestoreWithNewImage(int questionIndex, String imageUrl) {
        Map<String, Object> questionData = questionsList.get(questionIndex);
        questionData.put("imageUrl", imageUrl);

        // Update the entire quiz with the modified questions list
        Map<String, Object> updatedQuizData = new HashMap<>();
        updatedQuizData.put("questions", questionsList);

        db.collection("quizzes").document(quizId).update(updatedQuizData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Image updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update quiz", Toast.LENGTH_SHORT).show());
    }

    private void saveAllQuestions() {
        List<Map<String, Object>> updatedQuestionsList = new ArrayList<>();

        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View questionView = questionsContainer.getChildAt(i);

            EditText questionText = questionView.findViewById(R.id.question_text);
            EditText optionA = questionView.findViewById(R.id.option_a);
            EditText optionB = questionView.findViewById(R.id.option_b);
            EditText optionC = questionView.findViewById(R.id.option_c);
            EditText optionD = questionView.findViewById(R.id.option_d);
            Spinner correctAnswerSpinner = questionView.findViewById(R.id.correct_answer_spinner);

            Map<String, Object> questionData = new HashMap<>();
            questionData.put("questionText", questionText.getText().toString());
            questionData.put("optionA", optionA.getText().toString());
            questionData.put("optionB", optionB.getText().toString());
            questionData.put("optionC", optionC.getText().toString());
            questionData.put("optionD", optionD.getText().toString());
            questionData.put("correctAnswer", correctAnswerSpinner.getSelectedItem().toString());

            // Get the image URL (it should be updated from Cloudinary)
            String imageUrl = (String) questionsList.get(i).get("imageUrl");
            questionData.put("imageUrl", imageUrl);

            updatedQuestionsList.add(questionData);
        }

        // Save all questions to Firestore
        Map<String, Object> updatedQuizData = new HashMap<>();
        updatedQuizData.put("questions", updatedQuestionsList);

        db.collection("quizzes").document(quizId).update(updatedQuizData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to home
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update quiz", Toast.LENGTH_SHORT).show());
    }
}
