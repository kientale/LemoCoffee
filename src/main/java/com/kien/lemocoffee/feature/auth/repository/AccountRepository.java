package com.kien.lemocoffee.feature.auth.repository;

import com.kien.lemocoffee.feature.auth.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Account> findTopByOrderByIdDesc();

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Integer id);
}