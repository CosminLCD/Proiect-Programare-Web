package ro.proiect.proiect.dto;

import lombok.Data;

@Data
public class AnswerDetailDto {
    private Long id;
    private String text;
    private boolean isCorrect;
}