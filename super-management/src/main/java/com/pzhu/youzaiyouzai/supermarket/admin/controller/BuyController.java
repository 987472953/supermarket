package com.pzhu.youzaiyouzai.supermarket.admin.controller;

import com.pzhu.youzaiyouzai.supermarket.admin.common.Result;
import com.pzhu.youzaiyouzai.supermarket.admin.pojo.User;
import com.pzhu.youzaiyouzai.supermarket.admin.service.BuyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author QYstart
 * @date 2021/6/23
 */
@RestController
@CrossOrigin
@RequestMapping("buy")
@Api(tags = "购物管理")
public class BuyController {

    @Autowired
    private BuyService buyService;

    @GetMapping("user")
    @ApiOperation("根据手机号，查询用户信息")
    public Result getUerInfo(String phoneNum) {
        if (!StringUtils.hasLength(phoneNum)) {
            return Result.error().message("没有手机号");
        }
        User user = buyService.queryUserInfo(phoneNum);
        return Result.ok().data("userInfo", user);
    }

    @PostMapping("/afterBuy")
    @ApiOperation("购买之后的积分增加")
    public Result updatePointAfterBuy(Double paymentAccount, String phoneNum) {

        Map<String, Object> map = buyService.updateUserAfterBuy(phoneNum, paymentAccount);
        return Result.ok().data(map);
    }


}
