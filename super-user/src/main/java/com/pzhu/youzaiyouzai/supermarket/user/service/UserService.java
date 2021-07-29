package com.pzhu.youzaiyouzai.supermarket.user.service;

import com.pzhu.youzaiyouzai.supermarket.user.pojo.Cart;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.PointGoods;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.User;

import java.util.List;

/**
 * @author QYstart
 * @date 2021/6/22
 */
public interface UserService {

    /**
     * 进行登录，返回登录成功的用户
     * @param user
     * @return
     */
    User doLogin(User user);

    /**
     * 获得积分兑换商品
     * @return
     */
    List<PointGoods> getAllPointGoods();

    /**
     * 下订单购买积分商品
     * @param phoneNum
     * @param goodIds
     * @return
     */
    List<Cart> buyGood(String phoneNum, Integer[] goodIds);

    /**
     * 添加到购物车
     * @param phoneNum
     * @param goodId
     * @return
     */
    Cart addCart(String phoneNum, Integer goodId);

    /**
     * 获得购物车信息
     * @param phoneNum
     * @return
     */
    List<Cart> getCarts(String phoneNum);

    /**
     * 获取用户最新信息
     * @param phoneNum
     * @return
     */
    User getUser(String phoneNum);

    /**
     * 删除
     * @param id
     * @return
     */
    Cart deleteCart(Integer id);

    /**
     * 注册用户
     * @param user
     * @return
     */
    User registryUser(User user);
}
