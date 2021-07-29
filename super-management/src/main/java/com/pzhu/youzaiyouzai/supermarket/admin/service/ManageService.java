package com.pzhu.youzaiyouzai.supermarket.admin.service;

import com.github.pagehelper.PageInfo;
import com.pzhu.youzaiyouzai.supermarket.admin.pojo.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author QYstart
 * @date 2021/6/23
 */
public interface ManageService {
    /**
     * 登录
     * @param admin
     * @return
     */
    Admin doLogin(Admin admin);

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<User> queryByPage(Integer page, Integer size);

    /**
     * 上架积分商品
     * @param pointGoods
     * @return
     */
    PointGoods putPointGood(PointGoods pointGoods);

    /**
     * 修改pointGood信息
     * @param pointGoods
     * @return
     */
    Boolean updatePointGood(PointGoods pointGoods);

    /**
     * 添加积分规则
     * @param pointManagement
     * @return
     */
    Boolean putPointRule(PointManagement pointManagement);

    /**
     * 查询积分规则
     * @return
     */
    List<PointManagement> queryPointRule();

    /**
     * 将该pointType设置为启用
     * @param pointType
     * @return
     */
    boolean updateRuleToUse(Integer pointType);

    /**
     * 兑换商品后的处理
     * @param phoneNum
     * @param goods
     * @return
     */
    boolean exChangeGoods(String phoneNum, Integer[] goods);

    /**
     * 上传图片
     *
     * @param goodId
     * @param file
     * @return
     */
    String uploadImg(Integer goodId, MultipartFile file);

    /**
     * 删除商品
     * @param id
     * @return
     */
    boolean deletePointGood(Integer id);

    /**
     * 根据手机号返回用户购物车
     * @param phoneNum
     * @return
     */
    List<Cart> getUserCart(String phoneNum);

    /**
     * 根据主键更新用户信息
     * @param user
     * @return
     */
    Boolean updateUserById(User user);
}
