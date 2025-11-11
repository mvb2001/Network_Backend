package server;

import java.io.*;
import java.net.*;

public class TestClient {

    private static volatile boolean quizActive = false;
    private static PrintWriter out;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Read welcome message and name prompt
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.contains("Enter your name:")) {
                    break;
                }
            }

            String name = console.readLine();
            out.println(name);

            // Start thread to read messages from server
            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);

                        // Detect quiz start
                        if (response.equals("QUIZ_STARTED")) {
                            quizActive = true;
                            System.out.println("\n>>>  Quiz is now active! Answer the questions as they appear. <<<\n");
                        }

                        //   Auto-start timer when question arrives
                        if (response.startsWith("QUESTION_START:")) {
                            // Automatically send timing signal to server
                            out.println("QUESTION_START");
                            System.out.println("\n[Timer started - answer now!]\n");
                        }

                        // Detect quiz end
                        if (response.equals("QUIZ_ENDED")) {
                            quizActive = false;
                            System.out.println("\n>>> Quiz has ended. Type 'START_QUIZ' to begin a new quiz or 'quit' to exit. <<<\n");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            }).start();

            System.out.println("\n=== Quiz Client Started ===");
            System.out.println("Commands:");
            System.out.println("  correct / wrong - Answer current question");
            System.out.println("  GET_STATS - View your statistics");
            System.out.println("  GET_ALLTIME - View all-time leaderboard");
            System.out.println("  START_QUIZ - Start new quiz");
            System.out.println("  END_QUIZ - End current quiz (admin)");
            System.out.println("  quit - Exit");
            System.out.println("==========================\n");

            while (true) {
                String msg = console.readLine();
                if (msg == null || msg.equalsIgnoreCase("quit")) {
                    System.out.println("Exiting...");
                    break;
                }

                // Warn if trying to answer without active quiz
                if (!quizActive && (msg.equalsIgnoreCase("correct") || msg.equalsIgnoreCase("wrong"))) {
                    System.out.println("⚠ No active quiz! Type 'START_QUIZ' to begin.");
                    continue;
                }

                // Handle shorthand commands
                if (msg.equalsIgnoreCase("correct")) {
                    out.println("ANSWER:correct");
                } else if (msg.equalsIgnoreCase("wrong")) {
                    out.println("ANSWER:wrong");
                } else if (msg.equalsIgnoreCase("START_QUIZ")) {
                    out.println("START_QUIZ");
                } else if (msg.equalsIgnoreCase("GET_STATS")) {
                    out.println("GET_STATS");
                } else if (msg.equalsIgnoreCase("GET_ALLTIME")) {
                    out.println("GET_ALLTIME");
                } else if (msg.equalsIgnoreCase("END_QUIZ")) {
                    out.println("END_QUIZ");
                } else {
                    out.println(msg);
                }
            }

            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}