package com.example.myauth.service;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.LoginRequest;
import com.example.myauth.dto.LoginResponse;
import com.example.myauth.dto.SignupRequest;
import com.example.myauth.entity.RefreshToken;
import com.example.myauth.entity.User;
import com.example.myauth.repository.RefreshTokenRepository;
import com.example.myauth.repository.UserRepository;
import com.example.myauth.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;


  @Transactional
  public ApiResponse<Void> registerUser(SignupRequest signupRequest) {
    // 이메일을 정규화한다 (공백 제거, 소문자 변환)
    String normalizedEmail = signupRequest.getEmail().trim().toLowerCase();

    try {
      // signupRequest 정보를 이용하여 User Entity 인스턴스를 생성한다
      User user = User.builder()
          .email(normalizedEmail)  // 정규화된 이메일 사용
          .password(passwordEncoder.encode(signupRequest.getPassword()))
          .name(signupRequest.getUsername())
          .role(User.Role.ROLE_USER)
          .status(User.Status.ACTIVE)
          .isActive(true)
          .build();

      // DB에 저장한다 - unique constraint 위반 시 예외 발생
      userRepository.save(user);
      log.info("회원 가입 성공 : {}", user.getEmail());

      return ApiResponse.success("회원가입이 완료되었습니다.");

    } catch (DataIntegrityViolationException e) {
      // unique constraint 위반 (이미 존재하는 이메일)
      log.warn("중복된 이메일로 가입 시도 : {}", normalizedEmail);
      return ApiResponse.error("이미 가입된 이메일입니다.");

    } catch (Exception e) {
      // 기타 예외 처리
      log.error("회원가입 중 오류 발생 : {}", e.getMessage(), e);
      return ApiResponse.error("회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  }

  @Transactional
  public LoginResponse login(@Valid LoginRequest loginRequest) {
    // 1️⃣ 이메일을 정규화한다 (회원가입과 동일하게 처리)
    String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();
    log.info("로그인 시도: {}", normalizedEmail);

    // 2️⃣ 사용자를 조회한다
    User user = userRepository.findByEmail(normalizedEmail)
        .orElse(null);

    // 3️⃣ 사용자가 존재하지 않으면 에러 반환
    if (user == null) {
      log.warn("존재하지 않는 이메일로 로그인 시도: {}", normalizedEmail);
      // 보안상 이유로 이메일이 틀렸는지 비밀번호가 틀렸는지 알려주지 않음
      return createErrorResponse("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    // 4️⃣ 비밀번호를 검증한다
    boolean isPasswordValid = passwordEncoder.matches(
        loginRequest.getPassword(),  // 입력된 평문 비밀번호
        user.getPassword()            // DB에 저장된 암호화된 비밀번호
    );

    if (!isPasswordValid) {
      log.warn("잘못된 비밀번호로 로그인 시도: {}", normalizedEmail);
      // 보안상 동일한 에러 메시지 사용
      return createErrorResponse("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    // 5️⃣ 계정 상태를 확인한다

    // 5-1. 활성화 여부 확인
    if (!user.getIsActive()) {
      log.warn("비활성화된 계정으로 로그인 시도: {}", normalizedEmail);
      return createErrorResponse("비활성화된 계정입니다. 고객센터에 문의해주세요.");
    }

    // 5-2. 계정 상태 확인
    if (user.getStatus() != User.Status.ACTIVE) {
      log.warn("비정상 상태 계정으로 로그인 시도: {} (상태: {})", normalizedEmail, user.getStatus());

      // 상태에 따라 다른 메시지 반환
      String errorMessage = switch (user.getStatus()) {
        case SUSPENDED -> "정지된 계정입니다. 고객센터에 문의해주세요.";
        case DELETED -> "삭제된 계정입니다.";
        case INACTIVE -> "비활성화된 계정입니다. 고객센터에 문의해주세요.";
        case PENDING_VERIFICATION -> "이메일 인증이 필요합니다.";
        default -> "로그인할 수 없는 계정 상태입니다.";
      };
      return createErrorResponse(errorMessage);
    }

//    // 5-3. 계정 잠금 확인
//    if (user.getAccountLockedUntil() != null &&
//        user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
//      log.warn("잠긴 계정으로 로그인 시도: {} (잠금 해제: {})",
//          normalizedEmail, user.getAccountLockedUntil());
//      return createErrorResponse(
//          String.format("계정이 잠겨있습니다. %s 이후 다시 시도해주세요.",
//              user.getAccountLockedUntil())
//      );
//    }

    // 6️⃣ JWT 토큰 생성
    String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getId());
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

    log.info("JWT 토큰 생성 완료: {}", normalizedEmail);

    // 7️⃣ Refresh Token을 DB에 저장
    RefreshToken refreshTokenEntity = RefreshToken.builder()
        .token(refreshToken)
        .user(user)
        .expiresAt(LocalDateTime.ofInstant(
            jwtTokenProvider.getRefreshTokenExpiryDate().toInstant(),
            ZoneId.systemDefault()
        ))
        .build();

    refreshTokenRepository.save(refreshTokenEntity);
    log.info("Refresh Token DB 저장 완료: {}", normalizedEmail);

    // 8️⃣ 로그인 성공 응답 반환
    LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .role(user.getRole().name())
        .build();

    log.info("로그인 성공: {}", normalizedEmail);
    return LoginResponse.success(accessToken, refreshToken, userInfo);
  }

  /**
   * 에러 응답 생성 헬퍼 메서드
   */
  private LoginResponse createErrorResponse(String message) {
    return LoginResponse.builder()
        .success(false)
        .message(message)
        .build();
  }
}
