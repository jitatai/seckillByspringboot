package com.hnit.seckill.service;

import com.hnit.seckill.dao.OrderDao;
import com.hnit.seckill.domain.*;
import com.hnit.seckill.redis.OrderKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class OrderService {
    @Resource
    OrderDao orderDao;
    @Autowired
    RedisService redisService;
    /**
     * 因为要同时分别在订单详情表和秒杀订单表都新增一条数据，所以要保证两个操作是一个事物
     */
    @Transactional
    public OrderInfo createOrder(MiaoShaUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getGoodsPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);

        MiaoshaOrder seckillOrder = new MiaoshaOrder();
        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getId());
        orderDao.insertSeckillOrder(seckillOrder);

        redisService.set(OrderKey.getOrderByUidAndGid,""+user.getId() +"_" + goods.getId(),seckillOrder);
        return orderInfo;
    }

    public MiaoshaOrder getMiaoShaOrderByUserIdAndGoodsId(Long id, Long goodsId) {
        return orderDao.getOrderByUserIdGoodsId(id,goodsId);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
}
