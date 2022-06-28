package com.vieira.sogolon.ItauAutho.domain;

import com.vieira.sogolon.ItauAutho.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static javax.persistence.FetchType.EAGER;


@Data
@NoArgsConstructor
@Entity
@Table(name="critic")
public class UserCritic implements UserDetails {

    @SequenceGenerator(name = "critic_sequence",
            sequenceName = "critic_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "critic_sequence"
    )

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    @ManyToMany(fetch = EAGER)
    private Collection<Role> roles = new ArrayList<>();
    private UserRole userRole;
    private Boolean locked = false;
    private Boolean enabled = false;
    private Boolean authorized = false;
    private Integer failedAttempts = 0;
    private String title;
    private Integer points = 0;

    public UserCritic(String firstName,
                      String lastName,
                      String username,
                      String password,
                      UserRole userRole) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(userRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
