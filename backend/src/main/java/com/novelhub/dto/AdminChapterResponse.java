package com.novelhub.dto;

import com.novelhub.entity.Chapter;

import java.math.BigDecimal;

/** Chapter as seen in the admin panel (includes content so it can be edited). */
public record AdminChapterResponse(Long id, Long novelId, int chapterNumber, String title, String content, BigDecimal price) {

    public static AdminChapterResponse from(Chapter c) {
        return new AdminChapterResponse(c.getId(), c.getNovel().getId(), c.getChapterNumber(),
                c.getTitle(), c.getContent(), c.getPrice());
    }
}
