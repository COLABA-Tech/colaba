package com.example.colaba.auth.infrastructure.security;

import com.example.colaba.shared.common.application.dto.user.UserAuthDto;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserServicePort userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserAuthDto user;

        try {
            if (login.contains("@")) {
                user = userServiceClient.findForAuthByEmail(login);
            } else {
                user = userServiceClient.findForAuthByUsername(login);
            }
        } catch (FeignException.NotFound ex) {
            throw new UsernameNotFoundException("User not found: LOGIN " + login);
        } catch (Exception ex) {
            throw new UsernameNotFoundException("Error loading user: LOGIN " + login, ex);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found: LOGIN " + login);
        }

        return new User(
                user.username(),
                user.password(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
        );
    }
}
