
package servers;

import models.Question;
import models.QuestionWithStartTime;
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

            // Load all questions from MongoDB
            List<Document> questionDocs = MongoDBConnection.getAllQuestions();
            System.out.println("✅ Loaded " + questionDocs.size() + " questions from MongoDB");

            // Send questions sequentially
            for (Document doc : questionDocs) {
                Question q = Question.fromDocument(doc);

                // Set a synchronized start time (2 seconds in the future)
                long startTime = System.currentTimeMillis() + 2000;

                // Wrap question with start time
                QuestionWithStartTime qwt = new QuestionWithStartTime(q, startTime);

                // Send the question and start time to the client
                out.writeObject(qwt);
                out.flush();
                System.out.println("📤 Sent question to client " + clientSocket.getPort() +
                        " with start time: " + startTime);

                // Wait for client's answer (manual or auto-submitted)
                Object answerObj = in.readObject();
                String answer = answerObj instanceof String ? (String) answerObj : "";
                System.out.println("✅ Client " + clientSocket.getPort() +
                        " submitted answer: " + answer);
            }

            System.out.println("🏁 All questions sent to client " + clientSocket.getPort());

        } catch (Exception e) {
            System.err.println("❌ Error handling client " + clientSocket.getPort());
            e.printStackTrace();
        }
    }
}
