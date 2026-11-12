package hr.algebra.librarylendingsystem.web;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import hr.algebra.librarylendingsystem.web.form.BookCopyForm;
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

@Controller
@RequestMapping("/catalog/{bookTitleId}/copies")
public class BookCopyManagementController {

    private final BookCopyService bookCopyService;
    private final BookTitleService bookTitleService;

    public BookCopyManagementController(
            BookCopyService bookCopyService,
            BookTitleService bookTitleService
    ) {
        this.bookCopyService = bookCopyService;
        this.bookTitleService = bookTitleService;
    }

    @GetMapping("/new")
    public String showCreateForm(
            @PathVariable Long bookTitleId,
            Model model
    ) {
        BookTitleDetails book = findBookTitle(bookTitleId);

        ensureActive(book);

        model.addAttribute("copyForm", new BookCopyForm());
        model.addAttribute("book", book);

        return "catalog/copies/create";
    }

    @PostMapping("/new")
    public String createCopy(
            @PathVariable Long bookTitleId,
            @Valid @ModelAttribute("copyForm") BookCopyForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        BookTitleDetails book = findBookTitle(bookTitleId);

        ensureActive(book);

        if (bindingResult.hasErrors()) {
            model.addAttribute("book", book);
            return "catalog/copies/create";
        }

        try {
            bookCopyService.create(
                    bookTitleId,
                    toCommand(form)
            );
        } catch (InvalidBookCopyException exception) {
            bindingResult.reject(
                    "invalid.bookCopy",
                    exception.getMessage()
            );

            model.addAttribute("book", book);
            return "catalog/copies/create";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Physical copy added successfully."
        );

        return "redirect:/catalog/" + bookTitleId;
    }

    @GetMapping("/{copyId}/edit")
    public String showEditForm(
            @PathVariable Long bookTitleId,
            @PathVariable Long copyId,
            Model model
    ) {
        BookTitleDetails book = findBookTitle(bookTitleId);
        BookCopyDetails copy = findEditableCopy(bookTitleId, copyId);

        BookCopyForm form = new BookCopyForm();
        form.setInventoryCode(copy.inventoryCode());
        form.setCopyCondition(copy.copyCondition());
        form.setAcquiredAt(copy.acquiredAt());

        model.addAttribute("copyForm", form);
        model.addAttribute("book", book);
        model.addAttribute("copyId", copy.id());

        return "catalog/copies/edit";
    }

    @PostMapping("/{copyId}/edit")
    public String updateCopy(
            @PathVariable Long bookTitleId,
            @PathVariable Long copyId,
            @Valid @ModelAttribute("copyForm") BookCopyForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        BookTitleDetails book = findBookTitle(bookTitleId);

        findEditableCopy(bookTitleId, copyId);

        if (bindingResult.hasErrors()) {
            addEditContext(model, book, copyId);
            return "catalog/copies/edit";
        }

        try {
            bookCopyService.update(
                    bookTitleId,
                    copyId,
                    toCommand(form)
            );
        } catch (BookCopyNotFoundException exception) {
            throw copyNotFound(exception);
        } catch (InvalidBookCopyException exception) {
            bindingResult.reject(
                    "invalid.bookCopy",
                    exception.getMessage()
            );

            addEditContext(model, book, copyId);
            return "catalog/copies/edit";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Physical copy updated successfully."
        );

        return "redirect:/catalog/" + bookTitleId;
    }

    @PostMapping("/{copyId}/release")
    public String releaseCopyFromMaintenance(
            @PathVariable Long bookTitleId,
            @PathVariable Long copyId,
            RedirectAttributes redirectAttributes
    ) {
        findBookTitle(bookTitleId);

        try {
            bookCopyService.releaseFromMaintenance(
                    bookTitleId,
                    copyId
            );
        } catch (BookCopyNotFoundException exception) {
            throw copyNotFound(exception);
        } catch (BookTitleNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book title not found",
                    exception
            );
        } catch (InvalidBookCopyException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/catalog/" + bookTitleId;
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Physical copy released from maintenance successfully."
        );

        return "redirect:/catalog/" + bookTitleId;
    }

    @GetMapping("/{copyId}/withdraw")
    public String showWithdrawConfirmation(
            @PathVariable Long bookTitleId,
            @PathVariable Long copyId,
            Model model
    ) {
        BookTitleDetails book = findBookTitle(bookTitleId);
        BookCopyDetails copy = findWithdrawableCopy(
                bookTitleId,
                copyId
        );

        model.addAttribute("book", book);
        model.addAttribute("copy", copy);

        return "catalog/copies/withdraw";
    }

    @PostMapping("/{copyId}/withdraw")
    public String withdrawCopy(
            @PathVariable Long bookTitleId,
            @PathVariable Long copyId,
            RedirectAttributes redirectAttributes
    ) {
        findBookTitle(bookTitleId);

        // Check the requested copy and its visible circulation status.
        // The service performs the authoritative locked checks.
        findWithdrawableCopy(bookTitleId, copyId);

        try {
            bookCopyService.withdraw(bookTitleId, copyId);
        } catch (BookCopyNotFoundException exception) {
            throw copyNotFound(exception);
        } catch (BookTitleNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book title not found",
                    exception
            );
        } catch (InvalidBookCopyException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/catalog/" + bookTitleId;
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Physical copy withdrawn successfully."
        );

        return "redirect:/catalog/" + bookTitleId;
    }

    private BookTitleDetails findBookTitle(Long id) {
        try {
            return bookTitleService.findById(id);
        } catch (BookTitleNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book title not found",
                    exception
            );
        }
    }

    private BookCopyDetails findCopy(
            Long bookTitleId,
            Long copyId
    ) {
        try {
            return bookCopyService.findById(bookTitleId, copyId);
        } catch (BookCopyNotFoundException exception) {
            throw copyNotFound(exception);
        } catch (BookTitleNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book title not found",
                    exception
            );
        }
    }

    private BookCopyDetails findEditableCopy(
            Long bookTitleId,
            Long copyId
    ) {
        BookCopyDetails copy = findCopy(bookTitleId, copyId);

        if (!"AVAILABLE".equals(copy.status())
                && !"MAINTENANCE".equals(copy.status())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This physical copy cannot be edited in its current status."
            );
        }

        return copy;
    }

    private BookCopyDetails findWithdrawableCopy(
            Long bookTitleId,
            Long copyId
    ) {
        BookCopyDetails copy = findCopy(bookTitleId, copyId);

        if (!"AVAILABLE".equals(copy.status())
                && !"MAINTENANCE".equals(copy.status())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This physical copy cannot be withdrawn in its current status."
            );
        }

        return copy;
    }

    private ResponseStatusException copyNotFound(
            BookCopyNotFoundException exception
    ) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Physical copy not found",
                exception
        );
    }

    private void ensureActive(BookTitleDetails book) {
        if (!book.active()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot add physical copies to an inactive book title."
            );
        }
    }

    private BookCopyCommand toCommand(BookCopyForm form) {
        return new BookCopyCommand(
                form.getInventoryCode(),
                form.getCopyCondition(),
                form.getAcquiredAt()
        );
    }

    private void addEditContext(
            Model model,
            BookTitleDetails book,
            Long copyId
    ) {
        model.addAttribute("book", book);
        model.addAttribute("copyId", copyId);
    }
}
