package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔티티
 * 게시글에 달리는 댓글, 대댓글(답글) 지원
 * parent_id를 통한 자기참조로 계층 구조 구현
 *
 * 【테이블 정보】
 * - 테이블명: comments
 * - 주요 기능: 댓글/대댓글 계층 구조, 좋아요 수 캐싱
 *
 * 【연관 관계】
 * - Post: N:1 (여러 댓글이 한 게시글에 속함)
 * - User: N:1 (여러 댓글이 한 사용자에게 속함)
 * - Comment: 자기참조 (부모 댓글 - 대댓글 관계)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "comments", indexes = {
    // 게시글별 댓글 시간순 조회용 인덱스
    @Index(name = "idx_post_id", columnList = "post_id, created_at"),
    // 특정 사용자가 작성한 댓글 조회용 인덱스
    @Index(name = "idx_user_id", columnList = "user_id"),
    // 특정 댓글의 대댓글 조회용 인덱스
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class Comment {

  /**
   * 댓글 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 소속 게시글 - Post 엔티티와 N:1 관계
   * 게시글 삭제 시 모든 댓글 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  /**
   * 댓글 작성자 - User 엔티티와 N:1 관계
   * 사용자 삭제 시 해당 사용자의 댓글 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * 부모 댓글 - 자기참조 관계
   * NULL이면 최상위 댓글, 값이 있으면 대댓글
   * 부모 댓글 삭제 시 대댓글도 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Comment parent;

  /**
   * 대댓글 목록 (자식 댓글들)
   * 양방향 관계 - parent 필드의 반대편
   */
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Comment> replies = new ArrayList<>();

  /**
   * 댓글 내용 (최대 1000자)
   */
  @Column(nullable = false, length = 1000)
  private String content;

  /**
   * 댓글 좋아요 수 (캐싱용)
   * 매번 COUNT 쿼리를 실행하지 않고 이 필드를 조회
   */
  @Column(name = "like_count")
  @ColumnDefault("0")
  @Builder.Default
  private Integer likeCount = 0;

  /**
   * 삭제 여부 (Soft Delete)
   * - false(0): 활성 상태
   * - true(1): 삭제됨
   * 대댓글 보존을 위해 물리적 삭제 대신 논리적 삭제 사용
   * 예: "삭제된 댓글입니다" 표시 후 대댓글은 유지
   */
  @Column(name = "is_deleted")
  @ColumnDefault("false")
  @Builder.Default
  private Boolean isDeleted = false;

  /**
   * 작성 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  /**
   * 수정 일시 (자동 갱신)
   */
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ===== 편의 메서드 =====

  /**
   * 대댓글 추가
   */
  public void addReply(Comment reply) {
    replies.add(reply);
    reply.setParent(this);
  }

  /**
   * 좋아요 수 증가
   */
  public void incrementLikeCount() {
    this.likeCount++;
  }

  /**
   * 좋아요 수 감소
   */
  public void decrementLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }

  /**
   * 논리적 삭제 처리
   */
  public void softDelete() {
    this.isDeleted = true;
    this.content = "삭제된 댓글입니다.";
  }

  /**
   * 최상위 댓글인지 확인
   */
  public boolean isRootComment() {
    return this.parent == null;
  }
}