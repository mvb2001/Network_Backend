package utils;

import java.util.concurrent.*;

public class TimerTaskHandler {

    // ExecutorService for multithreading timer tasks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    /**
     * Starts a timer for a player's question.
     * @param seconds Time limit in seconds
     * @param onTimeout Runnable to execute when time expires
     */
    public void startTimer(int seconds, Runnable onTimeout) {
        // Multithreading: Each timer runs in its own thread
        scheduler.schedule(() -> {
            // Timer expired
            synchronized(this) { // Synchronization: prevents concurrent modification
                onTimeout.run();
            }
        }, seconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
