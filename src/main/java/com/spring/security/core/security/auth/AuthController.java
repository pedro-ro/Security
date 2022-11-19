package com.spring.security.core.security.auth;

import com.spring.security.login.AccountCredentialsVO;
import com.spring.security.login.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/signin")
    public ResponseEntity signin(@RequestBody AccountCredentialsVO data){
        if (ObjectUtils.isEmpty(data.getUsername()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client");

        var token = authService.signin(data);

        if (ObjectUtils.isEmpty(token))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid client");

        return token;
    }
}
