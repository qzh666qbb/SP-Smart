package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PhysicalTestBatchMapper extends BaseMapper<PhysicalTestBatchEntity> {
    @Select("SELECT id FROM physical_test_template WHERE status = 'ACTIVE' AND deleted_at IS NULL ORDER BY id LIMIT 1")
    Long findDefaultTemplateId();
}
