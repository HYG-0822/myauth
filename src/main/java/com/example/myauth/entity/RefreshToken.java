package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 * JWT의 Refresh Token을 DB에 저장하여 관리한다
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Refresh Token 문자열 (최대 500자)
   */
  @Column(nullable = false, length = 500)
  private String token;

  /**
   * 토큰 생성 시간
   */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * 토큰 만료 시간
   */
  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  /**
   * 토큰 취소 여부
   * true: 취소됨 (사용 불가), false: 활성 상태
   */
  @Column(name = "is_revoked", nullable = false)
  @Builder.Default
  private Boolean isRevoked = false;

  /**
   * 토큰 마지막 사용 시간
   */
  @Column(name = "last_used_at")
  private LocalDateTime lastUsedAt;

  /**
   * 토큰 소유자 (User와 다대일 관계)
   * 한 명의 사용자는 여러 개의 Refresh Token을 가질 수 있다
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // ----- 비즈니스 메서드 -----

  /**
   * 토큰이 만료되었는지 확인한다
   * @return 만료되었으면 true, 아니면 false
   */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  /**
   * 토큰이 유효한지 확인한다
   * @return 취소되지 않았고 만료되지 않았으면 true
   */
  public boolean isValid() {
    return !isRevoked && !isExpired();
  }

  /**
   * 토큰을 취소한다
   */
  public void revoke() {
    this.isRevoked = true;
  }

  /**
   * 토큰 사용 시간을 업데이트한다
   */
  public void updateLastUsedAt() {
    this.lastUsedAt = LocalDateTime.now();
  }
}