package com.example.myauth.exception;

/**
 * 이메일 중복 예외
 * 회원가입 시 이미 존재하는 이메일로 가입을 시도할 때 발생
 */
public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException(String message) {
    super(message);
  }

  public DuplicateEmailException(String message, Throwable cause) {
    super(message, cause);
  }
}
