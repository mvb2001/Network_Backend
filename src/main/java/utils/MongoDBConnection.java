// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;

public class MongoDBConnection {

    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    public MongoDBConnection() {
    }

    public static MongoDatabase getDatabase() {
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
}
