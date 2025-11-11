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
     * Get question by ID (MongoDB ObjectId)
     */
    public synchronized Question getQuestionById(String oid) {
        return questions.stream()
                .filter(q -> q.get_id().get$oid().equals(oid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add a new question
     */
    public synchronized boolean addQuestion(Question question) {
        try {
            // Ensure the question has an ID
            if (question.get_id() == null) {
                question.set_id(new Question.MongoId());
            }
            
            // Check if question with same ID already exists
            String oid = question.get_id().get$oid();
            if (getQuestionById(oid) != null) {
                System.err.println("Question with ID " + oid + " already exists.");
                return false;
            }
            
            // Ensure time limit is set
            if (question.getTimeLimitSeconds() == null) {
                question.setTimeLimitSeconds(new Question.TimeLimit(15));
            }
            
            questions.add(question);
            saveQuestions();
            System.out.println("Added question: " + oid);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update an existing question
     */
    public synchronized boolean updateQuestion(String oid, Question updatedQuestion) {
        try {
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).get_id().get$oid().equals(oid)) {
                    // Ensure ID doesn't change
                    updatedQuestion.set_id(questions.get(i).get_id());
                    questions.set(i, updatedQuestion);
                    saveQuestions();
                    System.out.println("Updated question: " + oid);
                    return true;
                }
            }
            System.err.println("Question with ID " + oid + " not found.");
            return false;
        } catch (Exception e) {
            System.err.println("Error updating question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a question by ID
     */
    public synchronized boolean deleteQuestion(String oid) {
        try {
            boolean removed = questions.removeIf(q -> q.get_id().get$oid().equals(oid));
            if (removed) {
                saveQuestions();
                System.out.println("Deleted question: " + oid);
                return true;
            }
            System.err.println("Question with ID " + oid + " not found.");
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get questions by text search
     */
    public synchronized List<Question> searchQuestions(String searchText) {
        return questions.stream()
                .filter(q -> q.getQuestionText().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Get questions by time limit
     */
    public synchronized List<Question> getQuestionsByTimeLimit(int seconds) {
        return questions.stream()
                .filter(q -> q.getTimeLimitSeconds().getSeconds() == seconds)
                .collect(Collectors.toList());
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
