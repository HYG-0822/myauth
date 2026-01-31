package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 게시글 이미지/미디어 엔티티
 * 한 게시글에 여러 이미지/동영상 첨부 지원 (인스타그램 다중 이미지)
 *
 * 【테이블 정보】
 * - 테이블명: post_images
 * - 주요 기능: 게시글에 첨부된 미디어 파일 정보 저장
 *
 * 【연관 관계】
 * - Post: N:1 (여러 이미지가 한 게시글에 속함)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "post_images", indexes = {
    // 게시글별 이미지 조회용 인덱스
    @Index(name = "idx_post_id", columnList = "post_id")
})
public class PostImage {

  /**
   * 미디어 고유 식별자 (자동 증가)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 소속 게시글 - Post 엔티티와 N:1 관계
   * 게시글 삭제 시 첨부된 모든 이미지 정보도 삭제됨 (CASCADE)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  /**
   * 원본 이미지/동영상 URL (CDN 경로)
   * 실제 파일이 저장된 URL
   */
  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  /**
   * 썸네일 URL (목록 표시용)
   * 로딩 속도 향상을 위해 작은 크기의 이미지
   */
  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  /**
   * 표시 순서
   * 0부터 시작, 숫자가 작을수록 먼저 표시
   */
  @Column(name = "sort_order")
  @ColumnDefault("0")
  @Builder.Default
  private Integer sortOrder = 0;

  /**
   * 원본 너비 (px)
   * 프론트엔드에서 레이아웃 계산에 사용
   */
  @Column
  private Integer width;

  /**
   * 원본 높이 (px)
   * 프론트엔드에서 레이아웃 계산에 사용
   */
  @Column
  private Integer height;

  /**
   * 파일 크기 (bytes)
   * 용량 관리 및 통계용
   */
  @Column(name = "file_size")
  private Integer fileSize;

  /**
   * 미디어 유형
   * - IMAGE: 이미지 파일
   * - VIDEO: 동영상 파일
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "media_type", length = 10)
  @ColumnDefault("'IMAGE'")
  @Builder.Default
  private MediaType mediaType = MediaType.IMAGE;

  /**
   * 업로드 일시 (자동 설정)
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}