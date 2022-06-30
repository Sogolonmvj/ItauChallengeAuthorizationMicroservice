package com.vieira.sogolon.ItauAutho.controller;

import com.vieira.sogolon.ItauAutho.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final TokenService tokenService;

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        tokenService.getRefreshToken(request, response);
    }

}
