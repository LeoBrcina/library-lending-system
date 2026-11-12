package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import hr.algebra.librarylendingsystem.web.BookCopyManagementController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
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

@WebMvcTest(BookCopyManagementController.class)
class BookCopyDeleteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private BookTitleService bookTitleService;

    @Test
    void shouldRenderConfirmationWithoutDeleting()
            throws Exception {
        prepareCopy("AVAILABLE", true);

        mockMvc.perform(get("/catalog/10/copies/20/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/delete"))
                .andExpect(model().attributeExists("book", "copy"))
                .andExpect(content().string(
                        containsString("Confirm Permanent Deletion")
                ))
                .andExpect(content().string(
                        containsString("LIB-020")
                ))
                .andExpect(content().string(
                        containsString("Clean Code")
                ));

        verify(bookCopyService, never()).delete(10L, 20L);
    }

    @Test
    void shouldDeleteAvailableCopyAndRedirect()
            throws Exception {
        prepareCopy("AVAILABLE", true);

        mockMvc.perform(post("/catalog/10/copies/20/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Physical copy deleted successfully."
                ));

        verify(bookCopyService).delete(10L, 20L);
    }

    @Test
    void shouldAllowMaintenanceAndWithdrawnCopies()
            throws Exception {
        for (String copyStatus : List.of(
                "MAINTENANCE",
                "WITHDRAWN"
        )) {
            prepareCopy(copyStatus, true);

            mockMvc.perform(get("/catalog/10/copies/20/delete"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/catalog/10/copies/20/delete"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/catalog/10"));
        }

        verify(bookCopyService,
                org.mockito.Mockito.times(2)).delete(10L, 20L);
    }

    @Test
    void shouldAllowDeleteWhenTitleIsInactive()
            throws Exception {
        prepareCopy("AVAILABLE", false);

        mockMvc.perform(post("/catalog/10/copies/20/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"));

        verify(bookCopyService).delete(10L, 20L);
    }

    @Test
    void shouldBlockOnLoanAndOnHoldStatuses()
            throws Exception {
        for (String copyStatus : List.of("ON_LOAN", "ON_HOLD")) {
            prepareCopy(copyStatus, true);

            mockMvc.perform(get("/catalog/10/copies/20/delete"))
                    .andExpect(status().isConflict());

            mockMvc.perform(post("/catalog/10/copies/20/delete"))
                    .andExpect(status().isConflict());
        }

        verify(bookCopyService, never()).delete(10L, 20L);
    }

    @Test
    void shouldShowErrorWhenLoanHistoryPreventsDeletion()
            throws Exception {
        prepareCopy("AVAILABLE", true);

        String errorMessage =
                "Cannot delete a copy with loan or reservation history. " +
                        "Withdraw the copy instead.";

        org.mockito.Mockito.doThrow(
                new InvalidBookCopyException(errorMessage)
        ).when(bookCopyService).delete(10L, 20L);

        mockMvc.perform(post("/catalog/10/copies/20/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "errorMessage", errorMessage
                ));

        verify(bookCopyService).delete(10L, 20L);
    }

    @Test
    void shouldReturnNotFoundForMissingCopy()
            throws Exception {
        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        when(bookCopyService.findById(10L, 999L))
                .thenThrow(new BookCopyNotFoundException(999L));

        mockMvc.perform(get("/catalog/10/copies/999/delete"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/catalog/10/copies/999/delete"))
                .andExpect(status().isNotFound());

        verify(bookCopyService, never()).delete(10L, 999L);
    }

    @Test
    void shouldReturnNotFoundForMissingTitle()
            throws Exception {
        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(get("/catalog/999/copies/20/delete"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/catalog/999/copies/20/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldMapCopyDisappearingDuringDeletionToNotFound()
            throws Exception {
        prepareCopy("AVAILABLE", true);

        org.mockito.Mockito.doThrow(
                new BookCopyNotFoundException(20L)
        ).when(bookCopyService).delete(10L, 20L);

        mockMvc.perform(post("/catalog/10/copies/20/delete"))
                .andExpect(status().isNotFound());
    }

    private void prepareCopy(
            String status,
            boolean titleActive
    ) {
        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(titleActive));

        when(bookCopyService.findById(10L, 20L))
                .thenReturn(copy(status));
    }

    private BookCopyDetails copy(String status) {
        return new BookCopyDetails(
                20L,
                10L,
                "LIB-020",
                "GOOD",
                status,
                LocalDate.of(2026, 11, 10)
        );
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
}
