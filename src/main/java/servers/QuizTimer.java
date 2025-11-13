package servers;

import java.util.Timer;
import java.util.TimerTask;

public class QuizTimer {
    private int timeRemaining;
    private Timer timer;
    private Runnable onTimeout; // auto-submit callback

    public QuizTimer(int timeLimit, Runnable onTimeout) {
        this.timeRemaining = timeLimit;
        this.onTimeout = onTimeout;
        this.timer = new Timer();
    }

    public void start() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (timeRemaining <= 0) {
                    timer.cancel();
                    synchronized (QuizTimer.this) {
                        onTimeout.run();
                    }
                } else {
                    System.out.println("Time remaining: " + timeRemaining + " seconds");
                    timeRemaining--;
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public synchronized void stop() {
        timer.cancel();
    }
}
