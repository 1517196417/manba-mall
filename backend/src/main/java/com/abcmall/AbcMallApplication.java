package com.abcmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * AbcMall 阿帕茨商城 —— 启动入口
 *
 * 技术栈：SpringBoot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL 8 + Session 身份认证
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.abcmall.mapper,com.abcmall.smai.mapper")
public class AbcMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbcMallApplication.class, args);
        System.out.println("""


                =========================================================
                  AbcMall 阿帕茨商城 后端启动成功
                  接口地址: http://localhost:8080/api
                  前端地址: http://localhost:5173
                =========================================================
                """);
    }
}
