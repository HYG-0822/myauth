package com.example.myauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 */
@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  /**
   * 생성자 - application.yaml의 JWT 설정을 주입받는다
   */
  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
  ) {
    // 비밀 키를 SecretKey 객체로 변환
    System.out.println("===========================================================================");
    System.out.println(secret);
    System.out.println(accessTokenExpiration);
    System.out.println(refreshTokenExpiration);
    System.out.println("===========================================================================");

    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  /**
   * Access Token 생성
   * @param userEmail 사용자 이메일
   * @param userId 사용자 ID
   * @return Access Token 문자열
   */
  public String generateAccessToken(String userEmail, Long userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    System.out.println("===========================================================================");
    log.debug("generateAccessToken() : userEmail={}, userId={}", userEmail, userId);
    log.debug("generateAccessToken::now : {}", now);
    log.debug("generateAccessToken::expiryDate : {}", expiryDate);
    System.out.println("===========================================================================");

    return Jwts.builder()
        .subject(userEmail)                    // 토큰 주체 (사용자 이메일)
        .claim("userId", userId)            // 사용자 ID 추가
        .claim("type", "access")         // 토큰 타입
        .issuedAt(now)                         // 발행 시간
        .expiration(expiryDate)                // 만료 시간
        .signWith(secretKey)                   // 서명
        .compact();
  }

  /**
   * Refresh Token 생성
   * @param userEmail 사용자 이메일
   * @return Refresh Token 문자열
   */
  public String generateRefreshToken(String userEmail) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

    System.out.println("===========================================================================");
    log.debug("generateRefreshToken() : userEmail={}", userEmail);
    log.debug("generateRefreshToken::now : {}", now);
    log.debug("generateRefreshToken::expiryDate : {}", expiryDate);
    System.out.println("===========================================================================");

    return Jwts.builder()
        .subject(userEmail)                    // 토큰 주체 (사용자 이메일)
        .claim("type", "refresh")              // 토큰 타입
        .issuedAt(now)                         // 발행 시간
        .expiration(expiryDate)                // 만료 시간
        .signWith(secretKey)                   // 서명
        .compact();
  }

  /**
   * 토큰에서 사용자 이메일 추출
   * @param token JWT 토큰
   * @return 사용자 이메일
   */
  public String getEmailFromToken(String token) {
    Claims claims = parseToken(token);
    return claims.getSubject();
  }

  /**
   * 토큰에서 사용자 ID 추출
   * @param token JWT 토큰
   * @return 사용자 ID
   */
  public Long getUserIdFromToken(String token) {
    Claims claims = parseToken(token);
    return claims.get("userId", Long.class);
  }

  /**
   * 토큰 유효성 검증
   *
   * @param token JWT 토큰
   * @return 유효하면 true
   * @throws ExpiredJwtException 토큰이 만료된 경우
   * @throws JwtException 토큰이 유효하지 않은 경우 (서명 오류, 형식 오류 등)
   * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
   */
  public boolean validateToken(String token) {
    // parseToken() 호출 시 발생하는 예외를 그대로 전파
    // - ExpiredJwtException: 토큰 만료
    // - SignatureException: 서명 불일치
    // - MalformedJwtException: 잘못된 JWT 형식
    // - UnsupportedJwtException: 지원하지 않는 JWT
    // - IllegalArgumentException: 빈 토큰
    parseToken(token);
    return true;
  }

  /**
   * 토큰 파싱 (내부 메서드)
   * @param token JWT 토큰
   * @return Claims 객체
   */
  private Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)                 // 서명 검증
        .build()
        .parseSignedClaims(token)              // 토큰 파싱
        .getPayload();                         // Claims 추출
  }

  /**
   * Refresh Token의 만료 시간 계산
   * @return 만료 시간 (LocalDateTime)
   */
  public Date getRefreshTokenExpiryDate() {
    return new Date(System.currentTimeMillis() + refreshTokenExpiration);
  }
}