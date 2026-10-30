package com.novelhub.dto;

import com.novelhub.entity.Chapter;

import java.math.BigDecimal;

/**
 * A chapter as shown in the chapter LIST of a novel. It intentionally has NO content field:
 * the text is only ever sent by GET /api/chapters/{id} after the access check.
 * "unlocked" = free, or bought by the current user (or the viewer is an admin).
 */
public record ChapterSummaryResponse(Long id, int chapterNumber, String title, BigDecimal price, boolean free, boolean unlocked) {

    public static ChapterSummaryResponse from(Chapter chapter, boolean unlocked) {
        return new ChapterSummaryResponse(chapter.getId(), chapter.getChapterNumber(), chapter.getTitle(),
                chapter.getPrice(), chapter.isFree(), unlocked);
    }
}
