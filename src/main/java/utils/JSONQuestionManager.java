package utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import model.Question;

/**
 * JSONQuestionManager handles question storage in JSON file
 * Used as fallback when MongoDB is not available
 */
public class JSONQuestionManager implements IQuestionManager {
    private final String jsonFilePath;
    private final ObjectMapper objectMapper;
    private List<Question> questions;

    public JSONQuestionManager(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.questions = new ArrayList<>();
        this.questions = loadQuestionsInternal();
    }
    
    /**
     * Internal method to load questions (called from constructor)
     */
    private List<Question> loadQuestionsInternal() {
        try {
            File file = new File(jsonFilePath);
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    List<Question> loaded = objectMapper.readValue(reader, new TypeReference<List<Question>>() {});
                    System.out.println("Loaded " + loaded.size() + " questions from JSON file: " + jsonFilePath);
                    return loaded;
                }
            } else {
                System.out.println("JSON file not found, starting with empty questions list");
            }
        } catch (IOException e) {
            System.err.println("Error loading questions from JSON: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Load questions from JSON file
     */
    @Override
    public synchronized List<Question> loadQuestions() {
        try {
            File file = new File(jsonFilePath);
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    questions = objectMapper.readValue(reader, new TypeReference<List<Question>>() {});
                    System.out.println("Loaded " + questions.size() + " questions from JSON file: " + jsonFilePath);
                }
            } else {
                System.out.println("JSON file not found, starting with empty questions list");
                questions = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Error loading questions from JSON: " + e.getMessage());
            questions = new ArrayList<>();
        }
        return new ArrayList<>(questions);
    }

    /**
     * Save questions to JSON file
     */
    private synchronized boolean saveQuestions() {
        try {
            File file = new File(jsonFilePath);
            file.getParentFile().mkdirs(); // Create directories if needed
            
            try (FileWriter writer = new FileWriter(file)) {
                objectMapper.writeValue(writer, questions);
                System.out.println("Saved " + questions.size() + " questions to JSON file");
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error saving questions to JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all questions
     */
    @Override
    public synchronized List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    /**
     * Get question by ID
     */
    @Override
    public synchronized Question getQuestionById(String oid) {
        return questions.stream()
                .filter(q -> q.get_id() != null && q.get_id().get$oid().equals(oid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add a new question
     */
    @Override
    public synchronized boolean addQuestion(Question question) {
        try {
            // Generate ID if not present
            if (question.get_id() == null || question.get_id().get$oid() == null) {
                question.set_id(new Question.MongoId(generateId()));
            }
            
            questions.add(question);
            return saveQuestions();
        } catch (Exception e) {
            System.err.println("Error adding question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update an existing question
     */
    @Override
    public synchronized boolean updateQuestion(String oid, Question updatedQuestion) {
        try {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                if (q.get_id() != null && q.get_id().get$oid().equals(oid)) {
                    // Preserve the original ID
                    updatedQuestion.set_id(new Question.MongoId(oid));
                    questions.set(i, updatedQuestion);
                    return saveQuestions();
                }
            }
            System.err.println("Question not found: " + oid);
            return false;
        } catch (Exception e) {
            System.err.println("Error updating question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a question by ID
     */
    @Override
    public synchronized boolean deleteQuestion(String oid) {
        try {
            boolean removed = questions.removeIf(q -> 
                q.get_id() != null && q.get_id().get$oid().equals(oid)
            );
            
            if (removed) {
                saveQuestions();
                System.out.println("Deleted question: " + oid);
                return true;
            }
            System.err.println("Question not found: " + oid);
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search questions by text
     */
    @Override
    public synchronized List<Question> searchQuestions(String searchText) {
        List<Question> results = new ArrayList<>();
        String searchLower = searchText.toLowerCase();
        
        for (Question q : questions) {
            if (q.getQuestionText() != null && 
                q.getQuestionText().toLowerCase().contains(searchLower)) {
                results.add(q);
            }
        }
        return results;
    }

    /**
     * Get questions by time limit
     */
    @Override
    public synchronized List<Question> getQuestionsByTimeLimit(int seconds) {
        List<Question> results = new ArrayList<>();
        for (Question q : questions) {
            if (q.getTimeLimitSeconds() != null && 
                q.getTimeLimitSeconds().getSeconds() == seconds) {
                results.add(q);
            }
        }
        return results;
    }

    /**
     * Get total number of questions
     */
    @Override
    public synchronized int getQuestionCount() {
        return questions.size();
    }

    /**
     * Clear all questions
     */
    @Override
    public synchronized void clearAllQuestions() {
        questions.clear();
        saveQuestions();
        System.out.println("Cleared all questions from JSON");
    }

    /**
     * Generate a unique ID (simple implementation)
     */
    private String generateId() {
        return String.format("%024x", System.currentTimeMillis() * 1000 + (int)(Math.random() * 1000));
    }
}
