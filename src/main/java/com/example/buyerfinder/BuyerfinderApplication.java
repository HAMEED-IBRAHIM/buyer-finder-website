package com.example.buyerfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BuyerfinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuyerfinderApplication.class, args);
	}

}
