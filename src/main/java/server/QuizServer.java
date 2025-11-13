package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QuizServer {

    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("🎮 Quiz Server starting on port " + PORT);

        // Initialize MongoDB
        utils.MongoDBConnection.getDatabase();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) { // Keep server running
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clients);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all clients except the sender
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Remove client when disconnected
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcast("Player left: " + client.getPlayerName(), client);
        System.out.println(client.getPlayerName() + " disconnected.");
    }
}
