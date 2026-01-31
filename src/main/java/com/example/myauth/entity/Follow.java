package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 팔로우 엔티티
 * 사용자 간 팔로우 관계 저장
 * follower → following 방향의 단방향 관계
 * 맞팔(서로 팔로우)은 양쪽에 각각 레코드 존재
 *
 * 【테이블 정보】
 * - 테이블명: follows
 * - 주요 기능: 사용자 간 팔로우 관계 관리
 *
 * 【연관 관계】
 * - User(follower): N:1 (여러 팔로우 관계가 한 팔로워에게 속함)
 * - User(following): N:1 (여러 팔로우 관계가 한 팔로잉에게 연결됨)
 *
 * 【유니크 제약조건】
 * - (follower_id, following_id) 조합이 유일해야 함
 * - 같은 사람을 중복 팔로우 불가
 *
 * 【용어 정리】
 * - follower: 팔로우 하는 사람 (주체) - "A가 B를 팔로우" 에서 A
 * - following: 팔로우 받는 사람 (대상) - "A가 B를 팔로우" 에서 B
 * - followers: 나를 팔로우하는 사람들 (팔로워 목록)
 * - followings: 내가 팔로우하는 사람들 (팔로잉 목록)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "follows",
    // 중복 팔로우 방지
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_follow",
            columnNames = {"follower_id", "following_id"}
        )
    },
    indexes = {
        // 특정 사용자의 팔로워 목록 조회용 인덱스
        @Index(name = "idx_following_id", columnList = "following_id")
    }
)
public class Follow {

  /**
   * 팔로우 관계 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 팔로우 하는 사람 (주체) - User 엔티티와 N:1 관계
   * "A가 B를 팔로우"에서 A
   * 사용자 삭제 시 관련 팔로우 관계 모두 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  /**
   * 팔로우 받는 사람 (대상) - User 엔티티와 N:1 관계
   * "A가 B를 팔로우"에서 B
   * 사용자 삭제 시 관련 팔로우 관계 모두 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "following_id", nullable = false)
  private User following;

  /**
   * 팔로우한 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  // ===== 팩토리 메서드 =====

  /**
   * 팔로우 관계 생성
   * @param follower 팔로우 하는 사람
   * @param following 팔로우 받는 사람
   */
  public static Follow create(User follower, User following) {
    return Follow.builder()
        .follower(follower)
        .following(following)
        .build();
  }

  // ===== 유틸리티 메서드 =====

  /**
   * 자기 자신을 팔로우하는지 검증
   * @throws IllegalArgumentException 자기 자신을 팔로우하려는 경우
   */
  @PrePersist
  public void validateSelfFollow() {
    if (follower != null && following != null &&
        follower.getId().equals(following.getId())) {
      throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
    }
  }
}