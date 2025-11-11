package server;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.List;

public class MongoUtil {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String MONGO_URI = dotenv.get("MONGO_URI");
    private static final String MONGO_DB = dotenv.get("MONGO_DB");

    private static final MongoClient mongoClient = MongoClients.create(MONGO_URI);
    private static final MongoDatabase database = mongoClient.getDatabase(MONGO_DB);

    /** ✅ Save question to MongoDB (used by AdminController REST API) */
    public static void addQuestion(Question q) {
        MongoCollection<Document> coll = database.getCollection("questions");
        Document doc = new Document("text", q.getText())
                .append("options", q.getOptions())
                .append("correctIndex", q.getCorrectIndex());
        coll.insertOne(doc);
        System.out.println("Question added to MongoDB: " + q.getText());
    }

    /** ✅ Load all questions (used when QuizServer starts) */
    public static List<Question> loadQuestions() {
        MongoCollection<Document> coll = database.getCollection("questions");
        List<Question> questions = new ArrayList<>();
        for (Document doc : coll.find()) {
            String text = doc.getString("text");
            List<String> options = (List<String>) doc.get("options");
            int correct = doc.getInteger("correctIndex", 0);
            questions.add(new Question(text, options, correct));
        }
        return questions;
    }

    /** ✅ Save player’s answer to a new MongoDB collection */
    public static void saveAnswer(String playerName, String questionText, String answer) {
        MongoCollection<Document> coll = database.getCollection("answers");
        Document doc = new Document("playerName", playerName)
                .append("questionText", questionText)
                .append("answer", answer)
                .append("timestamp", System.currentTimeMillis());
        coll.insertOne(doc);
        System.out.println("Saved answer from " + playerName + ": " + answer);
    }
}
