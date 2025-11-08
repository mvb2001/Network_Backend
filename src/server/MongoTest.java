package server;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import utils.MongoDBConnection;

public class MongoTest {
    public static void main(String[] args) {
        MongoDatabase db = MongoDBConnection.getDatabase();

        MongoCollection<Document> collection = db.getCollection("team");

        Document player = new Document("name", "Mahima")
                .append("score", 10);

        collection.insertOne(player);
        System.out.println("✅ Player inserted successfully!");
    }
}
