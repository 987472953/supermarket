package com.pzhu.youzaiyouzai.supermarket.user.pojo;

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
public class PointManagement {

    @Column
    private Integer pointType;

    @Column
    private Double radio;
}
