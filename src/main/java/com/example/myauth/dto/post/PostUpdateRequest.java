package com.example.myauth.dto.post;

import com.example.myauth.entity.Visibility;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 수정 요청 DTO
 *
 * 【사용 예시】
 * {
 *   "content": "수정된 내용입니다.",
 *   "visibility": "FOLLOWERS"
 * }
 *
 * 【참고】
 * - 모든 필드는 선택 사항 (null이면 기존 값 유지)
 * - 이미지 수정은 별도 API로 처리
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

  /**
   * 게시글 본문 내용
   * - null이면 기존 값 유지
   * - 최대 5000자
   */
  @Size(max = 5000, message = "게시글은 최대 5000자까지 작성 가능합니다.")
  private String content;

  /**
   * 공개 범위
   * - null이면 기존 값 유지
   */
  private Visibility visibility;
}