package com.vieira.sogolon.ItauAutho.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vieira.sogolon.ItauAutho.entity.UserCritic;
import com.vieira.sogolon.ItauAutho.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.naming.LimitExceededException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private final static int tokenTime = 10 * 60 * 1000;
    private final static int refreshTokenTime = 50 * 60 * 1000;
    private final static int attemptLimit = 3;
    private final static String ACCESS_TOKEN = "access_token";
    private final static String REFRESH_TOKEN = "refresh_token";
    private final static String LIMIT_EXCEEDED = "Login attempt limit has exceeded!";


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
       String username = request.getParameter("username");
       String password = request.getParameter("password");
       log.info("Username is: {}", username);
       log.info("Password is: {}", password);
       UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
       return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes(StandardCharsets.UTF_8));
        String access_token = generateToken(user, request, tokenTime)
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
        String refresh_token = generateToken(user, request, refreshTokenTime)
                .sign(algorithm);
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN, access_token);
        tokens.put(REFRESH_TOKEN, refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    @SneakyThrows
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failedAuthentication) {
        String username = request.getParameter("username");
        Optional<UserCritic> critic = userRepository.findByEmail(username);

        if (critic.isPresent()) {
            if (critic.get().getFailedAttempts() <= attemptLimit - 1) {
                Integer attemptsCounter = critic.get().getFailedAttempts();
                attemptsCounter++;
                critic.get().setFailedAttempts(attemptsCounter);
                userRepository.save(critic.get());
            } else {
                throw new LimitExceededException(LIMIT_EXCEEDED);
            }
        }
        super.unsuccessfulAuthentication(request, response, failedAuthentication);
    }

    public JWTCreator.Builder generateToken(User user, HttpServletRequest request, int tokenTime) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenTime))
                .withIssuer(request.getRequestURL().toString());
    }

}
