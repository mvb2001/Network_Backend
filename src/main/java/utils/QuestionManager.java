package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import io.github.cdimascio.dotenv.Dotenv;
import model.Question;

/**
 * QuestionManager handles CRUD operations for quiz questions
 * Stores and retrieves questions from MongoDB database
 */
public class QuestionManager {
    private final MongoCollection<Document> questionsCollection;
    private final ObjectMapper objectMapper;
    private final MongoClient mongoClient;

    public QuestionManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Load environment variables
        Dotenv dotenv = Dotenv.configure()
                .directory(".idea")
                .load();
        
        String mongoUri = dotenv.get("MONGO_URI");
        String dbName = dotenv.get("MONGO_DB");
        
        // Connect to MongoDB
        this.mongoClient = MongoClients.create(mongoUri);
        MongoDatabase database = mongoClient.getDatabase(dbName);
        this.questionsCollection = database.getCollection("questions");
        
        System.out.println("Connected to MongoDB database: " + dbName);
        System.out.println("Question Manager initialized with " + getQuestionCount() + " questions.");
    }

    /**
     * Load questions from MongoDB
     */
    public synchronized List<Question> loadQuestions() {
        try {
            List<Question> questions = new ArrayList<>();
            for (Document doc : questionsCollection.find()) {
                Question question = documentToQuestion(doc);
                questions.add(question);
            }
            System.out.println("Loaded " + questions.size() + " questions from MongoDB.");
            return questions;
        } catch (Exception e) {
            System.err.println("Error loading questions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get all questions
     */
    public synchronized List<Question> getAllQuestions() {
        return loadQuestions();
    }

    /**
     * Get question by ID (MongoDB ObjectId)
     */
    public synchronized Question getQuestionById(String oid) {
        try {
            Document doc = questionsCollection.find(Filters.eq("_id", new ObjectId(oid))).first();
            if (doc != null) {
                return documentToQuestion(doc);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting question by ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Add a new question
     */
    public synchronized boolean addQuestion(Question question) {
        try {
            // Convert question to MongoDB document
            Document doc = questionToDocument(question);
            
            // Insert into MongoDB
            questionsCollection.insertOne(doc);
            
            String oid = doc.getObjectId("_id").toString();
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
            ObjectId objectId = new ObjectId(oid);
            Document doc = questionToDocument(updatedQuestion);
            doc.put("_id", objectId); // Ensure ID doesn't change
            
            questionsCollection.replaceOne(
                Filters.eq("_id", objectId),
                doc,
                new ReplaceOptions().upsert(false)
            );
            
            System.out.println("Updated question: " + oid);
            return true;
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
            ObjectId objectId = new ObjectId(oid);
            var result = questionsCollection.deleteOne(Filters.eq("_id", objectId));
            
            if (result.getDeletedCount() > 0) {
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
        try {
            List<Question> results = new ArrayList<>();
            for (Document doc : questionsCollection.find(
                Filters.regex("questionText", searchText, "i")
            )) {
                results.add(documentToQuestion(doc));
            }
            return results;
        } catch (Exception e) {
            System.err.println("Error searching questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get questions by time limit
     */
    public synchronized List<Question> getQuestionsByTimeLimit(int seconds) {
        try {
            List<Question> allQuestions = loadQuestions();
            return allQuestions.stream()
                    .filter(q -> q.getTimeLimitSeconds().getSeconds() == seconds)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error filtering questions by time limit: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get total number of questions
     */
    public synchronized int getQuestionCount() {
        return (int) questionsCollection.countDocuments();
    }

    /**
     * Clear all questions (use with caution!)
     */
    public synchronized void clearAllQuestions() {
        questionsCollection.deleteMany(new Document());
        System.out.println("Cleared all questions.");
    }

    /**
     * Convert MongoDB Document to Question object
     */
    private Question documentToQuestion(Document doc) {
        Question question = new Question();
        
        // Set _id
        ObjectId objectId = doc.getObjectId("_id");
        question.set_id(new Question.MongoId(objectId.toString()));
        
        // Set question text
        question.setQuestionText(doc.getString("questionText"));
        
        // Set options
        List<String> optionsList = doc.getList("options", String.class);
        question.setOptions(optionsList.toArray(new String[0]));
        
        // Set correct option
        question.setCorrectOption(doc.getString("correctOption"));
        
        // Set time limit - handle both integer and Document formats
        Object timeLimitObj = doc.get("timeLimitSeconds");
        if (timeLimitObj instanceof Document) {
            // Format: {"$numberInt": "15"}
            Document timeLimitDoc = (Document) timeLimitObj;
            String timeValue = timeLimitDoc.getString("$numberInt");
            question.setTimeLimitSeconds(new Question.TimeLimit(timeValue));
        } else if (timeLimitObj instanceof Integer) {
            // Format: 15 (plain integer)
            Integer timeValue = (Integer) timeLimitObj;
            question.setTimeLimitSeconds(new Question.TimeLimit(timeValue));
        } else {
            // Default to 15 seconds
            question.setTimeLimitSeconds(new Question.TimeLimit(15));
        }
        
        return question;
    }

    /**
     * Convert Question object to MongoDB Document
     */
    private Document questionToDocument(Question question) {
        Document doc = new Document();
        
        // Set _id if provided
        if (question.get_id() != null && question.get_id().get$oid() != null) {
            try {
                doc.put("_id", new ObjectId(question.get_id().get$oid()));
            } catch (IllegalArgumentException e) {
                // If invalid ObjectId, MongoDB will generate a new one
            }
        }
        
        // Set question text
        doc.put("questionText", question.getQuestionText());
        
        // Set options
        List<String> optionsList = new ArrayList<>();
        for (String option : question.getOptions()) {
            optionsList.add(option);
        }
        doc.put("options", optionsList);
        
        // Set correct option
        doc.put("correctOption", question.getCorrectOption());
        
        // Set time limit in MongoDB extended JSON format
        Document timeLimitDoc = new Document("$numberInt", 
            question.getTimeLimitSeconds().get$numberInt());
        doc.put("timeLimitSeconds", timeLimitDoc);
        
        return doc;
    }

    /**
     * Close MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}
