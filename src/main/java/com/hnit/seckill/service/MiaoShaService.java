package com.hnit.seckill.service;

import com.hnit.seckill.domain.Goods;
import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.MiaoshaOrder;
import com.hnit.seckill.domain.OrderInfo;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MiaoShaService {
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    public MiaoshaOrder getMiaoShaOrderByUserIdAndGoodsId(Long id, Long goodsId) {
        return orderService.getMiaoShaOrderByUserIdAndGoodsId(id,goodsId);
    }

    public OrderInfo miaoSha(MiaoShaUser user, GoodsVo goodsVo) {
        //先减库存 在下订单 写入订单表
        //减库存
        boolean success = goodsService.reduceStock(goodsVo);
        if (success){
            //下订单 写入秒杀订单
            return orderService.createOrder(user, goodsVo);
        }
        return null;
    }
}
