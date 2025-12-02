package ro.proiect.proiect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull; // Poate ai nevoie de acest import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Dacă nu există header sau nu începe cu "Bearer ", trecem mai departe la următorul filtru.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Foarte important acest 'return'!
        }

        jwt = authHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);

        // Verificăm dacă avem username ȘI dacă nu există deja o autentificare în contextul curent.
        // A doua condiție previne verificarea la fiecare filtru din lanț.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Dacă token-ul este valid
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                // Creăm token-ul de autentificare pentru Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Nu avem nevoie de credentiale aici
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // Setăm autentificarea în context. Acum Spring știe că userul este logat.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Predăm controlul următorului filtru din lanț.
        filterChain.doFilter(request, response);
    }
}