package com.vieira.sogolon.ItauAutho.service;

import com.vieira.sogolon.ItauAutho.domain.Role;
import com.vieira.sogolon.ItauAutho.domain.UserCritic;
import com.vieira.sogolon.ItauAutho.repository.RoleRepository;
import com.vieira.sogolon.ItauAutho.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImplementation implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCritic critic = userRepository.findByUsername(username);
        if(critic == null) {
            log.error("User not found in the database.");
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            log.info("User found in the database: {}", username);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        critic.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new User(critic.getUsername(), critic.getPassword(), authorities);
    }

    @Override
    public UserCritic saveUser(UserCritic critic) {
        log.info("Saving new user to the database", critic.getFirstName());
        String encodedPassword = passwordEncoder.encode(critic.getPassword());

        critic.setPassword(encodedPassword);
        return userRepository.save(critic);
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        log.info("Adding role {} to user {}", roleName, username);
        UserCritic critic = userRepository.findByUsername(username);
        Role role = roleRepository.findByName(roleName);
        critic.getRoles().add(role);
    }

    @Override
    public UserCritic getUser(String username) {
        log.info("Fetching user {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public List<UserCritic> getUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }
}
