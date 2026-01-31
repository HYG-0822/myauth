package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 좋아요 엔티티
 * 게시글과 댓글의 좋아요를 하나의 테이블로 통합 관리 (다형성 연관)
 * target_type + target_id 조합으로 대상 구분
 *
 * 【테이블 정보】
 * - 테이블명: likes
 * - 주요 기능: 게시글/댓글 좋아요 통합 관리
 *
 * 【연관 관계】
 * - User: N:1 (여러 좋아요가 한 사용자에게 속함)
 * - 다형성 연관: target_type에 따라 Post 또는 Comment를 참조
 *
 * 【유니크 제약조건】
 * - (user_id, target_type, target_id) 조합이 유일해야 함
 * - 동일 사용자가 같은 대상에 중복 좋아요 불가
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "likes",
    // 복합 유니크 키: 중복 좋아요 방지
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_target",
            columnNames = {"user_id", "target_type", "target_id"}
        )
    },
    indexes = {
        // 특정 게시글/댓글의 좋아요 목록 조회용 인덱스
        @Index(name = "idx_target", columnList = "target_type, target_id")
    }
)
public class Like {

  /**
   * 좋아요 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 좋아요 누른 사용자 - User 엔티티와 N:1 관계
   * 사용자 삭제 시 해당 사용자의 좋아요도 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * 대상 유형
   * - POST: 게시글에 대한 좋아요
   * - COMMENT: 댓글에 대한 좋아요
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 10)
  private TargetType targetType;

  /**
   * 대상 ID
   * target_type에 따라 posts.id 또는 comments.id를 참조
   * 외래키 미설정: 다형성 연관으로 인해 두 테이블 참조 불가
   * → 애플리케이션 레벨에서 데이터 무결성 관리
   */
  @Column(name = "target_id", nullable = false)
  private Long targetId;

  /**
   * 좋아요 누른 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  // ===== 팩토리 메서드 =====

  /**
   * 게시글 좋아요 생성
   */
  public static Like forPost(User user, Long postId) {
    return Like.builder()
        .user(user)
        .targetType(TargetType.POST)
        .targetId(postId)
        .build();
  }

  /**
   * 댓글 좋아요 생성
   */
  public static Like forComment(User user, Long commentId) {
    return Like.builder()
        .user(user)
        .targetType(TargetType.COMMENT)
        .targetId(commentId)
        .build();
  }

  /**
   * 게시글 좋아요인지 확인
   */
  public boolean isPostLike() {
    return this.targetType == TargetType.POST;
  }

  /**
   * 댓글 좋아요인지 확인
   */
  public boolean isCommentLike() {
    return this.targetType == TargetType.COMMENT;
  }
}