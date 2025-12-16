package ro.proiect.proiect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.proiect.proiect.QuizResult;
import java.util.List; // Import necesar

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByQuizId(Long quizId);

}