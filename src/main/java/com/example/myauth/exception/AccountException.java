package com.example.myauth.exception;

/**
 * 계정 상태 관련 예외
 * 계정이 비활성화, 정지, 삭제 등의 상태일 때 발생
 */
public class AccountException extends RuntimeException {

  public AccountException(String message) {
    super(message);
  }

  public AccountException(String message, Throwable cause) {
    super(message, cause);
  }
}
