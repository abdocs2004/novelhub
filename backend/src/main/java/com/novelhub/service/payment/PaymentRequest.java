package com.novelhub.service.payment;

import java.math.BigDecimal;

public record PaymentRequest(Long userId, Long chapterId, BigDecimal amount, String currency) {
}
