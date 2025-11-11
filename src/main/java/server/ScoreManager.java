package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Thread-safe score manager for multiplayer quiz game
 * Handles scoring based on correctness, speed, and streaks
 *
 
 */
public class ScoreManager {

    // Thread-safe storage for player scores and streaks
    private final ConcurrentHashMap<String, Integer> playerScores = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> playerStreaks = new ConcurrentHashMap<>();

    // Configuration
    private static final int BASE_POINTS = 10;
    private static final double MAX_TIME_BONUS_MULTIPLIER = 0.5; // Up to 50% bonus for speed
    private static final int STREAK_BONUS_PER_LEVEL = 2; // +2 points per streak level
    private static final int MAX_STREAK_BONUS_LEVELS = 5; // Max +10 points at 5+ streak

    /**
     * Initialize a player with zero score
     * Call this when a player joins the game
     */
    public synchronized void initializePlayer(String playerName) {
        playerScores.putIfAbsent(playerName, 0);
        playerStreaks.putIfAbsent(playerName, 0);
        System.out.println(" Initialized score tracking for: " + playerName);
    }

    /**
     * Calculate and add score based on answer correctness and time taken
     *
     * @param playerName Player's name
     * @param isCorrect Whether the answer was correct
     * @param timeTakenMs Time taken to answer in milliseconds
     * @param timeLimitMs Question time limit in milliseconds
     * @return Points awarded this round
     */
    public int calculateAndAddScore(String playerName, boolean isCorrect,
                                    long timeTakenMs, long timeLimitMs) {
        if (!isCorrect) {
            // Reset streak on wrong answer
            playerStreaks.put(playerName, 0);
            System.out.println( playerName + " answered incorrectly. Streak reset.");
            return 0;
        }

        // Calculate time bonus: faster answers get more points
        double timeRatio = Math.max(0, 1.0 - ((double) timeTakenMs / timeLimitMs));
        int timeBonus = (int) (BASE_POINTS * timeRatio * MAX_TIME_BONUS_MULTIPLIER);

        // Calculate streak bonus: reward consecutive correct answers
        int currentStreak = playerStreaks.merge(playerName, 1, Integer::sum);
        int streakLevel = Math.min(currentStreak - 1, MAX_STREAK_BONUS_LEVELS);
        int streakBonus = streakLevel * STREAK_BONUS_PER_LEVEL;

        // Total points for this question
        int totalPoints = BASE_POINTS + timeBonus + streakBonus;

        // Add to player's total score (thread-safe)
        playerScores.merge(playerName, totalPoints, Integer::sum);

        // Log detailed scoring breakdown
        System.out.println(String.format(
                " %s scored %d points (Base: %d, Time: %d, Streak: +%d) | Streak: %d | Total: %d",
                playerName, totalPoints, BASE_POINTS, timeBonus,
                streakBonus, currentStreak, playerScores.get(playerName)
        ));

        return totalPoints;
    }

    /**
     * Manually add points to a player 
     */
    public void addScore(String playerName, int points) {
        playerScores.merge(playerName, points, Integer::sum);
    }

    /**
     * Get current score for a specific player
     */
    public int getScore(String playerName) {
        return playerScores.getOrDefault(playerName, 0);
    }

    /**
     * Get current streak for a specific player
     */
    public int getStreak(String playerName) {
        return playerStreaks.getOrDefault(playerName, 0);
    }

    /**
     * Get sorted leaderboard (highest score first)
     * Returns list of entries for easy iteration
     */
    public List<Map.Entry<String, Integer>> getLeaderboard() {
        return playerScores.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Descending order
                .collect(Collectors.toList());
    }

    /**
     * Get leaderboard as formatted string (for display/debugging)
     */
    public String getLeaderboardString() {
        StringBuilder sb = new StringBuilder("\n╔════════════════════════════════════╗\n");
        sb.append("║       🏆 LEADERBOARD 🏆           ║\n");
        sb.append("╠════════════════════════════════════╣\n");

        List<Map.Entry<String, Integer>> leaderboard = getLeaderboard();
        int rank = 1;

        for (Map.Entry<String, Integer> entry : leaderboard) {
            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
            int streak = getStreak(entry.getKey());
            String streakDisplay = streak > 0 ? " 🔥" + streak : "";

            sb.append(String.format("║ %s #%d %-15s %4d pts%s\n",
                    medal, rank, entry.getKey(), entry.getValue(), streakDisplay));
            rank++;
        }

        sb.append("╚════════════════════════════════════╝\n");
        return sb.toString();
    }

    /**
     * Reset all scores and streaks (for new game)
     */
    public synchronized void resetScores() {
        playerScores.clear();
        playerStreaks.clear();
        System.out.println(" All scores and streaks have been reset.");
    }

    /**
     * Get all player names currently tracked
     */
    public Set<String> getAllPlayers() {
        return playerScores.keySet();
    }

    /**
     * Check if a player exists in the score system
     */
    public boolean hasPlayer(String playerName) {
        return playerScores.containsKey(playerName);
    }

    /**
     * Print current leaderboard to console (for debugging)
     */
    public void printLeaderboard() {
        System.out.println(getLeaderboardString());
    }

    /**
     * Get player count
     */
    public int getPlayerCount() {
        return playerScores.size();
    }

    /**
     * Get statistics summary for a player
     */
    public Map<String, Object> getPlayerStats(String playerName) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("playerName", playerName);
        stats.put("score", getScore(playerName));
        stats.put("streak", getStreak(playerName));

        // Calculate rank
        List<Map.Entry<String, Integer>> leaderboard = getLeaderboard();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : leaderboard) {
            if (entry.getKey().equals(playerName)) {
                stats.put("rank", rank);
                break;
            }
            rank++;
        }

        return stats;
    }
}