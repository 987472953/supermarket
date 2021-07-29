package com.pzhu.youzaiyouzai.supermarket.admin.controller;


import com.github.pagehelper.PageInfo;
import com.pzhu.youzaiyouzai.supermarket.admin.common.Constant;
import com.pzhu.youzaiyouzai.supermarket.admin.common.Result;
import com.pzhu.youzaiyouzai.supermarket.admin.pojo.*;
import com.pzhu.youzaiyouzai.supermarket.admin.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author QYstart
 * @date 2021/6/23
 */
@RestController
@RequestMapping("super")
@CrossOrigin
@Api(tags = "超市管理员Controller")
@Slf4j
public class ManagementController {

    @Autowired
    @Qualifier("manageServiceImpl")
    private ManageService manageService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("login")
    @ApiOperation("登录")
    public Result toLogin(@RequestBody Admin admin, HttpServletResponse response) {

        if (!StringUtils.hasLength(admin.getAccount()) || !StringUtils.hasLength(admin.getPassword())) {
            return Result.error().message("账号或密码为空");
        }

        admin = manageService.doLogin(admin);
        if (admin == null) {
            return Result.error().message("用户名或密码错误");
        }

        Cookie cookie = new Cookie("admin", admin.getAccount());
        response.addCookie(cookie);
        return Result.ok().data("admin", admin);
    }

    @GetMapping("users")
    @ApiOperation("分页查询用户")
    public Result getAllUsers(Integer page, Integer size, HttpServletRequest request) {
        if (!checkLogin(request)) {
            return Result.error().message("未登录");
        }
        if (page > 0 && size > 0) {
            PageInfo<User> pageInfo = manageService.queryByPage(page, size);
            log.error("pageInfo toString()={}", pageInfo.toString());
            HashMap<String, Object> map = new HashMap<>();
            map.put("pages", pageInfo.getPages());
            map.put("nextPage", pageInfo.getNextPage());
            map.put("prePage", pageInfo.getPrePage());
            map.put("pageNum", pageInfo.getPageNum());
            map.put("size", pageInfo.getSize());
            map.put("total", pageInfo.getTotal());
            map.put("userList", pageInfo.getList());
            return Result.ok().data(map);
        }
        return Result.error().message("传入参数错误");
    }

    @PostMapping("user")
    @ApiOperation("根据用户id更改用户信息")
    public Result updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        if (!checkLogin(request)) {
            return Result.error().message("未登录");
        }
        if (user.getId() == null) {
            return Result.error().message("参数错误");
        }
        Boolean flag = manageService.updateUserById(user);
        if (flag) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    private boolean checkLogin(HttpServletRequest request) {
        String admin = (String) request.getAttribute("admin");
        return true;
//        try {
//            String loginKey = Constant.ADMIN_PREFIX + admin + Constant.LOGIN_SUFFIX;
//            if (redisTemplate.hasKey(loginKey)) {
//                //登录续期
//                redisTemplate.expire(loginKey, 30, TimeUnit.MINUTES);
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    @PutMapping("pointGood")
    @ApiOperation("上架积分商品")
    public Result putPointGood(@RequestBody PointGoods pointGoods) {
        if (!StringUtils.hasLength(pointGoods.getName()) || pointGoods.getPrices() < 0 || pointGoods.getAvailable() < 0) {
            return Result.error().message("参数错误");
        }

        PointGoods good = manageService.putPointGood(pointGoods);
        return Result.ok().data("pointGood", good);
    }

    @PutMapping("image")
    @ApiOperation("上传图片")
    public Result putImage(Integer goodId, MultipartFile file) {

        String url = manageService.uploadImg(goodId, file);
        return Result.ok().data("url", url);
    }

    @PostMapping("pointGood")
    @ApiOperation("修改积分商品")
    public Result updatePointGood(@RequestBody PointGoods pointGoods) {

        if (pointGoods.getId() < 0) {
            return Result.error().message("参数错误");
        }

        boolean flag = manageService.updatePointGood(pointGoods);
        if (flag) {
            return Result.ok();
        } else {
            return Result.error().message("更新失败");
        }
    }

    @DeleteMapping("pointGood")
    @ApiOperation("删除积分商品")
    public Result updatePointGood(Integer id) {

        if (id == null) {
            return Result.error().message("参数错误");
        }

        boolean flag = manageService.deletePointGood(id);
        if (flag) {
            return Result.ok().data("deleteId", id);
        } else {
            return Result.error().message("删除失败");
        }
    }


    @PutMapping("rule")
    @ApiOperation("修改积分规则")
    public Result putPointType(PointManagement pointManagement) {

        if (pointManagement.getPointType() == null || pointManagement.getRatio() == null) {
            return Result.error().message("参数错误");
        }

        Boolean flag = manageService.putPointRule(pointManagement);
        if (flag) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @GetMapping("rule")
    @ApiOperation("获得已有积分规则")
    public Result getPointType() {

        List<PointManagement> list = manageService.queryPointRule();
        return Result.ok().data("ruleList", list);
    }

    @PostMapping("rule")
    @ApiOperation("根据积分类型id，启动积分规则")
    public Result updateRule(Integer pointType) {

        boolean flag = manageService.updateRuleToUse(pointType);

        if (flag) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @PostMapping("/exchange")
    @ApiOperation("客户进行商品兑换")
    public Result exchangeGoods(String phoneNum, Integer[] goods) {

        boolean flag = manageService.exChangeGoods(phoneNum, goods);
        if (flag) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @GetMapping("/userCart")
    @ApiOperation("根据用户手机号获得购物车")
    public Result getUserCart(String phoneNum) {
        if (!StringUtils.hasLength(phoneNum)) {
            return Result.error().message("参数错误");
        }

        List<Cart> list = manageService.getUserCart(phoneNum);
        return Result.ok().data("carList", list);
    }

}
