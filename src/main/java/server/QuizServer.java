package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class QuizServer {

    private static final int PORT = 12345;
    private static final int TOTAL_QUESTIONS = 5;
    private static final int QUESTION_TIME_LIMIT = 30000; // 30 seconds
    private static final int BASE_POINTS = 10;

    private ServerSocket serverSocket;
    private ScoreManager scoreManager;
    private LeaderboardDAO leaderboardDAO;
    private List<ClientHandler> clients;
    private String currentGameId;
    private volatile boolean quizActive;
    private volatile int currentQuestionNumber;
    private Timer questionTimer;
    private Set<String> playersAnswered; // Track who answered current question
    private Object questionLock = new Object(); // For synchronization

    public QuizServer() {
        scoreManager = new ScoreManager();
        leaderboardDAO = new LeaderboardDAO();
        clients = new CopyOnWriteArrayList<>();
        playersAnswered = ConcurrentHashMap.newKeySet();
        currentGameId = "GAME_" + System.currentTimeMillis();
        quizActive = false;
        currentQuestionNumber = 0;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║   Quiz Server Started on Port " + PORT + "  ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println("Waiting for players to connect...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New player connected!");
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void startNewQuiz() {
        if (quizActive) {
            broadcastMessage(" Quiz already in progress!");
            return;
        }

        if (clients.size() < 2) {
            broadcastMessage(" Need at least 2 players to start quiz!");
            System.out.println("Cannot start quiz: Only " + clients.size() + " player(s) connected");
            return;
        }

        scoreManager.resetScores();
        playersAnswered.clear();
        currentGameId = "GAME_" + System.currentTimeMillis();
        quizActive = true;
        currentQuestionNumber = 0;

        // Initialize all players with 0 score
        for (ClientHandler client : clients) {
            scoreManager.addScore(client.getPlayerName(), 0);
        }

        broadcastMessage("═══════════════════════════════════");
        broadcastMessage("NEW QUIZ STARTING!");
        broadcastMessage("Game ID: " + currentGameId);
        broadcastMessage("Total Questions: " + TOTAL_QUESTIONS);
        broadcastMessage("Players: " + clients.size());
        broadcastMessage("⚡ Answer quickly for bonus points!");
        broadcastMessage("═══════════════════════════════════");
        broadcastMessage("QUIZ_STARTED");

        System.out.println("\n New quiz started!");
        System.out.println("Game ID: " + currentGameId);
        System.out.println("Players: " + clients.size());

        // Start first question after 3 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                nextQuestion();
            }
        }, 3000);
    }

    public synchronized void nextQuestion() {
        if (!quizActive) return;

        currentQuestionNumber++;

        if (currentQuestionNumber > TOTAL_QUESTIONS) {
            endQuiz();
            return;
        }

        // Clear answered players for new question
        playersAnswered.clear();

        // Broadcast question to all clients
        broadcastMessage("\n═══════════════════════════════════");
        broadcastMessage(" QUESTION " + currentQuestionNumber + " of " + TOTAL_QUESTIONS);
        broadcastMessage("═══════════════════════════════════");
        broadcastMessage("QUESTION_START:" + currentQuestionNumber);

        // Sample question (in real app, this would come from database)
        String question = getQuestion(currentQuestionNumber);
        broadcastMessage("Q: " + question);
        broadcastMessage("⏱ Time Limit: 30 seconds");
        broadcastMessage("⚡ First to answer gets bonus points!");
        broadcastMessage("───────────────────────────────────");

        System.out.println(" Question " + currentQuestionNumber + " sent to all players");

        // Set timeout - auto-advance if not all players answer in time
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        questionTimer = new Timer();
        questionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (questionLock) {
                    if (quizActive && currentQuestionNumber <= TOTAL_QUESTIONS) {
                        broadcastMessage("\n Time's up! Moving to next question...");
                        System.out.println(" Time expired. " + playersAnswered.size() + "/" + clients.size() + " players answered.");

                        // Wait 2 seconds to show leaderboard, then move to next
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                nextQuestion();
                            }
                        }, 2000);
                    }
                }
            }
        }, QUESTION_TIME_LIMIT);
    }

    /**
     * Called when a player submits an answer
     * Checks if all players have answered and auto-advances
     */
    public void recordAnswer(String playerName) {
        synchronized (questionLock) {
            playersAnswered.add(playerName);

            System.out.println( + playersAnswered.size() + "/" + clients.size() + " players answered question " + currentQuestionNumber);

            // Check if all players have answered
            if (playersAnswered.size() >= clients.size()) {
                System.out.println(" All players answered! Moving to next question...");

                // Cancel the timer since everyone answered
                if (questionTimer != null) {
                    questionTimer.cancel();
                }

                broadcastMessage("\n All players answered!");

                // Wait 3 seconds to show leaderboard, then move to next question
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (currentQuestionNumber < TOTAL_QUESTIONS) {
                            nextQuestion();
                        } else {
                            endQuiz();
                        }
                    }
                }, 3000);
            }
        }
    }

    private String getQuestion(int questionNumber) {
        // Sample questions - in production, load from database
        String[] questions = {
                "What is the capital of France?",
                "What is 2 + 2?",
                "What is the largest planet in our solar system?",
                "Who painted the Mona Lisa?",
                "What is the speed of light? (approximately)"
        };
        return questions[questionNumber - 1];
    }

    public synchronized void broadcastLeaderboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════╗\n");
        sb.append("║        🏆 LEADERBOARD 🏆          ║\n");
        sb.append("║     Question " + currentQuestionNumber + " of " + TOTAL_QUESTIONS + "               ║\n");
        sb.append("╚════════════════════════════════════╝\n");

        List<Map.Entry<String, Integer>> board = scoreManager.getLeaderboard();
        if (board.isEmpty()) {
            sb.append("No scores yet!\n");
        } else {
            int rank = 1;
            for (Map.Entry<String, Integer> entry : board) {
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
                int streak = scoreManager.getStreak(entry.getKey());
                String streakInfo = streak > 1 ? " 🔥x" + streak : "";
                sb.append(String.format("%s #%d  %-15s %3d pts%s\n",
                        medal, rank++, entry.getKey(), entry.getValue(), streakInfo));
            }
        }
        sb.append("────────────────────────────────────\n");

        String leaderboard = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(leaderboard);
        }
    }

    public synchronized void endQuiz() {
        if (!quizActive) {
            return;
        }

        quizActive = false;
        currentQuestionNumber = 0;
        playersAnswered.clear();

        if (questionTimer != null) {
            questionTimer.cancel();
        }

        System.out.println("\n Quiz ended! Saving results to MongoDB...");

        List<Map.Entry<String, Integer>> finalLeaderboard = scoreManager.getLeaderboard();

        // Save to MongoDB
        leaderboardDAO.saveFinalLeaderboard(finalLeaderboard, currentGameId);

        // Broadcast final results
        broadcastMessage("\n═══════════════════════════════════");
        broadcastMessage(" QUIZ ENDED! ");
        broadcastMessage("═══════════════════════════════════");

        // Final leaderboard
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════╗\n");
        sb.append("║       FINAL RESULTS          ║\n");
        sb.append("╚════════════════════════════════════╝\n");

        if (!finalLeaderboard.isEmpty()) {
            int rank = 1;
            for (Map.Entry<String, Integer> entry : finalLeaderboard) {
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
                sb.append(String.format("%s #%d  %-15s %3d pts\n",
                        medal, rank++, entry.getKey(), entry.getValue()));
            }

            // Announce winner
            Map.Entry<String, Integer> winner = finalLeaderboard.get(0);
            sb.append("────────────────────────────────────\n");
            sb.append(" WINNER: " + winner.getKey() + " with " + winner.getValue() + " points!\n");
        }
        sb.append("────────────────────────────────────\n");

        broadcastMessage(sb.toString());
        broadcastMessage(" Results saved to database.");
        broadcastMessage("═══════════════════════════════════");
        broadcastMessage("QUIZ_ENDED");

        System.out.println(" Quiz ended successfully. Results saved to MongoDB.");
        System.out.println("Type 'START_QUIZ' from any client to begin a new quiz.\n");
    }

    public void broadcastAllTimeLeaderboard() {
        List<org.bson.Document> allTimeBoard = leaderboardDAO.getAllTimeLeaderboard();

        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════╗\n");
        sb.append("║    ALL-TIME LEADERBOARD      ║\n");
        sb.append("╚════════════════════════════════════╝\n");

        if (allTimeBoard.isEmpty()) {
            sb.append("No historical data yet!\n");
        } else {
            int rank = 1;
            for (org.bson.Document player : allTimeBoard) {
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
                sb.append(String.format("%s #%d  %-15s Total: %d | Games: %d | Avg: %.1f\n",
                        medal, rank++,
                        player.getString("playerName"),
                        player.getInteger("totalScore"),
                        player.getInteger("gamesPlayed"),
                        player.getDouble("averageScore")));
            }
        }
        sb.append("────────────────────────────────────\n");

        String leaderboard = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(leaderboard);
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println(" Player disconnected: " + client.getPlayerName());
        System.out.println("Current players: " + clients.size());
    }

    public boolean hasPlayerAnswered(String playerName) {
        return playersAnswered.contains(playerName);
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public LeaderboardDAO getLeaderboardDAO() {
        return leaderboardDAO;
    }

    public String getCurrentGameId() {
        return currentGameId;
    }

    public boolean isQuizActive() {
        return quizActive;
    }

    public int getCurrentQuestionNumber() {
        return currentQuestionNumber;
    }

    public int getQuestionTimeLimit() {
        return QUESTION_TIME_LIMIT;
    }

    public int getBasePoints() {
        return BASE_POINTS;
    }

    public static void main(String[] args) {
        QuizServer server = new QuizServer();
        server.startServer();
    }
}