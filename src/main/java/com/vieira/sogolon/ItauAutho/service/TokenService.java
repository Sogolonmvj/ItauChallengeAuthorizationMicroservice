package com.vieira.sogolon.ItauAutho.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vieira.sogolon.ItauAutho.entity.UserCritic;
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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final UserService userService;
    private final Environment env;
    private final static String TOKEN_STARTER = "Bearer ";
    private final static int TOKEN_TIME = 10 * 60 * 1000;
    private final static String PROBLEM_MESSAGE = "A problem has occurred!";
    private final static String MISSING_REFRESH_TOKEN_MESSAGE = "Refresh token is missing!";
    private final static String ERROR_MESSAGE = "error";
    private final static String ACCESS_TOKEN = "access_token";
    private final static String REFRESH_TOKEN = "refresh_token";

    public void getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        String secret = env.getProperty("key.secret");

        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_STARTER)) {
            try {
                String refresh_token = authorizationHeader.substring(TOKEN_STARTER.length());
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
                    log.info(PROBLEM_MESSAGE, exception);
                }
            } catch (Exception exception) {
                response.setHeader(ERROR_MESSAGE, exception.getMessage());
                response.setStatus(FORBIDDEN.value());
                Map<String, String> error = getErrors(exception);
                response.setContentType(APPLICATION_JSON_VALUE);
                try {
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                } catch (IOException ioException) {
                    log.info(PROBLEM_MESSAGE, ioException);
                }
            }
        } else {
            throw new RuntimeException(MISSING_REFRESH_TOKEN_MESSAGE);
        }
    }

    public String generateAccessToken(UserCritic critic, HttpServletRequest request, Algorithm algorithm) {
        return JWT.create()
                .withSubject(critic.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_TIME))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("useRole", critic.getUserRole().toString())
                .sign(algorithm);
    }

    public Map<String, String> getTokens(String access_token, String refresh_token) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN, access_token);
        tokens.put(REFRESH_TOKEN, refresh_token);

        return tokens;
    }

    public Map<String, String> getErrors(Exception exception) {
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_MESSAGE, exception.getMessage());

        return error;
    }

}