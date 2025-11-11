package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.InetSocketAddress;

public class QuestionController {

    private static final ObjectMapper mapper = new ObjectMapper(); // Jackson JSON mapper

    public static void startServer(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/addQuestion", (HttpExchange exchange) -> {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Read JSON request body
                    byte[] data = exchange.getRequestBody().readAllBytes();
                    Question q = mapper.readValue(data, Question.class);

                    // Save to MongoDB
                    MongoUtil.addQuestion(q);

                    String response = "Question added successfully";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            } catch (Exception e) {
                e.printStackTrace();
                String response = "Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        server.start();
        System.out.println("REST API server running on port " + port);
    }
}
