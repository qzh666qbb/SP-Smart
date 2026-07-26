package com.example.training.auth.domain.repository;

import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByWechatOpenid(String openid);
    
    TeacherProfile saveTeacherProfile(TeacherProfile profile);
    
    Optional<TeacherProfile> findTeacherProfileByUserId(Long userId);
    
    ParentProfile saveParentProfile(ParentProfile profile);
    
    Optional<ParentProfile> findParentProfileByUserId(Long userId);
}
