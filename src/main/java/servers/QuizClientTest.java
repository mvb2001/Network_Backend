package servers;

import models.Question;

import java.io.ObjectInputStream;
import java.net.Socket;

public class QuizClientTest {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000);
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("Connected to QuizServer");

            while (true) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Question q) {
                        System.out.println("Question: " + q.getQuestionText());
                        System.out.println("Options: " + q.getOptions());
                        System.out.println("Time Limit: " + q.getTimeLimit() + " seconds");
                        System.out.println("----------------------");
                    }
                } catch (java.io.EOFException eof) {
                    System.out.println("Server closed connection. Exiting client.");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
