package com.hnit.seckill.controller;

import com.hnit.seckill.vo.LoginVo;
import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.result.Result;
import com.hnit.seckill.service.MiaoShaUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private MiaoShaUserService userService;
    @RequestMapping("to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Validated LoginVo loginVo){
        log.info(loginVo.toString());
//        //进行参数校验
//        if (loginVo == null){
//            return Result.success(false);
//        }
//        String mobile = loginVo.getMobile();
//        String password = loginVo.getPassword();
//        if(mobile == null){
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if(password == null){
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
        CodeMsg cm = userService.login(response,loginVo);
        if(cm.getCode() == 0){
            return Result.success(cm.getMessage());
        }
        return Result.error(CodeMsg.PASSWORD_ERROR);
    }

}
