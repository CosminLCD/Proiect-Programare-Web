package ro.proiect.proiect;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
public class QuizResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Quiz quiz;

    private double score; // Scorul obținut
    private String classification; // Nota, categoria etc.
}