package com.hnit.seckill.service;

import com.hnit.seckill.dao.GoodsDao;
import com.hnit.seckill.domain.Goods;
import com.hnit.seckill.vo.GoodsDetailVo;
import com.hnit.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class GoodsService {

    @Resource
    private GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsDetailVo GoodsVoDetail(Long goodsId) {
        GoodsVo goodsVo = goodsDao.getGoodsVo(goodsId);
        long endAt = goodsVo.getEndDate().getTime();
        long startAt = goodsVo.getStartDate().getTime();
        long now = System.currentTimeMillis();
        GoodsDetailVo detailVo = new GoodsDetailVo();
        detailVo.setGoods(goodsVo);
        if(now < startAt){ // 秒杀还未开始
            detailVo.setMiaoshaStatus(0);
            detailVo.setRemainSeconds((int)(startAt - now )/ 1000);
        }else if (now > endAt){
            detailVo.setMiaoshaStatus(2);
            detailVo.setRemainSeconds(-1);
        }else {
            detailVo.setMiaoshaStatus(1);
            detailVo.setRemainSeconds(0);
        }
        return detailVo;
    }

    public GoodsVo getGoodsVoByGoodsId(Long goodsId) {
        return goodsDao.getGoodsVo(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {
        if (goodsVo == null){
            return false;
        }
        int leastVersion = goodsDao.getVersionByGoodsId(goodsVo.getId());
        if (leastVersion != goodsVo.getVersion()){
            return false;
        }
        int ret = goodsDao.reduceStock(goodsVo.getId());
        return ret > 0;
    }
}
