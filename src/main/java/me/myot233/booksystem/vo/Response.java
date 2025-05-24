package me.myot233.booksystem.vo;

import lombok.Getter;

/**
 * 响应类
 */
public record Response<T>(boolean success, T data) {

    public static <T> Response<T> ok(T entity) {
        return new Response<T>(true, entity);
    }

}
