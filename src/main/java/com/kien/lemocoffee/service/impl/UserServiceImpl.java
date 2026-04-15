package com.kien.lemocoffee.service.impl;

import com.kien.lemocoffee.entity.Account;
import com.kien.lemocoffee.constant.AccountRoleEnum;
import com.kien.lemocoffee.constant.AccountStatusEnum;
import com.kien.lemocoffee.repository.AccountRepository;
import com.kien.lemocoffee.dto.UserInfoDTO;
import com.kien.lemocoffee.dto.UserTableDTO;
import com.kien.lemocoffee.entity.User;
import com.kien.lemocoffee.constant.UserManagementResult;
import com.kien.lemocoffee.mapper.UserMapper;
import com.kien.lemocoffee.repository.UserRepository;
import com.kien.lemocoffee.service.NotificationSender;
import com.kien.lemocoffee.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationSender notificationSender;

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int RESET_PASSWORD_LENGTH = 7;

    @Override
    public Page<UserTableDTO> getUser(int page, int size, String keyword, String field) {

        int pageNo = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        String kw = normalize(keyword);
        String fd = normalize(field).toLowerCase();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> userPage;

        if (kw.isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else if ("username".equals(fd)) {
            userPage = userRepository.findByAccount_UsernameContainingIgnoreCase(kw, pageable);
        } else {
            userPage = userRepository.findByFullNameContainingIgnoreCase(kw, pageable);
        }

        return userPage.map(userMapper::toUserTableDTO);
    }

    @Override
    @Transactional
    public UserManagementResult createUser(UserInfoDTO formData) {
        try {
            AccountRoleEnum role = formData.getRole();

            Account account = Account.builder()
                    .code(generateAccountCode(role))
                    .username(formData.getUsername())
                    .passwordHash(passwordEncoder.encode(formData.getPassword()))
                    .role(role)
                    .status(AccountStatusEnum.ACTIVE)
                    .build();

            account = accountRepository.save(account);

            User user = User.builder()
                    .account(account)
                    .fullName(formData.getFullName())
                    .email(formData.getEmail())
                    .phone(formData.getPhone())
                    .build();

            userRepository.save(user);

            return UserManagementResult.CREATE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to create user with username={}, fullName={}, role={}",
                    formData.getUsername(),
                    formData.getFullName(),
                    formData.getRole(),
                    e);
            return UserManagementResult.CREATE_FAILED;
        }
    }

    @Override
    public UserInfoDTO getUserInfoByAccountId(Integer accountId) {
        return userRepository.findByAccount_Id(accountId)
                .map(userMapper::toUserInfoDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public UserManagementResult updateUser(UserInfoDTO formData) {
        try {
            Integer accountId = formData.getAccountId();
            User user = findUserWithAccount(accountId);

            if (user == null) {
                return UserManagementResult.USER_NOT_FOUND;
            }

            Account account = user.getAccount();

            account.setUsername(formData.getUsername());
            account.setRole(formData.getRole());
            account.setStatus(formData.getStatus());

            user.setFullName(formData.getFullName());
            user.setEmail(formData.getEmail());
            user.setPhone(formData.getPhone());

            userRepository.save(user);
            accountRepository.save(account);

            return UserManagementResult.UPDATE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to update user with accountId={}, username={}",
                    formData.getAccountId(),
                    formData.getUsername(),
                    e);
            return UserManagementResult.UPDATE_FAILED;
        }
    }

    @Override
    @Transactional
    public UserManagementResult deleteUser(Integer accountId) {
        try {
            User user = findUserWithAccount(accountId);

            if (user == null) {
                return UserManagementResult.USER_NOT_FOUND;
            }

            Account account = user.getAccount();

            userRepository.delete(user);
            accountRepository.delete(account);

            return UserManagementResult.DELETE_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to delete user with accountId={}", accountId, e);
            return UserManagementResult.DELETE_FAILED;
        }
    }

    @Override
    @Transactional
    public UserManagementResult changeUserStatus(Integer accountId, AccountStatusEnum status) {
        try {
            if (status == null) {
                return UserManagementResult.CHANGE_STATUS_FAILED;
            }

            User user = findUserWithAccount(accountId);
            if (user == null) {
                return UserManagementResult.USER_NOT_FOUND;
            }

            Account account = user.getAccount();
            account.setStatus(status);

            accountRepository.save(account);

            return UserManagementResult.CHANGE_STATUS_SUCCESS;

        } catch (Exception e) {
            log.error("Failed to change user status. accountId={}, status={}", accountId, status, e);
            return UserManagementResult.CHANGE_STATUS_FAILED;
        }
    }

    @Override
    @Transactional
    public UserManagementResult resetPassword(int accountId) {
        try {
            User user = userRepository.findByAccount_Id(accountId)
                    .orElse(null);

            if (user == null) {
                return UserManagementResult.USER_NOT_FOUND;
            }

            String email = user.getEmail();

            if (email == null || email.isBlank()) {
                return UserManagementResult.EMAIL_NOT_FOUND;
            }

            String newRawPassword = generateRandomPassword();
            String encodedPassword = passwordEncoder.encode(newRawPassword);

            user.getAccount().setPasswordHash(encodedPassword);

            userRepository.saveAndFlush(user);

            boolean emailSent = notificationSender.send(
                    email,
                    "KeyCoffee | Your Password Has Been Reset",
                    buildResetPasswordEmailContent(user.getFullName(), newRawPassword)
            );

            if (!emailSent) {
                rollbackCurrentTransaction();
                return UserManagementResult.SEND_EMAIL_FAILED;
            }

            return UserManagementResult.RESET_PASSWORD_SUCCESS;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Reset password failed unexpectedly, accountId={}", accountId, e);
            return UserManagementResult.RESET_PASSWORD_FAILED;
        }
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(UserServiceImpl.RESET_PASSWORD_LENGTH);

        for (int i = 0; i < UserServiceImpl.RESET_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(PASSWORD_CHARS.length());
            sb.append(PASSWORD_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private String buildResetPasswordEmailContent(String fullName, String newPassword) {
        return """
                Hello %s,

                We received a request to reset your password for your KeyCoffee account.

                Your new temporary password is:
                %s

                Please log in using this password and change it immediately to keep your account secure.

                If you did not request this change, please contact our support team as soon as possible.

                Best regards,
                KeyCoffee Team
                """.formatted(
                fullName != null && !fullName.isBlank() ? fullName : "User",
                newPassword
        );
    }

    private String generateAccountCode(AccountRoleEnum role) {
        int nextId = accountRepository.findTopByOrderByIdDesc()
                .map(Account::getId)
                .orElse(0) + 1;

        String prefix = switch (role) {
            case ADMIN -> "ADM";
            case STAFF -> "STF";
        };

        return prefix + String.format("%03d", nextId);
    }

    private User findUserWithAccount(Integer accountId) {
        if (accountId == null || accountId <= 0) {
            return null;
        }

        User user = userRepository.findByAccount_Id(accountId).orElse(null);

        if (user == null || user.getAccount() == null) {
            return null;
        }

        return user;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private void rollbackCurrentTransaction() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
        }
    }

}
