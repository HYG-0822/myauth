package com.example.myauth.dto.post;

import com.example.myauth.entity.Post;
import com.example.myauth.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 상세 응답 DTO
 * 게시글 상세 조회 시 반환되는 전체 정보
 *
 * 【응답 예시】
 * {
 *   "id": 1,
 *   "content": "오늘 맛있는 저녁 먹었어요!",
 *   "visibility": "PUBLIC",
 *   "likeCount": 42,
 *   "commentCount": 5,
 *   "viewCount": 100,
 *   "author": {
 *     "id": 1,
 *     "name": "홍길동",
 *     "profileImage": "http://..."
 *   },
 *   "images": [
 *     { "id": 1, "imageUrl": "http://...", ... }
 *   ],
 *   "isLiked": true,
 *   "isBookmarked": false,
 *   "createdAt": "2025-01-24T10:30:00",
 *   "updatedAt": "2025-01-24T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

  /**
   * 게시글 ID
   */
  private Long id;

  /**
   * 게시글 본문 내용
   */
  private String content;

  /**
   * 공개 범위
   */
  private Visibility visibility;

  /**
   * 좋아요 수
   */
  private Integer likeCount;

  /**
   * 댓글 수
   */
  private Integer commentCount;

  /**
   * 조회수
   */
  private Integer viewCount;

  /**
   * 작성자 정보
   */
  private PostAuthorResponse author;

  /**
   * 첨부 이미지 목록
   */
  private List<PostImageResponse> images;

  /**
   * 현재 사용자가 좋아요 했는지 여부
   * (로그인한 사용자 기준)
   */
  private Boolean isLiked;

  /**
   * 현재 사용자가 북마크 했는지 여부
   * (로그인한 사용자 기준)
   */
  private Boolean isBookmarked;

  /**
   * 작성 일시
   */
  private LocalDateTime createdAt;

  /**
   * 수정 일시
   */
  private LocalDateTime updatedAt;

  /**
   * Entity → DTO 변환 (기본)
   * 좋아요/북마크 여부는 false로 설정
   */
  public static PostResponse from(Post post) {
    return from(post, false, false);
  }

  /**
   * Entity → DTO 변환 (좋아요/북마크 여부 포함)
   */
  public static PostResponse from(Post post, boolean isLiked, boolean isBookmarked) {
    return PostResponse.builder()
        .id(post.getId())
        .content(post.getContent())
        .visibility(post.getVisibility())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .viewCount(post.getViewCount())
        .author(PostAuthorResponse.from(post.getUser()))
        .images(post.getImages().stream()
            .map(PostImageResponse::from)
            .collect(Collectors.toList()))
        .isLiked(isLiked)
        .isBookmarked(isBookmarked)
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }
}