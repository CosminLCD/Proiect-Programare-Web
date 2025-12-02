package ro.proiect.proiect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // <-- ADAUGĂ ACEASTĂ LINIE
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
