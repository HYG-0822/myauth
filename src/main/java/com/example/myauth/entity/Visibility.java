package com.example.myauth.entity;

/**
 * 게시글 공개 범위 Enum
 * 게시글의 공개 대상을 지정
 */
public enum Visibility {
  /**
   * 전체 공개 - 모든 사용자에게 노출
   */
  PUBLIC,

  /**
   * 비공개 - 작성자 본인만 볼 수 있음
   */
  PRIVATE,

  /**
   * 팔로워 공개 - 팔로워에게만 노출
   */
  FOLLOWERS
}