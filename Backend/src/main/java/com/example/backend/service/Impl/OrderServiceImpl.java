package com.example.backend.service.Impl;

import com.example.backend.dto.OrderDTO;
import com.example.backend.entity.Listings;
import com.example.backend.entity.Orders;
import com.example.backend.entity.Payment;
import com.example.backend.entity.User;
import com.example.backend.repo.ListingRepository;
import com.example.backend.repo.OrdersRepository;
import com.example.backend.repo.PaymentRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.service.ListingService;
import com.example.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PaymentRepository paymentRepository;
    private final ListingService listingService;

    @Override
    @Transactional
    public OrderDTO createPaidOrder(Long userId, Long listingId, int quantity) {
        // Decrement stock via service
        listingService.purchaseEquipment(listingId, quantity);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Listings listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        String unit = listing.getPrice();
        // Since price is a String, keep total as String too
        // Attempt a safe numeric multiply if possible, else just echo unit
        String total;
        try {
            double u = Double.parseDouble(unit);
            total = String.valueOf(u * quantity);
        } catch (NumberFormatException ex) {
            total = unit; // fallback
        }

        Orders order = Orders.builder()
                .user(user)
                .listing(listing)
                .quantity(quantity)
                .unitPrice(unit)
                .total(total)
                .status("PAID")
                .createdAt(LocalDateTime.now())
                .build();
        ordersRepository.save(order);

    // Save a corresponding Payment record for analytics/audit
    paymentRepository.save(Payment.builder()
        .listingName(listing.getListingName())
        .quantity(quantity)
        .price(unit)
        .build());

        return OrderDTO.builder()
                .id(order.getId())
                .listingId(listing.getId())
                .listingName(listing.getListingName())
                .imageUrl(listing.getImageUrl())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    public List<OrderDTO> getMyOrders(Long userId) {
        return ordersRepository.findByUserId(userId).stream().map(o -> OrderDTO.builder()
                .id(o.getId())
                .listingId(o.getListing().getId())
                .listingName(o.getListing().getListingName())
                .imageUrl(o.getListing().getImageUrl())
                .quantity(o.getQuantity())
                .unitPrice(o.getUnitPrice())
                .total(o.getTotal())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build()).collect(Collectors.toList());
    }
}
