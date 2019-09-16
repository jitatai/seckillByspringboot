package com.hnit.seckill.controller;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.User;
import com.hnit.seckill.redis.GoodsKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.result.Result;
import com.hnit.seckill.service.GoodsService;
import com.hnit.seckill.service.MiaoShaUserService;
import com.hnit.seckill.vo.GoodsDetailVo;
import com.hnit.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;

import javax.servlet.http.HttpServletRequest;
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
    @Autowired
    private RedisService redisService;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;


    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String goodList(MiaoShaUser user, Model model, HttpServletRequest request,HttpServletResponse response){
        model.addAttribute("user",user);
        String html = redisService.get(GoodsKey.goodsList, "", String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("goodsList",goodsService.listGoodsVo());
        if(user == null || user.getId() == null){
            return "login";
        }

        html = getRedisHtml(model, request, response,"","goods_list");
        return html;
    }



    @RequestMapping(value = "/detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String goodsVoDetail(MiaoShaUser user,
                                Model model,@PathVariable("goodsId") Long goodsId, HttpServletRequest request,HttpServletResponse response){
        model.addAttribute("user",user);
        String html = redisService.get(GoodsKey.goodsDetail, "" + goodsId, String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("goodsDetail",goodsService.GoodsVoDetail(goodsId));
        model.addAttribute("goods",goodsService.GoodsVoDetail(goodsId).getGoods());

        html = getRedisHtml(model,request,response,""+goodsId,"goods_detail");
        //return "goods_detail";
        return html;
    }


    /**
     * 商品详情页面
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoShaUser user, @PathVariable("goodsId") long goodsId) {

        //根据id查询商品详情
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startTime = goods.getStartDate().getTime();
        long endTime = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;

        if (now < startTime) {//秒杀还没开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int) ((startTime - now) / 1000);
        } else if (now > endTime) {//秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(seckillStatus);

        return Result.success(vo);
    }

    private String getRedisHtml(Model model, HttpServletRequest request, HttpServletResponse response,String key,String htmlName) {
        String html;//手动渲染
        WebContext cxt = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process(htmlName, cxt);
        if (!StringUtils.isEmpty(html)){
            if (htmlName.equals("goods_detail")){
                redisService.set(GoodsKey.goodsDetail,key,html);
            }else if (htmlName.equals("goods_list")){
                redisService.set(GoodsKey.goodsList, key, html);
            }

        }
        return html;
    }

}
