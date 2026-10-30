package com.novelhub.controller;

import com.novelhub.dto.AdminChapterResponse;
import com.novelhub.dto.ChapterRequest;
import com.novelhub.dto.NovelRequest;
import com.novelhub.dto.NovelResponse;
import com.novelhub.dto.PurchaseResponse;
import com.novelhub.dto.UserResponse;
import com.novelhub.service.ChapterService;
import com.novelhub.service.NovelService;
import com.novelhub.service.PurchaseService;
import com.novelhub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Everything under /api/admin/** is restricted to the ADMIN role in SecurityConfig
 * (.requestMatchers("/api/admin/**").hasRole("ADMIN")), so the checks live in ONE place
 * and a USER token gets 403 before any of this code runs.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final PurchaseService purchaseService;
    private final UserService userService;

    public AdminController(NovelService novelService,
                           ChapterService chapterService,
                           PurchaseService purchaseService,
                           UserService userService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.purchaseService = purchaseService;
        this.userService = userService;
    }

    // ---------- Novels ----------

    @PostMapping("/novels")
    @ResponseStatus(HttpStatus.CREATED)
    public NovelResponse createNovel(@Valid @RequestBody NovelRequest request) {
        return novelService.create(request);
    }

    @PutMapping("/novels/{id}")
    public NovelResponse updateNovel(@PathVariable Long id, @Valid @RequestBody NovelRequest request) {
        return novelService.update(id, request);
    }

    @DeleteMapping("/novels/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNovel(@PathVariable Long id) {
        novelService.delete(id);
    }

    // ---------- Chapters ----------

    @GetMapping("/novels/{novelId}/chapters")
    public List<AdminChapterResponse> listChapters(@PathVariable Long novelId) {
        return chapterService.listForAdmin(novelId);
    }

    @PostMapping("/novels/{novelId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminChapterResponse createChapter(@PathVariable Long novelId, @Valid @RequestBody ChapterRequest request) {
        return chapterService.create(novelId, request);
    }

    @PutMapping("/chapters/{id}")
    public AdminChapterResponse updateChapter(@PathVariable Long id, @Valid @RequestBody ChapterRequest request) {
        return chapterService.update(id, request);
    }

    @DeleteMapping("/chapters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChapter(@PathVariable Long id) {
        chapterService.delete(id);
    }

    // ---------- Users & purchases ----------

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userService.listAll();
    }

    @GetMapping("/purchases")
    public List<PurchaseResponse> purchases() {
        return purchaseService.listAll();
    }
}
