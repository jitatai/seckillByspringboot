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
        //对象缓存
        MiaoShaUser user = redisService.get(MiaoShaUserKey.getById, "" + id, MiaoShaUser.class);
        if (user != null) {
            return user;
        }
        //取数据库
        user = userDao.getById(id);
        //再存入缓存
        if (user != null) {
            redisService.set(MiaoShaUserKey.getById, "" + id, user);
        }else{
            redisService.set(MiaoShaUserKey.getById.setGetByIdExpire(60), "" + id, "null");
        }
        return user;
    }

    /**
     * 典型缓存同步场景：更新密码
     */
    public boolean updatePassword(String token, long id, String formPass) {
        //取user
        MiaoShaUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoShaUser toBeUpdate = new MiaoShaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.fromPass2DBPass(formPass, user.getSalt()));
        userDao.update(toBeUpdate);
        //更新缓存：先删除再插入
        redisService.delete(MiaoShaUserKey.getById, ""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoShaUserKey.token, token, user);
        return true;
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
        String calcPass = MD5Util.fromPass2DBPass(loginVo.getPassword(), salt);
        if(!calcPass.equals(password)){
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
//        System.out.println(user);
        //添加cookie
        addCookie(response, user,token);
        return user;
    }
}
