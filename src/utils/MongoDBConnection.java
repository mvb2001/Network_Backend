package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb+srv://network2:Network%402@network2.iqmxbdi.mongodb.net/?appName=Network2"; // change if remote
    private static final String DATABASE_NAME = "Network2";
    private static MongoDatabase database = null;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("✅ Connected to MongoDB database: " + DATABASE_NAME);
        }
        return database;
    }
}
