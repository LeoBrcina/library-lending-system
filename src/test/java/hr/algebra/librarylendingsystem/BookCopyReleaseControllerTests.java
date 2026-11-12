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

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookCopyManagementController.class)
class BookCopyReleaseControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private BookTitleService bookTitleService;

    @Test
    void shouldReleaseCopyAndRedirectWithSuccessMessage()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        when(bookCopyService.releaseFromMaintenance(10L, 20L))
                .thenReturn(new BookCopyDetails(
                        20L,
                        10L,
                        "LIB-020",
                        "GOOD",
                        "AVAILABLE",
                        null
                ));

        mockMvc.perform(
                        post("/catalog/10/copies/20/release")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Physical copy released from maintenance successfully."
                ));

        verify(bookCopyService)
                .releaseFromMaintenance(10L, 20L);
    }

    @Test
    void shouldDisplayBusinessErrorWhenReleaseIsRejected()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        String errorMessage =
                "The copy must be in NEW, GOOD or FAIR condition " +
                        "before it can be released.";

        when(bookCopyService.releaseFromMaintenance(10L, 20L))
                .thenThrow(new InvalidBookCopyException(errorMessage));

        mockMvc.perform(
                        post("/catalog/10/copies/20/release")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        errorMessage
                ));
    }

    @Test
    void shouldDisplayErrorWhenWaitingReservationsPreventRelease()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        String errorMessage =
                "Cannot release this copy while reservations " +
                        "are waiting for the title.";

        when(bookCopyService.releaseFromMaintenance(10L, 20L))
                .thenThrow(new InvalidBookCopyException(errorMessage));

        mockMvc.perform(
                        post("/catalog/10/copies/20/release")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        errorMessage
                ));
    }

    @Test
    void shouldReturnNotFoundForMissingCopy()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        when(bookCopyService.releaseFromMaintenance(10L, 999L))
                .thenThrow(new BookCopyNotFoundException(999L));

        mockMvc.perform(
                        post("/catalog/10/copies/999/release")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForMissingTitle()
            throws Exception {

        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(
                        post("/catalog/999/copies/20/release")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowReleaseThroughGetRequest()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        mockMvc.perform(
                        get("/catalog/10/copies/20/release")
                )
                .andExpect(status().isMethodNotAllowed());
    }

    private BookTitleDetails sampleBook() {
        return new BookTitleDetails(
                10L,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                2008,
                "A software engineering book.",
                true,
                List.of(1L),
                1L
        );
    }
}
