package org.codequistify.master.global.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.domain.PlayerRoleType;
import org.codequistify.master.core.domain.player.dto.PlayerProfile;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${jwt.secret}")
    private String JWT_SECRET = "";
    private Key KEY;
    private final String ISS = "api.pol.or.kr";
    private final Long ACCESS_VALIDITY_TIME = 60 * 60 * 1000L;
    private final Long REFRESH_VALIDITY_TIME = 7 * 24 * 60 * 60 * 1000L;
    private final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);

    @PostConstruct
    protected void init() {
        KEY = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

        LOGGER.info("\n{}",generateAccessToken(Player.builder()
                .name("pol")
                .email("kr.or.pol@gmail.com")
                .roles(List.of(PlayerRoleType.SUPER_ADMIN.getRole()))
                .uid("POL-BDBEej-Gj5AntZprZ")
                .build()));

    }

    public String generateAccessToken(PlayerProfile response) {
        Claims claims = Jwts.claims();
        claims.put("role", response.roles());
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(response.uid())
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + ACCESS_VALIDITY_TIME))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateAccessToken] {}", token);
        return token;
    }

    public String generateAccessToken(Player player) {
        Claims claims = Jwts.claims();
        claims.put("role", player.getRoles());
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(player.getUid())
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + ACCESS_VALIDITY_TIME))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateAccessToken] {}", token);
        return token;
    }

    public String generateTempToken(String email) {
        Claims claims = Jwts.claims();
        claims.put("role", List.of(PlayerRoleType.TEMPORARY));
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(email)
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + ACCESS_VALIDITY_TIME / 6))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateTempToken] {}", token);
        return token;
    }

    public String generateRefreshToken(PlayerProfile response) {
        Claims claims = Jwts.claims();
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(response.uid())
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + REFRESH_VALIDITY_TIME))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateRefreshToken] {}", token);
        return token;
    }

    public String generateRefreshToken(Player player) {
        Claims claims = Jwts.claims();
        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setAudience(player.getUid())
                .setIssuedAt(now)
                .setIssuer(ISS)
                .setExpiration(new Date(now.getTime() + REFRESH_VALIDITY_TIME))
                .signWith(KEY)
                .compact();

        LOGGER.info("[generateRefreshToken] {}", token);
        return token;
    }

    public Claims getClaims(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);

            return claims.getBody();
        } catch (IllegalArgumentException exception) {
            LOGGER.info("잘못된 JWT 토큰");
            return null;
        } catch (SecurityException | MalformedJwtException exception) {
            LOGGER.info("잘못된 JWT 서명");
            return null;
        } catch (RuntimeException exception) {
            LOGGER.info("valid token error");
            return null;
        }
    }

    @LogExecutionTime
    public String getAudience(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);

            if (claims.getBody().getExpiration().before(new Date())) {
                throw new BusinessException(ErrorCode.EXPIRED_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
            }
            else {
                return claims.getBody().getAudience();
            }
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.EXPIRED_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
        } catch (SecurityException | MalformedJwtException e) {
            throw new BusinessException(ErrorCode.TAMPERED_TOKEN_SIGNATURE, HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
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

    public boolean isValidatedToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException exception) {
            LOGGER.info("만료된 JWT 토큰");
            return false;
        } catch (IllegalArgumentException exception) {
            LOGGER.info("잘못된 JWT 토큰");
            return false;
        } catch (SecurityException | MalformedJwtException e) {
            LOGGER.info("잘못된 JWT 서명");
            return false;
        } catch (RuntimeException exception) {
            LOGGER.info("valid token error");
            return false;
        }
    }

    public String resolveToken(HttpServletRequest httpServletRequest) {
        String authorization = httpServletRequest.getHeader("Authorization");
        if (authorization == null) {
            throw new BusinessException(ErrorCode.EMPTY_TOKEN_PROVIDED, HttpStatus.UNAUTHORIZED);
        } else if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        } else {
            return authorization;
        }
    }

}
