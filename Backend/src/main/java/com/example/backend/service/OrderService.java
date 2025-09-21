package com.example.backend.service;

import com.example.backend.dto.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createPaidOrder(Long userId, Long listingId, int quantity);
    List<OrderDTO> getMyOrders(Long userId);
}
