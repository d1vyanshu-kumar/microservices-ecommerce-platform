package com.BuildMicroservice.product.controller;

import com.BuildMicroservice.product.dto.ProductRequest;
import com.BuildMicroservice.product.dto.ProductResponse;
import com.BuildMicroservice.product.model.Product;
import com.BuildMicroservice.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private  final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        // Create product
        // so we have our controller that takes in  the RequestBody of the type  productRequest and this is going to pass
        //it to the createProduct method in the productService and the create product method is going to create a new product object
        //and save it to the product repository
        return productService.createProduct(productRequest);

        // i am adding this product  as a return type cause  i want to have some response back whenever i make a call for post call to create a product
        // i want to see that the product really is saved.

    }
    // now i want to create a another EndPoint or a  get method to Read  all the products
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
       // wait for 5 sec so its simulates the slowly responding Api
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return productService.getAllProducts();
    }
}
