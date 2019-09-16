package com.hnit.seckill.redis;

public class GoodsKey extends BasePrefix {
    public static final int TOKEN_EXPIRE = 3600 * 24;


    public GoodsKey(int expireSeconeds, String preFix) {
        super(expireSeconeds, preFix);
    }

    public GoodsKey(String prefix) {
        super(prefix);
    }

    public static GoodsKey goodsList = new GoodsKey(60,"gl");
    public static GoodsKey goodsDetail = new GoodsKey(60,"gd");
}
