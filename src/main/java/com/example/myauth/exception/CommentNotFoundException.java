package com.example.myauth.exception;

/**
 * 댓글을 찾을 수 없을 때 발생하는 예외
 * - 존재하지 않는 댓글 ID로 조회/수정/삭제 시도
 * - 이미 삭제된 댓글에 접근 시도
 */
public class CommentNotFoundException extends RuntimeException {

  public CommentNotFoundException(String message) {
    super(message);
  }

  public CommentNotFoundException(Long commentId) {
    super("댓글을 찾을 수 없습니다. (ID: " + commentId + ")");
  }
}
