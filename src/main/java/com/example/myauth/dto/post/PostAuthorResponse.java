package com.example.myauth.dto.post;

import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 작성자 정보 응답 DTO
 * 게시글 목록/상세에서 작성자 정보 표시용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAuthorResponse {

  /**
   * 사용자 ID
   */
  private Long id;

  /**
   * 닉네임
   */
  private String name;

  /**
   * 프로필 이미지 URL
   */
  private String profileImage;

  /**
   * Entity → DTO 변환
   */
  public static PostAuthorResponse from(User user) {
    return PostAuthorResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .profileImage(user.getProfileImage())
        .build();
  }
}