package com.kien.lemocoffee.security;

import com.kien.lemocoffee.entity.Account;
import com.kien.lemocoffee.constant.LoginResultEnum;
import com.kien.lemocoffee.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException(LoginResultEnum.USER_NOT_FOUND.getMessage()));

        return org.springframework.security.core.userdetails.User
                .withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .accountLocked(account.isLocked())
                .authorities(account.getRole().getAuthorities())
                .build();
    }
}
