package com.pzhu.youzaiyouzai.supermarket.admin.pojo;


import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@Data
@ToString
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String phoneNum;

    @Column
    private Integer points;

    @Column
    private String password;

    @Column
    private Integer lastPoint;

    @Column
    private Date lastUseTime;

}
