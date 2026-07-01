package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Payment;
import com.example.demo.model.User;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    public List<Payment> getByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getByPaymentDateBetween(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must be before end date");
        }
        return paymentRepository.findByPaymentDateBetween(start, end);
    }

    public Payment create(Payment payment) {
        User user = userRepository.findById(payment.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        payment.setUser(user);

        if (payment.getPeriodStart().isAfter(payment.getPeriodEnd())) {
            throw new BadRequestException("Period start must be before period end");
        }

        return paymentRepository.save(payment);
    }

    public Payment update(Long id, Payment updated) {
        Payment existing = getById(id);
        existing.setAmount(updated.getAmount());
        existing.setPaymentDate(updated.getPaymentDate());
        existing.setPeriodStart(updated.getPeriodStart());
        existing.setPeriodEnd(updated.getPeriodEnd());
        existing.setNote(updated.getNote());

        if (updated.getPeriodStart().isAfter(updated.getPeriodEnd())) {
            throw new BadRequestException("Period start must be before period end");
        }

        return paymentRepository.save(existing);
    }

    public void delete(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
}
