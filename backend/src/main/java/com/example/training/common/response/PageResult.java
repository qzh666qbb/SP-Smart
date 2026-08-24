package com.example.training.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        long pages = (total + pageSize - 1) / pageSize;
        return PageResult.<T>builder()
                .list(records)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(pages)
                .build();
    }
}
