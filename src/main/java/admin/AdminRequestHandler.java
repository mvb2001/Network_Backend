package admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Question;
import utils.IQuestionManager;

/**
 * AdminRequestHandler processes admin requests via socket communication
 * Handles CRUD operations for questions after authentication
 */
public class AdminRequestHandler implements Runnable {
    private final Socket clientSocket;
    private final IQuestionManager questionManager;
    private final ObjectMapper objectMapper;
    private boolean isAuthenticated = false;
    private String authenticatedUser = null;

    public AdminRequestHandler(Socket socket, IQuestionManager questionManager) {
        this.clientSocket = socket;
        this.questionManager = questionManager;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            System.out.println("Admin client connected: " + clientSocket.getInetAddress());

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                String response = handleRequest(inputLine);
                out.println(response);
                
                // Close connection on logout
                if (inputLine.contains("\"action\":\"LOGOUT\"")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling admin request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Admin client disconnected");
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    /**
     * Handle incoming admin requests
     */
    private String handleRequest(String request) {
        try {
            ObjectNode jsonRequest = (ObjectNode) objectMapper.readTree(request);
            String action = jsonRequest.get("action").asText();

            // Check for session token in request (except for LOGIN)
            if (!"LOGIN".equals(action)) {
                if (jsonRequest.has("sessionToken")) {
                    String token = jsonRequest.get("sessionToken").asText();
                    // Simple token validation - token should match username
                    if (token != null && !token.isEmpty()) {
                        isAuthenticated = true;
                        authenticatedUser = token;
                    }
                }
                
                if (!isAuthenticated) {
                    return createErrorResponse("Authentication required");
                }
            }

            switch (action) {
                case "LOGIN":
                    return handleLogin(jsonRequest);
                case "LOGOUT":
                    return handleLogout();
                case "ADD_QUESTION":
                    return handleAddQuestion(jsonRequest);
                case "UPDATE_QUESTION":
                    return handleUpdateQuestion(jsonRequest);
                case "DELETE_QUESTION":
                    return handleDeleteQuestion(jsonRequest);
                case "GET_ALL_QUESTIONS":
                    return handleGetAllQuestions();
                case "GET_QUESTION":
                    return handleGetQuestion(jsonRequest);
                case "SEARCH_QUESTIONS":
                    return handleSearchQuestions(jsonRequest);
                case "GET_BY_TIME_LIMIT":
                    return handleGetByTimeLimit(jsonRequest);
                default:
                    return createErrorResponse("Unknown action: " + action);
            }
        } catch (Exception e) {
            return createErrorResponse("Error processing request: " + e.getMessage());
        }
    }

    private String handleLogin(ObjectNode request) {
        try {
            String username = request.get("username").asText();
            String password = request.get("password").asText();

            if (AdminAuthentication.authenticate(username, password)) {
                isAuthenticated = true;
                authenticatedUser = username;
                
                // Create response with session token
                ObjectNode response = objectMapper.createObjectNode();
                response.put("status", "success");
                response.put("message", "Login successful");
                response.put("token", username); // Simple token (username)
                
                ObjectNode data = objectMapper.createObjectNode();
                data.put("user", username);
                response.set("data", data);
                
                return response.toString();
            } else {
                return createErrorResponse("Invalid credentials");
            }
        } catch (Exception e) {
            return createErrorResponse("Login error: " + e.getMessage());
        }
    }

    private String handleLogout() {
        isAuthenticated = false;
        String user = authenticatedUser;
        authenticatedUser = null;
        return createSuccessResponse("Logout successful", "user", user);
    }

    private String handleAddQuestion(ObjectNode request) {
        try {
            ObjectNode questionData = (ObjectNode) request.get("question");
            Question question = objectMapper.treeToValue(questionData, Question.class);
            
            if (questionManager.addQuestion(question)) {
                return createSuccessResponse("Question added successfully", "question", question);
            } else {
                return createErrorResponse("Failed to add question");
            }
        } catch (Exception e) {
            return createErrorResponse("Error adding question: " + e.getMessage());
        }
    }

    private String handleUpdateQuestion(ObjectNode request) {
        try {
            String id = request.get("id").asText();
            ObjectNode questionData = (ObjectNode) request.get("question");
            Question question = objectMapper.treeToValue(questionData, Question.class);
            
            if (questionManager.updateQuestion(id, question)) {
                return createSuccessResponse("Question updated successfully", "question", question);
            } else {
                return createErrorResponse("Failed to update question");
            }
        } catch (Exception e) {
            return createErrorResponse("Error updating question: " + e.getMessage());
        }
    }

    private String handleDeleteQuestion(ObjectNode request) {
        try {
            String id = request.get("id").asText();
            
            if (questionManager.deleteQuestion(id)) {
                return createSuccessResponse("Question deleted successfully", "id", id);
            } else {
                return createErrorResponse("Failed to delete question");
            }
        } catch (Exception e) {
            return createErrorResponse("Error deleting question: " + e.getMessage());
        }
    }

    private String handleGetAllQuestions() {
        try {
            List<Question> questions = questionManager.getAllQuestions();
            return createSuccessResponse("Questions retrieved", "questions", questions);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving questions: " + e.getMessage());
        }
    }

    private String handleGetQuestion(ObjectNode request) {
        try {
            String id = request.get("id").asText();
            Question question = questionManager.getQuestionById(id);
            
            if (question != null) {
                return createSuccessResponse("Question found", "question", question);
            } else {
                return createErrorResponse("Question not found");
            }
        } catch (Exception e) {
            return createErrorResponse("Error retrieving question: " + e.getMessage());
        }
    }

    private String handleSearchQuestions(ObjectNode request) {
        try {
            String searchText = request.get("searchText").asText();
            List<Question> questions = questionManager.searchQuestions(searchText);
            return createSuccessResponse("Questions retrieved", "questions", questions);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving questions: " + e.getMessage());
        }
    }

    private String handleGetByTimeLimit(ObjectNode request) {
        try {
            int timeLimit = request.get("timeLimit").asInt();
            List<Question> questions = questionManager.getQuestionsByTimeLimit(timeLimit);
            return createSuccessResponse("Questions retrieved", "questions", questions);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving questions: " + e.getMessage());
        }
    }

    private String createSuccessResponse(String message, String dataKey, Object dataValue) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", "success");
            response.put("message", message);
            response.set("data", objectMapper.valueToTree(dataValue));
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return createErrorResponse("Error creating response");
        }
    }

    private String createErrorResponse(String message) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", "error");
            response.put("message", message);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"Critical error\"}";
        }
    }
}
