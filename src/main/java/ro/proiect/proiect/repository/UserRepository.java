package ro.proiect.proiect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.proiect.proiect.User; // Importă clasa ta User
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Aici putem adăuga metode custom
    Optional<User> findByUsername(String username);

}