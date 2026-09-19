package com.DivyanshuLearn.microservices.order.repository;

import com.DivyanshuLearn.microservices.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
