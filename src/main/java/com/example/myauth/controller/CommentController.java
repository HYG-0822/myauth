package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.comment.CommentCreateRequest;
import com.example.myauth.dto.comment.CommentResponse;
import com.example.myauth.dto.comment.CommentUpdateRequest;
import com.example.myauth.entity.User;
import com.example.myauth.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 컨트롤러
 * 댓글/대댓글 CRUD API 엔드포인트 제공
 *
 * 【API 목록】
 * - POST   /api/posts/{postId}/comments       : 댓글 작성
 * - POST   /api/comments/{commentId}/replies  : 대댓글 작성
 * - PUT    /api/comments/{id}                 : 댓글 수정
 * - DELETE /api/comments/{id}                 : 댓글 삭제
 * - GET    /api/posts/{postId}/comments       : 게시글의 댓글 목록
 * - GET    /api/comments/{id}/replies         : 댓글의 대댓글 목록
 * - GET    /api/comments/{id}                 : 댓글 상세 조회
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  // ===== 댓글 작성 =====

  /**
   * 댓글 작성
   *
   * POST /api/posts/{postId}/comments
   * Content-Type: application/json
   *
   * 【요청 예시】
   * {
   *   "content": "좋은 게시글이네요!"
   * }
   */
  @PostMapping("/api/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
      @AuthenticationPrincipal User user,
      @PathVariable Long postId,
      @Valid @RequestBody CommentCreateRequest request
  ) {
    log.info("댓글 작성 요청 - userId: {}, postId: {}", user.getId(), postId);

    CommentResponse response = commentService.createComment(user.getId(), postId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("댓글이 작성되었습니다.", response));
  }

  /**
   * 대댓글(답글) 작성
   *
   * POST /api/comments/{commentId}/replies
   * Content-Type: application/json
   *
   * 【요청 예시】
   * {
   *   "content": "저도 그렇게 생각해요!"
   * }
   */
  @PostMapping("/api/comments/{commentId}/replies")
  public ResponseEntity<ApiResponse<CommentResponse>> createReply(
      @AuthenticationPrincipal User user,
      @PathVariable Long commentId,
      @Valid @RequestBody CommentCreateRequest request
  ) {
    log.info("대댓글 작성 요청 - userId: {}, parentCommentId: {}", user.getId(), commentId);

    CommentResponse response = commentService.createReply(user.getId(), commentId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("답글이 작성되었습니다.", response));
  }

  // ===== 댓글 수정 =====

  /**
   * 댓글 수정
   *
   * PUT /api/comments/{id}
   *
   * 【권한】
   * - 작성자 본인만 수정 가능
   */
  @PutMapping("/api/comments/{id}")
  public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    log.info("댓글 수정 요청 - userId: {}, commentId: {}", user.getId(), id);

    CommentResponse response = commentService.updateComment(user.getId(), id, request);

    return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", response));
  }

  // ===== 댓글 삭제 =====

  /**
   * 댓글 삭제 (Soft Delete)
   *
   * DELETE /api/comments/{id}
   *
   * 【권한】
   * - 작성자 본인만 삭제 가능
   *
   * 【참고】
   * - 대댓글이 있는 경우 "삭제된 댓글입니다"로 표시
   */
  @DeleteMapping("/api/comments/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal User user,
      @PathVariable Long id
  ) {
    log.info("댓글 삭제 요청 - userId: {}, commentId: {}", user.getId(), id);

    commentService.deleteComment(user.getId(), id);

    return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
  }

  // ===== 댓글 목록 조회 =====

  /**
   * 게시글의 댓글 목록 조회
   *
   * GET /api/posts/{postId}/comments?page=0&size=20
   *
   * 【쿼리 파라미터】
   * - page: 페이지 번호 (0부터 시작, 기본값 0)
   * - size: 페이지 크기 (기본값 20, 최대 50)
   *
   * 【응답】
   * - 최상위 댓글만 반환 (대댓글은 별도 API로 조회)
   * - 각 댓글의 대댓글 수 포함
   */
  @GetMapping("/api/posts/{postId}/comments")
  public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
      @AuthenticationPrincipal User user,
      @PathVariable Long postId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    log.info("댓글 목록 조회 요청 - postId: {}, page: {}", postId, page);

    // 페이지 크기 제한
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size);
    Long userId = user != null ? user.getId() : null;

    Page<CommentResponse> comments = commentService.getCommentsByPostId(userId, postId, pageable);

    return ResponseEntity.ok(ApiResponse.success("댓글 목록 조회 성공", comments));
  }

  /**
   * 댓글의 대댓글 목록 조회
   *
   * GET /api/comments/{commentId}/replies
   *
   * 【응답】
   * - 특정 댓글의 모든 대댓글 반환
   * - 대댓글의 대댓글은 지원하지 않음 (2단계까지만)
   */
  @GetMapping("/api/comments/{commentId}/replies")
  public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
      @AuthenticationPrincipal User user,
      @PathVariable Long commentId
  ) {
    log.info("대댓글 목록 조회 요청 - parentCommentId: {}", commentId);

    Long userId = user != null ? user.getId() : null;
    List<CommentResponse> replies = commentService.getReplies(userId, commentId);

    return ResponseEntity.ok(ApiResponse.success("대댓글 목록 조회 성공", replies));
  }

  /**
   * 댓글 상세 조회
   *
   * GET /api/comments/{id}
   */
  @GetMapping("/api/comments/{id}")
  public ResponseEntity<ApiResponse<CommentResponse>> getComment(
      @AuthenticationPrincipal User user,
      @PathVariable Long id
  ) {
    log.info("댓글 상세 조회 요청 - commentId: {}", id);

    Long userId = user != null ? user.getId() : null;
    CommentResponse response = commentService.getComment(userId, id);

    return ResponseEntity.ok(ApiResponse.success("댓글 조회 성공", response));
  }
}
