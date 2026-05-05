package com.kien.keycoffee.repository;

import com.kien.keycoffee.entity.User;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @NonNull
    @EntityGraph(attributePaths = "account")
    Page<User> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = "account")
    Page<User> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "account")
    Page<User> findByAccount_UsernameContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "account")
    Optional<User> findByAccount_Id(Integer accountId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndAccount_IdNot(String phone, Integer accountId);

    boolean existsByEmailIgnoreCaseAndAccount_IdNot(String email, Integer accountId);
}