package com.example.ticketing.user.infrastructure.jwt;

import com.example.ticketing.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증 Provider
 * - Access Token, Refresh Token 생성
 * - 토큰에서 사용자 ID 추출
 * - 토큰 유효성 검증
 */
@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long accessTokenValidity;
  private final long refreshTokenValidity;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.secretKey = Keys.hmacShaKeyFor(
        jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
    );
    this.accessTokenValidity = jwtProperties.accessTokenValidity();
    this.refreshTokenValidity = jwtProperties.refreshTokenValidity();
  }

  public String createAccessToken(Long userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenValidity);

    return Jwts.builder()
        .subject(String.valueOf(userId))
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }


  public String createRefreshToken(Long userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenValidity);

    return Jwts.builder()
        .subject(String.valueOf(userId))
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }


  public Long getUserIdFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return Long.parseLong(claims.getSubject());
  }



  public boolean validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
