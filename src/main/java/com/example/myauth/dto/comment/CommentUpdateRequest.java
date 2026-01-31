package com.example.myauth.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 댓글 수정 요청 DTO
 *
 * 【요청 예시】
 * PUT /api/comments/{id}
 * {
 *   "content": "수정된 댓글 내용입니다."
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

  /**
   * 수정할 댓글 내용 (필수, 1~1000자)
   */
  @NotBlank(message = "댓글 내용을 입력해주세요.")
  @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하로 입력해주세요.")
  private String content;
}