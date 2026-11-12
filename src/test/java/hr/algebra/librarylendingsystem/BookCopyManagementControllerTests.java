package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.DuplicateInventoryCodeException;
import hr.algebra.librarylendingsystem.web.BookCopyManagementController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(BookCopyManagementController.class)
class BookCopyManagementControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private BookTitleService bookTitleService;

    @Test
    void shouldRenderCreateFormForActiveTitle() throws Exception {
        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        mockMvc.perform(get("/catalog/10/copies/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/create"))
                .andExpect(model().attributeExists("copyForm", "book"))
                .andExpect(content().string(
                        containsString("Add Physical Copy")
                ))
                .andExpect(content().string(
                        containsString("Clean Code")
                ));
    }

    @Test
    void shouldCreateCopyAndRedirectToTitleDetails()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        when(bookCopyService.create(
                eq(10L),
                any(BookCopyCommand.class)
        )).thenReturn(new BookCopyDetails(
                20L,
                10L,
                "LIB-100",
                "GOOD",
                "AVAILABLE",
                LocalDate.of(2026, 11, 12)
        ));

        mockMvc.perform(
                        post("/catalog/10/copies/new")
                                .param("inventoryCode", "LIB-100")
                                .param("copyCondition", "GOOD")
                                .param("acquiredAt", "2026-11-12")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Physical copy added successfully."
                ));

        ArgumentCaptor<BookCopyCommand> captor =
                ArgumentCaptor.forClass(BookCopyCommand.class);

        verify(bookCopyService).create(eq(10L), captor.capture());

        BookCopyCommand command = captor.getValue();

        assertEquals("LIB-100", command.inventoryCode());
        assertEquals("GOOD", command.copyCondition());
        assertEquals(
                LocalDate.of(2026, 11, 12),
                command.acquiredAt()
        );
    }

    @Test
    void shouldRejectInvalidFormWithoutSaving()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        mockMvc.perform(
                        post("/catalog/10/copies/new")
                                .param("inventoryCode", "")
                                .param("copyCondition", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/create"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors(
                        "copyForm",
                        "inventoryCode",
                        "copyCondition"
                ))
                .andExpect(content().string(
                        containsString(
                                "Please correct the highlighted fields below."
                        )
                ));

        verify(bookCopyService, never())
                .create(eq(10L), any(BookCopyCommand.class));
    }

    @Test
    void shouldShowDuplicateCodeErrorWithoutRedirecting()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        when(bookCopyService.create(
                eq(10L),
                any(BookCopyCommand.class)
        )).thenThrow(new DuplicateInventoryCodeException("LIB-100"));

        mockMvc.perform(
                        post("/catalog/10/copies/new")
                                .param("inventoryCode", "LIB-100")
                                .param("copyCondition", "GOOD")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/create"))
                .andExpect(model().hasErrors())
                .andExpect(content().string(
                        containsString("Inventory code already exists")
                ));
    }

    @Test
    void shouldRejectInactiveTitle() throws Exception {
        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(false));

        mockMvc.perform(get("/catalog/10/copies/new"))
                .andExpect(status().isConflict());

        mockMvc.perform(
                        post("/catalog/10/copies/new")
                                .param("inventoryCode", "LIB-100")
                                .param("copyCondition", "GOOD")
                )
                .andExpect(status().isConflict());

        verify(bookCopyService, never())
                .create(eq(10L), any(BookCopyCommand.class));
    }

    @Test
    void shouldReturnNotFoundForMissingTitle()
            throws Exception {

        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(get("/catalog/999/copies/new"))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/catalog/999/copies/new")
                                .param("inventoryCode", "LIB-100")
                                .param("copyCondition", "GOOD")
                )
                .andExpect(status().isNotFound());
    }

    private BookTitleDetails sampleBook(boolean active) {
        return new BookTitleDetails(
                10L,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                2008,
                "A software engineering book.",
                active,
                List.of(1L),
                1L
        );
    }

    @Test
    void shouldRejectFutureAcquisitionDateWithoutSaving()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        LocalDate futureDate = LocalDate.now(
                java.time.ZoneId.of("Europe/Zagreb")
        ).plusDays(2);

        mockMvc.perform(
                        post("/catalog/10/copies/new")
                                .param("inventoryCode", "FUTURE-001")
                                .param("copyCondition", "GOOD")
                                .param("acquiredAt", futureDate.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/create"))
                .andExpect(model().attributeHasFieldErrors(
                        "copyForm",
                        "acquiredAt"
                ))
                .andExpect(content().string(
                        containsString(
                                "Acquisition date cannot be in the future."
                        )
                ));

        verify(bookCopyService, never())
                .create(eq(10L), any(BookCopyCommand.class));
    }

}
