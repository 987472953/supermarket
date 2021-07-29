package com.pzhu.youzaiyouzai.supermarket.user.mapper;

import com.pzhu.youzaiyouzai.supermarket.user.pojo.Cart;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author QYstart
 * @date 2021/5/24
 */
public interface CartMapper extends Mapper<Cart> {

    List<Cart> selectCarts(String phoneNum);
}
