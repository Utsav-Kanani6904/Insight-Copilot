package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableTransactionManagement
public class InsightCopilotApplication {
	public static void main(String[] args) {
		SpringApplication.run(InsightCopilotApplication.class, args);
	}

	@Bean
	public PlatformTransactionManager transactionmanaging(MongoDatabaseFactory dbfactory){
		return new MongoTransactionManager(dbfactory);
	}

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}
