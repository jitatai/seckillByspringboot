package com.hnit.seckill.controller;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.service.GoodsService;
import com.hnit.seckill.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author jiatai
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private MiaoShaUserService userService;
    @Autowired
    private GoodsService goodsService;
    @RequestMapping("/to_list")
    public String goodList(MiaoShaUser user,Model model){
        model.addAttribute("user",user);
        if(user == null || user.getId() == null){
            return "login";
        }
        model.addAttribute("goodsList",goodsService.listGoodsVo());
        return "goods_list";
    }

    @RequestMapping("/detail/{goodsId}")
    public String goodsVoDetail(MiaoShaUser user,
                                Model model,@PathVariable("goodsId") Long goodsId){
        model.addAttribute("user",user);
        model.addAttribute("goodsDetail",goodsService.GoodsVoDetail(goodsId));
        model.addAttribute("goods",goodsService.GoodsVoDetail(goodsId).getGoods());
        return "goods_detail";
    }


}
