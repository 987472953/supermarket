package com.pzhu.youzaiyouzai.supermarket.user.pojo;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@Data
@ToString
public class PointGoods {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String img;

    @Column
    private Integer prices;

    @Column
    private Integer available;
}
