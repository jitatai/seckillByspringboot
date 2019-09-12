package com.hnit.seckill.dao;

import com.hnit.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {
    @Select("select g.*, sg.stock_count, sg.start_date, sg.end_date, sg.seckill_price miaoshaPrice, sg.version from sk_goods_seckill sg left join sk_goods g on sg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();
    @Select("select g.*, sg.stock_count, sg.start_date, sg.end_date, sg.seckill_price miaoshaPrice, sg.version from sk_goods_seckill sg left join sk_goods g on sg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVo(@Param("goodsId") Long goodsId);
    //stock_count > 0 和 版本号实现乐观锁 防止超卖
    @Update("update sk_goods_seckill set stock_count = stock_count - 1 where goods_id = #{goodsId}")
    int reduceStock(Long id);
}
