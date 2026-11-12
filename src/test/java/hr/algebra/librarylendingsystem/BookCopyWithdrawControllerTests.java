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
class BookCopyWithdrawControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private BookTitleService bookTitleService;

    @Test
    void shouldRenderWithdrawalConfirmationWithoutChangingCopy()
            throws Exception {

        prepareCopy("AVAILABLE", true);

        mockMvc.perform(
                        get("/catalog/10/copies/20/withdraw")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/withdraw"))
                .andExpect(model().attributeExists("book", "copy"))
                .andExpect(content().string(
                        containsString("Confirm Withdrawal")
                ))
                .andExpect(content().string(
                        containsString("LIB-020")
                ))
                .andExpect(content().string(
                        containsString("Clean Code")
                ));

        verify(bookCopyService, never())
                .withdraw(10L, 20L);
    }

    @Test
    void shouldWithdrawAvailableCopyAndRedirect()
            throws Exception {

        prepareCopy("AVAILABLE", true);

        when(bookCopyService.withdraw(10L, 20L))
                .thenReturn(copy("WITHDRAWN"));

        mockMvc.perform(
                        post("/catalog/10/copies/20/withdraw")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Physical copy withdrawn successfully."
                ));

        verify(bookCopyService).withdraw(10L, 20L);
    }

    @Test
    void shouldAllowWithdrawingMaintenanceCopy()
            throws Exception {

        prepareCopy("MAINTENANCE", true);

        when(bookCopyService.withdraw(10L, 20L))
                .thenReturn(copy("WITHDRAWN"));

        mockMvc.perform(
                        post("/catalog/10/copies/20/withdraw")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"));

        verify(bookCopyService).withdraw(10L, 20L);
    }

    @Test
    void shouldAllowWithdrawalWhenTitleIsInactive()
            throws Exception {

        prepareCopy("AVAILABLE", false);

        when(bookCopyService.withdraw(10L, 20L))
                .thenReturn(copy("WITHDRAWN"));

        mockMvc.perform(
                        post("/catalog/10/copies/20/withdraw")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"));

        verify(bookCopyService).withdraw(10L, 20L);
    }

    @Test
    void shouldBlockWithdrawalOfProtectedStatuses()
            throws Exception {

        for (String copyStatus : List.of(
                "ON_LOAN",
                "ON_HOLD",
                "WITHDRAWN"
        )) {
            prepareCopy(copyStatus, true);

            mockMvc.perform(
                            get("/catalog/10/copies/20/withdraw")
                    )
                    .andExpect(status().isConflict());

            mockMvc.perform(
                            post("/catalog/10/copies/20/withdraw")
                    )
                    .andExpect(status().isConflict());
        }

        verify(bookCopyService, never())
                .withdraw(10L, 20L);
    }

    @Test
    void shouldShowBusinessErrorWhenWithdrawalIsRejected()
            throws Exception {

        prepareCopy("AVAILABLE", true);

        String message =
                "Cannot withdraw a copy with an active loan " +
                        "or reservation allocation.";

        when(bookCopyService.withdraw(10L, 20L))
                .thenThrow(new InvalidBookCopyException(message));

        mockMvc.perform(
                        post("/catalog/10/copies/20/withdraw")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        message
                ));
    }

    @Test
    void shouldReturnNotFoundForMissingCopy()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        when(bookCopyService.findById(10L, 999L))
                .thenThrow(new BookCopyNotFoundException(999L));

        mockMvc.perform(
                        get("/catalog/10/copies/999/withdraw")
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/catalog/10/copies/999/withdraw")
                )
                .andExpect(status().isNotFound());

        verify(bookCopyService, never())
                .withdraw(10L, 999L);
    }

    @Test
    void shouldReturnNotFoundForMissingTitle()
            throws Exception {

        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(
                        get("/catalog/999/copies/20/withdraw")
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/catalog/999/copies/20/withdraw")
                )
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
