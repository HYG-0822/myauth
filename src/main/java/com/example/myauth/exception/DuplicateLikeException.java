package com.example.myauth.exception;

/**
 * 중복 좋아요 시도 시 발생하는 예외
 * - 이미 좋아요한 게시글/댓글에 다시 좋아요 시도
 */
public class DuplicateLikeException extends RuntimeException {

  public DuplicateLikeException(String message) {
    super(message);
  }

  public DuplicateLikeException() {
    super("이미 좋아요한 항목입니다.");
  }
}
