package com.pzhu.youzaiyouzai.supermarket.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pzhu.youzaiyouzai.supermarket.admin.common.Constant;
import com.pzhu.youzaiyouzai.supermarket.admin.common.ConstantPropertiesUtil;
import com.pzhu.youzaiyouzai.supermarket.admin.mapper.*;
import com.pzhu.youzaiyouzai.supermarket.admin.pojo.*;
import com.pzhu.youzaiyouzai.supermarket.admin.service.ManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author QYstart
 * @date 2021/6/23
 */
@Service
@Slf4j
public class ManageServiceImpl implements ManageService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PointGoodsMapper pointGoodsMapper;

    @Autowired
    private PointManagementMapper pointManagementMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Admin doLogin(Admin admin) {
        Admin a = new Admin();
        a.setAccount(admin.getAccount());
        a = adminMapper.selectOne(a);

        // TODO 密码加密验证
        if (!admin.getPassword().equals(a.getPassword())) {
            return null;
        }
        a.setPassword("");
        String loginKey = Constant.ADMIN_PREFIX + a.getAccount() + Constant.LOGIN_SUFFIX;
        try {
            redisTemplate.opsForValue().set(loginKey, a.getAccount(), 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }

    @Override
    public PageInfo<User> queryByPage(Integer page, Integer size) {
        PageHelper.startPage(page, size);
        PageInfo<User> pageInfo = new PageInfo<>(userMapper.selectAll());
        List<User> list = pageInfo.getList();
        for (User user : list) {
            user.setPassword("");
        }
        pageInfo.setList(list);
        return pageInfo;
    }

    @Override
    public PointGoods putPointGood(PointGoods pointGoods) {
        pointGoods.setId(null);
        pointGoodsMapper.insertSelective(pointGoods);
        try {
            redisTemplate.opsForHash().put("GOODS", pointGoods.getId().toString(), JSON.toJSONString(pointGoods));
        } catch (Exception e) {
            e.printStackTrace();
            // 容错处理
            whenFault();
        }
        return pointGoods;
    }

    public void whenFault() {
        List<PointGoods> pointGoods = pointGoodsMapper.selectAll();
        HashMap<String, String> map = new HashMap<>();
        for (PointGoods goods : pointGoods) {
            map.put(goods.getId().toString(), JSON.toJSONString(goods));
        }
        redisTemplate.delete("GOODS");
        redisTemplate.opsForHash().putAll("GOODS", map);
    }

    @Override
    public Boolean updatePointGood(PointGoods pointGoods) {

        int i = pointGoodsMapper.updateByPrimaryKeySelective(pointGoods);
        if (i > 0) {
            try {
                PointGoods newPointGood = pointGoodsMapper.selectByPrimaryKey(pointGoods.getId());
                redisTemplate.opsForHash().put("GOODS", pointGoods.getId().toString(), JSON.toJSONString(newPointGood));
            } catch (Exception e) {
                e.printStackTrace();
                // 容错处理
                whenFault();
            }
        }
        return i > 0;
    }

    @Override
    public Boolean putPointRule(PointManagement pointManagement) {
        toBeUnUsed(pointManagement.getPointType());
        pointManagement.setUsed(true);
        int insert = pointManagementMapper.updateByPrimaryKeySelective(pointManagement);
        return insert == 1;
    }

    @Override
    public boolean updateRuleToUse(Integer pointType) {
        toBeUnUsed(pointType);
        PointManagement pointManagement = new PointManagement();
        pointManagement.setPointType(pointType);
        pointManagement.setUsed(true);
        int i = pointManagementMapper.updateByPrimaryKeySelective(pointManagement);
        return i > 0;
    }

    //将其他的rule变为未启动
    private void toBeUnUsed(int pointType) {
        List<PointManagement> list = pointManagementMapper.selectAll();
        for (PointManagement pointManagement : list) {
            if (pointManagement.getUsed() && pointManagement.getPointType() != pointType) {
                pointManagement.setUsed(false);
                pointManagementMapper.updateByPrimaryKeySelective(pointManagement);
            }
        }
    }


    @Override
    public List<PointManagement> queryPointRule() {
        return pointManagementMapper.selectAll();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exChangeGoods(String phoneNum, Integer[] carts) {
        Cart selectCart = new Cart();
        selectCart.setUserPhone(phoneNum);
        List<Cart> select = cartMapper.select(selectCart);

        for (Integer cartId : carts) {
            Cart cart = cartMapper.selectByPrimaryKey(cartId);
            if (cart.getUserPhone().equals(phoneNum) && cart.getState() == 1) {
                cart.setState(2);
                cartMapper.updateByPrimaryKeySelective(cart);
            }
        }
        return true;
    }

    private static String[] TYPESTR = {".png", ".jpg", ".bmp", ".gif", ".jpeg"};

    @Override
    public String uploadImg(Integer goodId, MultipartFile file) {

        OSS ossClient = null;
        String url = null;
        boolean flag = true;
        try {
            //创建OSSClient实例。
            ossClient = new OSSClientBuilder().build(ConstantPropertiesUtil.END_POINT
                    , ConstantPropertiesUtil.ACCESS_KEY_ID
                    , ConstantPropertiesUtil.ACCESS_KEY_SECRET);

            //判断文件格式
            for (String type : TYPESTR) {
                if (StringUtils.endsWithIgnoreCase(file.getOriginalFilename(), type)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return "格式不正确！";
            }

            //判断文件内容
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read != null) {
                log.debug("图片高度={}", read.getHeight());
                log.debug("图片宽度={}", read.getWidth());
            } else {
                return "图片内容错误";
            }

            //获得文件保存路径
            String filename = file.getOriginalFilename();
            String ext = filename.substring(filename.lastIndexOf('.'));
            String newName = UUID.randomUUID().toString() + ext;

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = simpleDateFormat.format(new Date());

            String newPath = ConstantPropertiesUtil.FILE_HOST + "/" + datePath + "/" + newName;


            // 上传文件流。
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(ConstantPropertiesUtil.BUCKET_NAME, newPath, inputStream);

            url = "https://" + ConstantPropertiesUtil.BUCKET_NAME + "." + ConstantPropertiesUtil.END_POINT + "/" + newPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }

        //id 不为空，则是修改某个商品的图片
        if (goodId != null) {
            PointGoods goods = pointGoodsMapper.selectByPrimaryKey(goodId);
            if (goods != null) {
                goods.setImg(url);
                pointGoodsMapper.updateByPrimaryKeySelective(goods);
            }
        }

        return url;
    }

    @Override
    public boolean deletePointGood(Integer id) {
        int i = pointGoodsMapper.deleteByPrimaryKey(id);
        if (i > 0) {
            try {
                redisTemplate.opsForHash().delete("GOODS", id.toString());
            } catch (Exception e) {
                e.printStackTrace();
                whenFault();
            }
        }
        return pointGoodsMapper.deleteByPrimaryKey(id) == 1;
    }

    @Override
    public List<Cart> getUserCart(String phoneNum) {
        Example example = new Example(Cart.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userPhone", phoneNum);

        List<Cart> carts = cartMapper.selectByExample(example);
        for (Cart cart : carts) {
            PointGoods pointGoods = pointGoodsMapper.selectByPrimaryKey(cart.getGoodId());
            cart.setPointGoods(pointGoods);
        }
        return carts;
    }

    @Override
    public Boolean updateUserById(User user) {
        int i = userMapper.updateByPrimaryKeySelective(user);
        return i == 1;
    }
}
