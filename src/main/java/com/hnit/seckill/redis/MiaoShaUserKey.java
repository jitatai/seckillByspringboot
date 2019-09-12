package com.hnit.seckill.redis;

public class MiaoShaUserKey extends BasePrefix {
    public static final int TOKEN_EXPIRE = 3600 * 24;
    public MiaoShaUserKey(int expireSeconeds, String preFix) {
        super(expireSeconeds, preFix);
    }

    public MiaoShaUserKey(String prefix) {
        super(prefix);
    }

    public static MiaoShaUserKey token = new MiaoShaUserKey(TOKEN_EXPIRE,"tk");
}
