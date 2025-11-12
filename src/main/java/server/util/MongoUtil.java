package server.util;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import io.github.cdimascio.dotenv.Dotenv;
import server.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB utility class for quiz data operations.
 * Handles connection, saving, and loading questions and answers.
 */
public class MongoUtil {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String MONGO_URI = dotenv.get("MONGO_URI");
    private static final String MONGO_DB = dotenv.get("MONGO_DB");

    private static final MongoClient mongoClient = MongoClients.create(MONGO_URI);
    private static final MongoDatabase database = mongoClient.getDatabase(MONGO_DB);

    /**
     * ✅ Save a new question to MongoDB (matches current schema)
     */
    public static void addQuestion(Question q) {
        MongoCollection<Document> coll = database.getCollection("questions");
        Document doc = new Document("questionText", q.getText())
                .append("options", q.getOptions())
                .append("correctOption", q.getOptions().get(q.getCorrectIndex()))
                .append("timeLimitSeconds", q.getTimeLimitSeconds());
        coll.insertOne(doc);
        System.out.println("✅ Question added to MongoDB: " + q.getText());
    }

    /**
     * ✅ Load all questions safely (matching MongoDB structure)
     */
    public static List<Question> loadQuestions() {
        MongoCollection<Document> coll = database.getCollection("questions");
        List<Question> questions = new ArrayList<>();

        for (Document doc : coll.find()) {
            String text = doc.getString("questionText");
            List<String> options = (List<String>) doc.get("options");
            String correctOption = doc.getString("correctOption");
            int correctIndex = 0;
            int timeLimit = 10;

            // ✅ Find correct index based on matching correctOption in options list
            if (options != null && correctOption != null) {
                correctIndex = options.indexOf(correctOption);
                if (correctIndex == -1) correctIndex = 0; // fallback
            }

            // ✅ Read time limit safely
            try {
                Object timeObj = doc.get("timeLimitSeconds");
                if (timeObj instanceof Integer) {
                    timeLimit = (Integer) timeObj;
                } else if (timeObj instanceof Double) {
                    timeLimit = ((Double) timeObj).intValue();
                } else if (timeObj instanceof String) {
                    timeLimit = Integer.parseInt((String) timeObj);
                } else if (timeObj instanceof Document) {
                    Document d = (Document) timeObj;
                    if (d.containsKey("$numberInt")) {
                        timeLimit = Integer.parseInt(d.getString("$numberInt"));
                    } else if (d.containsKey("value")) {
                        timeLimit = d.getInteger("value", 10);
                    }
                }
            } catch (Exception ignored) {}

            // ✅ Create question and add to list
            if (text != null && options != null && !options.isEmpty()) {
                questions.add(new Question(text, options, correctIndex, timeLimit));
            }
        }

        System.out.println("✅ Loaded " + questions.size() + " questions from MongoDB");
        return questions;
    }

    /**
     * ✅ Save player's submitted answer to MongoDB
     */
    public static void saveAnswer(String playerName, String questionText, String answer) {
        MongoCollection<Document> coll = database.getCollection("answers");
        Document doc = new Document("playerName", playerName)
                .append("questionText", questionText)
                .append("answer", answer)
                .append("timestamp", System.currentTimeMillis());
        coll.insertOne(doc);
        System.out.println("💾 Saved answer from " + playerName + ": " + answer);
    }

    /**
     * ✅ Close MongoDB connection when needed
     */
    public static void closeConnection() {
        mongoClient.close();
        System.out.println("🔒 MongoDB connection closed.");
    }
}
