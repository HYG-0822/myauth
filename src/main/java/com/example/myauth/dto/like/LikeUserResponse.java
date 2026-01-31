package com.example.myauth.dto.like;

import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 좋아요 누른 사용자 정보 응답 DTO
 * 좋아요 누른 사용자 목록 조회 시 반환
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
public class LikeUserResponse {

  /**
   * 사용자 ID
   */
  private Long id;

  /**
   * 사용자 이름
   */
  private String name;

  /**
   * 프로필 이미지 URL
   */
  private String profileImage;

  /**
   * User 엔티티 → DTO 변환
   */
  public static LikeUserResponse from(User user) {
    return LikeUserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .profileImage(user.getProfileImage())
        .build();
  }
}
