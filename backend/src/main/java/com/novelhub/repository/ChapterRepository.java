package com.novelhub.repository;

import com.novelhub.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);

    boolean existsByNovelIdAndChapterNumber(Long novelId, int chapterNumber);

    boolean existsByNovelIdAndChapterNumberAndIdNot(Long novelId, int chapterNumber, Long id);

    // Neighbour chapters, used for the "Previous / Next" buttons in the reader.
    Optional<Chapter> findFirstByNovelIdAndChapterNumberLessThanOrderByChapterNumberDesc(Long novelId, int chapterNumber);

    Optional<Chapter> findFirstByNovelIdAndChapterNumberGreaterThanOrderByChapterNumberAsc(Long novelId, int chapterNumber);

    // "join fetch" loads the chapter AND its novel in one SQL query (avoids a second lazy query).
    @Query("select c from Chapter c join fetch c.novel where c.id = :id")
    Optional<Chapter> findWithNovelById(@Param("id") Long id);

    // One query that returns the chapter count of every novel (instead of one query per novel).
    @Query("select c.novel.id as novelId, count(c) as total from Chapter c group by c.novel.id")
    List<ChapterCount> countByNovel();

    /** Interface projection: Spring fills the getters from the aliases used in the query above. */
    interface ChapterCount {
        Long getNovelId();

        Long getTotal();
    }
}
