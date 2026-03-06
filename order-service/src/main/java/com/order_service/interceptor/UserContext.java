package com.order_service.interceptor;


import com.order_service.dto.UserInfo;

public class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<UserInfo> userInfo = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        UserContext.userInfo.set(userInfo);
    }

    public static String getUserId() {
        return userInfo.get().userId();
    }

    public static String getUserEmail() {
        return userInfo.get().email();
    }

    public static String getUserRole() {
        return userInfo.get().roles();
    }

    public static void clear() {
        userInfo.remove();
    }
}
