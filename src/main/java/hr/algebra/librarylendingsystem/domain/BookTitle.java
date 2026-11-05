package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "booktitles")
public class BookTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "authors", nullable = false, length = 500)
    private String authors;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "publicationyear")
    private Integer publicationYear;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "createdat", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updatedat", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "bookTitle", fetch = FetchType.LAZY)
    private List<BookCopy> copies = new ArrayList<>();

    protected BookTitle() {
        // Required by JPA.
    }

    public BookTitle(String title, String authors) {
        this.title = title;
        this.authors = authors;
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<BookCopy> getCopies() {
        return Collections.unmodifiableList(copies);
    }

    public void addCopy(BookCopy copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Book copy cannot be null");
        }

        if (copy.getBookTitle() != null
                && copy.getBookTitle() != this) {
            throw new IllegalArgumentException(
                    "Book copy already belongs to another title"
            );
        }

        if (!copies.contains(copy)) {
            copies.add(copy);
        }

        copy.setBookTitle(this);
    }
}
