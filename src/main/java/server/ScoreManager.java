package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreManager {

    private ConcurrentHashMap<String, Integer> playerScores = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> playerStreaks = new ConcurrentHashMap<>();

    /**
     * Calculate score based on correctness, time taken, and streak
     */
    public void calculateAndAddScore(String playerName, boolean isCorrect,
                                     long timeTaken, long questionTimeLimit, int basePoints) {
        if (!isCorrect) {
            // Reset streak on wrong answer
            playerStreaks.put(playerName, 0);
            return;
        }

        // Time bonus: faster answers get more points (up to 50% bonus)
        double timeRatio = 1.0 - ((double) timeTaken / questionTimeLimit);
        int timeBonus = (int) (basePoints * timeRatio * 0.5);

        // Streak bonus: reward consecutive correct answers
        int streak = playerStreaks.merge(playerName, 1, Integer::sum);
        int streakBonus = Math.min(streak - 1, 5) * 2; // Max +10 for 5+ streak

        int totalPoints = basePoints + timeBonus + streakBonus;

        playerScores.merge(playerName, totalPoints, Integer::sum);

        System.out.println(String.format(" %s scored %d points (Base: %d, Time: %d, Streak: %d, Total streak: %d)",
                playerName, totalPoints, basePoints, timeBonus, streakBonus, streak));
    }

    /**
     * Add fixed score (for initialization or special cases)
     */
    public void addScore(String playerName, int score) {
        playerScores.merge(playerName, score, Integer::sum);
    }

    /**
     * Get current score for a player
     */
    public int getScore(String playerName) {
        return playerScores.getOrDefault(playerName, 0);
    }

    /**
     * Get leaderboard sorted by score (descending)
     */
    public List<Map.Entry<String, Integer>> getLeaderboard() {
        return playerScores.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Reset all scores for a new quiz
     */
    public void resetScores() {
        playerScores.clear();
        playerStreaks.clear();
    }

    /**
     * Get current streak for a player
     */
    public int getStreak(String playerName) {
        return playerStreaks.getOrDefault(playerName, 0);
    }

    /**
     * Print leaderboard to console (for debugging)
     */
    public void printLeaderboard() {
        System.out.println("\n=== Current Leaderboard ===");
        getLeaderboard().forEach(entry ->
                System.out.println(entry.getKey() + " : " + entry.getValue() + " points"));
        System.out.println("===========================\n");
    }
}