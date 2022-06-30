package com.vieira.sogolon.ItauAutho.service;

import com.vieira.sogolon.ItauAutho.entity.UserCritic;
import com.vieira.sogolon.ItauAutho.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MESSAGE = "User not found in the database!";
    private final static String USER_FOUND_MESSAGE = "User found in the database: {}";
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        Optional<UserCritic> critic = userRepository.findByEmail(email);

        if (critic.isPresent()) {
            log.info(USER_FOUND_MESSAGE, email);
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(critic.get().getUserRole().toString()));
            return new User(critic.get().getUsername(), critic.get().getPassword(), authorities);
        }

        log.error(USER_NOT_FOUND_MESSAGE);
        throw new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE);
    }

    public UserCritic getUser(String email) {
        log.info("Fetching user {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

}
