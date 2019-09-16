package com.hnit.seckill.controller;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.MiaoshaOrder;
import com.hnit.seckill.domain.OrderInfo;
import com.hnit.seckill.rabbitmq.MQSender;
import com.hnit.seckill.rabbitmq.SeckillMessage;
import com.hnit.seckill.redis.MiaoShaKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.result.Result;
import com.hnit.seckill.service.GoodsService;
import com.hnit.seckill.service.MiaoShaService;
import com.hnit.seckill.service.OrderService;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("miaosha")
public class SecKillController implements InitializingBean {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MiaoShaService miaoShaService;
    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    //做标记，判断该商品是否被处理过了
    private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();


    @RequestMapping("/do_miaosha")
    public String doMiaosha(MiaoShaUser user,Model model,Long goodsId){
        if (user == null || user.getId() == null){
            return "login";
        }
        System.out.println("miaosha");
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0){
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER);
            return "miaosha_fail";
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = miaoShaService.getMiaoShaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if (order != null){
            model.addAttribute("errmsg",CodeMsg.REPEATE_MIAOSHA.getMessage());
            return "miaosha_fail";
        }
        // 进行秒杀 先减库存 再下单 写入订单表
        OrderInfo orderInfo = miaoShaService.miaoSha(user,goodsVo);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goodsVo);
        return "order_detail";
    }

    @RequestMapping(value = "/do_seckill",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doSeckill(MiaoShaUser user, Model model, Long goodsId){
        if (user == null || user.getId() == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user",user);

        //内存标记 减少redis访问
        Boolean isOver = localOverMap.get(goodsId);
        if(isOver){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        Long stock = redisService.decr(MiaoShaKey.goodsStock, "" + goodsId);
        if(stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了 禁止重复秒杀
        MiaoshaOrder order = miaoShaService.getMiaoShaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if (order != null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //如果秒杀到了  入队
        SeckillMessage sm = new SeckillMessage();
        sm.setUser(user);
        sm.setGoodsId(goodsId);
        mqSender.sendSeckillMessage(sm);

        return Result.success(0);
        /*GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() <= 0){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = miaoShaService.getMiaoShaOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if (order != null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        // 进行秒杀 先减库存 再下单 写入订单表
        OrderInfo orderInfo = miaoShaService.miaoSha(user,goodsVo);
        return Result.success(orderInfo);*/
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null){
            return;
        }
        for (GoodsVo goods: goodsList) {
            redisService.set(MiaoShaKey.goodsStock,"" + goods.getId(),goods.getStockCount());
            //初始化商品都是没有处理过的
            localOverMap.put(goods.getId(), false);
        }
    }


    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, MiaoShaUser user,
                                      @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long orderId = miaoShaService.getSeckillResult(user.getId(), goodsId);
        return Result.success(orderId);
    }
}
