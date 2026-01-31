package com.example.myauth.dto.like;

import com.example.myauth.entity.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 좋아요 응답 DTO
 * 좋아요/좋아요 취소 후 반환되는 정보
 *
 * 【응답 예시】
 * {
 *   "targetType": "POST",
 *   "targetId": 1,
 *   "liked": true,
 *   "likeCount": 43
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {

  /**
   * 대상 유형 (POST/COMMENT)
   */
  private TargetType targetType;

  /**
   * 대상 ID
   */
  private Long targetId;

  /**
   * 좋아요 상태
   * - true: 좋아요 됨
   * - false: 좋아요 취소됨
   */
  private Boolean liked;

  /**
   * 현재 좋아요 수
   */
  private Integer likeCount;

  /**
   * 게시글 좋아요 응답 생성
   */
  public static LikeResponse forPost(Long postId, boolean liked, int likeCount) {
    return LikeResponse.builder()
        .targetType(TargetType.POST)
        .targetId(postId)
        .liked(liked)
        .likeCount(likeCount)
        .build();
  }

  /**
   * 댓글 좋아요 응답 생성
   */
  public static LikeResponse forComment(Long commentId, boolean liked, int likeCount) {
    return LikeResponse.builder()
        .targetType(TargetType.COMMENT)
        .targetId(commentId)
        .liked(liked)
        .likeCount(likeCount)
        .build();
  }
}
