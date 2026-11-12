package hr.algebra.librarylendingsystem.web;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.CategoryQueryService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.dto.CategoryOption;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import hr.algebra.librarylendingsystem.web.form.BookTitleForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/catalog")
public class BookTitleManagementController {

    private final BookTitleService bookTitleService;
    private final BookCopyService bookCopyService;
    private final CategoryQueryService categoryQueryService;

    public BookTitleManagementController(
            BookTitleService bookTitleService,
            BookCopyService bookCopyService,
            CategoryQueryService categoryQueryService
    ) {
        this.bookTitleService = bookTitleService;
        this.bookCopyService = bookCopyService;
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bookTitleForm", new BookTitleForm());
        addFormOptions(model);

        return "catalog/create";
    }

    @PostMapping("/new")
    public String createBookTitle(
            @Valid @ModelAttribute("bookTitleForm") BookTitleForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validatePrimaryCategory(form, bindingResult);

        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "catalog/create";
        }

        BookTitleDetails createdBook;

        try {
            createdBook = bookTitleService.create(toCommand(form));
        } catch (InvalidBookTitleException exception) {
            bindingResult.reject(
                    "invalid.bookTitle",
                    exception.getMessage()
            );

            addFormOptions(model);
            return "catalog/create";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Book title created successfully."
        );

        return "redirect:/catalog/" + createdBook.id();
    }

    @GetMapping("/{id}")
    public String showBookTitleDetails(
            @PathVariable Long id,
            Model model
    ) {
        BookTitleDetails book = findBookTitle(id);

        List<CategoryOption> selectedCategories =
                categoryQueryService.findAll()
                        .stream()
                        .filter(category ->
                                book.categoryIds().contains(category.id())
                        )
                        .toList();

        List<BookCopyDetails> copies =
                bookCopyService.findByBookTitleId(id);

        long availableCopyCount = copies.stream()
                .filter(copy -> "AVAILABLE".equals(copy.status()))
                .count();

        model.addAttribute("book", book);
        model.addAttribute("selectedCategories", selectedCategories);
        model.addAttribute("copies", copies);
        model.addAttribute("availableCopyCount", availableCopyCount);

        return "catalog/details";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model
    ) {
        BookTitleDetails book = findBookTitle(id);

        BookTitleForm form = new BookTitleForm();
        form.setTitle(book.title());
        form.setAuthors(book.authors());
        form.setIsbn(book.isbn());
        form.setPublisher(book.publisher());
        form.setPublicationYear(book.publicationYear());
        form.setDescription(book.description());
        form.setCategoryIds(book.categoryIds());
        form.setPrimaryCategoryId(book.primaryCategoryId());

        model.addAttribute("bookTitleForm", form);
        model.addAttribute("bookId", book.id());
        addFormOptions(model);

        return "catalog/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateBookTitle(
            @PathVariable Long id,
            @Valid @ModelAttribute("bookTitleForm") BookTitleForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validatePrimaryCategory(form, bindingResult);

        if (bindingResult.hasErrors()) {
            addEditFormOptions(model, id);
            return "catalog/edit";
        }

        try {
            bookTitleService.update(id, toCommand(form));
        } catch (InvalidBookTitleException exception) {
            bindingResult.reject(
                    "invalid.bookTitle",
                    exception.getMessage()
            );

            addEditFormOptions(model, id);
            return "catalog/edit";
        } catch (BookTitleNotFoundException exception) {
            throw notFound(exception);
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Book title updated successfully."
        );

        return "redirect:/catalog/" + id;
    }

    private BookTitleDetails findBookTitle(Long id) {
        try {
            return bookTitleService.findById(id);
        } catch (BookTitleNotFoundException exception) {
            throw notFound(exception);
        }
    }

    private ResponseStatusException notFound(
            BookTitleNotFoundException exception
    ) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Book title not found",
                exception
        );
    }

    private void validatePrimaryCategory(
            BookTitleForm form,
            BindingResult bindingResult
    ) {
        if (!bindingResult.hasErrors()
                && !form.getCategoryIds().contains(
                form.getPrimaryCategoryId()
        )) {
            bindingResult.rejectValue(
                    "primaryCategoryId",
                    "invalid.primaryCategory",
                    "The primary category must be one of the selected categories."
            );
        }
    }

    private BookTitleCommand toCommand(BookTitleForm form) {
        return new BookTitleCommand(
                form.getTitle(),
                form.getAuthors(),
                form.getIsbn(),
                form.getPublisher(),
                form.getPublicationYear(),
                form.getDescription(),
                form.getCategoryIds(),
                form.getPrimaryCategoryId()
        );
    }

    private void addEditFormOptions(Model model, Long id) {
        model.addAttribute("bookId", id);
        addFormOptions(model);
    }

    private void addFormOptions(Model model) {
        model.addAttribute(
                "categories",
                categoryQueryService.findAll()
        );
    }
}
