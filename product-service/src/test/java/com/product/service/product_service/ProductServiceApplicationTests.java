package com.product.service.product_service;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.hamcrest.Matchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mongoDBContainer.start();
	}

	@Test
	void shouldCreateProduct() {
		String requestBody = """
					{
				     "name": "samsung",
				     "description": "random mobile",
				     "price": "1500"
				 	}
				""";
		RestAssured.given().contentType("application/json").body(requestBody).when().post("/api/product")
				.then().statusCode(201).body("id", Matchers.notNullValue())
				.body("name", Matchers.equalTo("samsung"))
				.body("description", Matchers.equalTo("random mobile"))
				.body("price", Matchers.equalTo(1500));

	}

}
