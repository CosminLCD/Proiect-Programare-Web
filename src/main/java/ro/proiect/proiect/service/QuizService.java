package ro.proiect.proiect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ro.proiect.proiect.*;
import ro.proiect.proiect.dto.*;
import ro.proiect.proiect.repository.QuestionRepository;
import ro.proiect.proiect.repository.QuizRepository;
import ro.proiect.proiect.repository.QuizResultRepository;
import ro.proiect.proiect.repository.UserRepository;
import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final QuestionRepository questionRepository;

    // Metoda pentru a crea un quiz nou
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    // Metoda pentru a obține toate quiz-urile
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // Metoda PENTRU ADMIN pentru a obține un quiz după ID cu TOATE detaliile
    public QuizDetailDto getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        return convertToQuizDetailDto(quiz);
    }

    // Metoda pentru a actualiza un quiz existent
    public Quiz updateQuiz(Long id, Quiz quizDetails) {
        Quiz existingQuiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        existingQuiz.setTitle(quizDetails.getTitle());
        existingQuiz.setDescription(quizDetails.getDescription());
        existingQuiz.setEvaluationType(quizDetails.getEvaluationType());

        return quizRepository.save(existingQuiz);
    }

    // Metoda pentru a șterge un quiz
    @Transactional // <--- IMPORTANT: Adaugă @Transactional pentru a garanta ștergerea
    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) {
            throw new RuntimeException("Quiz not found with id: " + id);
        }

        // 1. Găsește și șterge toate rezultatele asociate acestui quiz
        List<QuizResult> results = quizResultRepository.findByQuizId(id);
        quizResultRepository.deleteAll(results);

        // 2. Acum poți șterge quiz-ul liniștit
        quizRepository.deleteById(id);
    }
    public void deleteAllResults() {
        quizResultRepository.deleteAll();
    }


    public Question addQuestionToQuiz(Long quizId, QuestionDto questionDto) {
        System.out.println("Date primite în Backend: " + questionDto);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        Question newQuestion = new Question();
        newQuestion.setText(questionDto.getText());
        newQuestion.setQuiz(quiz);

        List<Answer> answers = questionDto.getAnswers().stream().map(answerDto -> {
            Answer newAnswer = new Answer();
            newAnswer.setText(answerDto.getText());
            newAnswer.setCorrect(answerDto.isCorrect());
            newAnswer.setQuestion(newQuestion);
            return newAnswer;
        }).collect(Collectors.toList());

        newQuestion.setAnswers(answers);
        quiz.getQuestions().add(newQuestion);
        quizRepository.save(quiz);
        return newQuestion;
    }
    @Transactional // Asigură că ștergerea și adăugarea se fac atomic
    public Question updateQuestion(Long questionId, QuestionDto questionDto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        // 1. Actualizăm textul întrebării
        question.setText(questionDto.getText());

        // 2. Ștergem răspunsurile vechi (datorită orphanRemoval=true, clear() le va șterge din DB)
        question.getAnswers().clear();

        // 3. Creăm noile răspunsuri
        List<Answer> newAnswers = questionDto.getAnswers().stream().map(answerDto -> {
            Answer newAnswer = new Answer();
            newAnswer.setText(answerDto.getText());
            newAnswer.setCorrect(answerDto.isCorrect()); // Acum știm că DTO-ul e corect
            newAnswer.setQuestion(question);
            return newAnswer;
        }).collect(Collectors.toList());

        // 4. Adăugăm noile răspunsuri
        question.getAnswers().addAll(newAnswers);

        return questionRepository.save(question); // CORECT: folosim repository-ul potrivit
    }

    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(questionId);
    }

    // Metoda PENTRU GUEST pentru a obține un quiz (fără răspunsuri corecte)
    public List<QuestionForGuestDto> getQuizForGuest(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        return quiz.getQuestions().stream().map(question -> {
            QuestionForGuestDto qDto = new QuestionForGuestDto();
            qDto.setId(question.getId());
            qDto.setText(question.getText());

            List<AnswerForGuestDto> aDtos = question.getAnswers().stream().map(answer -> {
                AnswerForGuestDto aDto = new AnswerForGuestDto();
                aDto.setId(answer.getId());
                aDto.setText(answer.getText());
                return aDto;
            }).collect(Collectors.toList());
            qDto.setAnswers(aDtos);
            return qDto;
        }).collect(Collectors.toList());
    }

    // --- VERSIUNEA NOUĂ ȘI SIMPLIFICATĂ A METODEI ---
    public QuizResult calculateAndSaveResult(Long quizId, SubmissionDto submission) {
        // --- LINIA CHEIE PENTRU DEBUGGING ---
        // Aceasta ne va arăta în consola IntelliJ exact ce trimite frontend-ul.
        System.out.println("Am primit pentru Quiz ID " + quizId + " următoarea submisie: " + submission);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        // Pas A: Creăm o hartă cu răspunsurile corecte pentru căutare rapidă (mult mai eficient)
        // Harta va fi de forma: <QuestionID, CorrectAnswerID>
        Map<Long, Long> correctAnswersMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(
                        Question::getId, // Cheia hărții este ID-ul întrebării
                        question -> question.getAnswers().stream()
                                .filter(Answer::isCorrect) // Găsim răspunsul corect
                                .map(Answer::getId)        // Luăm ID-ul acestuia
                                .findFirst()
                                .orElse(-1L) // Punem o valoare invalidă dacă nu există un răspuns corect
                ));

        // Pas B: Numărăm câte răspunsuri trimise se potrivesc cu cele corecte
        int correctAnswersCount = 0;
        if (submission.getAnswers() != null) { // Verificăm dacă lista de răspunsuri nu e nullă
            for (SubmittedAnswerDto submittedAnswer : submission.getAnswers()) {
                Long questionId = submittedAnswer.getQuestionId();
                Long userAnswerId = submittedAnswer.getAnswerId();

                Long correctAnswerId = correctAnswersMap.get(questionId);

                if (userAnswerId.equals(correctAnswerId)) {
                    correctAnswersCount++;
                }
            }
        }

        // Pas C: Calculăm scorul (logica ta era deja corectă aici)
        double score = (quiz.getQuestions().isEmpty()) ? 0.0 : ((double) correctAnswersCount / quiz.getQuestions().size()) * 100.0;

        // Pas D: Restul logicii pentru clasificare și salvare (rămâne neschimbată)
        String classification = "";
        // ... (restul codului tău pentru switch, salvare, notificare WebSocket etc. rămâne identic)
        switch (quiz.getEvaluationType()) {
            case PUNCTAJ: classification = String.format("%.2f %%", score); break;
            case NOTE:
                if (score >= 90) classification = "10";
                else if (score >= 80) classification = "9";
                else if (score >= 70) classification = "8";
                else if (score >= 60) classification = "7";
                else if (score >= 50) classification = "6";
                else classification = "4";
                break;
            case CATEGORII:
                if (score > 75) classification = "Expert";
                else if (score > 50) classification = "Intermediar";
                else classification = "Începător";
                break;
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        QuizResult result = new QuizResult();
        result.setUser(currentUser);
        result.setQuiz(quiz);
        result.setScore(score);
        result.setClassification(classification);

        QuizResult savedResult = quizResultRepository.save(result);

        String notificationMessage = String.format(
                "User '%s' has completed the quiz '%s' with a score of %.2f%%.",
                savedResult.getUser().getUsername(),
                savedResult.getQuiz().getTitle(),
                savedResult.getScore()
        );
        messagingTemplate.convertAndSend("/topic/notifications", notificationMessage);

        return savedResult;
    }
    public List<QuizResult> getAllResults() {
        return quizResultRepository.findAll();
    }
    // Metoda Mapper (rămâne neschimbată)
    private QuizDetailDto convertToQuizDetailDto(Quiz quiz) {
        QuizDetailDto quizDto = new QuizDetailDto();
        quizDto.setId(quiz.getId());
        quizDto.setTitle(quiz.getTitle());
        quizDto.setDescription(quiz.getDescription());
        quizDto.setEvaluationType(quiz.getEvaluationType());

        if (quiz.getQuestions() != null) {
            List<QuestionDetailDto> questionDtos = quiz.getQuestions().stream().map(question -> {
                QuestionDetailDto questionDto = new QuestionDetailDto();
                questionDto.setId(question.getId());
                questionDto.setText(question.getText());

                if (question.getAnswers() != null) {
                    List<AnswerDetailDto> answerDtos = question.getAnswers().stream().map(answer -> {
                        AnswerDetailDto answerDto = new AnswerDetailDto();
                        answerDto.setId(answer.getId());
                        answerDto.setText(answer.getText());
                        answerDto.setCorrect(answer.isCorrect());
                        return answerDto;
                    }).collect(Collectors.toList());
                    questionDto.setAnswers(answerDtos);
                }
                return questionDto;
            }).collect(Collectors.toList());
            quizDto.setQuestions(questionDtos);
        }
        return quizDto;
    }
}