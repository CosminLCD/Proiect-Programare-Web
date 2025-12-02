package ro.proiect.proiect.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority; // Import nou
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import nou
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ro.proiect.proiect.User;
import ro.proiect.proiect.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Construim lista de autorități într-un mod mai robust
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        List<GrantedAuthority> authorities = Collections.singletonList(authority);

        // Construim obiectul UserDetails pe care Spring Security îl înțelege
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities // Folosim lista creată mai sus
        );
    }
}