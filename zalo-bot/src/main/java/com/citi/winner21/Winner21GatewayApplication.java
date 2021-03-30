package com.citi.winner21;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.citi")
@EnableTransactionManagement(proxyTargetClass = true)
public class Winner21GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(Winner21GatewayApplication.class, args);
	}

}
