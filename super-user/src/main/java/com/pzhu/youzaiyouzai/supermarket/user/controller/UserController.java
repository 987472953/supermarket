package com.pzhu.youzaiyouzai.supermarket.user.controller;

import com.alibaba.fastjson.JSON;
import com.pzhu.youzaiyouzai.supermarket.user.common.Constant;
import com.pzhu.youzaiyouzai.supermarket.user.common.Result;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.Cart;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.PointGoods;
import com.pzhu.youzaiyouzai.supermarket.user.pojo.User;
import com.pzhu.youzaiyouzai.supermarket.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
@CrossOrigin
@Slf4j
public class UserController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @GetMapping("sendSMS")
    @ApiOperation("获得短信验证码")
    public Result sendSMS(){

        return Result.ok();
    }
    @PostMapping("login")
    @ApiOperation("用户登录接口")
    public Result toLogin(@RequestBody User user, HttpServletResponse response) {

        String loginKey = Constant.USER_PREFIX + user.getPhoneNum() + Constant.LOGIN_SUFFIX;
        String newPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        User success = null;
        if (StringUtils.hasLength(user.getPhoneNum() + "") && StringUtils.hasLength(user.getPassword())) {
            success = userService.doLogin(user);
        }

        if (success == null) {
            return Result.error().message("账号或密码错误！");
        }

        try {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("id", success.getId() + "");
            map.put("name", success.getName());
            map.put("phoneNum", success.getPhoneNum());
            map.put("points", success.getPoints() + "");
            redisTemplate.opsForHash().putAll(loginKey, map);
            redisTemplate.expire(loginKey, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cookie cookie = new Cookie("loginPhone", success.getPhoneNum());
        response.addCookie(cookie);
        return Result.ok().data("user", success);
    }

    @GetMapping("/pointGoods")
    @ApiOperation("获得积分兑换商品列表")
    public Result getPointGoods() {

        log.debug("pointGoods == 获取redis数据");
        List<Object> goods = redisTemplate.opsForHash().values("GOODS");
        if (goods.size() == 0) {
            log.debug("pointGoods == 获取mysql数据");
            List<PointGoods> list = userService.getAllPointGoods();
            log.debug("pointGoods == mysql返回");
            return Result.ok().data("pointGoods", list);
        }
        log.debug("pointGoods == JSON转换");
        List<PointGoods> pointGoods = JSON.parseArray(goods.toString(), PointGoods.class);
        log.debug("pointGoods == redis返回");
        return Result.ok().data("pointGoods", pointGoods);
    }

    @GetMapping("/cart")
    @ApiOperation("获得用户购物车信息")
    public Result getCarts(HttpServletRequest request) {

        String phoneNum = (String) request.getAttribute("phoneNum");
        if (!loginCheck(phoneNum)) {
            return Result.error().message(phoneNum + "用户未登录");
        }

        List<Cart> carts = userService.getCarts(phoneNum);
        return Result.ok().data("carts", carts);
    }


    @PutMapping("/cart")
    @ApiOperation("添加商品到购物车")
    public Result addCart(Integer goodId, HttpServletRequest request) {

        String phoneNum = (String) request.getAttribute("phoneNum");
        if (!loginCheck(phoneNum)) {
            return Result.error().message(phoneNum + "用户未登录");
        }

        Cart cart = userService.addCart(phoneNum, goodId);

        if (cart == null) {
            return Result.error().message("商品不存在");
        } else {
            return Result.ok().data("cartGood", cart);
        }
    }

    @DeleteMapping("/cart")
    @ApiOperation("删除商品")
    public Result deleteCart(Integer id, HttpServletRequest request) {

        String phoneNum = (String) request.getAttribute("phoneNum");
        if (!loginCheck(phoneNum)) {
            return Result.error().message(phoneNum + "用户未登录");
        }

        Cart cart = userService.deleteCart(id);
        if (cart == null) {
            return Result.error().message("商品已购买或不存在");
        } else {
            return Result.ok().data("cartGood", cart);
        }
    }

    @PostMapping("/buys")
    @ApiOperation("传入一个需要购买的商品ID数组 JSON")
    public Result toBuyGoods(HttpServletRequest request, @RequestBody Integer[] goodIds) {

        String phoneNum = (String) request.getAttribute("phoneNum");
        if (!loginCheck(phoneNum)) {
            return Result.error().message(phoneNum + "用户未登录");
        }
        if (goodIds.length < 1) {
            return Result.error().message("输入错误的商品Id信息");
        }

        List<Cart> badBuyGoods = userService.buyGood(phoneNum, goodIds);
        HashMap<String, Object> map = new HashMap<>();
        map.put("badBuyGoods", badBuyGoods);
        try {
            String strPoint = (String) redisTemplate.opsForHash().get(Constant.USER_PREFIX + phoneNum + Constant.LOGIN_SUFFIX, "points");
            Integer points = Integer.valueOf(strPoint);
            map.put("freePoints", points);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok().data(map);
    }

    @PostMapping("/loginout")
    @ApiOperation("退出登录")
    public Result loginOut(HttpServletRequest request, HttpServletResponse response) {
        String phoneNum = (String) request.getAttribute("phoneNum");
        String loginKey = Constant.USER_PREFIX + phoneNum + Constant.LOGIN_SUFFIX;
        String cartKey = Constant.USER_PREFIX + phoneNum + Constant.USER_CART;
        try {
            redisTemplate.delete(loginKey);
            redisTemplate.delete(cartKey);
            return Result.ok().message("退出成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.addCookie(new Cookie("phoneNum", ""));
        return Result.error().message("退出失败");
    }

    @GetMapping("/updateMessage")
    @ApiOperation("更新用户信息(需要登录)")
    public Result updateMessage(HttpServletRequest request) {
        String phoneNum = (String) request.getAttribute("phoneNum");
        if (!loginCheck(phoneNum)) {
            return Result.error().message(phoneNum + "用户未登录");
        }

        User user = userService.getUser(phoneNum);
        return Result.ok().data("user", user);
    }

    @PostMapping("/registry")
    @ApiOperation("注册")
    public Result registry(@RequestBody User user) {

        if (!StringUtils.hasLength(user.getPhoneNum()) || !StringUtils.hasLength(user.getPassword())) {
            return Result.error().message("参数错误");
        }

        user.setPoints(0);
        user = userService.registryUser(user);
        if (user == null) {
            return Result.error().message("插入失败，用户已存在");
        } else {
            return Result.ok().data("user", user);
        }
    }


    private boolean loginCheck(String phoneNum) {
        String loginKey = Constant.USER_PREFIX + phoneNum + Constant.LOGIN_SUFFIX;
        if (redisTemplate.hasKey(loginKey)) {
            redisTemplate.expire(loginKey, 30, TimeUnit.MINUTES);
            return true;
        }
        return false;
    }
}
