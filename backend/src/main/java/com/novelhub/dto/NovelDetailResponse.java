package com.novelhub.dto;

import java.util.List;

public record NovelDetailResponse(NovelResponse novel, List<ChapterSummaryResponse> chapters) {
}
