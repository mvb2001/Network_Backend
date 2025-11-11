// NETWORK_BACKEND/src/main/java/server/ClientHandler.java
package server.service;

import java.io.*;
import java.net.Socket;
import java.util.List;

import server.model.Question;
import server.util.MongoUtil;

public class ClientHandler implements Runnable {

    private Socket socket;
    private List<ClientHandler> clients;
    private List<Question> questions;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;

    public ClientHandler(Socket socket, List<ClientHandler> clients, List<Question> questions) {
        this.socket = socket;
        this.clients = clients;
        this.questions = questions;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Receive player name
            Object obj = in.readObject();
            if (obj instanceof String name) {
                playerName = name;
                System.out.println("Player connected: " + playerName);
            }

            // Send questions
            for (Question q : questions) {
                out.writeObject(q);
                out.flush();

                Object answerObj = in.readObject();
                if (answerObj instanceof String answer) {
                    System.out.println(playerName + " answered: " + answer);
                    MongoUtil.saveAnswer(playerName, q.getText(), answer);
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
