package com.hnit.seckill.rabbitmq;


import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.MiaoshaOrder;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.service.GoodsService;
import com.hnit.seckill.service.MiaoShaService;
import com.hnit.seckill.service.OrderService;
import com.hnit.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jiangyunxiong on 2018/5/29.
 */
@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoShaService seckillService;

    @RabbitListener(queues=MQConfig.SECKILL_QUEUE)
    public void receiveSeckill(String message){
        log.info("receive message:"+message);
        SeckillMessage m = RedisService.stringToBean(message, SeckillMessage.class);
        MiaoShaUser user = m.getUser();
        long goodsId = m.getGoodsId();

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goodsVo.getStockCount();
        if(stock < 0){
            return;
        }

        //判断重复秒杀
        MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if(order != null) {
            return;
        }

        //减库存 下订单 写入秒杀订单
        seckillService.miaoSha(user, goodsVo);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info(" topic  queue1 message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info(" topic  queue2 message:" + message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE1)

    public void receiveHeader(byte[] msg){
        log.info(new String(msg));
    }

}
