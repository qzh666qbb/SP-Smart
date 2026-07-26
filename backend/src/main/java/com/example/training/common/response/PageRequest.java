package com.example.training.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    @Builder.Default
    private int pageNum = 1;
    
    @Builder.Default
    private int pageSize = 10;
    
    private String sortField;
    
    @Builder.Default
    private String sortOrder = "desc";
}
