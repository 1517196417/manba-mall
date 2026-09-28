package com.abcmall.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 分页结果包装
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> list = new ArrayList<>();
    private long total;
    private long page;
    private long size;
    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, long page, long size) {
        this.list = list == null ? new ArrayList<>() : list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size <= 0 ? 0 : (total + size - 1) / size;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 分页对象转换（实体 -> VO） */
    public static <E, T> PageResult<T> of(IPage<E> page, Function<E, T> mapper) {
        List<T> list = new ArrayList<>();
        for (E e : page.getRecords()) {
            list.add(mapper.apply(e));
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(new ArrayList<>(), 0, page, size);
    }
}
