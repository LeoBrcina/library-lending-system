package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BookCategoryId implements Serializable {

    @Column(name = "booktitleid")
    private Long bookTitleId;

    @Column(name = "categoryid")
    private Long categoryId;

    protected BookCategoryId() {
        // Required by JPA.
    }

    public BookCategoryId(Long bookTitleId, Long categoryId) {
        this.bookTitleId = bookTitleId;
        this.categoryId = categoryId;
    }

    public Long getBookTitleId() {
        return bookTitleId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof BookCategoryId that)) {
            return false;
        }

        return Objects.equals(bookTitleId, that.bookTitleId)
                && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookTitleId, categoryId);
    }
}
