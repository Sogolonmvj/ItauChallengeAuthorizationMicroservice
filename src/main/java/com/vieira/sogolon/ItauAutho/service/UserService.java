package com.vieira.sogolon.ItauAutho.service;

import com.vieira.sogolon.ItauAutho.domain.Role;
import com.vieira.sogolon.ItauAutho.domain.UserCritic;

import java.util.List;

public interface UserService {
    UserCritic saveUser(UserCritic critic);
    Role saveRole(Role role);
    void addRoleToUser(String username, String roleName);
    UserCritic getUser(String username);
    List<UserCritic> getUsers();
}
