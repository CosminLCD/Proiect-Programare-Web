package ro.proiect.proiect.dto;

import lombok.Data;
import ro.proiect.proiect.EvaluationType;
import java.util.List;

@Data
public class QuizDetailDto {
    private Long id;
    private String title;
    private String description;
    private EvaluationType evaluationType;
    private List<QuestionDetailDto> questions;
}