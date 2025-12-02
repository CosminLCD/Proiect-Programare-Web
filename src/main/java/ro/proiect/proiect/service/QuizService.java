package ro.proiect.proiect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.proiect.proiect.Quiz;
import ro.proiect.proiect.repository.QuizRepository;
import ro.proiect.proiect.Question;
import ro.proiect.proiect.Answer;
import ro.proiect.proiect.dto.QuestionDto;
import ro.proiect.proiect.dto.AnswerDto;
import java.util.stream.Collectors;
import ro.proiect.proiect.dto.QuestionForGuestDto;
import ro.proiect.proiect.dto.AnswerForGuestDto;
import ro.proiect.proiect.QuizResult;
import ro.proiect.proiect.User;
import ro.proiect.proiect.repository.QuizResultRepository;
import ro.proiect.proiect.dto.SubmissionDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ro.proiect.proiect.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    // Metoda pentru a crea un quiz nou
    public Quiz createQuiz(Quiz quiz) {
        // Aici poți adăuga validări, de ex: să nu existe deja un quiz cu același titlu
        return quizRepository.save(quiz);
    }
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;

    // Metoda pentru a obține toate quiz-urile
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // Metoda pentru a obține un quiz după ID
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
    }

    // Metoda pentru a actualiza un quiz existent
    public Quiz updateQuiz(Long id, Quiz quizDetails) {
        Quiz existingQuiz = getQuizById(id); // Refolosim metoda de mai sus pentru a găsi quiz-ul
        existingQuiz.setTitle(quizDetails.getTitle());
        existingQuiz.setDescription(quizDetails.getDescription());
        existingQuiz.setEvaluationType(quizDetails.getEvaluationType());

        return quizRepository.save(existingQuiz);
    }

    // Metoda pentru a șterge un quiz
    public void deleteQuiz(Long id) {
        Quiz existingQuiz = getQuizById(id); // Verificăm dacă există înainte de a șterge
        quizRepository.delete(existingQuiz);
    }
    public Question addQuestionToQuiz(Long quizId, QuestionDto questionDto) {
        // 1. Găsește quiz-ul căruia vrem să-i adăugăm întrebarea
        Quiz quiz = getQuizById(quizId); // Refolosim metoda existentă

        // 2. Creează o nouă entitate Question
        Question newQuestion = new Question();
        newQuestion.setText(questionDto.getText());
        newQuestion.setQuiz(quiz); // Foarte important: setăm legătura!

        // 3. Creează entitățile Answer și leagă-le de întrebare
        List<Answer> answers = questionDto.getAnswers().stream().map(answerDto -> {
            Answer newAnswer = new Answer();
            newAnswer.setText(answerDto.getText());
            newAnswer.setCorrect(answerDto.isCorrect());
            newAnswer.setQuestion(newQuestion); // Setăm legătura inversă
            return newAnswer;
        }).collect(Collectors.toList());

        newQuestion.setAnswers(answers);

        // 4. Adaugă noua întrebare la lista de întrebări a quiz-ului
        quiz.getQuestions().add(newQuestion);

        // 5. Salvează quiz-ul. Datorită `cascade = CascadeType.ALL`,
        //    JPA va salva automat și noua întrebare și noile răspunsuri.
        quizRepository.save(quiz);

        return newQuestion;
    }
    public List<QuestionForGuestDto> getQuizForGuest(Long quizId) {
        Quiz quiz = getQuizById(quizId); // Găsim quiz-ul

        // Mapăm fiecare Question la un QuestionForGuestDto
        return quiz.getQuestions().stream().map(question -> {
            QuestionForGuestDto qDto = new QuestionForGuestDto();
            qDto.setId(question.getId());
            qDto.setText(question.getText());

            // Mapăm fiecare Answer la un AnswerForGuestDto (fără isCorrect)
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
    public QuizResult calculateAndSaveResult(Long quizId, SubmissionDto submission) {
        // 1. Preluare quiz complet din BD (cu răspunsurile corecte)
        Quiz quiz = getQuizById(quizId);

        // 2. Calculare scor
        long correctAnswersCount = submission.getAnswers().stream()
                .filter(submittedAnswer -> {
                    // Găsim întrebarea corespunzătoare în lista de întrebări a quiz-ului
                    return quiz.getQuestions().stream()
                            .filter(question -> question.getId().equals(submittedAnswer.getQuestionId()))
                            .findFirst()
                            .map(question -> question.getAnswers().stream()
                                    // Găsim răspunsul corect pentru acea întrebare
                                    .filter(answer -> answer.isCorrect() && answer.getId().equals(submittedAnswer.getAnswerId()))
                                    .findFirst()
                                    .isPresent() // Dacă găsim un răspuns care e și corect și cel trimis de user
                            ).orElse(false); // Dacă întrebarea nu e găsită, răspunsul e considerat greșit
                }).count();

        double score = (double) correctAnswersCount / quiz.getQuestions().size() * 100;

        // 3. Aplicare tip de evaluare (logica de la început)
        String classification = "";
        switch (quiz.getEvaluationType()) {
            case PUNCTAJ:
                classification = String.format("%.2f %%", score);
                break;
            case NOTE:
                if (score >= 90) classification = "10";
                else if (score >= 80) classification = "9";
                else if (score >= 70) classification = "8";
                else if (score >= 60) classification = "7";
                else if (score >= 50) classification = "6";
                else classification = "4";
                break;
            case CATEGORI:
                if (score > 75) classification = "Expert";
                else if (score > 50) classification = "Intermediar";
                else classification = "Începător";
                break;
        }

        // 4. Identificare utilizator logat
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 5. Salvare rezultat
        QuizResult result = new QuizResult();
        result.setUser(currentUser);
        result.setQuiz(quiz);
        result.setScore(score);
        result.setClassification(classification);

        return quizResultRepository.save(result);
    }

}