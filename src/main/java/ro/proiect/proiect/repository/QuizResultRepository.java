package ro.proiect.proiect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.proiect.proiect.QuizResult;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
}