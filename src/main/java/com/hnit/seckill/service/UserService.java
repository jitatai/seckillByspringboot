package com.hnit.seckill.service;

import com.hnit.seckill.dao.UserDao;
import com.hnit.seckill.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class UserService {
    @Resource
    private UserDao userDao;

    public User getUser(int id){
        return userDao.getUser(id);
    }
}
