package com.example.training.auth.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeacherProfileMapper extends BaseMapper<TeacherProfileEntity> {
}
