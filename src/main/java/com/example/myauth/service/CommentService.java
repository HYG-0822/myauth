package com.example.myauth.service;

import com.example.myauth.dto.comment.CommentCreateRequest;
import com.example.myauth.dto.comment.CommentResponse;
import com.example.myauth.dto.comment.CommentUpdateRequest;
import com.example.myauth.entity.Comment;
import com.example.myauth.entity.Post;
import com.example.myauth.entity.User;
import com.example.myauth.exception.CommentNotFoundException;
import com.example.myauth.exception.PostNotFoundException;
import com.example.myauth.exception.UnauthorizedAccessException;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.LikeRepository;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 서비스
 * 댓글 CRUD 및 대댓글 관련 비즈니스 로직 처리
 *
 * 【주요 기능】
 * - 댓글 작성/수정/삭제
 * - 대댓글(답글) 작성
 * - 댓글 목록 조회 (페이징, 대댓글 포함)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final LikeRepository likeRepository;

  // ===== 댓글 작성 =====

  /**
   * 댓글 작성 (최상위 댓글)
   *
   * @param userId 작성자 ID
   * @param postId 게시글 ID
   * @param request 댓글 작성 요청
   * @return 생성된 댓글 응답
   */
  @Transactional
  public CommentResponse createComment(Long userId, Long postId, CommentCreateRequest request) {
    log.info("댓글 작성 시작 - userId: {}, postId: {}", userId, postId);

    // 1. 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 2. 게시글 조회
    Post post = postRepository.findByIdAndIsDeletedFalse(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));

    // 3. 댓글 엔티티 생성
    Comment comment = Comment.builder()
        .user(user)
        .post(post)
        .content(request.getContent())
        .build();

    // 4. 댓글 저장
    comment = commentRepository.save(comment);

    // 5. 게시글 댓글 수 증가
    postRepository.incrementCommentCount(postId);

    log.info("댓글 작성 완료 - commentId: {}", comment.getId());

    return CommentResponse.from(comment, false, 0);
  }

  /**
   * 대댓글(답글) 작성
   *
   * @param userId 작성자 ID
   * @param parentCommentId 부모 댓글 ID
   * @param request 댓글 작성 요청
   * @return 생성된 대댓글 응답
   */
  @Transactional
  public CommentResponse createReply(Long userId, Long parentCommentId, CommentCreateRequest request) {
    log.info("대댓글 작성 시작 - userId: {}, parentCommentId: {}", userId, parentCommentId);

    // 1. 사용자 조회
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 2. 부모 댓글 조회
    Comment parentComment = commentRepository.findByIdWithUserAndPost(parentCommentId)
        .orElseThrow(() -> new CommentNotFoundException(parentCommentId));

    // 3. 대댓글의 대댓글은 허용하지 않음 (2단계까지만)
    if (parentComment.getParent() != null) {
      throw new IllegalArgumentException("대댓글에는 답글을 작성할 수 없습니다.");
    }

    // 4. 대댓글 엔티티 생성
    Comment reply = Comment.builder()
        .user(user)
        .post(parentComment.getPost())
        .parent(parentComment)
        .content(request.getContent())
        .build();

    // 5. 대댓글 저장
    reply = commentRepository.save(reply);

    // 6. 게시글 댓글 수 증가
    postRepository.incrementCommentCount(parentComment.getPost().getId());

    log.info("대댓글 작성 완료 - replyId: {}, parentId: {}", reply.getId(), parentCommentId);

    return CommentResponse.from(reply, false, 0);
  }

  // ===== 댓글 수정 =====

  /**
   * 댓글 수정
   *
   * @param userId 요청 사용자 ID
   * @param commentId 댓글 ID
   * @param request 수정 요청
   * @return 수정된 댓글 응답
   */
  @Transactional
  public CommentResponse updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
    log.info("댓글 수정 시작 - userId: {}, commentId: {}", userId, commentId);

    // 1. 댓글 조회
    Comment comment = commentRepository.findByIdWithUserAndPost(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

    // 2. 권한 확인 (작성자 본인인지)
    if (!comment.getUser().getId().equals(userId)) {
      throw new UnauthorizedAccessException("댓글을 수정할 권한이 없습니다.");
    }

    // 3. 내용 수정
    comment.setContent(request.getContent());

    // 4. 저장
    comment = commentRepository.save(comment);

    // 5. 대댓글 수 조회
    long replyCount = commentRepository.countRepliesByParentId(commentId);

    // 6. 좋아요 여부 확인
    boolean isLiked = likeRepository.existsCommentLikeByUserId(userId, commentId);

    log.info("댓글 수정 완료 - commentId: {}", commentId);

    return CommentResponse.from(comment, isLiked, (int) replyCount);
  }

  // ===== 댓글 삭제 =====

  /**
   * 댓글 삭제 (Soft Delete)
   * 대댓글이 있는 경우 "삭제된 댓글입니다"로 표시
   *
   * @param userId 요청 사용자 ID
   * @param commentId 댓글 ID
   */
  @Transactional
  public void deleteComment(Long userId, Long commentId) {
    log.info("댓글 삭제 시작 - userId: {}, commentId: {}", userId, commentId);

    // 1. 댓글 조회
    Comment comment = commentRepository.findByIdWithUserAndPost(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

    // 2. 권한 확인 (작성자 본인인지)
    if (!comment.getUser().getId().equals(userId)) {
      throw new UnauthorizedAccessException("댓글을 삭제할 권한이 없습니다.");
    }

    // 3. Soft Delete 처리
    comment.softDelete();
    commentRepository.save(comment);

    // 4. 게시글 댓글 수 감소
    postRepository.decrementCommentCount(comment.getPost().getId());

    log.info("댓글 삭제 완료 (Soft Delete) - commentId: {}", commentId);
  }

  // ===== 댓글 조회 =====

  /**
   * 게시글의 댓글 목록 조회 (최상위 댓글만, 페이징)
   *
   * @param userId 요청 사용자 ID (좋아요 여부 확인용)
   * @param postId 게시글 ID
   * @param pageable 페이지 정보
   * @return 댓글 페이지
   */
  @Transactional(readOnly = true)
  public Page<CommentResponse> getCommentsByPostId(Long userId, Long postId, Pageable pageable) {
    log.info("게시글 댓글 목록 조회 - postId: {}, page: {}", postId, pageable.getPageNumber());

    // 1. 게시글 존재 확인
    if (!postRepository.existsById(postId)) {
      throw new PostNotFoundException(postId);
    }

    // 2. 최상위 댓글 목록 조회
    Page<Comment> comments = commentRepository.findRootCommentsByPostId(postId, pageable);

    // 3. 댓글 ID 목록 추출
    List<Long> commentIds = comments.getContent().stream()
        .map(Comment::getId)
        .collect(Collectors.toList());

    // 4. 사용자가 좋아요한 댓글 ID 목록 조회
    List<Long> likedCommentIds = userId != null && !commentIds.isEmpty()
        ? likeRepository.findLikedCommentIdsByUserId(userId, commentIds)
        : Collections.emptyList();

    // 5. 응답 DTO 변환
    return comments.map(comment -> {
      boolean isLiked = likedCommentIds.contains(comment.getId());
      long replyCount = commentRepository.countRepliesByParentId(comment.getId());
      return CommentResponse.from(comment, isLiked, (int) replyCount);
    });
  }

  /**
   * 특정 댓글의 대댓글 목록 조회
   *
   * @param userId 요청 사용자 ID (좋아요 여부 확인용)
   * @param commentId 부모 댓글 ID
   * @return 대댓글 목록
   */
  @Transactional(readOnly = true)
  public List<CommentResponse> getReplies(Long userId, Long commentId) {
    log.info("대댓글 목록 조회 - parentCommentId: {}", commentId);

    // 1. 부모 댓글 존재 확인
    if (!commentRepository.existsById(commentId)) {
      throw new CommentNotFoundException(commentId);
    }

    // 2. 대댓글 목록 조회
    List<Comment> replies = commentRepository.findRepliesByParentId(commentId);

    // 3. 대댓글 ID 목록 추출
    List<Long> replyIds = replies.stream()
        .map(Comment::getId)
        .collect(Collectors.toList());

    // 4. 사용자가 좋아요한 대댓글 ID 목록 조회
    List<Long> likedReplyIds = userId != null && !replyIds.isEmpty()
        ? likeRepository.findLikedCommentIdsByUserId(userId, replyIds)
        : Collections.emptyList();

    // 5. 응답 DTO 변환
    return replies.stream()
        .map(reply -> CommentResponse.from(reply, likedReplyIds.contains(reply.getId()), 0))
        .collect(Collectors.toList());
  }

  /**
   * 댓글 상세 조회
   *
   * @param userId 요청 사용자 ID
   * @param commentId 댓글 ID
   * @return 댓글 응답
   */
  @Transactional(readOnly = true)
  public CommentResponse getComment(Long userId, Long commentId) {
    log.info("댓글 상세 조회 - commentId: {}", commentId);

    // 1. 댓글 조회
    Comment comment = commentRepository.findByIdWithUserAndPost(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

    // 2. 좋아요 여부 확인
    boolean isLiked = userId != null && likeRepository.existsCommentLikeByUserId(userId, commentId);

    // 3. 대댓글 수 조회
    long replyCount = commentRepository.countRepliesByParentId(commentId);

    return CommentResponse.from(comment, isLiked, (int) replyCount);
  }
}
