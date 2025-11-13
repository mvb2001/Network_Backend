package utils;

import com.mongodb.client.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBConnection {

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    private static final String COLLECTION_NAME = "question";

    // --- Connect to MongoDB using .env variables ---
    private static synchronized MongoDatabase connect() {
        if (database == null) {
            Dotenv dotenv = Dotenv.load();

            String connectionString = dotenv.get("MONGO_URI");
            String databaseName = dotenv.get("MONGO_DB");

            if (connectionString == null || databaseName == null) {
                throw new RuntimeException("Missing MongoDB configuration in .env file");
            }

            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase(databaseName);
            System.out.println("✅ Connected to MongoDB database: " + databaseName);
        }
        return database;
    }

    // --- Return the database object for custom queries ---
    public static MongoDatabase getDatabase() {
        return connect();
    }

    // --- Return the questions collection directly ---
    public static MongoCollection<Document> getQuestionCollection() {
        MongoDatabase db = connect();
        return db.getCollection(COLLECTION_NAME);
    }

    // --- Return all quiz questions as a List ---
    public static List<Document> getAllQuestions() {
        MongoCollection<Document> collection = getQuestionCollection();
        List<Document> questions = new ArrayList<>();
        collection.find().into(questions);
        System.out.println("📥 Loaded " + questions.size() + " questions from MongoDB");
        return questions;
    }

    // --- Close connection cleanly ---
    public static synchronized void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("🔒 MongoDB connection closed.");
        }
    }
}
