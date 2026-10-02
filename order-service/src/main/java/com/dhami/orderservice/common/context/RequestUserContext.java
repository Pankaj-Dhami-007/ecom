package com.dhami.orderservice.common.context;

import com.dhami.orderservice.common.exception.UnauthenticatedException;

public final class RequestUserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private RequestUserContext() {}

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new UnauthenticatedException("Authenticated user is required");
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
