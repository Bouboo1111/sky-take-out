package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {
    @Bean
    @ConditionalOnMissingBean//创建对象时，先判断这个对象是否存在，不存在才创建
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("开始创建阿里云文件上传工具对象：{}", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                            aliOssProperties.getAccessKeyId(),
                            aliOssProperties.getAccessKeySecret(),
                            aliOssProperties.getBucketName());
    }
}
