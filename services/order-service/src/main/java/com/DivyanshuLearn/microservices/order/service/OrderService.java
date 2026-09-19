package com.DivyanshuLearn.microservices.order.service;

import com.DivyanshuLearn.microservices.order.client.InventoryClient;
import com.DivyanshuLearn.microservices.order.dto.OrderRequest;
import com.DivyanshuLearn.microservices.order.event.OrderPlacedEvent;
import com.DivyanshuLearn.microservices.order.model.Order;
import com.DivyanshuLearn.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_PLACED_TOPIC = "order-placed";

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public String placeOrder(OrderRequest orderRequest) {
        validateUserDetails(orderRequest);

        var isProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (!isProductInStock) {
            throw new RuntimeException(
                    "Product with SKU code " + orderRequest.skuCode() + " is not in stock");
        }

        Order order = buildOrderFromRequest(orderRequest);
        orderRepository.save(order);

        sendOrderPlacedEvent(order, orderRequest);

        return "Order Placed Successfully";
    }

    private void validateUserDetails(OrderRequest orderRequest) {
        if (orderRequest.userDetails() == null) {
            throw new IllegalArgumentException("User details are required to place an order");
        }
        if (isBlankOrNull(orderRequest.userDetails().email())
                || isBlankOrNull(orderRequest.userDetails().firstName())
                || isBlankOrNull(orderRequest.userDetails().lastName())) {
            throw new IllegalArgumentException(
                    "User email, firstName, and lastName are all required");
        }
    }

    private Order buildOrderFromRequest(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(orderRequest.price());
        order.setSkuCode(orderRequest.skuCode());
        order.setQuantity(orderRequest.quantity());
        return order;
    }

    /**
     * Builds and publishes an OrderPlacedEvent to Kafka.
     * Each field is explicitly converted to String to satisfy Avro's CharSequence contract
     * and prevent NullPointerException during serialization (fixes GitHub Issue #1).
     */
    private void sendOrderPlacedEvent(Order order, OrderRequest orderRequest) {
        OrderPlacedEvent event = new OrderPlacedEvent();
        event.setOrderNumber(order.getOrderNumber());
        event.setEmail(orderRequest.userDetails().email());
        event.setFirstName(orderRequest.userDetails().firstName());
        event.setLastName(orderRequest.userDetails().lastName());

        log.info("Sending OrderPlacedEvent to topic '{}': {}", ORDER_PLACED_TOPIC, event);
        kafkaTemplate.send(ORDER_PLACED_TOPIC, event);
        log.info("Successfully sent OrderPlacedEvent for order {}", order.getOrderNumber());
    }

    private static boolean isBlankOrNull(String value) {
        return value == null || value.isBlank();
    }
}
