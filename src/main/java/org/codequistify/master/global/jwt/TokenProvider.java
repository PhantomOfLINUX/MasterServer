package org.codequistify.master.global.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.SignInResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${jwt.secret}")
    private String JWT_SECRET = "";
    private Key KEY;
    private final String ISS = "api.pol.or.kr";
    private final Long ACCESS_VALIDITY_TIME = 60 * 60 * 1000L;
    private final Long REFRESH_VALIDITY_TIME = 24 * 60 * 60 * 1000L;
    private final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);

    @PostConstruct
    protected void init(){
        KEY = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateAccessToken(SignInResponse response){
        Claims claims = Jwts.claims();
        claims.put("name", response.name());
        claims.put("id", response.id());
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(response.email())
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + ACCESS_VALIDITY_TIME))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateAccessToken] {}", token);
        return token;
    }

    public String generateRefreshToken(SignInResponse response){
        Claims claims = Jwts.claims();
        claims.put("name", response.name());
        claims.put("id", response.id());
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(response.email())
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + REFRESH_VALIDITY_TIME))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateRefreshToken] {}", token);
        return token;
    }

    public Claims getClaims(String token) {
        try{
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);

            return claims.getBody();
        } catch (IllegalArgumentException exception){
            LOGGER.info("잘못된 JWT 토큰");
            return null;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            LOGGER.info("잘못된 JWT 서명");
            return null;
        } catch (RuntimeException exception){
            LOGGER.info("valid token error");
            return null;
        }
    }

    public boolean checkExpire(Claims claims) {
        try {
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException exception) {
            LOGGER.info("만료된 JWT 토큰");
            return false;
        }

    }

    public boolean isValidatedToken(String token){
        try{
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException exception){
            LOGGER.info("만료된 JWT 토큰");
            return false;
        } catch (IllegalArgumentException exception){
            LOGGER.info("잘못된 JWT 토큰");
            return false;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            LOGGER.info("잘못된 JWT 서명");
            return false;
        } catch (RuntimeException exception){
            LOGGER.info("valid token error");
            return false;
        }
    }

    public String resolveToken(HttpServletRequest httpServletRequest){
        String authorization = httpServletRequest.getHeader("Authorization");
        if (authorization == null){
            return null;
        }
        else if (authorization.startsWith("Bearer ")){
            return authorization.substring(7);
        }
        else {
            return authorization;
        }
    }

}
