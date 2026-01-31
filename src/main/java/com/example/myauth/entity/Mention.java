package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 멘션 엔티티
 * 게시글/댓글에서 @username으로 언급된 사용자 기록
 * 알림 발송 및 "나를 언급한 글" 조회에 사용
 *
 * 【테이블 정보】
 * - 테이블명: mentions
 * - 주요 기능: @멘션 기록 및 알림용 데이터 관리
 *
 * 【연관 관계】
 * - User: N:1 (여러 멘션이 한 사용자에게 속함)
 * - 다형성 연관: target_type에 따라 Post 또는 Comment를 참조
 *
 * 【사용 예시】
 * 게시글 본문: "오늘 @홍길동 님과 맛집 탐방!"
 * → mentions 테이블에 (user_id=홍길동ID, target_type=POST, target_id=게시글ID) 저장
 * → 홍길동에게 알림: "누군가가 게시글에서 회원님을 언급했습니다"
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "mentions", indexes = {
    // 특정 사용자가 멘션된 목록 조회용 인덱스 (알림)
    @Index(name = "idx_user_id", columnList = "user_id"),
    // 특정 게시글/댓글의 멘션 목록 조회용 인덱스
    @Index(name = "idx_target", columnList = "target_type, target_id")
})
public class Mention {

  /**
   * 멘션 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 멘션된 사용자 (알림 받을 대상) - User 엔티티와 N:1 관계
   * 사용자 삭제 시 해당 멘션도 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * 멘션 발생 위치
   * - POST: 게시글에서 멘션됨
   * - COMMENT: 댓글에서 멘션됨
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 10)
  private TargetType targetType;

  /**
   * 멘션이 포함된 게시글/댓글 ID
   * target_type에 따라 posts.id 또는 comments.id를 참조
   * 외래키 미설정: 다형성 연관으로 인해 두 테이블 참조 불가
   * → 애플리케이션 레벨에서 데이터 무결성 관리
   */
  @Column(name = "target_id", nullable = false)
  private Long targetId;

  /**
   * 멘션된 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  // ===== 팩토리 메서드 =====

  /**
   * 게시글 멘션 생성
   */
  public static Mention forPost(User user, Long postId) {
    return Mention.builder()
        .user(user)
        .targetType(TargetType.POST)
        .targetId(postId)
        .build();
  }

  /**
   * 댓글 멘션 생성
   */
  public static Mention forComment(User user, Long commentId) {
    return Mention.builder()
        .user(user)
        .targetType(TargetType.COMMENT)
        .targetId(commentId)
        .build();
  }

  /**
   * 게시글 멘션인지 확인
   */
  public boolean isPostMention() {
    return this.targetType == TargetType.POST;
  }

  /**
   * 댓글 멘션인지 확인
   */
  public boolean isCommentMention() {
    return this.targetType == TargetType.COMMENT;
  }
}