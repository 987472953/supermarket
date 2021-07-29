package com.pzhu.youzaiyouzai.supermarket.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class Swagger2Config {

    public static final String TAG_1 = "用户管理";
    public static final String TAG_2 = "管理员管理";
    public static final String TAG_3 = "购物管理";

    @Bean
    public Docket webApiConfig(){

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("webApi")
                .apiInfo(webApiInfo())
                .select()
                .build()
                .tags(new Tag(TAG_1,"用户Controller"))
                .tags(new Tag(TAG_2,"管理员Controller"))
                .tags(new Tag(TAG_3,"购物Controller"));

    }

    private ApiInfo webApiInfo(){
        return new ApiInfoBuilder()
                .title("网站-超市积分管理系统")
                .description("本文档描述了超市积分系统接口定义")
                .version("1.0")
                .contact(new Contact("DYQ", "http://www.dyqking.xyz", "987472953@qq.com"))
                .build();
    }
}
