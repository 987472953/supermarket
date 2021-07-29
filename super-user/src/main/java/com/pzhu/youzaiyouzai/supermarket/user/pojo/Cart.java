package com.pzhu.youzaiyouzai.supermarket.user.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@Data
@ToString
public class Cart implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Integer goodId;

    @Column
    private String userPhone;

    @Column
    private Integer point;

    @Column
    private Integer state;

    @Column
    private Date createTime;

    @Transient
    private PointGoods pointGoods;
}
