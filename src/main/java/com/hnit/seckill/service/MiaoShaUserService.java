package com.hnit.seckill.service;

import com.hnit.seckill.dao.MiaoShaUserDao;
import com.hnit.seckill.vo.LoginVo;
import com.hnit.seckill.domain.MiaoShaUser;
import com.hnit.seckill.exception.GlobalException;
import com.hnit.seckill.redis.MiaoShaUserKey;
import com.hnit.seckill.redis.RedisService;
import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.util.MD5Util;
import com.hnit.seckill.util.UUIDUtil;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoShaUserService {
    @Autowired
    private MiaoShaUserDao userDao;
    @Autowired
    private RedisService redisService;

    public static final String Cookie_Name_TOKEN = "token";

    private MiaoShaUser getById(long id){
        return userDao.getById(id);
    }

    public CodeMsg login(HttpServletResponse response,LoginVo loginVo) {
//        if(!ValidatorUtil.isMobile(loginVo.getMobile())){
//            return CodeMsg.MOBILE_ERROR;
//        }
        MiaoShaUser user = userDao.getById(Long.parseLong(loginVo.getMobile()));
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String password = user.getPassword();
        String salt = user.getSalt();
        String calepass = MD5Util.fromPass2DBPass(loginVo.getPassword(), salt);
        if(!calepass.equals(password)){
            return CodeMsg.PASSWORD_ERROR;
        }
        //添加cookie
        String token = UUIDUtil.uuid();
        addCookie(response, user,token);
        CodeMsg.SUCCESS.setMessage(token);
        return CodeMsg.SUCCESS;
    }

    private void addCookie(HttpServletResponse response, MiaoShaUser user,String token) {
        redisService.set(MiaoShaUserKey.token,token,user);
        Cookie cookie = new Cookie(Cookie_Name_TOKEN,token);
        cookie.setMaxAge(MiaoShaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public MiaoShaUser getUserByToken(HttpServletResponse response,String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        MiaoShaUser user = redisService.get(MiaoShaUserKey.token, token, MiaoShaUser.class);
        //添加cookie
        addCookie(response, user,token);
        return user;
    }
}
