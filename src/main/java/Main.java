import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import admin.AdminRequestHandler;
import utils.IQuestionManager;
import utils.JSONQuestionManager;
import utils.QuestionManager;

/**
 * Main server class for Quiz Game
 * Handles admin connections and question management
 */
public class Main {
    private static final int ADMIN_PORT = 8081;
    private static final String JSON_FILE_PATH = "src/main/resources/questions.json";
    private static IQuestionManager questionManager;
    private static boolean useMongoDb = true;

    public static void main(String[] args) {
        System.out.println("=== Quiz Game Server ===");
        
        // Initialize Question Manager (try MongoDB first, fallback to JSON)
        initializeQuestionManager();

        // Start Admin Server
        startAdminServer();
    }
    
    /**
     * Initialize question manager with MongoDB or JSON fallback
     */
    private static void initializeQuestionManager() {
        try {
            // Try MongoDB first
            QuestionManager mongoManager = new QuestionManager();
            int count = mongoManager.getQuestionCount();
            System.out.println("MongoDB Question Manager initialized with " + count + " questions.");
            questionManager = mongoManager;
            useMongoDb = true;
        } catch (Exception e) {
            // Fallback to JSON file storage
            System.err.println("MongoDB connection failed: " + e.getMessage());
            System.out.println("Falling back to JSON file storage...");
            
            JSONQuestionManager jsonManager = new JSONQuestionManager(JSON_FILE_PATH);
            int count = jsonManager.getQuestionCount();
            System.out.println("JSON Question Manager initialized with " + count + " questions.");
            questionManager = jsonManager;
            useMongoDb = false;
        }
    }

    /**
     * Start the admin server to handle admin requests
     */
    private static void startAdminServer() {
        try (ServerSocket adminServerSocket = new ServerSocket(ADMIN_PORT)) {
            System.out.println("Admin Server started on port " + ADMIN_PORT);
            System.out.println("Using " + (useMongoDb ? "MongoDB" : "JSON file") + " storage");
            System.out.println("Waiting for admin connections...");

            while (true) {
                try {
                    Socket clientSocket = adminServerSocket.accept();
                    // Handle each admin connection in a new thread
                    Thread adminThread = new Thread(new AdminRequestHandler(clientSocket, questionManager));
                    adminThread.start();
                } catch (IOException e) {
                    System.err.println("Error accepting admin connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start admin server on port " + ADMIN_PORT);
            e.printStackTrace();
        }
    }
}
