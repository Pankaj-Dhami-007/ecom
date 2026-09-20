package com.dhami.ecom.common.dto;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Meta meta
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null,
                Meta.now()
        );
    }

    public static <T> ApiResponse<T> failure(
            int code,
            String message
    ) {
        return new ApiResponse<>(
                false,
                null,
                new ApiError(code, message),
                Meta.now()
        );
    }
}
