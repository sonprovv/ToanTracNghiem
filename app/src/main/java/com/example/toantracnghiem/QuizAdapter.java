package com.example.toantracnghiem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Quiz> quizList;
    private OnQuizClickListener onQuizClickListener;

    public interface OnQuizClickListener {
        void onQuizClicked(Quiz quiz);
    }

    public QuizAdapter(List<Quiz> quizList, OnQuizClickListener onQuizClickListener) {
        this.quizList = quizList;
        this.onQuizClickListener = onQuizClickListener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_item, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.quizTitleTextView.setText("Title: " + quiz.getQuizTitle());
        holder.quizIdTextView.setText("ID: " + quiz.getQuizId());
        holder.itemView.setOnClickListener(v -> onQuizClickListener.onQuizClicked(quiz));
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class QuizViewHolder extends RecyclerView.ViewHolder {

        TextView quizTitleTextView, quizIdTextView;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            quizTitleTextView = itemView.findViewById(R.id.quiz_title);
            quizIdTextView = itemView.findViewById(R.id.quiz_id);
        }
    }
}

