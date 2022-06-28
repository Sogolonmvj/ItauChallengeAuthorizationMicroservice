package com.vieira.sogolon.ItauAutho.security;

import com.vieira.sogolon.ItauAutho.repository.UserRepository;
import com.vieira.sogolon.ItauAutho.security.filter.CustomAuthenticationFilter;
import com.vieira.sogolon.ItauAutho.security.filter.CustomAuthorizationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserRepository userRepository;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(userRepository, authenticationManager(authenticationConfiguration));
        customAuthenticationFilter.setFilterProcessesUrl("/api/login");

        http
                .csrf().disable();
        http
                .sessionManagement()
                .sessionCreationPolicy(STATELESS);
        http
                .authorizeRequests()
                .antMatchers("/api/login/**", "/api/token/refresh/**")
                .permitAll();
        http
                .authorizeRequests()
                .antMatchers(GET, "/api/user/**")
                .hasAnyAuthority("READER");
        http
                .authorizeRequests()
                .antMatchers(GET, "/api/users")
                .hasAnyAuthority("MODERATOR");
        http
                .authorizeRequests()
                .antMatchers(POST, "/api/user/save/**")
                .hasAnyAuthority("MODERATOR");
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated();
        http
                .addFilter(customAuthenticationFilter);
        http
                .addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*");
            }
        };
    }

}
