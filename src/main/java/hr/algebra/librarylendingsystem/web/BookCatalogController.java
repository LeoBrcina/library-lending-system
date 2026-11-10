package hr.algebra.librarylendingsystem.web;

import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.CategoryQueryService;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogItem;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/catalog")
public class BookCatalogController {

    private static final int PAGE_SIZE = 10;

    private final BookTitleService bookTitleService;
    private final CategoryQueryService categoryQueryService;

    public BookCatalogController(
            BookTitleService bookTitleService,
            CategoryQueryService categoryQueryService
    ) {
        this.bookTitleService = bookTitleService;
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping
    public String showCatalog(
            @RequestParam(defaultValue = "") String searchText,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            Model model
    ) {
        BookCatalogSearchCriteria criteria =
                new BookCatalogSearchCriteria(
                        searchText,
                        categoryId,
                        active,
                        page,
                        PAGE_SIZE,
                        sortBy,
                        ascending
                );

        Page<BookCatalogItem> results =
                bookTitleService.search(criteria);

        model.addAttribute("books", results);
        model.addAttribute("categories", categoryQueryService.findAll());

        model.addAttribute("searchText", searchText);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("active", active);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("ascending", ascending);

        return "catalog/list";
    }
}
