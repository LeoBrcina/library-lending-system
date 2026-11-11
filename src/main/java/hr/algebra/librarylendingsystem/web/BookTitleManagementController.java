package hr.algebra.librarylendingsystem.web;

import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.CategoryQueryService;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import hr.algebra.librarylendingsystem.web.form.BookTitleForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/catalog")
public class BookTitleManagementController {

    private final BookTitleService bookTitleService;
    private final CategoryQueryService categoryQueryService;

    public BookTitleManagementController(
            BookTitleService bookTitleService,
            CategoryQueryService categoryQueryService
    ) {
        this.bookTitleService = bookTitleService;
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

        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "catalog/create";
        }

        BookTitleCommand command = new BookTitleCommand(
                form.getTitle(),
                form.getAuthors(),
                form.getIsbn(),
                form.getPublisher(),
                form.getPublicationYear(),
                form.getDescription(),
                form.getCategoryIds(),
                form.getPrimaryCategoryId()
        );

        try {
            bookTitleService.create(command);
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

        return "redirect:/catalog/new";
    }

    private void addFormOptions(Model model) {
        model.addAttribute(
                "categories",
                categoryQueryService.findAll()
        );
    }
}
