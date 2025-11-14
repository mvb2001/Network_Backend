package utils;

import model.Question;
import java.util.List;

/**
 * Quick verification tool to list all questions in the database
 * Run this to verify frontend additions are stored
 */
public class VerifyQuestions {
    public static void main(String[] args) {
        System.out.println("=== Database Verification Tool ===\n");
        
        QuestionManager qm = new QuestionManager();
        List<Question> questions = qm.getAllQuestions();
        
        System.out.println("Total Questions in Database: " + questions.size());
        System.out.println("\n--- Question List ---\n");
        
        int count = 1;
        for (Question q : questions) {
            System.out.println(count + ". " + q.getQuestionText());
            System.out.println("   Options: " + String.join(" | ", q.getOptions()));
            System.out.println("   Correct: " + q.getCorrectOption());
            System.out.println("   Time: " + q.getTimeLimitSeconds().get$numberInt() + " seconds");
            System.out.println("   ID: " + q.get_id().get$oid());
            System.out.println();
            count++;
        }
        
        System.out.println("--- End of List ---");
        qm.close();
    }
}
