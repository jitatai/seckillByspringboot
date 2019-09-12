package com.hnit.seckill.dao;

import com.hnit.seckill.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author jiatai
 */
@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")
    public User getUser(int id);
}
