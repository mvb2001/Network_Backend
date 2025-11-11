package server;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clients;
    private String playerName;

    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Ask client for name
            out.println("Enter your name:");
            playerName = in.readLine();
            System.out.println(playerName + " joined the game.");

            // Try MongoDB insertion safely
            try {
                MongoDatabase db = utils.MongoDBConnection.getDatabase();
                if (db != null) {
                    db.getCollection("players").insertOne(new Document("name", playerName));
                    System.out.println("✅ Player inserted successfully into MongoDB!");
                } else {
                    System.out.println("⚠️ MongoDB not available. Player not saved.");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Failed to save player to MongoDB. Continuing without DB.");
                e.printStackTrace();
            }

            // Broadcast join message
            QuizServer.broadcast("Player joined: " + playerName, this);

            // Listen for messages from client
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(playerName + ": " + message);
                QuizServer.broadcast(playerName + ": " + message, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            QuizServer.removeClient(this);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
