package com.DivyanshuLearn.microservices.order.client;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;



//@FeignClient(value = "inventory",url = "${inventory.url}") // this is the url where the inventory service is running //no used library
public interface InventoryClient {
    Logger log = LoggerFactory.getLogger(InventoryClient.class);
    // so what kind of the method this http client will call we are defining here
    //getMethod
    //@RequestMapping(method = RequestMethod.GET, value = "")//no used library
    @GetExchange("/api/inventory") // this is the url where the inventory service is running
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);
    // similar like spring data jpa repository in which we have to only extend and use their method in the implemented class
    // now go to the order service class and inject the InventoryClient
    default boolean fallbackMethod(String Code, Integer quantity, Throwable throwable) {
        log.info("cannot get inventory for skuCode: {}, failure reason: {}", Code, throwable.getMessage());
        return false;
    }
}
