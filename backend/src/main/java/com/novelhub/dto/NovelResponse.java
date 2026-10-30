package com.novelhub.dto;

import com.novelhub.entity.Novel;

public record NovelResponse(Long id, String title, String author, String description, String coverImage, long chapterCount) {

    public static NovelResponse from(Novel novel, long chapterCount) {
        return new NovelResponse(novel.getId(), novel.getTitle(), novel.getAuthor(),
                novel.getDescription(), novel.getCoverImage(), chapterCount);
    }
}
