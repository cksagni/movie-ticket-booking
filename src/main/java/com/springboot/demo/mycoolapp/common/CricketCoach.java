package com.springboot.demo.mycoolapp.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


//@Lazy // Beans is initialized only when needed for DI
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // new object for each instance creation. Default is Singleton
// For "prototype scoped beans, Spring does not call the destroy method"
@Component
public class CricketCoach implements Coach{

    public CricketCoach(){
        System.out.println("In Constructor: " + getClass().getSimpleName());
    }

    @Override
    public String getDailyWorkout() {
        return "Practice fast bowling for 15 minutes";
    }

//    init method
    @PostConstruct
    public void doMyStartupStuff(){
        System.out.println("In init method : " + getClass().getSimpleName());
    }

//    destroy method
    @PreDestroy
    public void doMyCleanupStuff(){
        System.out.println("In destroy method : " + getClass().getSimpleName());
    }

}
