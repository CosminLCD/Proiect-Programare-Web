package ro.proiect.proiect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.proiect.proiect.dto.AuthResponse;
import ro.proiect.proiect.dto.LoginRequest;
import ro.proiect.proiect.dto.RegisterRequest;
import ro.proiect.proiect.security.JwtUtil;
import ro.proiect.proiect.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Injectăm toate componentele necesare folosind un constructor generat de Lombok
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        // 1. Aici Spring Security verifică dacă username-ul și parola sunt corecte.
        // Dacă nu sunt, va arunca o excepție (pe care o vom gestiona mai târziu).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. Dacă autentificarea a reușit, încărcăm detaliile userului
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

        // 3. Generăm token-ul JWT
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Returnăm token-ul în răspuns
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalStateException e) {
            // Prindem excepția aruncată de serviciu dacă user-ul există deja
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}