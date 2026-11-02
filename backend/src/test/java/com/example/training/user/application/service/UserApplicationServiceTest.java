package com.example.training.user.application.service;

import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.user.application.dto.UserCreateRequest;
import com.example.training.user.application.dto.UserResponse;
import com.example.training.user.application.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserApplicationService(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(service, "defaultTempPassword", "123456");
    }

    @Test
    void deleteUserSoftDeletesAndDisablesAccount() {
        User user = User.builder().id(5L).username("parent").role("PARENT").status("ENABLED").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.deleteUser(5L);

        assertEquals("DISABLED", user.getStatus());
        assertNotNull(user.getDeletedAt());
        verify(userRepository).save(user);
    }

    @Test
    void teacherResponseContainsTeacherProfileIdentity() {
        User user = User.builder().id(4L).username("teacher").role("TEACHER").status("ENABLED").build();
        TeacherProfile profile = TeacherProfile.builder().id(21L).userId(4L).name("张老师").build();
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.findTeacherProfileByUserId(4L)).thenReturn(Optional.of(profile));

        UserResponse response = service.getUserById(4L);

        assertEquals(21L, response.getProfileId());
        assertEquals("张老师", response.getProfileName());
    }

    @Test
    void createTeacherUsesPhoneAsUsernameAndCreatesProfile() {
        UserCreateRequest request = UserCreateRequest.builder()
                .role("TEACHER")
                .phone("13800138000")
                .teacherName("张老师")
                .build();
        when(userRepository.findByUsername("13800138000")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("13800138000")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        AtomicReference<TeacherProfile> savedProfile = new AtomicReference<>();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(8L);
            return user;
        });
        when(userRepository.saveTeacherProfile(any(TeacherProfile.class))).thenAnswer(invocation -> {
            TeacherProfile profile = invocation.getArgument(0);
            profile.setId(18L);
            savedProfile.set(profile);
            return profile;
        });
        when(userRepository.findTeacherProfileByUserId(8L)).thenAnswer(invocation ->
                Optional.ofNullable(savedProfile.get()));

        UserResponse response = service.createUser(request);

        assertEquals("13800138000", response.getUsername());
        assertEquals("张老师", response.getProfileName());
        assertEquals("T00000008", savedProfile.get().getTeacherNo());
        assertTrue(savedProfile.get().getJoinedDate() != null);
    }

    @Test
    void editIncompleteTeacherRepairsLoginAndProfile() {
        User teacher = User.builder().id(2L).username("wx_old")
                .role("TEACHER").status("ENABLED").build();
        UserUpdateRequest request = UserUpdateRequest.builder()
                .role("TEACHER")
                .phone("13900139000")
                .teacherName("李老师")
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(userRepository.findByUsername("13900139000")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("13900139000")).thenReturn(Optional.empty());
        when(userRepository.findTeacherProfileByUserId(2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(TeacherProfile.builder().id(12L).userId(2L).name("李老师").build()));
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(userRepository.save(teacher)).thenReturn(teacher);
        when(userRepository.saveTeacherProfile(any(TeacherProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.updateUser(2L, request);

        assertEquals("13900139000", teacher.getUsername());
        assertEquals("13900139000", teacher.getPhone());
        assertEquals("encoded", teacher.getPasswordHash());
        assertEquals("李老师", response.getProfileName());
    }
}
