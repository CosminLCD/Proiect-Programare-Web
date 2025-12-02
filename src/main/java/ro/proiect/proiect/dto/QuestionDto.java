package ro.proiect.proiect.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDto {
    private String text;
    private List<AnswerDto> answers;
}