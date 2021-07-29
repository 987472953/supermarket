package com.pzhu.youzaiyouzai.supermarket.admin.service.impl;

import com.pzhu.youzaiyouzai.supermarket.admin.mapper.PointManagementMapper;
import com.pzhu.youzaiyouzai.supermarket.admin.mapper.UserMapper;
import com.pzhu.youzaiyouzai.supermarket.admin.pojo.PointManagement;
import com.pzhu.youzaiyouzai.supermarket.admin.pojo.User;
import com.pzhu.youzaiyouzai.supermarket.admin.service.BuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author QYstart
 * @date 2021/6/23
 */
@Service
public class BuyServiceImpl implements BuyService {

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private PointManagementMapper pointManagementMapper;

    @Override
    public User queryUserInfo(String phoneNum) {

        User user = new User();
        user.setPhoneNum(phoneNum);
        user = userMapper.selectOne(user);
        if (user != null) {
            user.setPassword("");
        }
        return user;
    }

    @Override
    public Map<String, Object> updateUserAfterBuy(String phoneNum, Double paymentAccount) {

        HashMap<String, Object> map = new HashMap<>();

        Example example = new Example(PointManagement.class);
        example.createCriteria().andEqualTo("used", 1);
        PointManagement pointManagement = pointManagementMapper.selectOneByExample(example);
        int point = getPointByPointType(paymentAccount, pointManagement);

        if (point != 0) {
            int points = updateUserPoints(phoneNum, point);
            map.put("freePoints", points);
        }
        map.put("point", point);
        return map;
    }

    private int getPointByPointType(Double paymentAccount, PointManagement pointManagement) {
        int point = 0;
        if (pointManagement.getPointType() == 1) {
            //百分比积分
            point = (int) Math.floor(paymentAccount * pointManagement.getRatio());
        } else if (pointManagement.getPointType() == 2) {
            //满多少积一分
            point = (int) Math.floor(paymentAccount / pointManagement.getRatio());
        }
        // TODO 在这儿修改积分规则
        return point;
    }

    private int updateUserPoints(String phoneNum, int point) {
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("phoneNum", phoneNum);
        User user = userMapper.selectOneByExample(example);

        user.setPoints(user.getPoints() + point);
        user.setLastPoint(point);
        user.setLastUseTime(new Date());
        int i = userMapper.updateByPrimaryKey(user);
        if (i < 0) {
            return -1;
        }
        return user.getPoints();
    }

}
