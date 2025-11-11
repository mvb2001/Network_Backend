import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import admin.AdminRequestHandler;
import utils.QuestionManager;

/**
 * Main server class for Quiz Game
 * Handles admin connections and question management
 */
public class Main {
    private static final int ADMIN_PORT = 8081;
    private static QuestionManager questionManager;

    public static void main(String[] args) {
        System.out.println("=== Quiz Game Server ===");
        
        // Initialize Question Manager
        questionManager = new QuestionManager();
        System.out.println("Question Manager initialized with " + 
                         questionManager.getQuestionCount() + " questions.");

        // Start Admin Server
        startAdminServer();
    }

    /**
     * Start the admin server to handle admin requests
     */
    private static void startAdminServer() {
        try (ServerSocket adminServerSocket = new ServerSocket(ADMIN_PORT)) {
            System.out.println("Admin Server started on port " + ADMIN_PORT);
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
