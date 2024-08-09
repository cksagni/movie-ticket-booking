package com.springboot.demo.mycoolapp.rest;

import com.springboot.demo.mycoolapp.common.Coach;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoControllerDI {
    private Coach myCoach;

//    use @Qualifier to give explicit info for DI --> public DemoControllerDI(@Qualifier("cricketCoach") Coach theCoach)
//    use @Primary annotation to mark one class as primary in case of multiple implementation
//    priority --> @Qualifier > @Primary
    @Autowired
    public DemoControllerDI(@Qualifier("swimCoach") Coach theCoach){
        System.out.println("In Constructor: " + getClass().getSimpleName());
        myCoach = theCoach;
    }


    @GetMapping("/dailyworkout")
    public String getDailyWorkout(){
        return myCoach.getDailyWorkout();
    }
}
