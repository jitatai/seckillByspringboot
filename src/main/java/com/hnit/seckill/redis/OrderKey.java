package com.hnit.seckill.redis;

public class OrderKey extends BasePrefix  {

    public OrderKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getOrderByUidAndGid = new OrderKey("order");
}
