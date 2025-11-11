package server.model;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private String text;
    private List<String> options;
    private int correctIndex;
    private int timeLimitSeconds; // ⏱️ new field

    public Question() {} // Needed for Jackson & MongoDB

    public Question(String text, List<String> options, int correctIndex, int timeLimitSeconds) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
}
