package com.shubham.csa.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.entity.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
@Autowired
private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        return new UserPrincipal(user);
    }

    // Load user by ID (for token-based auth)
    public UserDetails loadUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        return new UserPrincipal(user);
    }
}