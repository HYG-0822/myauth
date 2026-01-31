package com.example.myauth.repository;

import com.example.myauth.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글 이미지 Repository
 * 게시글에 첨부된 이미지 관리를 위한 데이터 접근 계층
 */
@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

  /**
   * 특정 게시글의 이미지 목록 조회 (정렬 순서대로)
   * @param postId 게시글 ID
   * @return 이미지 목록
   */
  List<PostImage> findByPostIdOrderBySortOrderAsc(Long postId);

  /**
   * 특정 게시글의 이미지 개수 조회
   * @param postId 게시글 ID
   * @return 이미지 개수
   */
  long countByPostId(Long postId);

  /**
   * 특정 게시글의 모든 이미지 삭제
   * @param postId 게시글 ID
   */
  @Modifying
  @Query("DELETE FROM PostImage pi WHERE pi.post.id = :postId")
  void deleteByPostId(@Param("postId") Long postId);

  /**
   * 특정 게시글의 첫 번째 이미지 조회 (썸네일용)
   * @param postId 게시글 ID
   * @return 첫 번째 이미지
   */
  @Query("SELECT pi FROM PostImage pi WHERE pi.post.id = :postId ORDER BY pi.sortOrder ASC LIMIT 1")
  PostImage findFirstByPostId(@Param("postId") Long postId);
}