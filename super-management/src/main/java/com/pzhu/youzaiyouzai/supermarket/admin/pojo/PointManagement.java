package com.pzhu.youzaiyouzai.supermarket.admin.pojo;


import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author QYstart
 * @date 2021/6/22
 */
@Data
@ToString
public class PointManagement {

    @Id
    private Integer pointType;

    @Column
    private Double ratio;

    @Column
    private Boolean used;

    @Column
    private String description;
}
