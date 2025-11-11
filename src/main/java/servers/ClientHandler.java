//package servers;
//
//import models.Question;
//import utils.MongoDBConnection;
//import org.bson.Document;
//
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
//import java.util.List;
//
//public class ClientHandler implements Runnable {
//    private Socket clientSocket;
//
//    public ClientHandler(Socket clientSocket) {
//        this.clientSocket = clientSocket;
//    }
//
//    @Override
//    public void run() {
//        try {
//            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
//            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
//
//            // Load questions from MongoDB
//            List<Document> questionDocs = MongoDBConnection.getAllQuestions();
//
//            for (Document doc : questionDocs) {
//                Question q = Question.fromDocument(doc);
//
//                // Send question object
//                out.writeObject(q);
//                out.flush();
//
//                // Read the answer from the client (blocks until received)
//                Object answerObj = in.readObject();
//                String answer = answerObj instanceof String ? (String) answerObj : "";
//                System.out.println("Client " + clientSocket.getPort() +
//                        " submitted answer: " + answer);
//            }
//
//            System.out.println("All questions sent to client " + clientSocket.getPort());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
package servers;

import models.Question;
import utils.MongoDBConnection;
import org.bson.Document;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // Load all questions
            List<Document> questionDocs = MongoDBConnection.getAllQuestions();
            System.out.println("Loaded " + questionDocs.size() + " questions from MongoDB");

            // Send questions sequentially
            for (Document doc : questionDocs) {
                Question q = Question.fromDocument(doc);

                // Send the question to the client
                out.writeObject(q);
                out.flush();

                // Wait for client's answer (manual or auto-submit)
                Object answerObj = in.readObject();
                String answer = answerObj instanceof String ? (String) answerObj : "";
                System.out.println("Client " + clientSocket.getPort() + " submitted answer: " + answer);
            }

            System.out.println("All questions sent to client " + clientSocket.getPort());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
