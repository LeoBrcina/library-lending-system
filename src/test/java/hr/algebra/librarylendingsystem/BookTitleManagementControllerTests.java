package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.CategoryQueryService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.dto.BookTitleCommand;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.dto.CategoryOption;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import hr.algebra.librarylendingsystem.web.BookTitleManagementController;
import hr.algebra.librarylendingsystem.web.form.BookTitleForm;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BookTitleManagementController.class)
class BookTitleManagementControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookTitleService bookTitleService;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    // Create Book Title

    @Test
    void shouldRenderCreateFormWithCategories() throws Exception {
        when(categoryQueryService.findAll()).thenReturn(
                sampleCategories()
        );

        mockMvc.perform(get("/catalog/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/create"))
                .andExpect(model().attributeExists("bookTitleForm"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(content().string(
                        containsString("Create Book Title")
                ))
                .andExpect(content().string(
                        containsString("Fiction")
                ))
                .andExpect(content().string(
                        containsString("History")
                ));
    }

    @Test
    void shouldCreateBookTitleAndRedirectOnValidSubmission()
            throws Exception {

        when(bookTitleService.create(any(BookTitleCommand.class)))
                .thenReturn(sampleBookTitleDetails());

        mockMvc.perform(
                        post("/catalog/new")
                                .param("title", "Clean Code")
                                .param("authors", "Robert C. Martin")
                                .param("isbn", "9780132350884")
                                .param("publisher", "Prentice Hall")
                                .param("publicationYear", "2008")
                                .param(
                                        "description",
                                        "A software engineering book."
                                )
                                .param("categoryIds", "1", "2")
                                .param("primaryCategoryId", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Book title created successfully."
                ));

        ArgumentCaptor<BookTitleCommand> captor =
                ArgumentCaptor.forClass(BookTitleCommand.class);

        verify(bookTitleService).create(captor.capture());

        BookTitleCommand command = captor.getValue();

        assertEquals("Clean Code", command.title());
        assertEquals("Robert C. Martin", command.authors());
        assertEquals("9780132350884", command.isbn());
        assertEquals("Prentice Hall", command.publisher());
        assertEquals(2008, command.publicationYear());
        assertEquals(
                "A software engineering book.",
                command.description()
        );
        assertEquals(List.of(1L, 2L), command.categoryIds());
        assertEquals(1L, command.primaryCategoryId());
    }

    @Test
    void shouldRejectMissingRequiredFieldsWithoutSaving()
            throws Exception {

        when(categoryQueryService.findAll()).thenReturn(
                sampleCategories()
        );

        mockMvc.perform(
                        post("/catalog/new")
                                .param("title", "")
                                .param("authors", "")
                                .param("primaryCategoryId", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors(
                        "bookTitleForm",
                        "title",
                        "authors",
                        "categoryIds",
                        "primaryCategoryId"
                ))
                .andExpect(content().string(
                        containsString(
                                "Please correct the following errors:"
                        )
                ));

        verify(bookTitleService, never())
                .create(any(BookTitleCommand.class));
    }

    @Test
    void shouldRejectPrimaryCategoryOutsideSelectedCategories()
            throws Exception {

        when(categoryQueryService.findAll()).thenReturn(
                sampleCategories()
        );

        mockMvc.perform(
                        post("/catalog/new")
                                .param("title", "Test Book")
                                .param("authors", "Test Author")
                                .param("categoryIds", "2")
                                .param("primaryCategoryId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "bookTitleForm",
                        "primaryCategoryId"
                ))
                .andExpect(content().string(
                        containsString(
                                "The primary category must be one of the selected categories."
                        )
                ));

        verify(bookTitleService, never())
                .create(any(BookTitleCommand.class));
    }

    @Test
    void shouldDisplayServiceValidationErrorWithoutRedirecting()
            throws Exception {

        when(categoryQueryService.findAll()).thenReturn(
                sampleCategories()
        );

        when(bookTitleService.create(any(BookTitleCommand.class)))
                .thenThrow(new InvalidBookTitleException(
                        "The selected category is not valid."
                ));

        mockMvc.perform(
                        post("/catalog/new")
                                .param("title", "Test Book")
                                .param("authors", "Test Author")
                                .param("categoryIds", "1")
                                .param("primaryCategoryId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/create"))
                .andExpect(model().hasErrors())
                .andExpect(content().string(
                        containsString(
                                "The selected category is not valid."
                        )
                ));

        verify(bookTitleService)
                .create(any(BookTitleCommand.class));
    }

    // Book Title Details

    @Test
    void shouldRenderDetailsWithBookAndSelectedCategories()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBookTitleDetails());

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        when(bookCopyService.findByBookTitleId(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/catalog/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/details"))
                .andExpect(model().attributeExists(
                        "book",
                        "selectedCategories",
                        "copies",
                        "availableCopyCount"
                ))
                .andExpect(content().string(
                        containsString("Clean Code")
                ))
                .andExpect(content().string(
                        containsString("Robert C. Martin")
                ))
                .andExpect(content().string(
                        containsString("Fiction")
                ))
                .andExpect(content().string(
                        containsString("History")
                ))
                .andExpect(content().string(
                        containsString("Physical Copies")
                ))
                .andExpect(content().string(
                        containsString("/catalog/10/edit")
                ));

        verify(bookTitleService).findById(10L);
        verify(bookCopyService).findByBookTitleId(10L);
    }

    @Test
    void shouldRenderPhysicalCopiesAndAvailabilityCount()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBookTitleDetails());

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        when(bookCopyService.findByBookTitleId(10L))
                .thenReturn(sampleCopies());

        mockMvc.perform(get("/catalog/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/details"))
                .andExpect(model().attribute("availableCopyCount", 1L))
                .andExpect(content().string(
                        containsString("3 total")
                ))
                .andExpect(content().string(
                        containsString("1 available for borrowing")
                ))
                .andExpect(content().string(
                        containsString("LIB-001")
                ))
                .andExpect(content().string(
                        containsString("LIB-002")
                ))
                .andExpect(content().string(
                        containsString("LIB-003")
                ))
                .andExpect(content().string(
                        containsString("AVAILABLE")
                ))
                .andExpect(content().string(
                        containsString("ON LOAN")
                ))
                .andExpect(content().string(
                        containsString("MAINTENANCE")
                ))
                .andExpect(content().string(
                        containsString("12.11.2026")
                ))
                .andExpect(content().string(
                        containsString("Not recorded")
                ));

        verify(bookCopyService).findByBookTitleId(10L);
    }

    @Test
    void shouldDisplayEmptyInventoryState()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBookTitleDetails());

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        when(bookCopyService.findByBookTitleId(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/catalog/10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("availableCopyCount", 0L))
                .andExpect(content().string(
                        containsString("0 total")
                ))
                .andExpect(content().string(
                        containsString("0 available for borrowing")
                ))
                .andExpect(content().string(
                        containsString("No physical copies registered")
                ));
    }

    @Test
    void shouldRenderAllUnavailableCopiesWithoutCountingThem()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBookTitleDetails());

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        when(bookCopyService.findByBookTitleId(10L))
                .thenReturn(List.of(
                        new BookCopyDetails(
                                21L, 10L, "HOLD-001",
                                "GOOD", "ON_HOLD", null
                        ),
                        new BookCopyDetails(
                                22L, 10L, "OUT-001",
                                "FAIR", "WITHDRAWN", null
                        )
                ));

        mockMvc.perform(get("/catalog/10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("availableCopyCount", 0L))
                .andExpect(content().string(
                        containsString("2 total")
                ))
                .andExpect(content().string(
                        containsString("HOLD-001")
                ))
                .andExpect(content().string(
                        containsString("OUT-001")
                ))
                .andExpect(content().string(
                        containsString("ON HOLD")
                ))
                .andExpect(content().string(
                        containsString("WITHDRAWN")
                ));
    }

    @Test
    void shouldReturnNotFoundForMissingBookDetails()
            throws Exception {

        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(get("/catalog/999"))
                .andExpect(status().isNotFound());

        verify(bookCopyService, never())
                .findByBookTitleId(999L);
    }

    // Edit Book Title

    @Test
    void shouldRenderEditFormWithExistingValues()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBookTitleDetails());

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        var result = mockMvc.perform(get("/catalog/10/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/edit"))
                .andExpect(model().attribute("bookId", 10L))
                .andExpect(model().attributeExists(
                        "bookTitleForm",
                        "categories"
                ))
                .andExpect(content().string(
                        containsString("Save Changes")
                ))
                .andReturn();

        Object formObject = result.getModelAndView()
                .getModel()
                .get("bookTitleForm");

        assertNotNull(formObject);

        BookTitleForm form = (BookTitleForm) formObject;

        assertEquals("Clean Code", form.getTitle());
        assertEquals("Robert C. Martin", form.getAuthors());
        assertEquals("9780132350884", form.getIsbn());
        assertEquals("Prentice Hall", form.getPublisher());
        assertEquals(2008, form.getPublicationYear());
        assertEquals(
                "A software engineering book.",
                form.getDescription()
        );
        assertEquals(List.of(1L, 2L), form.getCategoryIds());
        assertEquals(1L, form.getPrimaryCategoryId());
    }

    @Test
    void shouldReturnNotFoundForMissingBookEditForm()
            throws Exception {

        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(get("/catalog/999/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateBookTitleAndRedirectToDetails()
            throws Exception {

        when(bookTitleService.update(
                org.mockito.ArgumentMatchers.eq(10L),
                any(BookTitleCommand.class)
        )).thenReturn(sampleBookTitleDetails());

        mockMvc.perform(
                        post("/catalog/10/edit")
                                .param("title", "Clean Code Updated")
                                .param("authors", "Robert C. Martin")
                                .param("isbn", "9780132350884")
                                .param("publisher", "Updated Publisher")
                                .param("publicationYear", "2010")
                                .param(
                                        "description",
                                        "Updated description."
                                )
                                .param("categoryIds", "1", "2")
                                .param("primaryCategoryId", "2")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Book title updated successfully."
                ));

        ArgumentCaptor<BookTitleCommand> captor =
                ArgumentCaptor.forClass(BookTitleCommand.class);

        verify(bookTitleService).update(
                org.mockito.ArgumentMatchers.eq(10L),
                captor.capture()
        );

        BookTitleCommand command = captor.getValue();

        assertEquals("Clean Code Updated", command.title());
        assertEquals("Robert C. Martin", command.authors());
        assertEquals("Updated Publisher", command.publisher());
        assertEquals(2010, command.publicationYear());
        assertEquals(
                "Updated description.",
                command.description()
        );
        assertEquals(List.of(1L, 2L), command.categoryIds());
        assertEquals(2L, command.primaryCategoryId());
    }

    @Test
    void shouldRejectInvalidEditWithoutUpdating()
            throws Exception {

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        mockMvc.perform(
                        post("/catalog/10/edit")
                                .param("title", "")
                                .param("authors", "")
                                .param("primaryCategoryId", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/edit"))
                .andExpect(model().attribute("bookId", 10L))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors(
                        "bookTitleForm",
                        "title",
                        "authors",
                        "categoryIds",
                        "primaryCategoryId"
                ))
                .andExpect(content().string(
                        containsString(
                                "Please correct the following errors:"
                        )
                ));

        verify(bookTitleService, never())
                .update(
                        org.mockito.ArgumentMatchers.anyLong(),
                        any(BookTitleCommand.class)
                );
    }

    @Test
    void shouldRejectInvalidPrimaryCategoryOnEdit()
            throws Exception {

        when(categoryQueryService.findAll())
                .thenReturn(
                        sampleCategories()
                );

        mockMvc.perform(
                        post("/catalog/10/edit")
                                .param("title", "Test Book")
                                .param("authors", "Test Author")
                                .param("categoryIds", "1")
                                .param("primaryCategoryId", "2")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/edit"))
                .andExpect(model().attributeHasFieldErrors(
                        "bookTitleForm",
                        "primaryCategoryId"
                ))
                .andExpect(content().string(
                        containsString(
                                "The primary category must be one of the selected categories."
                        )
                ));

        verify(bookTitleService, never())
                .update(
                        org.mockito.ArgumentMatchers.anyLong(),
                        any(BookTitleCommand.class)
                );
    }

    @Test
    void shouldDisplayServiceValidationErrorOnEdit()
            throws Exception {

        when(categoryQueryService.findAll())
                .thenReturn(sampleCategories());

        when(bookTitleService.update(
                org.mockito.ArgumentMatchers.eq(10L),
                any(BookTitleCommand.class)
        )).thenThrow(new InvalidBookTitleException(
                "One or more selected categories do not exist."
        ));

        mockMvc.perform(
                        post("/catalog/10/edit")
                                .param("title", "Test Book")
                                .param("authors", "Test Author")
                                .param("categoryIds", "1")
                                .param("primaryCategoryId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/edit"))
                .andExpect(model().hasErrors())
                .andExpect(content().string(
                        containsString(
                                "One or more selected categories do not exist."
                        )
                ));

        verify(bookTitleService).update(
                org.mockito.ArgumentMatchers.eq(10L),
                any(BookTitleCommand.class)
        );
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingBook()
            throws Exception {

        when(bookTitleService.update(
                org.mockito.ArgumentMatchers.eq(999L),
                any(BookTitleCommand.class)
        )).thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(
                        post("/catalog/999/edit")
                                .param("title", "Test Book")
                                .param("authors", "Test Author")
                                .param("categoryIds", "1")
                                .param("primaryCategoryId", "1")
                )
                .andExpect(status().isNotFound());
    }

    private List<CategoryOption> sampleCategories() {
        return List.of(
                new CategoryOption(1L, "Fiction"),
                new CategoryOption(2L, "History")
        );
    }

    private BookTitleDetails sampleBookTitleDetails() {
        return new BookTitleDetails(
                10L,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                2008,
                "A software engineering book.",
                true,
                List.of(1L, 2L),
                1L
        );
    }

    private List<BookCopyDetails> sampleCopies() {
        return List.of(
                new BookCopyDetails(
                        11L,
                        10L,
                        "LIB-001",
                        "GOOD",
                        "AVAILABLE",
                        LocalDate.of(2026, 11, 12)
                ),
                new BookCopyDetails(
                        12L,
                        10L,
                        "LIB-002",
                        "FAIR",
                        "ON_LOAN",
                        null
                ),
                new BookCopyDetails(
                        13L,
                        10L,
                        "LIB-003",
                        "POOR",
                        "MAINTENANCE",
                        null
                )
        );
    }
}
