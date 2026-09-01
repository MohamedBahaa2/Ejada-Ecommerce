package com.ejada.ecommerce.auth.service;

import com.ejada.ecommerce.auth.domain.AppUser;
import com.ejada.ecommerce.auth.domain.Role;
import com.ejada.ecommerce.auth.dto.*;
import com.ejada.ecommerce.auth.exception.DuplicateAccountException;
import com.ejada.ecommerce.auth.exception.InvalidCredentialsException;
import com.ejada.ecommerce.auth.mapper.UserMapper;
import com.ejada.ecommerce.auth.repository.AppUserRepository;
import com.ejada.ecommerce.auth.repository.RoleRepository;
import com.ejada.ecommerce.auth.security.AccessTokenService;
import com.ejada.ecommerce.auth.security.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ejada.ecommerce.auth.dto.LogoutRequest;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * A valid BCrypt hash of a value nobody knows. When the username does not exist we
     * still run a comparison against this so the response time of "unknown user" matches
     * "known user, wrong password".
     */
    private static final String DUMMY_HASH =
            "$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final AppUserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;

    public AuthService(AppUserRepository users, RoleRepository roles,
                       PasswordEncoder passwordEncoder, UserMapper userMapper,
                       AccessTokenService accessTokens, RefreshTokenService refreshTokens) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.accessTokens = accessTokens;
        this.refreshTokens = refreshTokens;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new DuplicateAccountException("Username is already taken");
        }
        if (users.existsByEmail(request.email())) {
            throw new DuplicateAccountException("Email is already registered");
        }

        Role userRole = roles.findByName(Role.USER)
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_USER missing — did V2__seed_roles.sql run?"));

        AppUser user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        try {
            AppUser saved = users.saveAndFlush(user);
            log.info("Registered user {}", saved.getId());
            return userMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // Two simultaneous registrations both passed the exists() checks above.
            // The unique constraint is what actually decides.
            throw new DuplicateAccountException("Username or email is already registered");
        }
    }

    @Transactional
    public TokenPairResponse authenticate(LoginRequest request) {
        AppUser user = users.findByUsername(request.username()).orElse(null);

        if (user == null) {
            passwordEncoder.matches(request.password(), DUMMY_HASH); // result discarded, cost paid
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isEnabled()) {
            // Same response — whether an account is disabled is still information
            // about whether it exists.
            throw new InvalidCredentialsException();
        }

        String accessToken = accessTokens.issue(user);
        String refreshToken = refreshTokens.issueNewFamily(user.getId());
        log.info("Issued token pair for user {}", user.getId());

        return TokenPairResponse.bearer(accessToken, refreshToken, accessTokens.accessTokenTtlSeconds());
    }

    @Transactional
    public TokenPairResponse refresh(RefreshRequest request) {
        RefreshTokenService.Rotation rotation = refreshTokens.rotate(request.refreshToken());

        AppUser user = users.findById(rotation.userId())
                .orElseThrow(InvalidCredentialsException::new);

        return TokenPairResponse.bearer(
                accessTokens.issue(user), rotation.refreshToken(), accessTokens.accessTokenTtlSeconds());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokens.revoke(request.refreshToken());
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(Long userId) {
        return users.findById(userId)
                .map(userMapper::toResponse)
                .orElseThrow(InvalidCredentialsException::new);
    }
}