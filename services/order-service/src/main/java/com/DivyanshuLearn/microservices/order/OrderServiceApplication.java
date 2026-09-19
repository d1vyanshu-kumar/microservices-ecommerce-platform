package com.DivyanshuLearn.microservices.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
//@EnableFeignClients // this enables the auto-configuration of necessary classes to use openfeign in our order service project // this is not needed as we are using WebClient

public class  OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
