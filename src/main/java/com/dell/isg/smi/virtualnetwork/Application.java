/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.virtualnetwork;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The Class Application.
 */
@Configuration
@EnableDiscoveryClient
// @RefreshScope
@EnableSwagger2
@EnableAutoConfiguration
@EnableOAuth2Sso
@ComponentScan({ "com.dell.isg.smi.virtualnetwork" })
@EnableJpaRepositories("com.dell.isg.smi.virtualnetwork.repository")
public class Application extends WebMvcConfigurerAdapter {
    
    
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    /**
     * Locale resolver.
     *
     * @return the locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US);
        return slr;
    }


    /**
     * Locale change interceptor.
     *
     * @return the locale change interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }


    /**
     * Api.
     *
     * @return the docket
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("virtualnetwork").apiInfo(apiInfo()).select().paths(regex("/api/1.0/.*")).build();
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Virtual Network and IPAM Microservice").description("API for the Virtual Network and IPAM Microservice.").version("1.0").build();
    }
}
