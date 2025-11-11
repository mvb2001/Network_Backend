package server;

import java.io.*;
import java.net.*;
import org.bson.Document;

public class ClientHandler extends Thread {

    private Socket socket;
    private QuizServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private long questionStartTime;
    private int lastAnsweredQuestion;

    public ClientHandler(Socket socket, QuizServer server) {
        this.socket = socket;
        this.server = server;
        this.lastAnsweredQuestion = 0;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Welcome message
            out.println("╔════════════════════════════════════╗");
            out.println("║     Welcome to Quiz Game!      ║");
            out.println("╚════════════════════════════════════╝");
            out.println("Enter your name:");
            playerName = in.readLine();

            if (playerName == null || playerName.trim().isEmpty()) {
                out.println("Invalid name. Disconnecting...");
                socket.close();
                return;
            }

            System.out.println(" Player joined: " + playerName);

            // Send player's historical stats
            sendPlayerStats();

            out.println("\nWaiting for quiz to start...");
            out.println("Type 'START_QUIZ' to begin when ready!");
            out.println("Type 'GET_STATS' to view your statistics");
            out.println("Type 'GET_ALLTIME' to view all-time leaderboard");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("ANSWER:")) {
                    handleAnswer(message);
                } else if (message.equals("QUESTION_START")) {
                    questionStartTime = System.currentTimeMillis();
                } else if (message.equals("GET_STATS")) {
                    sendPlayerStats();
                } else if (message.equals("GET_ALLTIME")) {
                    server.broadcastAllTimeLeaderboard();
                } else if (message.equals("START_QUIZ")) {
                    server.startNewQuiz();
                } else if (message.equals("END_QUIZ")) {
                    if (server.isQuizActive()) {
                        server.endQuiz();
                    } else {
                        sendMessage(" No active quiz to end.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Player disconnected: " + playerName);
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAnswer(String message) {
        if (!server.isQuizActive()) {
            sendMessage(" No active quiz! Wait for quiz to start.");
            return;
        }

        int currentQuestion = server.getCurrentQuestionNumber();

        // Prevent answering the same question twice
        if (lastAnsweredQuestion == currentQuestion) {
            sendMessage(" You already answered this question!");
            return;
        }

        // Check if already answered (using server tracking)
        if (server.hasPlayerAnswered(playerName)) {
            sendMessage(" You already answered this question!");
            return;
        }

        long timeTaken = System.currentTimeMillis() - questionStartTime;

        // Prevent answers after time limit
        if (timeTaken > server.getQuestionTimeLimit()) {
            sendMessage(" Time's up! Your answer was not counted.");
            return;
        }

        String[] parts = message.split(":");
        boolean isCorrect = parts[1].equals("correct");

        int pointsEarned = 0;

        if (isCorrect) {
            pointsEarned = calculatePoints(timeTaken);
            server.getScoreManager().calculateAndAddScore(
                    playerName,
                    true,
                    timeTaken,
                    server.getQuestionTimeLimit(),
                    server.getBasePoints()
            );

            // Show time taken
            double secondsTaken = timeTaken / 1000.0;
            sendMessage(String.format(" Correct! +%d points (answered in %.1f seconds)",
                    pointsEarned, secondsTaken));
        } else {
            server.getScoreManager().calculateAndAddScore(
                    playerName,
                    false,
                    timeTaken,
                    server.getQuestionTimeLimit(),
                    server.getBasePoints()
            );
            sendMessage(" Wrong answer! No points.");
        }

        lastAnsweredQuestion = currentQuestion;

        // Record that this player has answered
        server.recordAnswer(playerName);

        // Broadcast updated leaderboard to all players
        server.broadcastLeaderboard();
    }

    private int calculatePoints(long timeTaken) {
        int basePoints = server.getBasePoints();
        double timeRatio = 1.0 - ((double) timeTaken / server.getQuestionTimeLimit());
        int timeBonus = (int) (basePoints * timeRatio * 0.5);
        return basePoints + timeBonus;
    }

    private void sendPlayerStats() {
        try {
            Document stats = server.getLeaderboardDAO().getPlayerStats(playerName);
            if (stats != null) {
                String statsMessage = String.format(
                        "\n Your Statistics:\n" +
                                "───────────────────────────────────\n" +
                                "Total Score: %d\n" +
                                "Games Played: %d\n" +
                                "Wins: %d\n" +
                                "Average Score: %.1f\n" +
                                "Best Score: %d\n" +
                                "───────────────────────────────────\n",
                        stats.getInteger("totalScore", 0),
                        stats.getInteger("gamesPlayed", 0),
                        stats.getInteger("wins", 0),
                        stats.getDouble("averageScore"),
                        stats.getInteger("bestScore", 0)
                );
                sendMessage(statsMessage);
            } else {
                sendMessage(" Welcome! This is your first game.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching stats for " + playerName + ": " + e.getMessage());
        }
    }

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public String getPlayerName() {
        return playerName;
    }
}