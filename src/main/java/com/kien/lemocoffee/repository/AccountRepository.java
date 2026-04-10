package com.kien.lemocoffee.repository;

import com.kien.lemocoffee.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Account> findTopByOrderByIdDesc();

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Integer id);
}