package server;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import utils.MongoDBConnection;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

public class LeaderboardDAO {

    private MongoCollection<Document> leaderboardCollection;
    private MongoCollection<Document> gameHistoryCollection;

    public LeaderboardDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.leaderboardCollection = db.getCollection("leaderboard");
        this.gameHistoryCollection = db.getCollection("game_history");
    }

    /**
     * Save final game results to MongoDB
     */
    public void saveFinalLeaderboard(List<Map.Entry<String, Integer>> leaderboard, String gameId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        Document gameDoc = new Document("gameId", gameId)
                .append("timestamp", timestamp)
                .append("players", new ArrayList<>());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : leaderboard) {
            Document playerDoc = new Document("rank", rank)
                    .append("playerName", entry.getKey())
                    .append("score", entry.getValue());

            ((List<Document>) gameDoc.get("players")).add(playerDoc);

            // Update player's all-time stats
            updatePlayerStats(entry.getKey(), entry.getValue(), rank);
            rank++;
        }

        gameHistoryCollection.insertOne(gameDoc);
        System.out.println(" Game results saved to MongoDB with ID: " + gameId);
    }

    /**
     * Update individual player statistics
     */
    private void updatePlayerStats(String playerName, int score, int rank) {
        Document existingPlayer = leaderboardCollection.find(eq("playerName", playerName)).first();

        if (existingPlayer == null) {
            // New player
            Document newPlayer = new Document("playerName", playerName)
                    .append("totalScore", score)
                    .append("gamesPlayed", 1)
                    .append("wins", rank == 1 ? 1 : 0)
                    .append("averageScore", (double) score)
                    .append("bestScore", score)
                    .append("lastPlayed", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            leaderboardCollection.insertOne(newPlayer);
        } else {
            // Update existing player
            int totalScore = existingPlayer.getInteger("totalScore") + score;
            int gamesPlayed = existingPlayer.getInteger("gamesPlayed") + 1;
            int wins = existingPlayer.getInteger("wins") + (rank == 1 ? 1 : 0);
            int bestScore = Math.max(existingPlayer.getInteger("bestScore"), score);
            double averageScore = (double) totalScore / gamesPlayed;

            Document update = new Document("$set", new Document()
                    .append("totalScore", totalScore)
                    .append("gamesPlayed", gamesPlayed)
                    .append("wins", wins)
                    .append("averageScore", averageScore)
                    .append("bestScore", bestScore)
                    .append("lastPlayed", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));

            leaderboardCollection.updateOne(eq("playerName", playerName), update);
        }
    }

    /**
     * Get all-time leaderboard from MongoDB
     */
    public List<Document> getAllTimeLeaderboard() {
        List<Document> leaderboard = new ArrayList<>();
        leaderboardCollection.find()
                .sort(descending("totalScore"))
                .limit(10)
                .into(leaderboard);
        return leaderboard;
    }

    /**
     * Get recent game history
     */
    public List<Document> getRecentGames(int limit) {
        List<Document> games = new ArrayList<>();
        gameHistoryCollection.find()
                .sort(descending("timestamp"))
                .limit(limit)
                .into(games);
        return games;
    }

    /**
     * Get player statistics
     */
    public Document getPlayerStats(String playerName) {
        return leaderboardCollection.find(eq("playerName", playerName)).first();
    }
}