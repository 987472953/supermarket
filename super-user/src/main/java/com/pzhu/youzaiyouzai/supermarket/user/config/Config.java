package com.pzhu.youzaiyouzai.supermarket.user.config;

import com.aliyun.dysmsapi20170525.Client;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
@MapperScan(basePackages = "com.pzhu.youzaiyouzai.supermarket.user.mapper")
public class Config {

//    @Bean
//    public Client smsClient() throws Exception {
//        Config config = new Config()
//                // 您的AccessKey ID
//                .setAccessKeyId(accessKeyId)
//                // 您的AccessKey Secret
//                .setAccessKeySecret(accessKeySecret);
//        // 访问的域名
//        config.endpoint = "dysmsapi.aliyuncs.com";
//        return new com.aliyun.dysmsapi20170525.Client(config);
//    }
}
