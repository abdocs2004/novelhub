package com.novelhub.service.payment;

/**
 * The ONLY thing PurchaseService knows about payments. It depends on this interface, not on a concrete class.
 *
 * To use a real gateway later (Stripe, Paymob, Fawry ...):
 *   1. create StripePaymentService implements PaymentService
 *   2. annotate it with @ConditionalOnProperty(name = "app.payment.provider", havingValue = "stripe")
 *   3. set app.payment.provider: stripe
 * PurchaseService, controllers, database and frontend stay untouched. (Real gateways usually confirm the
 * payment asynchronously with a webhook; then you would add a PENDING status. That is a nice next exercise.)
 */
public interface PaymentService {

    PaymentResult charge(PaymentRequest request);
}
