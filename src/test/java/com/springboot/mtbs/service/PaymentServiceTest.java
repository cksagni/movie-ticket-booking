package com.springboot.mtbs.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void isPaymentSuccessfulReturnsTrueForValidToken() {
        assertTrue(paymentService.isPaymentSuccessful("tok_123"));
    }

    @Test
    void isPaymentSuccessfulReturnsFalseForNullOrFailedToken() {
        assertFalse(paymentService.isPaymentSuccessful(null));
        assertFalse(paymentService.isPaymentSuccessful("please_fail_this"));
    }
}
