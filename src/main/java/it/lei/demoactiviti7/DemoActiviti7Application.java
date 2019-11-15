package it.lei.demoactiviti7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 该项目为activiti7的入门项目,对应的视频教程在百度云盘位置为 黑马19年视频教程/黑马java/SaaS-IHRM项目的位置
 * 快捷提取码为 https://pan.baidu.com/s/1sm0j8em7O_PcGNBtmtmAwA
 * 一些学习资源的网址https://www.imxushuai.com/2000/01/01/%E5%AD%A6%E4%B9%A0%E8%B5%84%E6%BA%90/#视频学习资源
 * activiti-app 教程https://www.jianshu.com/p/ad35a325443b
 */
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DemoActiviti7Application {

    public static void main(String[] args) {
        SpringApplication.run(DemoActiviti7Application.class, args);
    }

}
