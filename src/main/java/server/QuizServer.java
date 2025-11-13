package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main Quiz Server
 * Manages all client connections and coordinates scoring
 */
public class QuizServer {
    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    // SHARED INSTANCES (one for all clients)
    private static final ScoreManager scoreManager = new ScoreManager();
    private static final LeaderboardDAO leaderboardDAO = new LeaderboardDAO();

    public static void main(String[] args) {
        System.out.println("🎮 Quiz Server starting on port " + PORT);
        System.out.println("═══════════════════════════════════════");

        // Initialize MongoDB connection
        utils.MongoDBConnection.getDatabase();
        System.out.println("✅ MongoDB connected");
        System.out.println("✅ Server ready. Waiting for players...\n");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                // Accept new client connection
                Socket socket = serverSocket.accept();
                System.out.println("🔌 New connection from: " + socket.getInetAddress());

                // Create client handler with SHARED score manager
                ClientHandler handler = new ClientHandler(socket, scoreManager, leaderboardDAO);
                clients.add(handler);

                // Start client thread
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("❌ Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Save final leaderboard when server shuts down
            saveFinalLeaderboard();
            System.out.println("🔒 Server shut down.");
        }
    }

    /**
     * Broadcast message to all connected clients
     */
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Broadcast to ALL clients (including sender)
     */
    public static void broadcastToAll(Object message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

   public static void broadcastLeaderboardUpdate() {
    List<Map.Entry<String, Integer>> leaderboard = scoreManager.getLeaderboard();
    
    StringBuilder sb = new StringBuilder("LEADERBOARD:");
    for (Map.Entry<String, Integer> entry : leaderboard) {
        int streak = scoreManager.getStreak(entry.getKey());
        sb.append(entry.getKey()).append(":")
          .append(entry.getValue()).append(":")
          .append(streak).append(",");
    }
    
    // Remove trailing comma
    if (sb.charAt(sb.length() - 1) == ',') {
        sb.setLength(sb.length() - 1);
    }
    
    broadcastToAll(sb.toString());
}

    /**
     * Remove client when they disconnect
     */
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.getPlayerName() != null) {
            broadcast("PLAYER_LEFT:" + client.getPlayerName(), client);
            System.out.println("📤 Broadcasted player left: " + client.getPlayerName());
        }
    }

    /**
     * Get the shared score manager (for API endpoints)
     */
    public static ScoreManager getScoreManager() {
        return scoreManager;
    }

    /**
     * Get the shared leaderboard DAO (for API endpoints)
     */
    public static LeaderboardDAO getLeaderboardDAO() {
        return leaderboardDAO;
    }

    /**
     * Save final leaderboard to MongoDB
     */
    public static void saveFinalLeaderboard() {
        if (scoreManager.getPlayerCount() > 0) {
            String gameId = "GAME_" + System.currentTimeMillis();
            leaderboardDAO.saveFinalLeaderboard(scoreManager.getLeaderboard(), gameId);
            System.out.println("💾 Final leaderboard saved to MongoDB with ID: " + gameId);
        }
    }

    /**
     * Get number of connected players
     */
    public static int getPlayerCount() {
        return clients.size();
    }

    /**
     * Print current leaderboard to console
     */
    public static void printLeaderboard() {
        scoreManager.printLeaderboard();
    }

    /**
     * Reset game (for new round)
     */
    public static void resetGame() {
        scoreManager.resetScores();
        System.out.println("🔄 Game reset. All scores cleared.");
    }
}