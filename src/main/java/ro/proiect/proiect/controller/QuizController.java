package ro.proiect.proiect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.proiect.proiect.Quiz;
import ro.proiect.proiect.service.QuizService;
import ro.proiect.proiect.Question;
import ro.proiect.proiect.dto.QuestionDto;
import ro.proiect.proiect.dto.QuestionForGuestDto;
import ro.proiect.proiect.dto.SubmissionDto;
import ro.proiect.proiect.QuizResult;



import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Endpoint pentru a crea un quiz. Doar ADMIN poate face asta.
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        Quiz createdQuiz = quizService.createQuiz(quiz);
        return new ResponseEntity<>(createdQuiz, HttpStatus.CREATED);
    }

    // Endpoint pentru a vedea toate quiz-urile. Oricine este autentificat poate face asta.
    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    // Endpoint pentru a vedea un singur quiz.
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    // Endpoint pentru a actualiza un quiz. Doar ADMIN poate face asta.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quizDetails) {
        Quiz updatedQuiz = quizService.updateQuiz(id, quizDetails);
        return ResponseEntity.ok(updatedQuiz);
    }

    // Endpoint pentru a șterge un quiz. Doar ADMIN poate face asta.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build(); // Răspuns standard pentru ștergere cu succes
    }
    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Question> addQuestionToQuiz(@PathVariable Long quizId, @RequestBody QuestionDto questionDto) {
        Question newQuestion = quizService.addQuestionToQuiz(quizId, questionDto);
        return new ResponseEntity<>(newQuestion, HttpStatus.CREATED);
    }
    // Endpoint pentru ca un user logat să ia un quiz (fără răspunsuri corecte)
    @GetMapping("/{quizId}/play")
    public ResponseEntity<List<QuestionForGuestDto>> getQuizForPlay(@PathVariable Long quizId) {
        List<QuestionForGuestDto> questions = quizService.getQuizForGuest(quizId);
        return ResponseEntity.ok(questions);
    }

    // Endpoint pentru a trimite răspunsurile
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizResult> submitQuiz(@PathVariable Long quizId, @RequestBody SubmissionDto submission) {
        QuizResult result = quizService.calculateAndSaveResult(quizId, submission);
        return ResponseEntity.ok(result);
    }
}
