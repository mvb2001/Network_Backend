package server;

import models.Question;
import org.bson.Document;
import utils.MongoDBConnection;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * Handles individual client connections and quiz gameplay
 * Integrates with ScoreManager for scoring and LeaderboardDAO for persistence
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;
    private ScoreManager scoreManager;
    private LeaderboardDAO leaderboardDAO;

    public ClientHandler(Socket socket, ScoreManager scoreManager, LeaderboardDAO leaderboardDAO) {
        this.socket = socket;
        this.scoreManager = scoreManager;
        this.leaderboardDAO = leaderboardDAO;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void run() {
        try {
            // Setup streams
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Get player name
            out.writeObject("ENTER_NAME");
            out.flush();
            playerName = (String) in.readObject();
            System.out.println(" Player connected: " + playerName);

            // Initialize player in score manager
            scoreManager.initializePlayer(playerName);

            // Save player to MongoDB
            savePlayerToMongoDB();

            // Load questions from MongoDB
            List<Document> questionDocs = new java.util.ArrayList<>();
            MongoDBConnection.getDatabase().getCollection("question").find().into(questionDocs);
            System.out.println(" Loaded " + questionDocs.size() + " questions for " + playerName);

            // Send questions one by one
            for (Document doc : questionDocs) {
                Question question = Question.fromDocument(doc);
                sendQuestionAndProcessAnswer(question);
            }

            // Quiz finished - send final leaderboard
            sendFinalLeaderboard();

            System.out.println(playerName + " completed the quiz!");

        } catch (Exception e) {
            System.err.println(" Error with client " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * Send a question, wait for answer, calculate score
     */
    private void sendQuestionAndProcessAnswer(Question question) {
        try {
            // Record start time
            long startTime = System.currentTimeMillis();

            // Send question to client
            out.writeObject(question);
            out.flush();
            System.out.println("📤 Sent to " + playerName + ": " + question.getQuestionText());

            // Wait for answer (with timeout)
            socket.setSoTimeout((question.getTimeLimit() + 2) * 1000); // +2 seconds buffer
            String answer = null;

            try {
                Object answerObj = in.readObject();
                answer = answerObj instanceof String ? (String) answerObj : "";
                System.out.println( playerName + " answered: " + answer);
            } catch (java.net.SocketTimeoutException e) {
                System.out.println( playerName + " timed out (auto-submit)");
                answer = ""; // Empty answer = wrong
            }

            // Calculate time taken
            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            // Check if answer is correct
            boolean isCorrect = answer != null &&
                    answer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());

            // Calculate and add score
            int pointsAwarded = scoreManager.calculateAndAddScore(
                    playerName,
                    isCorrect,
                    timeTaken,
                    question.getTimeLimit() * 1000L // Convert to milliseconds
            );

            // Send feedback to player
            Map<String, Object> feedback = Map.of(
                    "correct", isCorrect,
                    "points", pointsAwarded,
                    "totalScore", scoreManager.getScore(playerName),
                    "streak", scoreManager.getStreak(playerName)
            );

            out.writeObject("FEEDBACK:" + feedback.toString());
            out.flush();

            // Send updated leaderboard to all clients
            broadcastLeaderboard();

        } catch (Exception e) {
            System.err.println(" Error processing question for " + playerName);
            e.printStackTrace();
        }
    }

    /**
     * Send final leaderboard and save to MongoDB
     */
    private void sendFinalLeaderboard() {
        try {
            List<Map.Entry<String, Integer>> leaderboard = scoreManager.getLeaderboard();

            // Convert to serializable format
            List<String> leaderboardStrings = new java.util.ArrayList<>();
            int rank = 1;
            for (Map.Entry<String, Integer> entry : leaderboard) {
                leaderboardStrings.add(rank + ". " + entry.getKey() + " - " + entry.getValue() + " points");
                rank++;
            }

            // Send to client
            out.writeObject("GAME_FINISHED");
            out.writeObject(leaderboardStrings);
            out.flush();

            System.out.println(" Sent final leaderboard to " + playerName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast current leaderboard to all clients
     */
    private void broadcastLeaderboard() {
        // This will be implemented in QuizServer to broadcast to all clients
        QuizServer.broadcastLeaderboardUpdate();
    }

    /**
     * Save player info to MongoDB
     */
    private void savePlayerToMongoDB() {
        try {
            var db = MongoDBConnection.getDatabase();
            db.getCollection("players").insertOne(
                    new Document("name", playerName)
                            .append("joinedAt", new java.util.Date())
            );
            System.out.println(" Saved " + playerName + " to MongoDB");
        } catch (Exception e) {
            System.err.println("Failed to save player to MongoDB: " + e.getMessage());
        }
    }

    /**
     * Cleanup when client disconnects
     */
    private void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        QuizServer.removeClient(this);
        System.out.println( playerName + " disconnected.");
    }

    /**
     * Send a message to this client
     */
    public void sendMessage(Object message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Failed to send message to " + playerName);
        }
    }
}