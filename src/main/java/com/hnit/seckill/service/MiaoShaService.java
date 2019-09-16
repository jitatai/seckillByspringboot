package com.hnit.seckill.service;

import com.hnit.seckill.domain.Goods;
import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.MiaoshaOrder;
import com.hnit.seckill.domain.OrderInfo;
import com.hnit.seckill.redis.MiaoShaKey;
import com.hnit.seckill.redis.OrderKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
@Transactional
public class MiaoShaService {
    @Autowired
    GoodsService goodsService;
    @Autowired
    OrderService orderService;
    @Autowired
    RedisService redisService;

    public long getSeckillResult(Long userId, long goodsId) {
        MiaoshaOrder order = getMiaoShaOrderByUserIdAndGoodsId(userId, goodsId);
        if (order != null){
            return order.getOrderId();
        }else{
            boolean isOver = getGoodsOver(goodsId);
            if(isOver) {
                setGoodsOver(goodsId);
                return -1;
            }else {
                return 0;
            }
        }
    }

    public MiaoshaOrder getMiaoShaOrderByUserIdAndGoodsId(Long id, Long goodsId) {
        return redisService.get(OrderKey.getOrderByUidAndGid,""+id +"_" + goodsId,MiaoshaOrder.class);
//        return orderService.getMiaoShaOrderByUserIdAndGoodsId(id,goodsId);
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoShaKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoShaKey.isGoodsOver, ""+goodsId);
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

    public BufferedImage createVerifyCode(MiaoShaUser user, long goodsId) {
        if (user == null || goodsId <= 0){
            return null;
        }
        int width = 80;
        int height = 32;
        //画图
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0,0,width,height);
        g.setColor(Color.BLACK);
        g.drawRect(0,0,width-1,height-1);

        Random rdm = new Random();
        for (int i = 0;i<50;i++){
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x,y,0,0);
        }

        //生成验证码
        String verifyCode = generaterVerifyCode(rdm);
        g.setColor(new Color(0,100,0));
        g.setFont(new Font("Condara",Font.BOLD,24));
        g.drawString(verifyCode,8,24);
        g.dispose();
        //把验证码计算出来 存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoShaKey.verifyCode,""+user.getId()+"_"+goodsId,rnd);
        //将图片返回
        return image;
    }

    private int calc(String verifyCode) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(verifyCode);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private static final char[] ops = {'+','-','*'};
    private String generaterVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char ops1 = ops[rdm.nextInt(3)];
        char ops2 = ops[rdm.nextInt(3)];
        return ""+num1 +ops1+num2+ops2+num3;
    }

    public boolean checkVerifyCode(MiaoShaUser user, Long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0){
            return false;
        }
        Integer oldCode = redisService.get(MiaoShaKey.verifyCode, "" + user.getId() + "_" + goodsId, Integer.class);
        if(oldCode  == null || oldCode - verifyCode != 0){
            return false;
        }
        return true;
    }
}
