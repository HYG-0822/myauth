package com.example.myauth.dto.post;

import com.example.myauth.entity.MediaType;
import com.example.myauth.entity.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 게시글 이미지 응답 DTO
 * 게시글에 첨부된 이미지/동영상 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageResponse {

  /**
   * 이미지 ID
   */
  private Long id;

  /**
   * 원본 이미지 URL
   */
  private String imageUrl;

  /**
   * 썸네일 URL (목록 표시용)
   */
  private String thumbnailUrl;

  /**
   * 표시 순서 (0부터 시작)
   */
  private Integer sortOrder;

  /**
   * 이미지 너비 (px)
   */
  private Integer width;

  /**
   * 이미지 높이 (px)
   */
  private Integer height;

  /**
   * 미디어 유형 (IMAGE/VIDEO)
   */
  private MediaType mediaType;

  /**
   * Entity → DTO 변환
   */
  public static PostImageResponse from(PostImage image) {
    return PostImageResponse.builder()
        .id(image.getId())
        .imageUrl(image.getImageUrl())
        .thumbnailUrl(image.getThumbnailUrl())
        .sortOrder(image.getSortOrder())
        .width(image.getWidth())
        .height(image.getHeight())
        .mediaType(image.getMediaType())
        .build();
  }
}