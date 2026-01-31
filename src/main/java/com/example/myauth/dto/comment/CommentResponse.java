package com.example.myauth.dto.comment;

import com.example.myauth.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 응답 DTO
 * 댓글 조회 시 반환되는 정보
 *
 * 【응답 예시】
 * {
 *   "id": 1,
 *   "content": "좋은 게시글이네요!",
 *   "author": {
 *     "id": 1,
 *     "name": "홍길동",
 *     "profileImage": "http://..."
 *   },
 *   "parentId": null,
 *   "replyCount": 2,
 *   "likeCount": 5,
 *   "isLiked": false,
 *   "isDeleted": false,
 *   "createdAt": "2025-01-24T10:30:00",
 *   "updatedAt": "2025-01-24T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

  /**
   * 댓글 ID
   */
  private Long id;

  /**
   * 댓글 내용
   * 삭제된 경우 "삭제된 댓글입니다."로 표시
   */
  private String content;

  /**
   * 작성자 정보
   */
  private CommentAuthorResponse author;

  /**
   * 부모 댓글 ID (대댓글인 경우)
   * null이면 최상위 댓글
   */
  private Long parentId;

  /**
   * 대댓글 수
   */
  private Integer replyCount;

  /**
   * 좋아요 수
   */
  private Integer likeCount;

  /**
   * 현재 사용자의 좋아요 여부
   */
  private Boolean isLiked;

  /**
   * 삭제 여부
   */
  private Boolean isDeleted;

  /**
   * 작성 일시
   */
  private LocalDateTime createdAt;

  /**
   * 수정 일시
   */
  private LocalDateTime updatedAt;

  /**
   * 대댓글 목록 (선택적으로 포함)
   */
  private List<CommentResponse> replies;

  /**
   * Entity → DTO 변환 (기본)
   */
  public static CommentResponse from(Comment comment) {
    return from(comment, false, 0);
  }

  /**
   * Entity → DTO 변환 (좋아요 여부 및 대댓글 수 포함)
   *
   * @param comment 댓글 엔티티
   * @param isLiked 좋아요 여부
   * @param replyCount 대댓글 수
   */
  public static CommentResponse from(Comment comment, boolean isLiked, int replyCount) {
    return CommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(CommentAuthorResponse.from(comment.getUser()))
        .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
        .replyCount(replyCount)
        .likeCount(comment.getLikeCount())
        .isLiked(isLiked)
        .isDeleted(comment.getIsDeleted())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }

  /**
   * Entity → DTO 변환 (대댓글 포함)
   * 계층적 구조로 댓글 목록 반환
   *
   * @param comment 댓글 엔티티
   * @param isLiked 좋아요 여부
   * @param likedReplyIds 좋아요한 대댓글 ID 목록
   */
  public static CommentResponse fromWithReplies(
      Comment comment,
      boolean isLiked,
      List<Long> likedReplyIds
  ) {
    // 대댓글 변환
    List<CommentResponse> replyResponses = comment.getReplies().stream()
        .filter(reply -> !reply.getIsDeleted())  // 삭제된 대댓글 제외
        .map(reply -> CommentResponse.from(
            reply,
            likedReplyIds != null && likedReplyIds.contains(reply.getId()),
            0  // 대댓글의 대댓글은 지원하지 않음
        ))
        .collect(Collectors.toList());

    return CommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(CommentAuthorResponse.from(comment.getUser()))
        .parentId(null)  // 최상위 댓글
        .replyCount(replyResponses.size())
        .likeCount(comment.getLikeCount())
        .isLiked(isLiked)
        .isDeleted(comment.getIsDeleted())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .replies(replyResponses)
        .build();
  }
}