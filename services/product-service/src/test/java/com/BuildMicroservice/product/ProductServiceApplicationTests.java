package com.BuildMicroservice.product;

import io.restassured.RestAssured;
import jdk.jfr.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // whenEver i am running the Integration Test The test Will spin Up the Application and by defaults it will run on the Random Port
class ProductServiceApplicationTests {
	@ServiceConnection // dynamically assign the mongoDb Host and Port while running The Application
	//initialize a mongoDB container
	static MongoDBContainer mongoDBContainer =  new MongoDBContainer("mongo:7.0.5");
	// now i am going to add one more dependency in the pom.xml file mainly to make rest call from our integration test i.e. Rest assured
	@LocalServerPort // this annotation will give me the port on which my application is running
	private Integer port;
	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mongoDBContainer.start();
	}

	@Test
	void shouldCreateProduct() {
		// this test is supposed to create a product then we will verify that the product is created successfully or not
		String requestBody = """
				{
				     "name": "iphone 15 pro",
				     "description": "This is iphone from Apple",
				     "price": "99999"
				 }
				""";
		RestAssured.given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("name", Matchers.equalTo("iphone 15 pro"))
				.body("description", Matchers.equalTo("This is iphone from Apple"))
				.body("price", Matchers.equalTo(99999));
	}

}
// Here we are write Integration test for our two End Point createEndPoint and ReadAllEndPoint for that i make use of the test containers
//the test container is the library that spin up the docker container for us and we can use that docker container to run our integration test
//so we can test our application against the real database MongoDB