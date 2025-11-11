package model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Question model representing a quiz question (MongoDB format)
 * This class is used to store and manage quiz questions
 */
public class Question {
    @JsonProperty("_id")
    private MongoId _id;
    
    @JsonProperty("questionText")
    private String questionText;
    
    @JsonProperty("options")
    private String[] options;
    
    @JsonProperty("correctOption")
    private String correctOption;
    
    @JsonProperty("timeLimitSeconds")
    private TimeLimit timeLimitSeconds;

    // Default constructor (required for Jackson)
    public Question() {
        this._id = new MongoId();
        this.timeLimitSeconds = new TimeLimit(15);
    }

    // Constructor with all parameters
    public Question(String questionText, String[] options, String correctOption, int timeLimitSeconds) {
        this._id = new MongoId();
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
        this.timeLimitSeconds = new TimeLimit(timeLimitSeconds);
    }

    // Constructor with MongoDB ID
    public Question(MongoId _id, String questionText, String[] options, String correctOption, int timeLimitSeconds) {
        this._id = _id;
        this.questionText = questionText;
        this.options = options;
        this.correctOption = correctOption;
        this.timeLimitSeconds = new TimeLimit(timeLimitSeconds);
    }

    // Getters and Setters
    public MongoId get_id() {
        return _id;
    }

    public void set_id(MongoId _id) {
        this._id = _id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public TimeLimit getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(TimeLimit timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    @Override
    public String toString() {
        return "Question{" +
                "_id=" + _id +
                ", questionText='" + questionText + '\'' +
                ", correctOption='" + correctOption + '\'' +
                ", timeLimitSeconds=" + timeLimitSeconds +
                '}';
    }

    // Inner class for MongoDB ObjectId format
    public static class MongoId {
        @JsonProperty("$oid")
        private String $oid;

        public MongoId() {
            this.$oid = generateObjectId();
        }

        public MongoId(String oid) {
            this.$oid = oid;
        }

        public String get$oid() {
            return $oid;
        }

        public void set$oid(String $oid) {
            this.$oid = $oid;
        }

        // Generate a MongoDB-like ObjectId (24 hex characters)
        private String generateObjectId() {
            long timestamp = System.currentTimeMillis() / 1000;
            String timestampHex = String.format("%08x", timestamp);
            String randomHex = String.format("%016x", (long)(Math.random() * Long.MAX_VALUE));
            return timestampHex + randomHex.substring(0, 16);
        }

        @Override
        public String toString() {
            return $oid;
        }
    }

    // Inner class for time limit format
    public static class TimeLimit {
        @JsonProperty("$numberInt")
        private String $numberInt;

        public TimeLimit() {
            this.$numberInt = "15";
        }

        public TimeLimit(int seconds) {
            this.$numberInt = String.valueOf(seconds);
        }

        public TimeLimit(String numberInt) {
            this.$numberInt = numberInt;
        }

        public String get$numberInt() {
            return $numberInt;
        }

        public void set$numberInt(String $numberInt) {
            this.$numberInt = $numberInt;
        }

        public int getSeconds() {
            return Integer.parseInt($numberInt);
        }

        @Override
        public String toString() {
            return $numberInt;
        }
    }
}
