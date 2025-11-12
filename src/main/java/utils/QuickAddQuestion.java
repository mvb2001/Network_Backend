package utils;

import model.Question;

/**
 * Quick test to add a sample question and verify it's saved
 */
public class QuickAddQuestion {
    public static void main(String[] args) {
        System.out.println("=== Quick Question Add Test ===\n");
        
        QuestionManager qm = new QuestionManager();
        
        // Show current count
        int beforeCount = qm.getQuestionCount();
        System.out.println("Questions BEFORE adding: " + beforeCount);
        
        // Add a test question
        Question testQ = new Question();
        testQ.setQuestionText("Test: What color is the sky?");
        testQ.setOptions(new String[]{"Red", "Blue", "Green", "Yellow"});
        testQ.setCorrectOption("Blue");
        testQ.setTimeLimitSeconds(new Question.TimeLimit(10));
        
        boolean success = qm.addQuestion(testQ);
        
        if (success) {
            System.out.println("✅ Question added successfully!");
        } else {
            System.out.println("❌ Failed to add question!");
        }
        
        // Show new count
        int afterCount = qm.getQuestionCount();
        System.out.println("Questions AFTER adding: " + afterCount);
        System.out.println("Difference: +" + (afterCount - beforeCount));
        
        // Clean up
        qm.close();
        
        System.out.println("\n✅ Test complete! Check MongoDB Atlas to verify.");
    }
}
