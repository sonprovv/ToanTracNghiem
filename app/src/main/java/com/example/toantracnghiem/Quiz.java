package com.example.toantracnghiem;

import java.util.List;
import java.util.Map;

public class Quiz {
    private String id;
    private String quizTitle;
    private String level;
    private int timeLimit;
    private List<Map<String, Object>> questions;

    public Quiz() {

    }


    // Các getter và setter
    public String getQuizId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public List<Map<String, Object>> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Map<String, Object>> questions) {
        this.questions = questions;
    }
}


