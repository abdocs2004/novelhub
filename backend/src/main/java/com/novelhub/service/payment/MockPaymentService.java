package com.novelhub.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Fake payment for learning: it always succeeds. Active when app.payment.provider=mock (the default). */
@Service
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentService implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentService.class);

    @Override
    public PaymentResult charge(PaymentRequest request) {
        log.info("[MOCK PAYMENT] user={} chapter={} amount={} {}",
                request.userId(), request.chapterId(), request.amount(), request.currency());
        return PaymentResult.success("MOCK-" + UUID.randomUUID());
    }
}
