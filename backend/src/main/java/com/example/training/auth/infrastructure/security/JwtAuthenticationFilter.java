package com.example.training.auth.infrastructure.security;

import com.example.training.auth.domain.service.JwtService;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.security.CurrentUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && !jwtService.isTokenExpired(jwt) && jwtService.isAccessToken(jwt)) {
                Long userId = jwtService.extractUserId(jwt);
                String username = jwtService.extractUsername(jwt);
                String role = jwtService.extractRole(jwt);
                User databaseUser = userRepository.findById(userId).orElse(null);
                if (databaseUser == null || !databaseUser.isEnabled() || !role.equals(databaseUser.getRole())) {
                    filterChain.doFilter(request, response);
                    return;
                }
                
                String phone = databaseUser.getPhone();
                username = databaseUser.getUsername();
                Long profileId = jwtService.extractProfileId(jwt);
                String profileType = jwtService.extractProfileType(jwt);
                if ("TEACHER".equals(role)) {
                    profileId = userRepository.findTeacherProfileByUserId(userId)
                            .map(com.example.training.auth.domain.model.TeacherProfile::getId).orElse(null);
                    profileType = "TEACHER";
                } else if ("PARENT".equals(role)) {
                    profileId = userRepository.findParentProfileByUserId(userId)
                            .map(com.example.training.auth.domain.model.ParentProfile::getId).orElse(null);
                    profileType = "PARENT";
                }

                CurrentUser currentUser = CurrentUser.builder()
                        .userId(userId)
                        .username(username)
                        .role(role)
                        .phone(phone)
                        .profileId(profileId)
                        .profileType(profileType)
                        .build();
                
                UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                                currentUser,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            log.error("无法设置用户认证: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
