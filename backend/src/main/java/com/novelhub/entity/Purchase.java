package com.novelhub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * "User X bought Chapter Y". This is the record the backend checks before sending paid content.
 * It is effectively a join table between users and chapters (many-to-many) that also carries
 * extra data (price, status, date), which is why it is a real entity.
 */
@Entity
@Table(name = "purchases", indexes = @Index(name = "idx_purchase_user_chapter", columnList = "user_id, chapter_id"))
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    // Price paid AT PURCHASE TIME. If the admin changes the chapter price later, history stays correct.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseStatus status;

    // Id returned by the payment provider (useful for refunds / support with a real gateway).
    @Column(length = 100)
    private String paymentReference;

    @Column(nullable = false, updatable = false)
    private Instant purchasedAt = Instant.now();

    protected Purchase() { } // required by JPA

    public Purchase(User user, Chapter chapter, BigDecimal price, PurchaseStatus status, String paymentReference) {
        this.user = user;
        this.chapter = chapter;
        this.price = price;
        this.status = status;
        this.paymentReference = paymentReference;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Chapter getChapter() { return chapter; }
    public BigDecimal getPrice() { return price; }
    public PurchaseStatus getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getPurchasedAt() { return purchasedAt; }
}
