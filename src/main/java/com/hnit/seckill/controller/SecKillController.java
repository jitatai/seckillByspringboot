package com.hnit.seckill.controller;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.MiaoshaOrder;
import com.hnit.seckill.domain.OrderInfo;
import com.hnit.seckill.rabbitmq.MQSender;
import com.hnit.seckill.rabbitmq.SeckillMessage;
import com.hnit.seckill.redis.AccessKey;
import com.hnit.seckill.redis.MiaoShaKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.result.Result;
import com.hnit.seckill.service.GoodsService;
import com.hnit.seckill.service.MiaoShaService;
import com.hnit.seckill.service.OrderService;
import com.hnit.seckill.util.MD5Util;
import com.hnit.seckill.util.UUIDUtil;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
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

    @RequestMapping(value = "/getPath",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getPath(MiaoShaUser user, Model model, Long goodsId,
                                  @RequestParam(value = "verifyCode",defaultValue = "0")Integer verifyCode, HttpServletRequest request){
        if (user == null || user.getId() == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        String uri = request.getRequestURI();
        String key = uri + "_" + user.getId();
        Integer count = redisService.get(AccessKey.access, key, Integer.class);
        if(count == null){
            redisService.set(AccessKey.access,key,1);
        }else if (count < 5){
            redisService.incr(AccessKey.access,key);
        }else {
            return Result.error(CodeMsg.REQUEST_LIMIT_REACHED);
        }
        boolean check = miaoShaService.checkVerifyCode(user,goodsId,verifyCode);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String str = createCheckPath(user, goodsId);
        return Result.success(str);
    }

    private String createCheckPath(MiaoShaUser user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoShaKey.getMiaoShaPath,""+user.getId()+"_"+goodsId,str);
        return str;
    }

    /**
     * GET POST
     * 1、GET幂等,服务端获取数据，无论调用多少次结果都一样
     * 2、POST，向服务端提交数据，不是幂等
     * <p>
     * 将同步下单改为异步下单
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/do_seckill/{path}",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doSeckill(MiaoShaUser user, Model model,@PathVariable("path")String path, Long goodsId){
        if (user == null || user.getId() == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = checkMiaoShaPath(user, path, goodsId);

        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

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

    private boolean checkMiaoShaPath(MiaoShaUser user,String path, Long goodsId) {
        if (path == null || goodsId == null){
            return false;
        }
        String oldPath = redisService.get(MiaoShaKey.getMiaoShaPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(oldPath);
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

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> verifyCode(MiaoShaUser user, @RequestParam("goodsId") long goodsId, HttpServletResponse response) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoShaService.createVerifyCode(user,goodsId);
        try {
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }

    }

}
