package com.novelhub.dto;

import com.novelhub.entity.Purchase;
import com.novelhub.entity.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PurchaseResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        Long novelId,
        String novelTitle,
        Long chapterId,
        int chapterNumber,
        String chapterTitle,
        BigDecimal price,
        PurchaseStatus status,
        Instant purchasedAt
) {
    public static PurchaseResponse from(Purchase p) {
        return new PurchaseResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getName(),
                p.getUser().getEmail(),
                p.getChapter().getNovel().getId(),
                p.getChapter().getNovel().getTitle(),
                p.getChapter().getId(),
                p.getChapter().getChapterNumber(),
                p.getChapter().getTitle(),
                p.getPrice(),
                p.getStatus(),
                p.getPurchasedAt());
    }
}
