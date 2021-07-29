package com.pzhu.youzaiyouzai.supermarket.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author QYstart
 * @date 2021/4/4
 */
@Configuration
@MapperScan(basePackages = "com.pzhu.youzaiyouzai.supermarket.admin.mapper")
public class Config {

}
