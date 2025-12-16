package ro.proiect.proiect.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty; // <-- IMPORT NECESAR

@Data
public class AnswerDto {
    private String text;

    // Adaugă această adnotare pentru a forța maparea corectă
    @JsonProperty("isCorrect")
    private boolean isCorrect;
}