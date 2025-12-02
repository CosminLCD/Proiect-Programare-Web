package ro.proiect.proiect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.proiect.proiect.Role;
import ro.proiect.proiect.User;
import ro.proiect.proiect.dto.RegisterRequest;
import ro.proiect.proiect.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        // Verifică dacă userul există deja (opțional, dar recomandat)
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // Criptăm parola înainte de a o salva!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Toți userii noi sunt GUEST. Adminii pot fi adăugați manual în BD sau printr-un endpoint special.
        user.setRole(Role.GUEST);

        userRepository.save(user);
    }
}