package servers;

public class MultiClientTest {
    public static void main(String[] args) {
        int numPlayers = 10; // simulate 10 players

        for (int i = 1; i <= numPlayers; i++) {
            final int playerId = i;
            new Thread(() -> {
                System.out.println("Starting player " + playerId);
                QuizClientTest.main(new String[]{String.valueOf(playerId)});
                System.out.println("Player " + playerId + " finished.");
            }, "Player-" + playerId).start();
        }
    }
}
