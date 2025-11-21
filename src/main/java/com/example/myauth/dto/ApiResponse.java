package com.example.myauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 제네릭 API 응답 DTO
 * @param <T> 응답 데이터 타입
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  /**
   * 성공 여부
   */
  private Boolean success;

  /**
   * 응답 메시지
   */
  private String message;

  /**
   * 응답 데이터 (옵션)
   */
  private T data;

  /**
   * 데이터 없는 성공 응답 생성
   */
  public static <T> ApiResponse<T> success(String message) {
    return new ApiResponse<>(true, message, null);
  }

  /**
   * 데이터 포함 성공 응답 생성
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data);
  }

  /**
   * 에러 응답 생성
   */
  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message, null);
  }
}
