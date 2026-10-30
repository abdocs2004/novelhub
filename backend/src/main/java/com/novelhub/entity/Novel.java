package com.novelhub.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "novels")
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    // URL of the cover image (optional). We only store the link, not the file.
    @Column(length = 500)
    private String coverImage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * One novel has many chapters. "mappedBy" means the FOREIGN KEY lives in the chapters table
     * (Chapter.novel is the owning side). cascade = ALL: saving/deleting a novel also saves/deletes
     * its chapters. orphanRemoval: removing a chapter from this list deletes it from the DB.
     */
    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chapterNumber ASC")
    private List<Chapter> chapters = new ArrayList<>();

    /** Keeps both sides of the relation in sync. */
    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
        chapter.setNovel(this);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Chapter> getChapters() { return chapters; }
}
