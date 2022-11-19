package com.spring.security.login;

import com.spring.security.user.UserRepository;
import com.spring.security.core.security.jwt.JwtTokenProvider;
import com.spring.security.core.security.jwt.TokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository repository;

    public ResponseEntity<TokenVO> signin(AccountCredentialsVO data) {
            final var username = data.getUsername();

            Optional.of(data).ifPresent(it ->
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(it.getUsername(), it.getPassword())
                    )
            );

            final var user = repository.findBy(username);

            return user.map(it -> ResponseEntity
                    .ok(tokenProvider.createAccessToken(username, it.getRoles()))
            ).orElseThrow();
    }

    public ResponseEntity<TokenVO> refreshToken(String username) {
        var user = repository.findBy(username);

        return user.map(it -> ResponseEntity
                .ok(tokenProvider.createAccessToken(username, it.getRoles()))
        ).orElseThrow();
    }
}
