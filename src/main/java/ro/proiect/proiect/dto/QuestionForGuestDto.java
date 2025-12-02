package ro.proiect.proiect.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionForGuestDto {
    private Long id;
    private String text;
    private List<AnswerForGuestDto> answers;
}