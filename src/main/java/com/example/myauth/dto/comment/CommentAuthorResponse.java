package com.example.myauth.dto.comment;

import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 댓글 작성자 정보 응답 DTO
 * 댓글 목록/상세 조회 시 작성자 정보를 포함
 *
 * 【응답 예시】
 * {
 *   "id": 1,
 *   "name": "홍길동",
 *   "profileImage": "http://..."
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAuthorResponse {

  /**
   * 작성자 ID
   */
  private Long id;

  /**
   * 작성자 이름
   */
  private String name;

  /**
   * 프로필 이미지 URL
   */
  private String profileImage;

  /**
   * User 엔티티 → DTO 변환
   */
  public static CommentAuthorResponse from(User user) {
    return CommentAuthorResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .profileImage(user.getProfileImage())
        .build();
  }
}