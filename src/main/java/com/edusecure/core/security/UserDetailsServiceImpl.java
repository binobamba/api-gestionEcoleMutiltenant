package com.edusecure.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.debug("Chargement utilisateur : {}", username);

        if ("superadmin".equals(username)) {
            return User.builder()
                    .username("superadmin")
                    // BCrypt de "Admin123!" — à changer en production
                    .password("$2a$12$iM4bEoMSFyVjuCjVMGzmNebtmNSPVqZxNBcWTLQCI7K4VGWRG8V5S")
                    .authorities(List.of( new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                    .build();
        }

        throw new UsernameNotFoundException( "Utilisateur introuvable : " + username);
    }
}

