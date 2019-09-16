package com.hnit.seckill.rabbitmq;

import com.hnit.seckill.domain.MiaoShaUser;

/**
 * Created by jiangyunxiong on 2018/5/29.
 *
 * 消息体
 */
public class SeckillMessage {

    private MiaoShaUser user;
    private long goodsId;

    public MiaoShaUser getUser() {
        return user;
    }

    public void setUser(MiaoShaUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
