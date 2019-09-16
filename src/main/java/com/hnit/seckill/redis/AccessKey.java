package com.hnit.seckill.redis;

public class AccessKey extends BasePrefix {


    public AccessKey(int expireSeconeds, String preFix) {
        super(expireSeconeds, preFix);
    }

    public AccessKey(String prefix) {
        super(prefix);
    }

    public static AccessKey access = new AccessKey(5,"access");
}
