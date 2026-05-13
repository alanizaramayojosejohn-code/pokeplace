package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.config.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;       // ← nuevo
    private final SecurityUtils securityUtils;     // ← nuevo

    public List<Order> getAll() {
        return orderRepository.findAll();
    }
    @Transactional(readOnly = true) 
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order create(Order order) {
        order.setDateTime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);

        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        order.setUser(user);

        if (order.getClient() != null && order.getClient().getId() != null) {
            Client client = clientRepository.findById(order.getClient().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
            order.setClient(client);
        } else {
            order.setClient(null);
        }

        double total = 0;
        for (OrderDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + detail.getProduct().getId()));

            if (product instanceof Inedible inedible) {
                if (inedible.getStock() < detail.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + product.getName()
                            + " (available: " + inedible.getStock() + ", requested: " + detail.getQuantity() + ")");
                }
                inedible.setStock(inedible.getStock() - detail.getQuantity());
            }

            detail.setProduct(product);
            detail.setSubtotal(product.getPrice() * detail.getQuantity());
            detail.setOrder(order);
            total += detail.getSubtotal();
        }

        order.setTotal(total);
        Order saved = orderRepository.save(order);

        // ← auditoría
        auditService.log(
            "ORDER",
            saved.getId(),
            Audit.AuditAction.CREATE,
            null,
            saved.getId(),
            securityUtils.getCurrentUsername()
        );

        return saved;
    }

    @Transactional
    public Order updateStatus(Long id, Order.OrderStatus status) {
        Order previous = getById(id);
        Order.OrderStatus previousStatus = previous.getStatus(); // ← guardamos solo el status anterior

        previous.setStatus(status);
        Order saved = orderRepository.save(previous);

        // ← auditoría
        auditService.log(
            "ORDER",
            id,
            Audit.AuditAction.UPDATE,
            previousStatus,
            status,
            securityUtils.getCurrentUsername()
        );

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Order order = getById(id); // ← obtener la orden antes de borrarla
        
        orderRepository.deleteById(id);

        // ← auditoría con el ID de la orden borrada
        auditService.log(
            "ORDER",
            id,
            Audit.AuditAction.DELETE,
            order.getId(), // ← usar el ID de la orden obtenida
            null,
            securityUtils.getCurrentUsername()
        );
    }
}