package com.example.training.hour.application.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HourRankResponse {
    private Long id;
    private String name;
    private BigDecimal value;
    private int count;
}
