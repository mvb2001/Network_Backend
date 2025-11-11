package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import server.model.Question;
import server.util.MongoUtil;
import server.controller.QuestionController;
import server.service.ClientHandler;

public class QuizServer {
    private static final int PORT = 5000;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static List<Question> questions = new ArrayList<>();

    public static void main(String[] args) {
        // Load questions from MongoDB
        questions = MongoUtil.loadQuestions();
        System.out.println("📘 Loaded " + questions.size() + " questions from MongoDB");

        // Start REST API for Admin
        try {
            QuestionController.startServer(8080);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start TCP server for quiz clients
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🎯 Quiz Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(socket, clients, questions);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
