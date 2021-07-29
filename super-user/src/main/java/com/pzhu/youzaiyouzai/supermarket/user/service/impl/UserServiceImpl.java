package com.pzhu.youzaiyouzai.supermarket.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.pzhu.youzaiyouzai.supermarket.user.common.Constant;
import com.pzhu.youzaiyouzai.supermarket.user.mapper.CartMapper;
import com.pzhu.youzaiyouzai.supermarket.user.mapper.PointGoodsMapper;
import com.pzhu.youzaiyouzai.supermarket.user.mapper.UserMapper;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.Cart;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.PointGoods;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.User;
import com.pzhu.youzaiyouzai.supermarket.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointGoodsMapper pointGoodsMapper;

    @Autowired
    private CartMapper cartMapper;

    @Override
    public User doLogin(User user) {
        User loginUser = new User();
        loginUser.setPhoneNum(user.getPhoneNum());
        loginUser = userMapper.selectOne(loginUser);

        if (user.getPassword().equals(loginUser.getPassword())) {
            loginUser.setPassword("");
            return loginUser;
        }
        return null;
    }

    @Override
    public List<PointGoods> getAllPointGoods() {
        List<PointGoods> pointGoods = pointGoodsMapper.selectAll();

        try {
            log.debug("service == getAllPointGoods redis存储");
            HashMap<String, String> map = new HashMap<>();
            for (PointGoods pointGood : pointGoods) {
                map.put(pointGood.getId() + "", JSON.toJSONString(pointGood));
            }
            redisTemplate.opsForHash().putAll("GOODS", map);

            log.debug("service == getAllPointGoods redis存储结束");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pointGoods;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Cart> buyGood(String phoneNum, Integer[] goodIds) {
        Integer freePoints;
        int sum = 0;//用来记录这次使用了多少积分
        List<Cart> badUpdateGoods = new ArrayList<>();
        try {
            String strPoint = (String) redisTemplate.opsForHash().get(Constant.USER_PREFIX + phoneNum + Constant.LOGIN_SUFFIX, "points");
            freePoints = Integer.valueOf(strPoint);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        for (Integer goodId : goodIds) {

            Cart cart = new Cart();
            cart.setUserPhone(phoneNum);
            cart.setGoodId(goodId);
            List<Cart> cartList = cartMapper.select(cart);
            Cart updateCart = cartList.get(0);
            for (Cart cart1 : cartList) {
                if (cart1.getState() == 0) {
                    updateCart = cart1;
                    break;
                }
            }

            Integer point = updateCart.getPoint();
            if (updateCart.getState() != 0 || freePoints - point < 0) {
                //不是刚加入购物车的状态（已支付或已使用），或者积分不够
                badUpdateGoods.add(cart);
                continue;
            }

            freePoints = freePoints - point;
            sum += point;
            updateCart.setState(1);
            cartMapper.updateByPrimaryKeySelective(updateCart);
        }
        try {
            redisTemplate.opsForHash().put(Constant.USER_PREFIX + phoneNum + Constant.LOGIN_SUFFIX, "points", freePoints + "");
        } catch (Exception e) {
            e.printStackTrace();
            //TODO 异常错误通知给消息队列
        }

        updateUserPoints(phoneNum, freePoints, sum);

        return badUpdateGoods;
    }

    private void updateUserPoints(String phoneNum, Integer freePoints, int sum) {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("phoneNum", phoneNum);
        User user = new User();
        user.setPoints(freePoints);
        user.setLastUseTime(new Date());
        user.setLastPoint(-sum);
        userMapper.updateByExampleSelective(user, example);
    }

    @Override
    public Cart addCart(String phoneNum, Integer goodId) {

        PointGoods pointGoods = pointGoodsMapper.selectByPrimaryKey(goodId);
        if (pointGoods == null) {
            //商品不存在
            return null;
        }

        Cart cart = new Cart();

        cart.setGoodId(goodId);
        cart.setPoint(pointGoods.getPrices());
        cart.setState(0);
        cart.setUserPhone(phoneNum);
        cart.setCreateTime(new Date());

        cartMapper.insertSelective(cart);

        return cart;
    }

    @Override
    public List<Cart> getCarts(String phoneNum) {

        String cartKey = Constant.USER_PREFIX + phoneNum + Constant.USER_CART;

        Cart selectCart = new Cart();
        selectCart.setUserPhone(phoneNum);
//        List<Cart> select = cartMapper.select(selectCart);
        List<Cart> selects = cartMapper.selectCarts(phoneNum);


        for (Cart cart : selects) {
            try {
                Example example = new Example(PointGoods.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("id", cart.getGoodId());
                PointGoods pointGoods = pointGoodsMapper.selectOneByExample(example);
                cart.setPointGoods(pointGoods);
                redisTemplate.opsForHash().put(cartKey, cart.getId() + "", cart.toString());
                redisTemplate.expire(cartKey, 35, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return selects;
    }

    @Override
    public User getUser(String phoneNum) {

        User user = new User();
        user.setPhoneNum(phoneNum);
        return userMapper.selectOne(user);
    }

    @Override
    public Cart deleteCart(Integer id) {

        Cart cart = cartMapper.selectByPrimaryKey(id);
        if (cart != null && cart.getState() == 0) {
            cartMapper.deleteByPrimaryKey(id);
        }
        return cart;
    }

    @Override
    public User registryUser(User user) {
        user.setId(null);
        if (!StringUtils.hasLength(user.getName())) {
            user.setName("默认用户名");
        }
        user.setLastPoint(0);
        user.setLastUseTime(new Date());
        try {
            int i = userMapper.insertSelective(user);
        } catch (Exception e) {
            return null;
        }
        return user;
    }
}
