package ro.proiect.proiect.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDetailDto {
    private Long id;
    private String text;
    private List<AnswerDetailDto> answers;
}