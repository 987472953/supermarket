package com.pzhu.youzaiyouzai.supermarket.user.mapper;

import com.pzhu.youzaiyouzai.supermarket.user.pojo.PointGoods;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author QYstart
 * @date 2021/5/24
 */
public interface PointGoodsMapper extends Mapper<PointGoods> {
    /**
     * 根据商品号查询 商品
     * @param goodId
     * @return
     */
    PointGoods selectPointGoods(Integer goodId);
}
