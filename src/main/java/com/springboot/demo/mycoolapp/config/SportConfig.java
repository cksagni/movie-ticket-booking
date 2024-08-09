package com.springboot.demo.mycoolapp.config;

import com.springboot.demo.mycoolapp.common.Coach;
import com.springboot.demo.mycoolapp.common.SwimCoach;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SportConfig {

    // @Bean("beanId") --> custom beanId can be used with@Qualifier
    @Bean // can be used to make an existing third-party class available to Spring framework
    public Coach swimCoach(){
        return new SwimCoach();
    }

}
