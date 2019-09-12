package com.hnit.seckill.config;

import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.service.MiaoShaUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private MiaoShaUserService userService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class c = methodParameter.getParameterType();
        return c == MiaoShaUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
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
