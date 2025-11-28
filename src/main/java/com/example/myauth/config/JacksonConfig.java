package com.example.myauth.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 3 ObjectMapper Bean 설정
 * Spring Boot 4.0.0은 Jackson 3를 자동으로 포함하지만 커스터마이징을 위해 명시적으로 정의
 */
@Configuration
public class JacksonConfig {

  /**
   * Jackson 3 ObjectMapper Bean 생성
   * JSON 직렬화/역직렬화에 사용되는 ObjectMapper를 Spring Bean으로 등록
   * 패키지명: com.fasterxml.jackson (Jackson 2) → tools.jackson (Jackson 3)
   *
   * @return ObjectMapper 인스턴스
   */
  @Bean
  public ObjectMapper objectMapper() {
    // Jackson 3에서는 JsonMapper.builder()를 사용하여 생성
    return JsonMapper.builder().build();
  }
}
