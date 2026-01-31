package com.example.myauth.entity;

/**
 * 대상 유형 Enum
 * 좋아요, 멘션 등에서 대상이 게시글인지 댓글인지 구분
 * 다형성 연관(Polymorphic Association)을 위해 사용
 */
public enum TargetType {
  /**
   * 게시글 (posts 테이블)
   */
  POST,

  /**
   * 댓글 (comments 테이블)
   */
  COMMENT
}