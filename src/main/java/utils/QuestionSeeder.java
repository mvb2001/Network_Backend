package utils;

/**
 * Utility class to seed questions into MongoDB database
 * Run this class to add sample questions
 * mvn compile
 * mvn exec:java '-Dexec.mainClass=utils.QuestionSeeder'
 */
public class QuestionSeeder {
    public static void main(String[] args) {
        QuestionManager qm = new QuestionManager();
        
        // ALREADY ADDED - Comment out to avoid duplicates
        /*
        // Example: Add a new question
        Question newQuestion = new Question();
        newQuestion.setQuestionText("What is the largest planet in our solar system?");
        newQuestion.setOptions(new String[]{"Earth", "Jupiter", "Saturn", "Mars"});
        newQuestion.setCorrectOption("Jupiter");
        newQuestion.setTimeLimitSeconds(new Question.TimeLimit(20));
        
        qm.addQuestion(newQuestion);
        System.out.println("Question added successfully!");
        
        // Add more questions here if needed
        Question question2 = new Question();
        question2.setQuestionText("What is the speed of light?");
        question2.setOptions(new String[]{"299,792 km/s", "150,000 km/s", "500,000 km/s", "1,000,000 km/s"});
        question2.setCorrectOption("299,792 km/s");
        question2.setTimeLimitSeconds(new Question.TimeLimit(25));
        
        qm.addQuestion(question2);
        System.out.println("Second question added successfully!");

        //Question 3
        Question question3 = new Question();
        question3.setQuestionText("What does HTTP stand for?");
        question3.setOptions(new String[]{"HyperText Transfer Protocol", "High Tech Transfer Process", "Home Tool Transfer Protocol", "Hyperlink Text Transfer Protocol"});
        question3.setCorrectOption("HyperText Transfer Protocol");
        question3.setTimeLimitSeconds(new Question.TimeLimit(15));

        qm.addQuestion(question3);
        System.out.println("Third question added successfully!");

        //Question 4
        Question question4 = new Question();
        question4.setQuestionText("What is the default port for HTTP?");
        question4.setOptions(new String[]{"80", "443", "21", "25"});
        question4.setCorrectOption("80");
        question4.setTimeLimitSeconds(new Question.TimeLimit(15));

        qm.addQuestion(question4);
        System.out.println("Fourth question added successfully!");
        */
        
        // ===== ADD NEW QUESTIONS BELOW THIS LINE =====
        
        // Example for question 5:
        // Question question5 = new Question();
        // question5.setQuestionText("Your new question here?");
        // question5.setOptions(new String[]{"Option A", "Option B", "Option C", "Option D"});
        // question5.setCorrectOption("Option A");
        // question5.setTimeLimitSeconds(new Question.TimeLimit(15));
        // qm.addQuestion(question5);
        // System.out.println("Fifth question added successfully!");

        // Display all questions
        System.out.println("\nTotal questions in database: " + qm.getAllQuestions().size());
        
        // Clean up connection
        qm.close();
    }
}
