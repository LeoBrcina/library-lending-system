package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.domain.BookCategory;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.domain.Category;
import hr.algebra.librarylendingsystem.repository.BookCategoryRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookTitleServiceImpl implements BookTitleService {

    private final BookTitleRepository bookTitleRepository;
    private final CategoryRepository categoryRepository;
    private final BookCategoryRepository bookCategoryRepository;

    public BookTitleServiceImpl(
            BookTitleRepository bookTitleRepository,
            CategoryRepository categoryRepository,
            BookCategoryRepository bookCategoryRepository
    ) {
        this.bookTitleRepository = bookTitleRepository;
        this.categoryRepository = categoryRepository;
        this.bookCategoryRepository = bookCategoryRepository;
    }

    @Override
    @Transactional
    public BookTitleDetails create(BookTitleCommand command) {
        validateCommand(command);

        Map<Long, Category> categories = resolveCategories(command);

        BookTitle title = new BookTitle(
                command.title().trim(),
                command.authors().trim()
        );

        applyMetadata(title, command);

        title = bookTitleRepository.saveAndFlush(title);

        synchronizeCategories(
                title,
                categories,
                command.primaryCategoryId()
        );

        return toDetails(title);
    }

    @Override
    @Transactional(readOnly = true)
    public BookTitleDetails findById(Long id) {
        BookTitle title = findTitle(id);
        return toDetails(title);
    }

    @Override
    @Transactional
    public BookTitleDetails update(
            Long id,
            BookTitleCommand command
    ) {
        BookTitle title = findTitle(id);

        validateCommand(command);

        Map<Long, Category> categories = resolveCategories(command);

        applyMetadata(title, command);

        synchronizeCategories(
                title,
                categories,
                command.primaryCategoryId()
        );

        return toDetails(title);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        BookTitle title = findTitle(id);
        title.setActive(false);
    }

    private BookTitle findTitle(Long id) {
        if (id == null || id <= 0) {
            throw new BookTitleNotFoundException(id);
        }

        return bookTitleRepository.findById(id)
                .orElseThrow(
                        () -> new BookTitleNotFoundException(id)
                );
    }

    private void validateCommand(BookTitleCommand command) {
        if (command == null) {
            throw new InvalidBookTitleException(
                    "Book title data is required."
            );
        }

        validateRequiredText(command.title(), 255, "Title");
        validateRequiredText(command.authors(), 500, "Authors");

        validateOptionalText(command.isbn(), 20, "ISBN");
        validateOptionalText(command.publisher(), 255, "Publisher");

        Integer year = command.publicationYear();

        if (year != null && (year < 1450 || year > 2200)) {
            throw new InvalidBookTitleException(
                    "Publication year must be between 1450 and 2200."
            );
        }

        List<Long> categoryIds = command.categoryIds();

        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new InvalidBookTitleException(
                    "Select at least one category."
            );
        }

        for (Long categoryId : categoryIds) {
            if (categoryId == null || categoryId <= 0) {
                throw new InvalidBookTitleException(
                        "Category identifiers must be valid."
                );
            }
        }

        Long primaryCategoryId = command.primaryCategoryId();

        if (primaryCategoryId == null
                || !categoryIds.contains(primaryCategoryId)) {
            throw new InvalidBookTitleException(
                    "The primary category must be one of the selected categories."
            );
        }
    }

    private void validateRequiredText(
            String value,
            int maximumLength,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new InvalidBookTitleException(
                    fieldName + " is required."
            );
        }

        if (value.trim().length() > maximumLength) {
            throw new InvalidBookTitleException(
                    fieldName + " exceeds " + maximumLength + " characters."
            );
        }
    }

    private void validateOptionalText(
            String value,
            int maximumLength,
            String fieldName
    ) {
        if (value != null
                && value.trim().length() > maximumLength) {
            throw new InvalidBookTitleException(
                    fieldName + " exceeds " + maximumLength + " characters."
            );
        }
    }

    private Map<Long, Category> resolveCategories(
            BookTitleCommand command
    ) {
        Set<Long> selectedIds =
                new LinkedHashSet<>(command.categoryIds());

        List<Category> foundCategories =
                categoryRepository.findAllById(selectedIds);

        Map<Long, Category> categories = new LinkedHashMap<>();

        for (Category category : foundCategories) {
            categories.put(category.getId(), category);
        }

        if (categories.size() != selectedIds.size()) {
            throw new InvalidBookTitleException(
                    "One or more selected categories do not exist."
            );
        }

        return categories;
    }

    private void applyMetadata(
            BookTitle title,
            BookTitleCommand command
    ) {
        title.setTitle(command.title().trim());
        title.setAuthors(command.authors().trim());

        title.setIsbn(trimToNull(command.isbn()));
        title.setPublisher(trimToNull(command.publisher()));
        title.setPublicationYear(command.publicationYear());
        title.setDescription(trimToNull(command.description()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void synchronizeCategories(
            BookTitle title,
            Map<Long, Category> selectedCategories,
            Long primaryCategoryId
    ) {
        List<BookCategory> existingAssociations =
                bookCategoryRepository.findByBookTitle_Id(title.getId());

        Map<Long, BookCategory> existingByCategoryId =
                new HashMap<>();

        for (BookCategory association : existingAssociations) {
            existingByCategoryId.put(
                    association.getCategory().getId(),
                    association
            );
        }

        // First remove the previous primary designation.
        // PostgreSQL allows at most one primary category per title.
        boolean primaryChanged = false;

        for (BookCategory association : existingAssociations) {
            Long categoryId = association.getCategory().getId();

            if (association.isPrimary()
                    && !categoryId.equals(primaryCategoryId)) {
                association.setPrimary(false);
                primaryChanged = true;
            }
        }

        if (primaryChanged) {
            bookCategoryRepository.flush();
        }

        // Remove category associations that are no longer selected.
        List<BookCategory> associationsToRemove = new ArrayList<>();

        for (BookCategory association : existingAssociations) {
            Long categoryId = association.getCategory().getId();

            if (!selectedCategories.containsKey(categoryId)) {
                associationsToRemove.add(association);
                existingByCategoryId.remove(categoryId);
            }
        }

        if (!associationsToRemove.isEmpty()) {
            bookCategoryRepository.deleteAll(associationsToRemove);
            bookCategoryRepository.flush();
        }

        // Retain existing associations and create only missing ones.
        for (Map.Entry<Long, Category> entry
                : selectedCategories.entrySet()) {

            Long categoryId = entry.getKey();
            BookCategory association =
                    existingByCategoryId.get(categoryId);

            boolean shouldBePrimary =
                    categoryId.equals(primaryCategoryId);

            if (association == null) {
                BookCategory newAssociation = new BookCategory(
                        title,
                        entry.getValue(),
                        shouldBePrimary
                );

                bookCategoryRepository.save(newAssociation);
            } else {
                association.setPrimary(shouldBePrimary);
            }
        }

        bookCategoryRepository.flush();
    }

    private BookTitleDetails toDetails(BookTitle title) {
        List<BookCategory> associations =
                bookCategoryRepository.findByBookTitle_Id(title.getId());

        List<Long> categoryIds = associations.stream()
                .map(association -> association.getCategory().getId())
                .sorted()
                .toList();

        Long primaryCategoryId = associations.stream()
                .filter(BookCategory::isPrimary)
                .map(association -> association.getCategory().getId())
                .findFirst()
                .orElse(null);

        return new BookTitleDetails(
                title.getId(),
                title.getTitle(),
                title.getAuthors(),
                title.getIsbn(),
                title.getPublisher(),
                title.getPublicationYear(),
                title.getDescription(),
                title.isActive(),
                categoryIds,
                primaryCategoryId
        );
    }
}
