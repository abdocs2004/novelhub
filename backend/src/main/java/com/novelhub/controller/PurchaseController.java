package com.novelhub.controller;

import com.novelhub.dto.PurchaseResponse;
import com.novelhub.entity.PurchaseStatus;
import com.novelhub.security.UserPrincipal;
import com.novelhub.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /** Buy a chapter. The user id comes from the JWT (the principal), never from the request body. */
    @PostMapping("/{chapterId}")
    public ResponseEntity<PurchaseResponse> purchase(@PathVariable Long chapterId,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        PurchaseResponse purchase = purchaseService.purchase(user.getId(), chapterId);
        HttpStatus status = purchase.status() == PurchaseStatus.SUCCESS ? HttpStatus.CREATED : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(purchase);
    }

    /** My Library. */
    @GetMapping("/my")
    public List<PurchaseResponse> myLibrary(@AuthenticationPrincipal UserPrincipal user) {
        return purchaseService.getLibrary(user.getId());
    }
}
