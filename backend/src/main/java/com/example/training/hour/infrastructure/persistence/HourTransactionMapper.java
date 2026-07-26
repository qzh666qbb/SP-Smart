package com.example.training.hour.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HourTransactionMapper extends BaseMapper<HourTransactionEntity> {
}
