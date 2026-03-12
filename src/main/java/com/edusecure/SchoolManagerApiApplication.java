package com.edusecure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
		exclude = {},
		scanBasePackages = "com.edusecure"
)
@EnableConfigurationProperties
public class SchoolManagerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchoolManagerApiApplication.class, args);
	}
}