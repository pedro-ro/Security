package com.spring.security.core.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JwtTokenProvider {

    private final static String KEY = "secret";

    private static final String SECRET_KEY = Base64.getEncoder().encodeToString(KEY.getBytes());

    private final static long VALIDITY_IN_MILLISECONDS = 3600000;

    private final static String BEARER = "Bearer ";

    private final static String AUTHORIZATION = "Authorization";

    private final static String ROLES = "roles";

    @Autowired
    private UserDetailsService userDetailsService;

    private Algorithm algorithm = null;

    @PostConstruct
    protected void init() {
        this.algorithm = Algorithm.HMAC256(SECRET_KEY.getBytes());
    }

    public TokenVO createAccessToken(String username, List<String> roles) {
        Date now = new Date();
        Date validity = this.getValidity(now);
        var accessToken = getAccessToken(username, roles, now, validity);
        var refreshToken = getRefreshToken(username, roles, now);

        return new TokenVO(username, true, now, validity, accessToken, refreshToken);
    }


    public TokenVO refreshToken(String refreshToken) {
        var verifier = JWT.require(algorithm).build();
        var decodedJWT = verifier.verify(
                this.isMinimalValidToken(refreshToken)
        );
        var username = decodedJWT.getSubject();
        var roles = decodedJWT.getClaim(ROLES).asList(String.class);
        return createAccessToken(username, roles);
    }

    private String getAccessToken(String username, List<String> roles, Date now, Date validity) {
        String issuerUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath().build().toUriString();
        return JWT.create()
                .withClaim(ROLES, roles)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withSubject(username)
                .withIssuer(issuerUrl)
                .sign(algorithm)
                .strip();
    }

    private String getRefreshToken(String username, List<String> roles, Date now) {
        var validityRefreshToken = this.getValidity(now);
        return JWT.create()
                .withClaim(ROLES, roles)
                .withIssuedAt(now)
                .withExpiresAt(validityRefreshToken)
                .withSubject(username)
                .sign(algorithm)
                .strip();
    }

    public Authentication getAuthentication(String token) {
        var decodedJWT = decodedToken(token);
        var userDetails = this.userDetailsService.loadUserByUsername(decodedJWT.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT decodedToken(String token) {
        var alg = Algorithm.HMAC256(SECRET_KEY.getBytes());
        var verifier = JWT.require(alg).build();
        return verifier.verify(token);
    }

    public String resolveToken(HttpServletRequest req) {
        return this.isMinimalValidToken(req.getHeader(AUTHORIZATION));
    }

    public boolean validateToken(String token) {
        DecodedJWT decodedJWT = decodedToken(token);
        try {
            if (decodedJWT.getExpiresAt().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Expired or invalid JWT token!");
        }
    }

    private String isMinimalValidToken(String token){
        return Optional.ofNullable(token)
                .filter(it -> it.startsWith(BEARER))
                .map(it -> it.substring(BEARER.length()))
                .orElse(null);
    }

    private Date getValidity(Date now){
        return new Date(now.getTime()+(VALIDITY_IN_MILLISECONDS*8760));
    }
}
