package com.novelhub.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "chapters",
        // A novel cannot have two chapters with the same number.
        uniqueConstraints = @UniqueConstraint(columnNames = {"novel_id", "chapter_number"})
)
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Money => BigDecimal, never double/float. price == 0 means the chapter is free.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;

    /**
     * Many chapters belong to one novel. This is the OWNING side: it creates the "novel_id" column.
     * LAZY = the novel is loaded from the DB only when we actually access it.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    public boolean isFree() {
        return price.compareTo(BigDecimal.ZERO) == 0;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getChapterNumber() { return chapterNumber; }
    public void setChapterNumber(int chapterNumber) { this.chapterNumber = chapterNumber; }
    public Novel getNovel() { return novel; }
    public void setNovel(Novel novel) { this.novel = novel; }
}
