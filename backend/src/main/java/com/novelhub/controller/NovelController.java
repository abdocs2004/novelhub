package com.novelhub.controller;

import com.novelhub.dto.NovelDetailResponse;
import com.novelhub.dto.NovelResponse;
import com.novelhub.security.UserPrincipal;
import com.novelhub.service.NovelService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public browsing endpoints (no login needed, see SecurityConfig). */
@RestController
@RequestMapping("/api/novels")
public class NovelController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping
    public List<NovelResponse> list() {
        return novelService.listAll();
    }

    // "viewer" is null when the visitor is anonymous, otherwise the logged-in user.
    @GetMapping("/{id}")
    public NovelDetailResponse details(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal viewer) {
        return novelService.getDetails(id, viewer);
    }
}
