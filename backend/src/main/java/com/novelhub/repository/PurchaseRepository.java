package com.novelhub.repository;

import com.novelhub.entity.Purchase;
import com.novelhub.entity.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** THE access-control query: did this user successfully buy this chapter? */
    boolean existsByUserIdAndChapterIdAndStatus(Long userId, Long chapterId, PurchaseStatus status);

    // My Library: the user's successful purchases, with user + chapter + novel loaded in a single query.
    @Query("""
            select p from Purchase p
            join fetch p.user
            join fetch p.chapter c
            join fetch c.novel
            where p.user.id = :userId and p.status = :status
            order by p.purchasedAt desc
            """)
    List<Purchase> findByUser(@Param("userId") Long userId, @Param("status") PurchaseStatus status);

    // Ids of the chapters (of one novel) that the user already owns. Used by the novel details page.
    @Query("""
            select p.chapter.id from Purchase p
            where p.user.id = :userId and p.chapter.novel.id = :novelId and p.status = :status
            """)
    List<Long> findPurchasedChapterIds(@Param("userId") Long userId,
                                       @Param("novelId") Long novelId,
                                       @Param("status") PurchaseStatus status);

    // Admin: every purchase with the user, chapter and novel loaded.
    @Query("""
            select p from Purchase p
            join fetch p.user
            join fetch p.chapter c
            join fetch c.novel
            order by p.purchasedAt desc
            """)
    List<Purchase> findAllWithDetails();

    // Bulk deletes: needed before deleting a chapter/novel, otherwise the foreign key blocks the delete.
    @Modifying(flushAutomatically = true)
    @Query("delete from Purchase p where p.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Long chapterId);

    @Modifying(flushAutomatically = true)
    @Query("delete from Purchase p where p.chapter.id in (select c.id from Chapter c where c.novel.id = :novelId)")
    void deleteByNovelId(@Param("novelId") Long novelId);
}
