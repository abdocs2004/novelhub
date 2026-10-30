package com.novelhub.service;

import com.novelhub.dto.PurchaseResponse;
import com.novelhub.entity.Chapter;
import com.novelhub.entity.Purchase;
import com.novelhub.entity.PurchaseStatus;
import com.novelhub.entity.User;
import com.novelhub.exception.BadRequestException;
import com.novelhub.exception.ConflictException;
import com.novelhub.exception.ResourceNotFoundException;
import com.novelhub.repository.ChapterRepository;
import com.novelhub.repository.PurchaseRepository;
import com.novelhub.repository.UserRepository;
import com.novelhub.service.payment.PaymentRequest;
import com.novelhub.service.payment.PaymentResult;
import com.novelhub.service.payment.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseService {

    private static final String CURRENCY = "USD";

    private final PurchaseRepository purchaseRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService; // interface: today the mock, tomorrow a real gateway

    public PurchaseService(PurchaseRepository purchaseRepository,
                           ChapterRepository chapterRepository,
                           UserRepository userRepository,
                           PaymentService paymentService) {
        this.purchaseRepository = purchaseRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    /**
     * Buying a chapter:
     *  1. the chapter must exist and must be paid
     *  2. the user must not already own it
     *  3. charge through PaymentService
     *  4. save a Purchase row (SUCCESS or FAILED)
     * @Transactional: everything runs in one DB transaction (all-or-nothing).
     * (With a real gateway you would NOT keep a DB transaction open during a slow network call; that is
     * fine for this learning version.)
     */
    @Transactional
    public PurchaseResponse purchase(Long userId, Long chapterId) {
        Chapter chapter = chapterRepository.findWithNovelById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        if (chapter.isFree()) {
            throw new BadRequestException("This chapter is free, there is nothing to buy");
        }
        if (purchaseRepository.existsByUserIdAndChapterIdAndStatus(userId, chapterId, PurchaseStatus.SUCCESS)) {
            throw new ConflictException("You already own this chapter");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // The price comes from OUR database, never from the client (the client could send any number).
        PaymentResult result = paymentService.charge(
                new PaymentRequest(userId, chapterId, chapter.getPrice(), CURRENCY));

        PurchaseStatus status = result.success() ? PurchaseStatus.SUCCESS : PurchaseStatus.FAILED;
        Purchase purchase = purchaseRepository.save(
                new Purchase(user, chapter, chapter.getPrice(), status, result.reference()));

        return PurchaseResponse.from(purchase);
    }

    /** "My Library": chapters the user bought successfully, newest first. */
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getLibrary(Long userId) {
        return purchaseRepository.findByUser(userId, PurchaseStatus.SUCCESS).stream()
                .map(PurchaseResponse::from)
                .toList();
    }

    /** Admin: all purchases of all users. */
    @Transactional(readOnly = true)
    public List<PurchaseResponse> listAll() {
        return purchaseRepository.findAllWithDetails().stream()
                .map(PurchaseResponse::from)
                .toList();
    }
}
