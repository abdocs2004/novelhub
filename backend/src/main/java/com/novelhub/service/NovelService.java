package com.novelhub.service;

import com.novelhub.dto.ChapterSummaryResponse;
import com.novelhub.dto.NovelDetailResponse;
import com.novelhub.dto.NovelRequest;
import com.novelhub.dto.NovelResponse;
import com.novelhub.entity.Chapter;
import com.novelhub.entity.Novel;
import com.novelhub.entity.PurchaseStatus;
import com.novelhub.exception.ResourceNotFoundException;
import com.novelhub.repository.ChapterRepository;
import com.novelhub.repository.NovelRepository;
import com.novelhub.repository.PurchaseRepository;
import com.novelhub.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NovelService {

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final PurchaseRepository purchaseRepository;

    public NovelService(NovelRepository novelRepository,
                        ChapterRepository chapterRepository,
                        PurchaseRepository purchaseRepository) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.purchaseRepository = purchaseRepository;
    }

    // readOnly = true: tells Hibernate/DB we will not modify anything (small optimization + safety).
    @Transactional(readOnly = true)
    public List<NovelResponse> listAll() {
        Map<Long, Long> counts = chapterRepository.countByNovel().stream()
                .collect(Collectors.toMap(ChapterRepository.ChapterCount::getNovelId, ChapterRepository.ChapterCount::getTotal));

        return novelRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(novel -> NovelResponse.from(novel, counts.getOrDefault(novel.getId(), 0L)))
                .toList();
    }

    /**
     * Novel page: the novel + its chapter list (WITHOUT content).
     * "viewer" is null for anonymous visitors. It is used only to compute which chapters are unlocked.
     */
    @Transactional(readOnly = true)
    public NovelDetailResponse getDetails(Long novelId, UserPrincipal viewer) {
        Novel novel = findOrThrow(novelId);
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId);

        Set<Long> ownedChapterIds = new HashSet<>();
        if (viewer != null && !viewer.isAdmin()) {
            ownedChapterIds.addAll(purchaseRepository.findPurchasedChapterIds(viewer.getId(), novelId, PurchaseStatus.SUCCESS));
        }
        boolean isAdmin = viewer != null && viewer.isAdmin();

        List<ChapterSummaryResponse> chapterDtos = chapters.stream()
                .map(c -> ChapterSummaryResponse.from(c, isAdmin || c.isFree() || ownedChapterIds.contains(c.getId())))
                .toList();

        return new NovelDetailResponse(NovelResponse.from(novel, chapters.size()), chapterDtos);
    }

    @Transactional
    public NovelResponse create(NovelRequest request) {
        Novel novel = new Novel();
        apply(novel, request);
        return NovelResponse.from(novelRepository.save(novel), 0);
    }

    @Transactional
    public NovelResponse update(Long id, NovelRequest request) {
        Novel novel = findOrThrow(id);
        apply(novel, request); // no save() needed: the entity is "managed", Hibernate detects the change and runs UPDATE at commit
        long chapterCount = chapterRepository.findByNovelIdOrderByChapterNumberAsc(id).size();
        return NovelResponse.from(novel, chapterCount);
    }

    @Transactional
    public void delete(Long id) {
        Novel novel = findOrThrow(id);
        // Purchases reference chapters through a foreign key, so they must go first.
        // (A real product would probably archive the novel instead of deleting paid content.)
        purchaseRepository.deleteByNovelId(id);
        novelRepository.delete(novel); // cascade = ALL also deletes the chapters
    }

    private Novel findOrThrow(Long id) {
        return novelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Novel not found"));
    }

    private void apply(Novel novel, NovelRequest request) {
        novel.setTitle(request.title().trim());
        novel.setAuthor(request.author().trim());
        novel.setDescription(blankToNull(request.description()));
        novel.setCoverImage(blankToNull(request.coverImage()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
