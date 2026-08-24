package com.example.training.hour.application.dto;

import lombok.Data;
import java.math.BigDecimal;

public class HourRankResponse {
    private Long id;
    private String name;
    private BigDecimal value;
    private int count;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }    public void setName(String name) { this.name = name; }
    public BigDecimal getValue() { return value; }    public void setValue(BigDecimal value) { this.value = value; }
    public int getCount() { return count; }    public void setCount(int count) { this.count = count; }
}
