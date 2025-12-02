package ro.proiect.proiect;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    private boolean isCorrect; // Răspunsul corect

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}