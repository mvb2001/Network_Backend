package utils;

import java.util.List;

import model.Question;

/**
 * Interface for question storage managers
 * Allows different implementations (MongoDB, JSON file, etc.)
 */
public interface IQuestionManager {
    List<Question> loadQuestions();
    List<Question> getAllQuestions();
    Question getQuestionById(String oid);
    boolean addQuestion(Question question);
    boolean updateQuestion(String oid, Question updatedQuestion);
    boolean deleteQuestion(String oid);
    List<Question> searchQuestions(String searchText);
    List<Question> getQuestionsByTimeLimit(int seconds);
    int getQuestionCount();
    void clearAllQuestions();
}
