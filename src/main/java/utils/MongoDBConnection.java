package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnection {

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                Dotenv dotenv = Dotenv.load();
                String connectionString = dotenv.get("MONGO_URI");
                String databaseName = dotenv.get("MONGO_DB");

                if (connectionString == null || databaseName == null) {
                    System.out.println("⚠️ MongoDB configuration missing in .env file. Skipping database setup.");
                    return null;
                }

                mongoClient = MongoClients.create(connectionString);
                database = mongoClient.getDatabase(databaseName);
                System.out.println("✅ Connected to MongoDB database: " + databaseName);
            } catch (Exception e) {
                System.out.println("⚠️ Could not connect to MongoDB. Continuing without DB...");
                e.printStackTrace();
            }
        }
        return database;
    }
}
