package com.novelhub.dto;

import com.novelhub.entity.Chapter;

import java.math.BigDecimal;

/** A chapter WITH its text. Only created after the access check passed. */
public record ChapterContentResponse(
        Long id,
        Long novelId,
        String novelTitle,
        int chapterNumber,
        String title,
        String content,
        BigDecimal price,
        Long previousChapterId,
        Long nextChapterId
) {
    public static ChapterContentResponse from(Chapter c, Long previousChapterId, Long nextChapterId) {
        return new ChapterContentResponse(c.getId(), c.getNovel().getId(), c.getNovel().getTitle(),
                c.getChapterNumber(), c.getTitle(), c.getContent(), c.getPrice(), previousChapterId, nextChapterId);
    }
}
