package models;

import java.io.Serializable;

public class QuestionWithStartTime implements Serializable {
    private static final long serialVersionUID = 1L;

    private Question question;
    private long startTimeMillis; // server-defined start time

    public QuestionWithStartTime(Question question, long startTimeMillis) {
        this.question = question;
        this.startTimeMillis = startTimeMillis;
    }

    public Question getQuestion() { return question; }
    public long getStartTimeMillis() { return startTimeMillis; }
}
