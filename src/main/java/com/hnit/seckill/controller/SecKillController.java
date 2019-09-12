package com.hnit.seckill.controller;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.MiaoshaOrder;
import com.hnit.seckill.domain.OrderInfo;
import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.service.GoodsService;
import com.hnit.seckill.service.MiaoShaService;
import com.hnit.seckill.service.OrderService;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("miaosha")
public class SecKillController{
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MiaoShaService miaoShaService;
    @RequestMapping("/do_miaosha")
    public String GoodsVoDetail(MiaoShaUser user,Model model,Long goodsId){
        if (user == null || user.getId() == null){
            return "login";
        }
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



}
