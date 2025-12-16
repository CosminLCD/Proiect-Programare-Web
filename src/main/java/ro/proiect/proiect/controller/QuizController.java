package ro.proiect.proiect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ro.proiect.proiect.Question;
import ro.proiect.proiect.Quiz;
import ro.proiect.proiect.QuizResult;
import ro.proiect.proiect.dto.*;
import ro.proiect.proiect.repository.QuizResultRepository;
import ro.proiect.proiect.service.QuizService;
import ro.proiect.proiect.repository.QuestionRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
        Quiz createdQuiz = quizService.createQuiz(quiz);
        return new ResponseEntity<>(createdQuiz, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailDto> getQuizById(@PathVariable Long id) {
        QuizDetailDto quizDto = quizService.getQuizById(id);
        return ResponseEntity.ok(quizDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quizDetails) {
        Quiz updatedQuiz = quizService.updateQuiz(id, quizDetails);
        return ResponseEntity.ok(updatedQuiz);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        System.out.println("!!! A AJUNS CEREREA DE DELETE PENTRU ID: " + id); // <-- Debug
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Question> addQuestionToQuiz(@PathVariable Long quizId, @RequestBody QuestionDto questionDto) {
        Question newQuestion = quizService.addQuestionToQuiz(quizId, questionDto);
        return new ResponseEntity<>(newQuestion, HttpStatus.CREATED);
    }

    @GetMapping("/{quizId}/play")
    public ResponseEntity<List<QuestionForGuestDto>> getQuizForPlay(@PathVariable Long quizId) {
        List<QuestionForGuestDto> questions = quizService.getQuizForGuest(quizId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizResult> submitQuiz(@PathVariable Long quizId, @RequestBody SubmissionDto submission) {
        QuizResult result = quizService.calculateAndSaveResult(quizId, submission);
        return ResponseEntity.ok(result);
    }
    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @RequestBody QuestionDto questionDto) {
        Question updatedQuestion = quizService.updateQuestion(questionId, questionDto);
        return ResponseEntity.ok(updatedQuestion);
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/results")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<QuizResultDto>> getAllResults() {
        // Mapăm entitățile la DTO-uri pentru a fi mai curat
        List<QuizResult> results = quizService.getAllResults();

        List<QuizResultDto> dtos = results.stream().map(result -> {
            QuizResultDto dto = new QuizResultDto();
            dto.setId(result.getId());
            dto.setUsername(result.getUser().getUsername());
            dto.setQuizTitle(result.getQuiz().getTitle());
            dto.setScore(result.getScore());
            dto.setClassification(result.getClassification());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
    @DeleteMapping("/results") // <-- AICI ERA PROBLEMA (lipsea "/results")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteAllResults() {
        quizService.deleteAllResults();
        return ResponseEntity.ok("All results have been deleted");
    }
}