package ro.proiect.proiect;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter

@Entity
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    private boolean isCorrect; // Răspunsul corect

    @ManyToOne
    @JsonBackReference
    @JsonIgnore
    @JoinColumn(name = "question_id")
    private Question question;
}