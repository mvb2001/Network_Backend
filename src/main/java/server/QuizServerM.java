package server;

import java.net.ServerSocket;
import java.net.Socket;

public class QuizServerM {
    public static void main(String[] args) {
        int port = 5050;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Quiz Server running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandlerM(clientSocket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
