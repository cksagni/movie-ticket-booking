package com.springboot.mtbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieApp {

	public static void main(String[] args) {

		SpringApplication.run(MovieApp.class, args);
	}
}
