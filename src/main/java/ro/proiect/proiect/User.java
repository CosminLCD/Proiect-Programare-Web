package ro.proiect.proiect;

import jakarta.persistence.*;
import lombok.Getter; // Importă
import lombok.Setter; // Importă

@Entity
@Table(name = "users")
@Getter // <-- ADAUGĂ ASTA
@Setter // <-- ADAUGĂ ASTA
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, GUEST
}

