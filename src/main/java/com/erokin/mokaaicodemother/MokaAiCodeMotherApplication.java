package com.erokin.mokaaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.erokin.mokaaicodemother.mapper")
public class MokaAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MokaAiCodeMotherApplication.class, args);
    }

}
