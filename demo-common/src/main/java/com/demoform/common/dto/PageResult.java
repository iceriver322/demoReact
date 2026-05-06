package com.demoform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * 分页查询结果
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private long total;
    private int page;
    private int size;
    private List<T> records;

    public static <T> PageResult<T> of(long total, int page, int size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
