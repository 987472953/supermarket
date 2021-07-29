package com.pzhu.youzaiyouzai.supermarket.admin.mapper;


import com.pzhu.youzaiyouzai.supermarket.admin.pojo.Cart;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author QYstart
 * @date 2021/5/24
 */
public interface CartMapper extends Mapper<Cart> {

    List<Cart> selectUserCart(String phoneNum);
}
