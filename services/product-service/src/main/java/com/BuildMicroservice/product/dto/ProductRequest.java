package com.BuildMicroservice.product.dto;

import java.math.BigDecimal;
//this class is make for we are going to receive the product information from the client
public record ProductRequest(String id, String name, String description, String skuCode, BigDecimal  price) {

}
