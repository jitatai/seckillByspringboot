package com.hnit.seckill.dao;

import com.hnit.seckill.domain.MiaoShaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MiaoShaUserDao {
    @Select("select * from miaosha_user where id = #{id}")
    MiaoShaUser getById(@Param("id") long id);

    @Update("update set password = #{password} where id = #{id}")
    void update(MiaoShaUser toBeUpdate);
}
