package com.hopeandsparks;


/**
 * 文件职责：HopeAndSparksApplication 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\HopeAndSparksApplication.java，用于承载对应分层或接口的基础职责。
 */
import com.hopeandsparks.common.config.HopeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HopeProperties.class)
public class HopeAndSparksApplication {

    public static void main(String[] args) {
        SpringApplication.run(HopeAndSparksApplication.class, args);
    }
}

