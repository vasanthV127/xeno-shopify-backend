package com.xeno.service;

import com.xeno.dto.*;
import com.xeno.model.Tenant;
import com.xeno.repository.TenantRepository;
import com.xeno.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (tenantRepository.existsByShopifyDomain(request.getShopifyDomain())) {
            throw new RuntimeException("Shopify domain already registered");
        }

        Tenant tenant = Tenant.builder()
                .tenantId(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .storeName(request.getStoreName())
                .shopifyDomain(request.getShopifyDomain())
                .shopifyAccessToken(request.getShopifyAccessToken())
                .active(true)
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("New tenant registered: {}", tenant.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .tenantId(tenant.getTenantId())
                .email(tenant.getEmail())
                .storeName(tenant.getStoreName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        Tenant tenant = tenantRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .tenantId(tenant.getTenantId())
                .email(tenant.getEmail())
                .storeName(tenant.getStoreName())
                .build();
    }

    public Tenant getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return tenantRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }
}
