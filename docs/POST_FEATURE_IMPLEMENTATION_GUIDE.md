# 게시글/소셜 기능 백엔드 구현 가이드

> post.sql 스키마를 기반으로 한 Spring Boot 백엔드 구현 로드맵

## 📋 목차

1. [개요](#1-개요)
2. [구현 순서 요약](#2-구현-순서-요약)
3. [Phase 1: 핵심 기능](#3-phase-1-핵심-기능)
4. [Phase 2: 상호작용 기능](#4-phase-2-상호작용-기능)
5. [Phase 3: 소셜 기능](#5-phase-3-소셜-기능)
6. [Phase 4: 고급 기능](#6-phase-4-고급-기능)
7. [각 기능별 상세 구현](#7-각-기능별-상세-구현)
8. [테스트 전략](#8-테스트-전략)
9. [API 엔드포인트 목록](#9-api-엔드포인트-목록)

---

## 1. 개요

### 1.1 구현할 기능 목록

```
┌─────────────────────────────────────────────────────────────────────┐
│                         소셜 미디어 기능                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  📝 게시글 (Post)                                                   │
│     └── 작성, 수정, 삭제, 조회 (CRUD)                               │
│     └── 다중 이미지/동영상 첨부                                      │
│     └── 공개 범위 설정 (전체/비공개/팔로워)                          │
│                                                                     │
│  💬 댓글 (Comment)                                                  │
│     └── 댓글 작성, 수정, 삭제                                        │
│     └── 대댓글 (답글) 지원                                           │
│                                                                     │
│  ❤️ 좋아요 (Like)                                                   │
│     └── 게시글 좋아요                                                │
│     └── 댓글 좋아요                                                  │
│                                                                     │
│  #️⃣ 해시태그 (Hashtag)                                              │
│     └── 게시글에 해시태그 추가                                       │
│     └── 해시태그로 게시글 검색                                       │
│                                                                     │
│  🔖 북마크 (Bookmark)                                               │
│     └── 게시글 저장/저장 취소                                        │
│                                                                     │
│  👥 팔로우 (Follow)                                                 │
│     └── 사용자 팔로우/언팔로우                                       │
│     └── 팔로워/팔로잉 목록                                           │
│                                                                     │
│  📢 멘션 (Mention)                                                  │
│     └── @사용자 언급 기능                                            │
│     └── 멘션 알림                                                   │
│                                                                     │
│  📰 피드 (Feed)                                                     │
│     └── 팔로잉 사용자의 게시글 피드                                  │
│     └── 탐색 피드 (추천 게시글)                                      │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.2 이미 완료된 작업

- ✅ 데이터베이스 스키마 설계 (post.sql)
- ✅ Entity 클래스 생성 (Post, PostImage, Comment, Like, Hashtag, PostHashtag, Bookmark, Follow, Mention)
- ✅ Enum 클래스 생성 (Visibility, MediaType, TargetType)

---

## 2. 구현 순서 요약

```
┌─────────────────────────────────────────────────────────────────────┐
│                         구현 로드맵                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Phase 1: 핵심 기능 (Week 1-2)                                      │
│  ─────────────────────────────                                      │
│    1.1 게시글 CRUD                                                  │
│    1.2 게시글 이미지 업로드                                          │
│    1.3 게시글 목록 조회 (페이지네이션)                               │
│                                                                     │
│  Phase 2: 상호작용 기능 (Week 2-3)                                  │
│  ─────────────────────────────────                                  │
│    2.1 댓글 CRUD                                                    │
│    2.2 대댓글 (답글)                                                 │
│    2.3 좋아요 (게시글/댓글)                                          │
│                                                                     │
│  Phase 3: 소셜 기능 (Week 3-4)                                      │
│  ─────────────────────────────                                      │
│    3.1 팔로우/언팔로우                                               │
│    3.2 팔로워/팔로잉 목록                                            │
│    3.3 북마크 (저장)                                                 │
│                                                                     │
│  Phase 4: 고급 기능 (Week 4-5)                                      │
│  ─────────────────────────────                                      │
│    4.1 해시태그                                                      │
│    4.2 멘션 (@)                                                     │
│    4.3 피드 (타임라인)                                               │
│    4.4 검색 기능                                                     │
│                                                                     │
│  Phase 5: 최적화 (Week 5-6)                                         │
│  ───────────────────────────                                        │
│    5.1 캐싱 (Redis)                                                 │
│    5.2 성능 최적화                                                   │
│    5.3 알림 시스템                                                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Phase 1: 핵심 기능

### 3.1 게시글 CRUD

**구현 순서:**

```
1. Repository 생성
   └── PostRepository.java

2. DTO 생성
   ├── PostCreateRequest.java    (게시글 작성 요청)
   ├── PostUpdateRequest.java    (게시글 수정 요청)
   ├── PostResponse.java         (게시글 응답)
   └── PostListResponse.java     (게시글 목록 응답)

3. Service 생성
   └── PostService.java
       ├── createPost()          (게시글 작성)
       ├── updatePost()          (게시글 수정)
       ├── deletePost()          (게시글 삭제 - Soft Delete)
       ├── getPost()             (게시글 상세 조회)
       └── getPostsByUser()      (사용자별 게시글 목록)

4. Controller 생성
   └── PostController.java
       ├── POST   /api/posts           (게시글 작성)
       ├── PUT    /api/posts/{id}      (게시글 수정)
       ├── DELETE /api/posts/{id}      (게시글 삭제)
       ├── GET    /api/posts/{id}      (게시글 상세)
       └── GET    /api/posts           (게시글 목록)
```

**생성할 파일:**

```
src/main/java/com/example/myauth/
├── repository/
│   └── PostRepository.java
├── dto/
│   ├── PostCreateRequest.java
│   ├── PostUpdateRequest.java
│   ├── PostResponse.java
│   └── PostListResponse.java
├── service/
│   └── PostService.java
└── controller/
    └── PostController.java
```

**PostRepository 예시:**

```java
public interface PostRepository extends JpaRepository<Post, Long> {

    // 특정 사용자의 게시글 목록 (최신순, 삭제되지 않은 것만)
    Page<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(
        Long userId, Pageable pageable);

    // 공개 게시글 목록 (피드용)
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false " +
           "AND p.visibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findPublicPosts(Pageable pageable);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);
}
```

### 3.2 게시글 이미지 업로드

**구현 순서:**

```
1. Repository 생성
   └── PostImageRepository.java

2. Service 확장
   └── PostService.java
       └── 이미지 업로드 로직 추가

3. 기존 ImageStorageService 활용
   └── 이미지 저장 후 PostImage 엔티티 생성
```

**이미지 처리 흐름:**

```
┌─────────────────────────────────────────────────────────────────────┐
│                    게시글 + 이미지 업로드 흐름                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. 클라이언트                                                       │
│     └── 게시글 내용 + 이미지 파일들 전송                             │
│                                                                     │
│  2. PostController                                                  │
│     └── @RequestPart("post") PostCreateRequest                     │
│     └── @RequestPart("images") List<MultipartFile>                 │
│                                                                     │
│  3. PostService.createPost()                                        │
│     ├── Post 엔티티 생성 및 저장                                    │
│     ├── 각 이미지에 대해:                                           │
│     │   ├── ImageStorageService.store() 호출                       │
│     │   └── PostImage 엔티티 생성 및 저장                           │
│     └── Post 반환                                                   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.3 페이지네이션

**Spring Data JPA 페이지네이션 활용:**

```java
// Controller
@GetMapping
public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPosts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getPosts(pageable);
    return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", posts));
}
```

---

## 4. Phase 2: 상호작용 기능

### 4.1 댓글 CRUD

**구현 순서:**

```
1. Repository 생성
   └── CommentRepository.java

2. DTO 생성
   ├── CommentCreateRequest.java
   ├── CommentUpdateRequest.java
   └── CommentResponse.java

3. Service 생성
   └── CommentService.java
       ├── createComment()
       ├── createReply()          (대댓글)
       ├── updateComment()
       ├── deleteComment()        (Soft Delete)
       └── getCommentsByPost()

4. Controller 생성
   └── CommentController.java
       ├── POST   /api/posts/{postId}/comments           (댓글 작성)
       ├── POST   /api/comments/{commentId}/replies      (대댓글 작성)
       ├── PUT    /api/comments/{id}                     (댓글 수정)
       ├── DELETE /api/comments/{id}                     (댓글 삭제)
       └── GET    /api/posts/{postId}/comments           (댓글 목록)
```

**대댓글 조회 전략:**

```
옵션 1: 계층적 조회 (Nested)
─────────────────────────
{
  "comments": [
    {
      "id": 1,
      "content": "좋은 게시글이네요!",
      "replies": [
        { "id": 2, "content": "저도 그렇게 생각해요" },
        { "id": 3, "content": "동의합니다" }
      ]
    }
  ]
}

옵션 2: 평면적 조회 (Flat) - 추천
─────────────────────────────
{
  "comments": [
    { "id": 1, "parentId": null, "content": "좋은 게시글이네요!" },
    { "id": 2, "parentId": 1, "content": "저도 그렇게 생각해요" },
    { "id": 3, "parentId": 1, "content": "동의합니다" }
  ]
}
→ 프론트엔드에서 parentId로 그룹핑
```

### 4.2 좋아요 기능

**구현 순서:**

```
1. Repository 생성
   └── LikeRepository.java

2. DTO 생성
   └── LikeResponse.java

3. Service 생성
   └── LikeService.java
       ├── likePost()
       ├── unlikePost()
       ├── likeComment()
       ├── unlikeComment()
       ├── isLikedByUser()       (좋아요 여부 확인)
       └── getLikeUsers()        (좋아요 누른 사용자 목록)

4. Controller 생성
   └── LikeController.java
       ├── POST   /api/posts/{postId}/like       (게시글 좋아요)
       ├── DELETE /api/posts/{postId}/like       (게시글 좋아요 취소)
       ├── POST   /api/comments/{commentId}/like (댓글 좋아요)
       └── DELETE /api/comments/{commentId}/like (댓글 좋아요 취소)
```

**좋아요 토글 vs 개별 API:**

```
옵션 1: 토글 API (간단)
─────────────────────
POST /api/posts/{postId}/like/toggle
→ 좋아요 되어있으면 취소, 안되어있으면 추가

옵션 2: 개별 API (명확) - 추천
───────────────────────────
POST   /api/posts/{postId}/like  → 좋아요
DELETE /api/posts/{postId}/like  → 좋아요 취소
→ RESTful하고 의도가 명확함
```

**좋아요 수 동기화:**

```java
// LikeService.java
@Transactional
public void likePost(Long userId, Long postId) {
    // 1. 중복 좋아요 확인
    if (likeRepository.existsByUserIdAndTargetTypeAndTargetId(
            userId, TargetType.POST, postId)) {
        throw new DuplicateLikeException("이미 좋아요한 게시글입니다.");
    }

    // 2. 좋아요 저장
    Like like = Like.forPost(userRepository.getReferenceById(userId), postId);
    likeRepository.save(like);

    // 3. 게시글 좋아요 수 증가 (캐싱 필드)
    postRepository.incrementLikeCount(postId);
}
```

---

## 5. Phase 3: 소셜 기능

### 5.1 팔로우 기능

**구현 순서:**

```
1. Repository 생성
   └── FollowRepository.java

2. DTO 생성
   ├── FollowRequest.java
   ├── FollowerResponse.java
   └── FollowingResponse.java

3. Service 생성
   └── FollowService.java
       ├── follow()
       ├── unfollow()
       ├── getFollowers()        (팔로워 목록)
       ├── getFollowings()       (팔로잉 목록)
       ├── isFollowing()         (팔로우 여부)
       └── getFollowCounts()     (팔로워/팔로잉 수)

4. Controller 생성
   └── FollowController.java
       ├── POST   /api/users/{userId}/follow      (팔로우)
       ├── DELETE /api/users/{userId}/follow      (언팔로우)
       ├── GET    /api/users/{userId}/followers   (팔로워 목록)
       └── GET    /api/users/{userId}/followings  (팔로잉 목록)
```

**팔로우 관계 쿼리:**

```java
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로우 여부 확인
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 팔로워 목록 (나를 팔로우하는 사람들)
    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    Page<User> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    // 팔로잉 목록 (내가 팔로우하는 사람들)
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    Page<User> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 팔로워 수
    long countByFollowingId(Long userId);

    // 팔로잉 수
    long countByFollowerId(Long userId);
}
```

### 5.2 북마크 기능

**구현 순서:**

```
1. Repository 생성
   └── BookmarkRepository.java

2. Service 생성
   └── BookmarkService.java
       ├── bookmark()
       ├── unbookmark()
       ├── getBookmarkedPosts()  (저장한 게시글 목록)
       └── isBookmarked()        (북마크 여부)

3. Controller 생성
   └── BookmarkController.java
       ├── POST   /api/posts/{postId}/bookmark    (북마크)
       ├── DELETE /api/posts/{postId}/bookmark    (북마크 취소)
       └── GET    /api/me/bookmarks               (내 북마크 목록)
```

---

## 6. Phase 4: 고급 기능

### 6.1 해시태그 기능

**구현 순서:**

```
1. Repository 생성
   ├── HashtagRepository.java
   └── PostHashtagRepository.java

2. Service 생성
   └── HashtagService.java
       ├── extractHashtags()     (본문에서 해시태그 추출)
       ├── getOrCreateHashtag()  (해시태그 조회 또는 생성)
       ├── linkHashtagsToPost()  (게시글에 해시태그 연결)
       ├── getPostsByHashtag()   (해시태그로 게시글 검색)
       └── getTrendingHashtags() (인기 해시태그 목록)

3. Controller 생성
   └── HashtagController.java
       ├── GET /api/hashtags/trending             (인기 해시태그)
       └── GET /api/hashtags/{name}/posts         (해시태그 검색)
```

**해시태그 추출 로직:**

```java
public class HashtagService {

    // 해시태그 패턴: #한글영문숫자_
    private static final Pattern HASHTAG_PATTERN =
        Pattern.compile("#([\\w가-힣]+)");

    /**
     * 본문에서 해시태그 추출
     * 예: "오늘 #맛집 탐방! #서울맛집 #데이트"
     *     → ["맛집", "서울맛집", "데이트"]
     */
    public List<String> extractHashtags(String content) {
        if (content == null) return Collections.emptyList();

        List<String> hashtags = new ArrayList<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content);

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }

        return hashtags.stream().distinct().collect(Collectors.toList());
    }
}
```

### 6.2 멘션 기능

**구현 순서:**

```
1. Repository 생성
   └── MentionRepository.java

2. Service 생성
   └── MentionService.java
       ├── extractMentions()     (본문에서 멘션 추출)
       ├── createMentions()      (멘션 레코드 생성)
       └── getMyMentions()       (나를 멘션한 글 목록)

3. Controller에 통합
   └── 게시글/댓글 작성 시 자동으로 멘션 처리
```

**멘션 추출 및 처리:**

```java
public class MentionService {

    // 멘션 패턴: @사용자이름 (영문, 숫자, 언더스코어)
    private static final Pattern MENTION_PATTERN =
        Pattern.compile("@([\\w]+)");

    /**
     * 본문에서 멘션된 사용자 추출 및 저장
     */
    @Transactional
    public List<Mention> processMentions(String content, TargetType targetType, Long targetId) {
        List<String> usernames = extractMentions(content);
        List<Mention> mentions = new ArrayList<>();

        for (String username : usernames) {
            userRepository.findByUsername(username).ifPresent(user -> {
                Mention mention = Mention.builder()
                    .user(user)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
                mentions.add(mentionRepository.save(mention));

                // TODO: 알림 발송
                // notificationService.sendMentionNotification(user, targetType, targetId);
            });
        }

        return mentions;
    }
}
```

### 6.3 피드 (타임라인)

**구현 순서:**

```
1. Service 생성
   └── FeedService.java
       ├── getHomeFeed()         (팔로잉 게시글 피드)
       └── getExploreFeed()      (탐색 피드 - 추천)

2. Controller 생성
   └── FeedController.java
       ├── GET /api/feed          (홈 피드)
       └── GET /api/feed/explore  (탐색 피드)
```

**홈 피드 쿼리 (팔로잉 게시글):**

```java
@Query("SELECT p FROM Post p " +
       "WHERE p.user.id IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
       "AND p.isDeleted = false " +
       "AND (p.visibility = 'PUBLIC' OR p.visibility = 'FOLLOWERS') " +
       "ORDER BY p.createdAt DESC")
Page<Post> findHomeFeed(@Param("userId") Long userId, Pageable pageable);
```

---

## 7. 각 기능별 상세 구현

### 7.1 파일 구조 (최종)

```
src/main/java/com/example/myauth/
├── entity/           # 이미 완성됨 ✅
│   ├── Post.java
│   ├── PostImage.java
│   ├── Comment.java
│   ├── Like.java
│   ├── Hashtag.java
│   ├── PostHashtag.java
│   ├── Bookmark.java
│   ├── Follow.java
│   ├── Mention.java
│   ├── Visibility.java
│   ├── MediaType.java
│   └── TargetType.java
│
├── repository/       # Phase 1-4에서 순차 구현
│   ├── PostRepository.java
│   ├── PostImageRepository.java
│   ├── CommentRepository.java
│   ├── LikeRepository.java
│   ├── HashtagRepository.java
│   ├── PostHashtagRepository.java
│   ├── BookmarkRepository.java
│   ├── FollowRepository.java
│   └── MentionRepository.java
│
├── dto/              # Phase 1-4에서 순차 구현
│   ├── post/
│   │   ├── PostCreateRequest.java
│   │   ├── PostUpdateRequest.java
│   │   ├── PostResponse.java
│   │   └── PostListResponse.java
│   ├── comment/
│   │   ├── CommentCreateRequest.java
│   │   ├── CommentUpdateRequest.java
│   │   └── CommentResponse.java
│   ├── like/
│   │   └── LikeResponse.java
│   ├── follow/
│   │   ├── FollowerResponse.java
│   │   └── FollowingResponse.java
│   ├── bookmark/
│   │   └── BookmarkResponse.java
│   └── hashtag/
│       ├── HashtagResponse.java
│       └── TrendingHashtagResponse.java
│
├── service/          # Phase 1-4에서 순차 구현
│   ├── PostService.java
│   ├── CommentService.java
│   ├── LikeService.java
│   ├── FollowService.java
│   ├── BookmarkService.java
│   ├── HashtagService.java
│   ├── MentionService.java
│   └── FeedService.java
│
├── controller/       # Phase 1-4에서 순차 구현
│   ├── PostController.java
│   ├── CommentController.java
│   ├── LikeController.java
│   ├── FollowController.java
│   ├── BookmarkController.java
│   ├── HashtagController.java
│   └── FeedController.java
│
└── exception/        # 필요에 따라 추가
    ├── PostNotFoundException.java
    ├── CommentNotFoundException.java
    ├── DuplicateLikeException.java
    ├── DuplicateFollowException.java
    └── SelfFollowException.java
```

---

## 8. 테스트 전략

### 8.1 단위 테스트

```
src/test/java/com/example/myauth/
├── service/
│   ├── PostServiceTest.java
│   ├── CommentServiceTest.java
│   └── LikeServiceTest.java
└── repository/
    ├── PostRepositoryTest.java
    └── FollowRepositoryTest.java
```

### 8.2 통합 테스트

```
src/test/java/com/example/myauth/
└── controller/
    ├── PostControllerIntegrationTest.java
    └── FollowControllerIntegrationTest.java
```

### 8.3 테스트 케이스 예시

```
게시글 서비스 테스트:
├── 게시글 작성 성공
├── 게시글 작성 실패 (내용 없음)
├── 게시글 수정 성공
├── 게시글 수정 실패 (권한 없음)
├── 게시글 삭제 성공 (Soft Delete)
├── 게시글 조회 성공
├── 게시글 조회 실패 (삭제된 게시글)
└── 게시글 목록 페이지네이션

좋아요 서비스 테스트:
├── 좋아요 성공
├── 좋아요 실패 (중복)
├── 좋아요 취소 성공
├── 좋아요 취소 실패 (좋아요 안 한 상태)
└── 좋아요 수 동기화 확인

팔로우 서비스 테스트:
├── 팔로우 성공
├── 팔로우 실패 (자기 자신)
├── 팔로우 실패 (중복)
├── 언팔로우 성공
├── 팔로워 목록 조회
└── 팔로잉 목록 조회
```

---

## 9. API 엔드포인트 목록

### 9.1 게시글 (Post)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts` | 게시글 작성 | O |
| PUT | `/api/posts/{id}` | 게시글 수정 | O |
| DELETE | `/api/posts/{id}` | 게시글 삭제 | O |
| GET | `/api/posts/{id}` | 게시글 상세 | O |
| GET | `/api/posts` | 게시글 목록 | O |
| GET | `/api/users/{userId}/posts` | 사용자별 게시글 | O |

### 9.2 댓글 (Comment)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts/{postId}/comments` | 댓글 작성 | O |
| POST | `/api/comments/{commentId}/replies` | 대댓글 작성 | O |
| PUT | `/api/comments/{id}` | 댓글 수정 | O |
| DELETE | `/api/comments/{id}` | 댓글 삭제 | O |
| GET | `/api/posts/{postId}/comments` | 댓글 목록 | O |

### 9.3 좋아요 (Like)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts/{postId}/like` | 게시글 좋아요 | O |
| DELETE | `/api/posts/{postId}/like` | 게시글 좋아요 취소 | O |
| POST | `/api/comments/{commentId}/like` | 댓글 좋아요 | O |
| DELETE | `/api/comments/{commentId}/like` | 댓글 좋아요 취소 | O |
| GET | `/api/posts/{postId}/likes` | 좋아요 사용자 목록 | O |

### 9.4 팔로우 (Follow)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/users/{userId}/follow` | 팔로우 | O |
| DELETE | `/api/users/{userId}/follow` | 언팔로우 | O |
| GET | `/api/users/{userId}/followers` | 팔로워 목록 | O |
| GET | `/api/users/{userId}/followings` | 팔로잉 목록 | O |

### 9.5 북마크 (Bookmark)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts/{postId}/bookmark` | 북마크 추가 | O |
| DELETE | `/api/posts/{postId}/bookmark` | 북마크 취소 | O |
| GET | `/api/me/bookmarks` | 내 북마크 목록 | O |

### 9.6 해시태그 (Hashtag)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/hashtags/trending` | 인기 해시태그 | X |
| GET | `/api/hashtags/{name}/posts` | 해시태그 검색 | O |

### 9.7 피드 (Feed)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/feed` | 홈 피드 | O |
| GET | `/api/feed/explore` | 탐색 피드 | O |

---

## 부록: 구현 시 고려사항

### A. 성능 최적화

```
1. N+1 문제 해결
   └── @EntityGraph 또는 Fetch Join 사용
   └── 게시글 목록 조회 시 작성자 정보 함께 로드

2. 좋아요/댓글 수 캐싱
   └── 매번 COUNT 쿼리 대신 캐싱 필드 사용
   └── 트리거 또는 서비스 레이어에서 동기화

3. 페이지네이션
   └── Offset 방식: 간단하지만 대용량에서 성능 저하
   └── Cursor 방식: 대용량에서 효율적 (권장)

4. 인덱스 활용
   └── 자주 조회되는 컬럼에 인덱스 설정
   └── 복합 인덱스 순서 고려
```

### B. 보안

```
1. 게시글 권한 확인
   └── 수정/삭제 시 작성자 본인인지 확인
   └── 비공개 게시글 접근 권한 확인

2. 공개 범위 적용
   └── PRIVATE: 작성자 본인만
   └── FOLLOWERS: 팔로워만
   └── PUBLIC: 모든 사용자

3. Rate Limiting
   └── 좋아요, 팔로우 등 연속 요청 제한
```

### C. 확장 고려

```
1. 알림 시스템
   └── 좋아요, 댓글, 팔로우, 멘션 시 알림
   └── 실시간 알림 (WebSocket) 또는 폴링

2. 검색 기능
   └── Elasticsearch 연동
   └── 게시글 본문, 해시태그 검색

3. 추천 알고리즘
   └── 탐색 피드용 게시글 추천
   └── 팔로우 추천 (공통 팔로워 기반)
```

---

**문서 작성일**: 2026-01-24
**프로젝트**: MyAuth (Spring Boot)
**버전**: 1.0