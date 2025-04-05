package org.codequistify.master.global.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PlayerRoleType;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.vo.Email;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.infrastructure.security.TokenPlayer;
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
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private final String ISS = "api.pol.or.kr";
    private final Long ACCESS_VALIDITY_TIME = 60 * 60 * 1000L;
    private final Long REFRESH_VALIDITY_TIME = 7 * 24 * 60 * 60 * 1000L;
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    @Value("${jwt.secret}")
    private String JWT_SECRET = "";
    private Key KEY;

    @PostConstruct
    protected void init() {
        KEY = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

        logger.info("\n{}", generateAccessToken(Player.builder()
                                                      .name("pol")
                                                      .email("kr.or.pol@gmail.com")
                                                      .roles(Set.of(PlayerRoleType.SUPER_ADMIN.getRole()))
                                                      .uid(PolId.of("POL-BDBEej-Gj5AntZprZ"))
                                                      .build()));

    }

    public String generateAccessToken(Player player) {
        return Jwts.builder()
                   .setSubject("access")
                   .setAudience(player.getUid().getValue())
                   .claim("uid", player.getUid().getValue())
                   .claim("name", player.getName())
                   .claim("email", player.getEmail())
                   .claim("roles", player.getRoles())
                   .setIssuedAt(new Date())
                   .setIssuer(ISS)
                   .setExpiration(new Date(System.currentTimeMillis() + ACCESS_VALIDITY_TIME))
                   .signWith(SignatureAlgorithm.HS256, KEY)
                   .compact();
    }

    public String generateTempToken(Email email) {
        Claims claims = Jwts.claims();
        claims.put("role", List.of(PlayerRoleType.TEMPORARY));
        Date now = new Date();

        String token = Jwts.builder()
                           .setClaims(claims)
                           .setAudience(email.getValue())
                           .setIssuedAt(now)
                           .setIssuer(ISS)
                           .setExpiration(new Date(now.getTime() + ACCESS_VALIDITY_TIME / 6))
                           .signWith(KEY)
                           .compact();

        logger.info("[generateTempToken] {}", token);
        return token;
    }


    public String generateRefreshToken(Player player) {
        Claims claims = Jwts.claims();
        Date now = new Date();

        String token = Jwts.builder()
                           .setClaims(claims)
                           .setAudience(player.getUid().getValue())
                           .setIssuedAt(now)
                           .setIssuer(ISS)
                           .setExpiration(new Date(now.getTime() + REFRESH_VALIDITY_TIME))
                           .signWith(KEY)
                           .compact();

        logger.info("[generateRefreshToken] {}", token);
        return token;
    }

    public Claims getClaims(String token) {
        return parseToken(token)
                .map(Jws::getBody)
                .orElse(null);
    }

    @LogExecutionTime
    public String getAudience(String token) {
        return parseToken(token)
                .map(Jws::getBody)
                .map(body -> {
                    if (body.getExpiration().before(new Date())) {
                        throw new ApplicationException(ErrorCode.EXPIRED_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
                    }
                    return body.getAudience();
                })
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED));
    }

    public boolean checkExpire(Claims claims) {
        return Optional.ofNullable(claims)
                       .map(c -> !c.getExpiration().before(new Date()))
                       .orElse(false);
    }

    private Optional<Jws<Claims>> parseToken(String token) {
        try {
            return Optional.of(Jwts.parserBuilder()
                                   .setSigningKey(KEY)
                                   .build()
                                   .parseClaimsJws(token));
        } catch (ExpiredJwtException e) {
            logger.info("[TokenProvider] 만료된 JWT 토큰");
            throw new ApplicationException(ErrorCode.EXPIRED_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
        } catch (SecurityException | MalformedJwtException e) {
            logger.info("[TokenProvider] 위조된 JWT 서명");
            throw new ApplicationException(ErrorCode.TAMPERED_TOKEN_SIGNATURE, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.warn("[TokenProvider] JWT 파싱 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isValidatedToken(String token) {
        return parseToken(token)
                .map(Jws::getBody)
                .map(claims -> !claims.getExpiration().before(new Date()))
                .orElse(false);
    }

    public String resolveToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                       .filter(auth -> auth.startsWith("Bearer "))
                       .map(auth -> auth.substring(7))
                       .orElseThrow(() -> new ApplicationException(
                               ErrorCode.EMPTY_TOKEN_PROVIDED, HttpStatus.UNAUTHORIZED)
                       );
    }

    public TokenPlayer extractTokenPlayer(String token) {
        Claims claims = getClaims(token);
        if (claims == null) {
            throw new ApplicationException(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
        }

        return TokenPlayer.builder()
                          .uid(PolId.of(claims.get("uid", String.class)))
                          .name(claims.get("name", String.class))
                          .email(claims.get("email", String.class))
                          .roles(Set.copyOf((List<String>) claims.get("roles")))
                          .build();
    }
}
