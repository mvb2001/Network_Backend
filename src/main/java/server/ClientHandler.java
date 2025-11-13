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

            // Save to MongoDB if available
            try {
                MongoDatabase db = utils.MongoDBConnection.getDatabase();
                if (db != null) {
                    db.getCollection("players").insertOne(new Document("name", playerName));
                    System.out.println("✅ Player inserted successfully into MongoDB!");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Failed to save player to MongoDB. Continuing without DB.");
                e.printStackTrace();
            }

            // Add to client set
            synchronized (clients) {
                clients.add(this);
            }

            // Broadcast join message
            QuizServer.broadcast("Player joined: " + playerName, this);

            // Send full lobby list to this client
            sendLobbyList();

            // Listen for messages from client
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("REQUEST_LOBBY_LIST")) {
                    sendLobbyList(); // respond to explicit requests
                } else {
                    System.out.println(playerName + ": " + message);
                    QuizServer.broadcast(playerName + ": " + message, this);
                }
            }

        } catch (IOException e) {
            // Normal if client disconnects abruptly
            System.out.println(playerName + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}

            synchronized (clients) {
                clients.remove(this);
            }

            QuizServer.broadcast("Player left: " + playerName, this);
        }
    }

    // Send a message to this client
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // Send the full lobby list to this client
    private void sendLobbyList() {
        StringBuilder sb = new StringBuilder();
        synchronized (clients) {
            for (ClientHandler c : clients) {
                sb.append(c.getPlayerName()).append(",");
            }
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1); // remove trailing comma
        sendMessage("LobbyList:" + sb.toString());
    }
}
