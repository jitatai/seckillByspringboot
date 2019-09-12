package com.hnit.seckill.redis;

public class BasePrefix implements KeyPrefix{
    private int expireSeconeds; //过期时间
    private String preFix; //键的前缀

    public BasePrefix(int expireSeconeds, String preFix) {
        this.expireSeconeds = expireSeconeds;
        this.preFix = preFix;
    }

    public BasePrefix(String prefix) {//0代表永不过期
        this(0, prefix);
    }

    @Override
    public int expireSeconds() {//默认0代表永不过期
        return expireSeconeds;
    }

    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className+":" + preFix;
    }
}
