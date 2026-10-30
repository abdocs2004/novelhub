package com.novelhub.service.payment;

/** success + the provider's transaction id (or an error message when it failed). */
public record PaymentResult(boolean success, String reference, String message) {

    public static PaymentResult success(String reference) {
        return new PaymentResult(true, reference, null);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}
