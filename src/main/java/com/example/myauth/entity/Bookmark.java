package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 북마크(저장) 엔티티
 * 사용자가 나중에 보기 위해 저장한 게시글 목록
 * 인스타그램의 "저장" 기능과 동일
 *
 * 【테이블 정보】
 * - 테이블명: bookmarks
 * - 주요 기능: 사용자별 게시글 저장(북마크) 관리
 *
 * 【연관 관계】
 * - User: N:1 (여러 북마크가 한 사용자에게 속함)
 * - Post: N:1 (여러 북마크가 한 게시글에 연결됨)
 *
 * 【유니크 제약조건】
 * - (user_id, post_id) 조합이 유일해야 함
 * - 같은 사용자가 같은 게시글을 중복 북마크 불가
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "bookmarks",
    // 중복 북마크 방지
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_post",
            columnNames = {"user_id", "post_id"}
        )
    },
    indexes = {
        // 게시글별 북마크 수 조회용 인덱스
        @Index(name = "idx_post_id", columnList = "post_id")
    }
)
public class Bookmark {

  /**
   * 북마크 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 북마크한 사용자 - User 엔티티와 N:1 관계
   * 사용자 삭제 시 해당 사용자의 북마크도 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * 북마크된 게시글 - Post 엔티티와 N:1 관계
   * 게시글 삭제 시 해당 북마크도 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  /**
   * 북마크한 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  // ===== 팩토리 메서드 =====

  /**
   * 북마크 생성
   */
  public static Bookmark create(User user, Post post) {
    return Bookmark.builder()
        .user(user)
        .post(post)
        .build();
  }
}