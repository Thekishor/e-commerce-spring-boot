package com.product_service.interceptor;

public class UserContext {

    private UserContext() {
        /* This utility class should not be instantiated */
    }

    private static final ThreadLocal<String> userHolder = new ThreadLocal<>();

    public static void setUserId(String userId) {
        userHolder.set(userId);
    }

    public static String getUserId() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}
