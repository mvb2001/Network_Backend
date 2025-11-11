package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import model.Question;

/**
 * QuestionManager handles CRUD operations for quiz questions
 * Stores and retrieves questions from a JSON file
 */
public class QuestionManager {
    private static final String QUESTIONS_FILE = "src/main/resources/questions.json";
    private final ObjectMapper objectMapper;
    private List<Question> questions;

    public QuestionManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.questions = new ArrayList<>();
        loadQuestions();
    }

    /**
     * Load questions from JSON file
     */
    public synchronized void loadQuestions() {
        try {
            File file = new File(QUESTIONS_FILE);
            if (file.exists()) {
                Question[] questionsArray = objectMapper.readValue(file, Question[].class);
                questions = new ArrayList<>(Arrays.asList(questionsArray));
                System.out.println("Loaded " + questions.size() + " questions from file.");
            } else {
                // Create file with empty array if it doesn't exist
                questions = new ArrayList<>();
                saveQuestions();
                System.out.println("Created new questions file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
            questions = new ArrayList<>();
        }
    }

    /**
     * Save questions to JSON file
     */
    private synchronized void saveQuestions() {
        try {
            File file = new File(QUESTIONS_FILE);
            file.getParentFile().mkdirs(); // Create directories if they don't exist
            objectMapper.writeValue(file, questions);
            System.out.println("Saved " + questions.size() + " questions to file.");
        } catch (IOException e) {
            System.err.println("Error saving questions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all questions
     */
    public synchronized List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    /**
     * Get question by ID
     */
    public synchronized Question getQuestionById(String id) {
        return questions.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add a new question
     */
    public synchronized boolean addQuestion(Question question) {
        try {
            // Check if question with same ID already exists
            if (getQuestionById(question.getId()) != null) {
                System.err.println("Question with ID " + question.getId() + " already exists.");
                return false;
            }
            
            // Generate ID if not provided
            if (question.getId() == null || question.getId().isEmpty()) {
                question.setId(generateQuestionId());
            }
            
            questions.add(question);
            saveQuestions();
            System.out.println("Added question: " + question.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error adding question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update an existing question
     */
    public synchronized boolean updateQuestion(String id, Question updatedQuestion) {
        try {
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).getId().equals(id)) {
                    updatedQuestion.setId(id); // Ensure ID doesn't change
                    questions.set(i, updatedQuestion);
                    saveQuestions();
                    System.out.println("Updated question: " + id);
                    return true;
                }
            }
            System.err.println("Question with ID " + id + " not found.");
            return false;
        } catch (Exception e) {
            System.err.println("Error updating question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a question by ID
     */
    public synchronized boolean deleteQuestion(String id) {
        try {
            boolean removed = questions.removeIf(q -> q.getId().equals(id));
            if (removed) {
                saveQuestions();
                System.out.println("Deleted question: " + id);
                return true;
            }
            System.err.println("Question with ID " + id + " not found.");
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get questions by category
     */
    public synchronized List<Question> getQuestionsByCategory(String category) {
        return questions.stream()
                .filter(q -> q.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Get questions by difficulty
     */
    public synchronized List<Question> getQuestionsByDifficulty(String difficulty) {
        return questions.stream()
                .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                .collect(Collectors.toList());
    }

    /**
     * Generate a unique question ID
     */
    private String generateQuestionId() {
        return "Q" + System.currentTimeMillis() + "_" + (questions.size() + 1);
    }

    /**
     * Get total number of questions
     */
    public synchronized int getQuestionCount() {
        return questions.size();
    }

    /**
     * Clear all questions (use with caution!)
     */
    public synchronized void clearAllQuestions() {
        questions.clear();
        saveQuestions();
        System.out.println("Cleared all questions.");
    }
}
