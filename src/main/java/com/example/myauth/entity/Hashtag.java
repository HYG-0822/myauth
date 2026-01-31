package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 해시태그 엔티티
 * 전체 시스템에서 사용되는 고유 해시태그 목록
 * 해시태그는 전역적으로 공유됨 (중복 생성 안됨)
 *
 * 【테이블 정보】
 * - 테이블명: hashtags
 * - 주요 기능: 해시태그 마스터 관리, 사용 빈도 통계
 *
 * 【연관 관계】
 * - PostHashtag: 1:N (하나의 해시태그가 여러 게시글에 연결됨)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "hashtags",
    // 해시태그명 중복 방지
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_name", columnNames = {"name"})
    },
    indexes = {
        // 인기 해시태그 조회용 인덱스 (사용 빈도 높은 순)
        @Index(name = "idx_post_count", columnList = "post_count DESC")
    }
)
public class Hashtag {

  /**
   * 해시태그 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 해시태그 이름 (# 제외)
   * 예: "여행", "맛집", "일상"
   */
  @Column(nullable = false, unique = true, length = 100)
  private String name;

  /**
   * 이 해시태그가 사용된 게시글 수 (캐싱용)
   * 인기 해시태그 정렬 시 사용
   */
  @Column(name = "post_count")
  @ColumnDefault("0")
  @Builder.Default
  private Integer postCount = 0;

  /**
   * 최초 생성 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  /**
   * 이 해시태그를 사용하는 게시글 연결 목록
   */
  @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PostHashtag> postHashtags = new ArrayList<>();

  // ===== 편의 메서드 =====

  /**
   * 게시글 수 증가 (해시태그가 게시글에 추가될 때)
   */
  public void incrementPostCount() {
    this.postCount++;
  }

  /**
   * 게시글 수 감소 (해시태그가 게시글에서 제거될 때)
   */
  public void decrementPostCount() {
    if (this.postCount > 0) {
      this.postCount--;
    }
  }

  /**
   * 해시태그 문자열 반환 (# 포함)
   */
  public String getHashtagString() {
    return "#" + this.name;
  }
}