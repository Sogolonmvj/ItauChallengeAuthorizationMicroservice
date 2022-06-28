package com.vieira.sogolon.ItauAutho.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vieira.sogolon.ItauAutho.domain.Role;
import com.vieira.sogolon.ItauAutho.domain.UserCritic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserService userService;
    private final static String tokenStarter = "Bearer ";
    private final Environment env;
    private final static int tokenTime = 10 * 60 * 1000;

    public void getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        String secret = env.getProperty("key.secret");

        if (authorizationHeader != null && authorizationHeader.startsWith(tokenStarter)) {
            try {
                String refresh_token = authorizationHeader.substring(tokenStarter.length());
                Algorithm algorithm = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                UserCritic critic = userService.getUser(username);
                String access_token = generateAccessToken(critic, request, algorithm);
                Map<String, String> tokens = getTokens(access_token, refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                try {
                    new ObjectMapper().writeValue(response.getOutputStream(), tokens);
                } catch (Exception exception) {
                    log.info("A problem has occurred!", exception);
                }
            } catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());
                Map<String, String> error = getErrors(exception);
                response.setContentType(APPLICATION_JSON_VALUE);
                try {
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                } catch (IOException ioException) {
                    log.info("A problem has occurred!", ioException);
                }
            }
        } else {
            throw new RuntimeException("Refresh token is missing.");
        }
    }

    public String generateAccessToken(UserCritic critic, HttpServletRequest request, Algorithm algorithm) {
        return JWT.create()
                .withSubject(critic.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenTime))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", critic.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .sign(algorithm);
    }

    public Map<String, String> getTokens(String access_token, String refresh_token) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);

        return tokens;
    }

    public Map<String, String> getErrors(Exception exception) {
        Map<String, String> error = new HashMap<>();
        error.put("error_message", exception.getMessage());

        return error;
    }

}