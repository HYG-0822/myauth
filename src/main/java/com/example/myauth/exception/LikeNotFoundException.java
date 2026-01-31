package com.example.myauth.exception;

/**
 * 좋아요 기록을 찾을 수 없을 때 발생하는 예외
 * - 좋아요하지 않은 항목의 좋아요 취소 시도
 */
public class LikeNotFoundException extends RuntimeException {

  public LikeNotFoundException(String message) {
    super(message);
  }

  public LikeNotFoundException() {
    super("좋아요 기록을 찾을 수 없습니다.");
  }
}
