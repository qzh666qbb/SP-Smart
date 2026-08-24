package com.example.training.auth.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

    @Update("UPDATE sys_user SET last_login_at = NOW(), updated_at = NOW() WHERE id = #{id}")
    int updateLastLoginAt(Long id);
}
