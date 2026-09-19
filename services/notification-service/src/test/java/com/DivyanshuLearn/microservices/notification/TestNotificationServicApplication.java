package com.DivyanshuLearn.microservices.notification;

import org.springframework.boot.SpringApplication;

public class TestNotificationServicApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationServicApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
