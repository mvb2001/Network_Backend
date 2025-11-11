package model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Question model representing a quiz question
 * This class is used to store and manage quiz questions
 */
public class Question {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("question")
    private String question;
    
    @JsonProperty("options")
    private String[] options;
    
    @JsonProperty("correctAnswer")
    private int correctAnswer;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("difficulty")
    private String difficulty;

    // Default constructor (required for Jackson)
    public Question() {
    }

    // Constructor with all parameters
    public Question(String id, String question, String[] options, int correctAnswer, String category, String difficulty) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
