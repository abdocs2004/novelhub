package com.novelhub.controller;

import com.novelhub.dto.ChapterContentResponse;
import com.novelhub.security.UserPrincipal;
import com.novelhub.service.ChapterService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    /**
     * Requires login (401 otherwise, from Spring Security).
     * The service then returns the text, or throws ChapterLockedException (403) if it is paid and not purchased.
     */
    @GetMapping("/{id}")
    public ChapterContentResponse read(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal viewer) {
        return chapterService.read(id, viewer);
    }
}
