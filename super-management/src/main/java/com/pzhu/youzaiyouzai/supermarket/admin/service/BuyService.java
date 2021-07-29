package com.pzhu.youzaiyouzai.supermarket.admin.service;

import com.pzhu.youzaiyouzai.supermarket.admin.pojo.User;

import java.util.Map;

/**
 * @author QYstart
 * @date 2021/6/23
 */
public interface BuyService {
    /**
     * 根据手机号查询用户
     * @param phoneNum
     * @return
     */
    User queryUserInfo(String phoneNum);

    /**
     * 根据用户手机号与支付金额增加积分
     * @param phoneNum
     * @param paymentAccount
     * @return
     */
    Map<String, Object> updateUserAfterBuy(String phoneNum, Double paymentAccount);
}
