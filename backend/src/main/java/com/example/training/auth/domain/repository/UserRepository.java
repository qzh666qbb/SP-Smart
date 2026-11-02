package com.example.training.auth.domain.repository;

import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByWechatOpenid(String openid);

    Optional<User> findByWechatUnionid(String unionid);

    List<User> findAll(int pageNum, int pageSize);

    List<User> findAll(String keyword, String role, int pageNum, int pageSize);

    long countAll();

    long countAll(String keyword, String role);
    
    TeacherProfile saveTeacherProfile(TeacherProfile profile);
    
    Optional<TeacherProfile> findTeacherProfileByUserId(Long userId);

    Optional<TeacherProfile> findTeacherProfileById(Long profileId);
    
    ParentProfile saveParentProfile(ParentProfile profile);
    
    Optional<ParentProfile> findParentProfileByUserId(Long userId);

    void updateLastLoginAt(Long userId);
}
