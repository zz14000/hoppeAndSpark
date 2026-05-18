package com.hopeandsparks.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hope and Sparks 后端应用启动类。
 *
 * <p>当前项目采用多 Maven 子模块的模块化单体结构，真正启动时由这个 boot 模块
 * 扫描 {@code com.hopeandsparks} 下的所有 Controller、配置和组件。后续新增业务模块时，
 * 只要包名仍在这个根包下面，并且 boot 的 POM 依赖了该模块，就会被装配进同一个 Spring Boot 应用。</p>
 */
@SpringBootApplication(scanBasePackages = "com.hopeandsparks")
public class HopeAndSparksApplication {

    public static void main(String[] args) {
        SpringApplication.run(HopeAndSparksApplication.class, args);
    }
}
