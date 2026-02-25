package com.prography.backend.global.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    @Getter
    public static class ErrorBody {
        private final String code;
        private final String message;

        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}