package com.hnit.seckill.access;

import com.hnit.seckill.domain.MiaoShaUser;

public class UserContext {
    private static ThreadLocal<MiaoShaUser> userHandler = new ThreadLocal<>();

    public static void setUser(MiaoShaUser user) {
        userHandler.set(user);
    }

    public static MiaoShaUser getUser(){
        return userHandler.get();
    }
}
