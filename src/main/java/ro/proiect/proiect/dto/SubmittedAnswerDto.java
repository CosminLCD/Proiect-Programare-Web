package ro.proiect.proiect.dto;

import lombok.Data;

@Data
public class SubmittedAnswerDto {
    private Long questionId;
    private Long answerId;
}