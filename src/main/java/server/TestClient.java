package server;

import models.Question;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 * Test client to connect to your quiz server
 * Run this AFTER starting QuizServer
 */
public class TestClient {

    public static void main(String[] args) {
        String playerName = args.length > 0 ? "Player" + args[0] : "TestPlayer";

        try (Socket socket = new Socket("localhost", 5000)) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            System.out.println(" Connected to Quiz Server");

            // Read name prompt
            Object namePrompt = in.readObject();
            System.out.println("Server: " + namePrompt);

            // Send player name
            out.writeObject(playerName);
            out.flush();
            System.out.println(" Registered as: " + playerName);

            Scanner scanner = new Scanner(System.in);
            Random random = new Random();

            // Receive and answer questions
            while (true) {
                try {
                    Object obj = in.readObject();

                    if (obj instanceof String) {
                        String message = (String) obj;

                        if (message.equals("GAME_FINISHED")) {
                            System.out.println("\n Quiz Finished!");

                            // Read final leaderboard
                            Object leaderboardObj = in.readObject();
                            System.out.println("Final Leaderboard: " + leaderboardObj);
                            break;

                        } else if (message.startsWith("FEEDBACK:")) {
                            System.out.println( message);

                        } else if (message.startsWith("LEADERBOARD:")) {
                            System.out.println( message);

                        } else {
                            System.out.println("Server: " + message);
                        }

                    } else if (obj instanceof Question) {
                        Question question = (Question) obj;

                        System.out.println("\n Question: " + question.getQuestionText());
                        System.out.println("Options: " + question.getOptions());
                        System.out.println("Time Limit: " + question.getTimeLimit() + " seconds");
                        System.out.print("Your answer: ");

                        // Choose answer mode
                        String answer;
                        if (args.length > 0 && args[0].equals("auto")) {
                            // Auto mode: pick random answer with random delay
                            Thread.sleep(random.nextInt(question.getTimeLimit() * 1000));
                            answer = question.getOptions().get(random.nextInt(question.getOptions().size()));
                            System.out.println(answer + " (auto)");
                        } else {
                            // Manual mode: user types answer
                            answer = scanner.nextLine();
                        }

                        // Send answer
                        out.writeObject(answer);
                        out.flush();
                    }

                } catch (java.io.EOFException e) {
                    System.out.println("🔌 Server closed connection.");
                    break;
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}