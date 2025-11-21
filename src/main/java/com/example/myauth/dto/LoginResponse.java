package com.example.myauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 성공 시 반환하는 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
  /**
   * 로그인 성공 여부
   */
  private Boolean success;

  /**
   * 응답 메시지
   */
  private String message;

  /**
   * Access Token (짧은 만료 시간, API 요청 시 사용)
   */
  private String accessToken;

  /**
   * Refresh Token (긴 만료 시간, Access Token 갱신 시 사용)
   */
  private String refreshToken;

  /**
   * 사용자 정보 (선택사항)
   */
  private UserInfo user;

  /**
   * 사용자 기본 정보
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserInfo {
    private Long id;
    private String email;
    private String name;
    private String role;
  }

  /**
   * 성공 응답 생성 헬퍼 메서드
   */
  public static LoginResponse success(String accessToken, String refreshToken, UserInfo user) {
    return LoginResponse.builder()
        .success(true)
        .message("로그인 성공")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .user(user)
        .build();
  }
}