package ro.proiect.proiect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.proiect.proiect.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // Deocamdată nu avem nevoie de metode custom aici, dar le putem adăuga ulterior
}