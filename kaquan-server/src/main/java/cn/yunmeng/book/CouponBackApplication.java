package cn.yunmeng.book;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/** 卡券权益商城后端 Spring Boot 启动类 */
@SpringBootApplication
@MapperScan("cn.yunmeng.book.mapper")
public class CouponBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponBackApplication.class, args);
    }
}