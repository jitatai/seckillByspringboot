package com.hnit.seckill.redis;

public class MiaoShaKey extends BasePrefix {

    public static MiaoShaKey verifyCode = new MiaoShaKey(300,"verifyCode");

    public MiaoShaKey(int expireSeconeds, String preFix) {
        super(expireSeconeds, preFix);
    }

    public MiaoShaKey(String prefix) {
        super(prefix);
    }
    public static MiaoShaKey isGoodsOver = new MiaoShaKey("idGoodsOver");
    public static MiaoShaKey goodsStock = new MiaoShaKey("gs");
    public static MiaoShaKey getMiaoShaPath = new MiaoShaKey(60,"path");


}
