package com.example.myauth.exception;

/**
 * 권한이 없는 리소스에 접근할 때 발생하는 예외
 * - 다른 사용자의 게시글 수정/삭제 시도
 * - 비공개 게시글에 접근 시도
 */
public class UnauthorizedAccessException extends RuntimeException {

  public UnauthorizedAccessException(String message) {
    super(message);
  }

  public UnauthorizedAccessException() {
    super("해당 작업을 수행할 권한이 없습니다.");
  }
}