package com.BuildMicroservice.product.repository;

import com.BuildMicroservice.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

}
