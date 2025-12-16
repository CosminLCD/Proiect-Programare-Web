package ro.proiect.proiect.dto;
import lombok.Data;
import java.time.LocalDateTime; // Dacă ai câmp de dată, dacă nu, ignoră

@Data
public class QuizResultDto {
    private Long id;
    private String username;
    private String quizTitle;
    private double score;
    private String classification;
}