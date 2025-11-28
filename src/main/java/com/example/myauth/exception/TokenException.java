package com.example.myauth.exception;

/**
 * 토큰 관련 예외
 * Refresh Token이 유효하지 않거나 만료되었을 때 발생
 */
public class TokenException extends RuntimeException {

  public TokenException(String message) {
    super(message);
  }

  public TokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
