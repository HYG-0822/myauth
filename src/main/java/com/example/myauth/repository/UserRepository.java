package com.example.myauth.repository;

import com.example.myauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 정보를 관리하는 Repository
 * Spring Data JPA가 자동으로 구현을 생성한다
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * 이메일로 사용자를 조회한다
   * @param email 조회할 이메일
   * @return 사용자 정보 (Optional)
   */
  Optional<User> findByEmail(String email);

  /**
   * 이메일이 이미 존재하는지 확인한다
   * @param email 확인할 이메일
   * @return 존재하면 true, 아니면 false
   */
  boolean existsByEmail(String email);
}
