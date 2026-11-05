package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookcategories")
public class BookCategory {

    @EmbeddedId
    private BookCategoryId id = new BookCategoryId(null, null);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bookTitleId")
    @JoinColumn(name = "booktitleid", nullable = false)
    private BookTitle bookTitle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("categoryId")
    @JoinColumn(name = "categoryid", nullable = false)
    private Category category;

    @Column(name = "isprimary", nullable = false)
    private boolean primary;

    protected BookCategory() {
        // Required by JPA.
    }

    public BookCategory(
            BookTitle bookTitle,
            Category category,
            boolean primary
    ) {
        if (bookTitle == null) {
            throw new IllegalArgumentException(
                    "Book title cannot be null"
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category cannot be null"
            );
        }

        this.bookTitle = bookTitle;
        this.category = category;
        this.primary = primary;
    }

    public BookCategoryId getId() {
        return id;
    }

    public BookTitle getBookTitle() {
        return bookTitle;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
