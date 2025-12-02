package ro.proiect.proiect.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmissionDto {
    private List<SubmittedAnswerDto> answers;
}