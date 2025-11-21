package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.LoginRequest;
import com.example.myauth.dto.LoginResponse;
import com.example.myauth.dto.SignupRequest;
import com.example.myauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @GetMapping("/health")
  public ResponseEntity<ApiResponse<Void>> health() {
    return ResponseEntity.ok(ApiResponse.success("Auth Service is running"));
  }


  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest signupRequest) {
    log.info("다음 이메일로 회원가입 요청: {}", signupRequest.getEmail());

    // 회원가입 처리 시도 - 기본 검증은 @Valid로 이미 완료된 상태
    ApiResponse<Void> response = authService.registerUser(signupRequest);

    HttpStatusCode statusCode = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(statusCode).body(response);
  }


  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
    log.info("로그인 요청: {}", loginRequest.getEmail());

    // 로그인 처리 - 기본 검증은 @Valid로 이미 완료된 상태
    LoginResponse response = authService.login(loginRequest);

    // 성공 시 200 OK, 실패 시 400 Bad Request
    HttpStatusCode statusCode = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(statusCode).body(response);
  }


}
