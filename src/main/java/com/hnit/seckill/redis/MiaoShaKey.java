package com.hnit.seckill.redis;

public class MiaoShaKey extends BasePrefix {



    public MiaoShaKey(String prefix) {
        super(prefix);
    }
    public static KeyPrefix isGoodsOver = new MiaoShaKey("idGoodsOver");
    public static MiaoShaKey goodsStock = new MiaoShaKey("gs");


}
