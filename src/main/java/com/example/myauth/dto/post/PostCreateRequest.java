package com.example.myauth.dto.post;

import com.example.myauth.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 작성 요청 DTO
 *
 * 【사용 예시】
 * {
 *   "content": "오늘 맛있는 저녁 먹었어요! #맛집 #저녁",
 *   "visibility": "PUBLIC"
 * }
 *
 * 【참고】
 * - 이미지는 별도의 MultipartFile로 전송됨
 * - 해시태그는 content에서 자동 추출됨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

  /**
   * 게시글 본문 내용
   * - 필수 입력
   * - 최대 5000자
   */
  @NotBlank(message = "게시글 내용을 입력해주세요.")
  @Size(max = 5000, message = "게시글은 최대 5000자까지 작성 가능합니다.")
  private String content;

  /**
   * 공개 범위
   * - PUBLIC: 전체 공개 (기본값)
   * - PRIVATE: 비공개 (작성자만 볼 수 있음)
   * - FOLLOWERS: 팔로워에게만 공개
   */
  @Builder.Default
  private Visibility visibility = Visibility.PUBLIC;
}