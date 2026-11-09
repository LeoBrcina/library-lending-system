package hr.algebra.librarylendingsystem.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class BookTitleForm {

    @NotBlank(message = "Title is required.")
    @Size(max = 255, message = "Title must not exceed 255 characters.")
    private String title;

    @NotBlank(message = "Authors are required.")
    @Size(max = 500, message = "Authors must not exceed 500 characters.")
    private String authors;

    @Size(max = 20, message = "ISBN must not exceed 20 characters.")
    private String isbn;

    @Size(max = 255, message = "Publisher must not exceed 255 characters.")
    private String publisher;

    @Min(value = 1450, message = "Publication year must be 1450 or later.")
    @Max(value = 2200, message = "Publication year must not exceed 2200.")
    private Integer publicationYear;

    private String description;

    @NotEmpty(message = "Select at least one category.")
    private List<Long> categoryIds = new ArrayList<>();

    @NotNull(message = "Select a primary category.")
    private Long primaryCategoryId;

    public BookTitleForm() {
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Long getPrimaryCategoryId() {
        return primaryCategoryId;
    }

    public void setPrimaryCategoryId(Long primaryCategoryId) {
        this.primaryCategoryId = primaryCategoryId;
    }
}
