package com.hnit.seckill.controller;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.domain.User;
import com.hnit.seckill.rabbitmq.MQConfig;
import com.hnit.seckill.rabbitmq.MQSender;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.redis.UserKey;
import com.hnit.seckill.result.Result;
import com.hnit.seckill.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Queue;

@Controller
public class DemoController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;
    @Autowired
    MQSender mqSender;

    @RequestMapping("hello")
    public String thymeleaf(Model model){
        model.addAttribute("name","jiatai");
        return "hello";
    }

    @RequestMapping("user")
    @ResponseBody
    public Result<User> getUser(){
        User user = userService.getUser(1);
        return Result.success(user);
    }

    @RequestMapping("set")
    @ResponseBody
    public Result<User> setUser(){
        User user = userService.getUser(1);
        boolean flag = redisService.set(UserKey.getById, "1", user);
        System.out.println(flag);
        User u = redisService.get(UserKey.getById, "1", User.class);
        return Result.success(u);
    }

    @RequestMapping("/userinfo")
    @ResponseBody
    public Result<MiaoShaUser> userInfo(MiaoShaUser user){
        log.info(user.toString());
        return Result.success(user);
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        mqSender.send("nihao,jiatai");
        return Result.success("nihao");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTopic(){
        mqSender.sendTopic("nihao,jiatai");
        return Result.success("nihao");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout(){
        mqSender.sendFanout("nihao,jiatai");
        return Result.success("nihao");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader(){
        mqSender.sendHeader("nihao,jiatai");
        return Result.success("nihao");
    }
}
