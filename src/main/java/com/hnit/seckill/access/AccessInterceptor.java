package com.hnit.seckill.access;

import com.alibaba.fastjson.JSON;
import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.redis.AccessKey;
import com.hnit.seckill.redis.MiaoShaKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.result.Result;
import com.hnit.seckill.service.MiaoShaUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    MiaoShaUserService userService;
    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MiaoShaUser user = getUser(request, response);
        UserContext.setUser(user);
        if (handler instanceof HandlerMethod){

            HandlerMethod mh = (HandlerMethod) handler;
            AceessLimit accessLimit = mh.getMethodAnnotation(AceessLimit.class);
            if (accessLimit == null){
                return true;
            }
            int maxCount = accessLimit.maxCount();
            int seconds = accessLimit.seconds();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin){
                if(user == null){
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }else {
                //do nothing
            }
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if(count == null){
                redisService.set(ak,key,1);
            }else if (count < maxCount){
                redisService.incr(ak,key);
            }else {
                render(response,CodeMsg.REQUEST_LIMIT_REACHED);
                return false;
            }
        }

        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoShaUser getUser(HttpServletRequest request,HttpServletResponse response){
        String paramToken = request.getParameter(MiaoShaUserService.Cookie_Name_TOKEN);
        String cookieToken = getCookiesValue(request, MiaoShaUserService.Cookie_Name_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getUserByToken(response,token);
    }

    private String getCookiesValue(HttpServletRequest request,String key){
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length == 0){
            return null;
        }
        for(Cookie c:cookies){
            if(c.getName().equals(key)){
                return c.getValue();
            }
        }
        return null;
    }
}
