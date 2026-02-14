package com.springboot.mtbs.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public boolean isPaymentSuccessful(String paymentToken) {
        return paymentToken != null && !paymentToken.trim().toLowerCase().contains("fail");
    }
}
