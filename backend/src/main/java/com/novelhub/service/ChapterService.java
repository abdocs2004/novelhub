package com.novelhub.service;

import com.novelhub.dto.AdminChapterResponse;
import com.novelhub.dto.ChapterContentResponse;
import com.novelhub.dto.ChapterRequest;
import com.novelhub.entity.Chapter;
import com.novelhub.entity.Novel;
import com.novelhub.entity.PurchaseStatus;
import com.novelhub.exception.ChapterLockedException;
import com.novelhub.exception.ConflictException;
import com.novelhub.exception.ResourceNotFoundException;
import com.novelhub.repository.ChapterRepository;
import com.novelhub.repository.NovelRepository;
import com.novelhub.repository.PurchaseRepository;
import com.novelhub.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final PurchaseRepository purchaseRepository;

    public ChapterService(ChapterRepository chapterRepository,
                          NovelRepository novelRepository,
                          PurchaseRepository purchaseRepository) {
        this.chapterRepository = chapterRepository;
        this.novelRepository = novelRepository;
        this.purchaseRepository = purchaseRepository;
    }

    // ------------------------------------------------------------------
    //  READING  (the most important method of the project)
    // ------------------------------------------------------------------

    /**
     * Returns the chapter text ONLY if the viewer is allowed to read it.
     * Step 1 (is the user logged in?) was already enforced by Spring Security: this URL requires authentication,
     * so a "viewer" always exists here. The remaining steps are business rules, so they live in the service.
     */
    @Transactional(readOnly = true)
    public ChapterContentResponse read(Long chapterId, UserPrincipal viewer) {
        Chapter chapter = chapterRepository.findWithNovelById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        assertCanRead(chapter, viewer); // throws BEFORE any content is put in a response

        Long novelId = chapter.getNovel().getId();
        Long previousId = chapterRepository
                .findFirstByNovelIdAndChapterNumberLessThanOrderByChapterNumberDesc(novelId, chapter.getChapterNumber())
                .map(Chapter::getId).orElse(null);
        Long nextId = chapterRepository
                .findFirstByNovelIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(novelId, chapter.getChapterNumber())
                .map(Chapter::getId).orElse(null);

        return ChapterContentResponse.from(chapter, previousId, nextId);
    }

    private void assertCanRead(Chapter chapter, UserPrincipal viewer) {
        // 2. Free chapter? Anyone logged in can read it.
        if (chapter.isFree()) {
            return;
        }
        // Admins can preview everything (they manage the content).
        if (viewer.isAdmin()) {
            return;
        }
        // 3. Paid chapter: does this user have a SUCCESSFUL purchase for it?
        boolean purchased = purchaseRepository.existsByUserIdAndChapterIdAndStatus(
                viewer.getId(), chapter.getId(), PurchaseStatus.SUCCESS);

        // 5. No purchase => 403 and NO content.
        if (!purchased) {
            throw new ChapterLockedException("You need to purchase this chapter to read it");
        }
        // 4. Purchased => continue and return the content.
    }

    // ------------------------------------------------------------------
    //  ADMIN CRUD
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AdminChapterResponse> listForAdmin(Long novelId) {
        if (!novelRepository.existsById(novelId)) {
            throw new ResourceNotFoundException("Novel not found");
        }
        return chapterRepository.findByNovelIdOrderByChapterNumberAsc(novelId).stream()
                .map(AdminChapterResponse::from)
                .toList();
    }

    @Transactional
    public AdminChapterResponse create(Long novelId, ChapterRequest request) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResourceNotFoundException("Novel not found"));

        if (chapterRepository.existsByNovelIdAndChapterNumber(novelId, request.chapterNumber())) {
            throw new ConflictException("This novel already has a chapter number " + request.chapterNumber());
        }

        Chapter chapter = new Chapter();
        apply(chapter, request);
        chapter.setNovel(novel);
        return AdminChapterResponse.from(chapterRepository.save(chapter));
    }

    @Transactional
    public AdminChapterResponse update(Long chapterId, ChapterRequest request) {
        Chapter chapter = findOrThrow(chapterId);

        Long novelId = chapter.getNovel().getId();
        if (chapterRepository.existsByNovelIdAndChapterNumberAndIdNot(novelId, request.chapterNumber(), chapterId)) {
            throw new ConflictException("This novel already has a chapter number " + request.chapterNumber());
        }

        apply(chapter, request);
        return AdminChapterResponse.from(chapter);
    }

    @Transactional
    public void delete(Long chapterId) {
        Chapter chapter = findOrThrow(chapterId);
        purchaseRepository.deleteByChapterId(chapterId); // remove purchases first (foreign key)
        chapterRepository.delete(chapter);
    }

    private Chapter findOrThrow(Long id) {
        return chapterRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));
    }

    private void apply(Chapter chapter, ChapterRequest request) {
        chapter.setTitle(request.title().trim());
        chapter.setContent(request.content());
        chapter.setPrice(request.price());
        chapter.setChapterNumber(request.chapterNumber());
    }
}
