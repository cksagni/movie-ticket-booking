package com.springboot.restapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(
		scanBasePackages = {
				"com.springboot.demo.mycoolapp",
				"com.springboot.demo.util"
		})
public class RestappApplication {

	public static void main(String[] args) {

		SpringApplication.run(RestappApplication.class, args);
	}
}
