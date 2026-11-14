package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.CategoryQueryService;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookTitleException;
import hr.algebra.librarylendingsystem.web.BookTitleManagementController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
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
class BookTitleLifecycleControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookTitleService bookTitleService;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @Test
    void shouldRenderDeactivateConfirmationWithoutChangingTitle()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        mockMvc.perform(get("/catalog/10/deactivate"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/deactivate"))
                .andExpect(model().attributeExists("book"))
                .andExpect(content().string(
                        containsString("Confirm Deactivation")
                ))
                .andExpect(content().string(
                        containsString("Clean Code")
                ));

        verify(bookTitleService, never()).deactivate(10L);
    }

    @Test
    void shouldDeactivateActiveTitleAndRedirect()
            throws Exception {

        mockMvc.perform(post("/catalog/10/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Book title deactivated successfully."
                ));

        verify(bookTitleService).deactivate(10L);
    }

    @Test
    void shouldRejectDeactivateConfirmationForInactiveTitle()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(false));

        mockMvc.perform(get("/catalog/10/deactivate"))
                .andExpect(status().isConflict());

        verify(bookTitleService, never()).deactivate(10L);
    }

    @Test
    void shouldShowErrorWhenDeactivationIsBlocked()
            throws Exception {

        String errorMessage =
                "Cannot deactivate a book title with active loans " +
                        "or reservations.";

        doThrow(new InvalidBookTitleException(errorMessage))
                .when(bookTitleService)
                .deactivate(10L);

        mockMvc.perform(post("/catalog/10/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        errorMessage
                ));

        verify(bookTitleService).deactivate(10L);
    }

    @Test
    void shouldRenderDeleteConfirmationWithoutDeletingTitle()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        mockMvc.perform(get("/catalog/10/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/delete"))
                .andExpect(model().attributeExists("book"))
                .andExpect(content().string(
                        containsString("Confirm Permanent Deletion")
                ))
                .andExpect(content().string(
                        containsString("Clean Code")
                ));

        verify(bookTitleService, never()).delete(10L);
    }

    @Test
    void shouldDeleteEligibleTitleAndRedirectToCatalog()
            throws Exception {

        mockMvc.perform(post("/catalog/10/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Book title deleted successfully."
                ));

        verify(bookTitleService).delete(10L);
    }

    @Test
    void shouldShowErrorWhenPhysicalCopiesPreventDeletion()
            throws Exception {

        String errorMessage =
                "Cannot delete a book title with physical copies. " +
                        "Deactivate the title instead.";

        doThrow(new InvalidBookTitleException(errorMessage))
                .when(bookTitleService)
                .delete(10L);

        mockMvc.perform(post("/catalog/10/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        errorMessage
                ));

        verify(bookTitleService).delete(10L);
    }

    @Test
    void shouldReturnNotFoundForMissingTitleConfirmation()
            throws Exception {

        when(bookTitleService.findById(999L))
                .thenThrow(new BookTitleNotFoundException(999L));

        mockMvc.perform(get("/catalog/999/deactivate"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/catalog/999/delete"))
                .andExpect(status().isNotFound());

        verify(bookTitleService, never()).deactivate(999L);
        verify(bookTitleService, never()).delete(999L);
    }

    @Test
    void shouldReturnNotFoundWhenTitleDisappearsDuringOperation()
            throws Exception {

        doThrow(new BookTitleNotFoundException(999L))
                .when(bookTitleService)
                .deactivate(999L);

        doThrow(new BookTitleNotFoundException(999L))
                .when(bookTitleService)
                .delete(999L);

        mockMvc.perform(post("/catalog/999/deactivate"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/catalog/999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldShowLifecycleActionsOnActiveTitleDetails()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(true));

        when(categoryQueryService.findAll())
                .thenReturn(List.of());

        when(bookCopyService.findByBookTitleId(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/catalog/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("/catalog/10/deactivate")
                ))
                .andExpect(content().string(
                        containsString("/catalog/10/delete")
                ));
    }

    @Test
    void shouldHideDeactivateActionForInactiveTitle()
            throws Exception {

        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook(false));

        when(categoryQueryService.findAll())
                .thenReturn(List.of());

        when(bookCopyService.findByBookTitleId(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/catalog/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("/catalog/10/delete")
                ))
                .andExpect(content().string(
                        not(containsString("/catalog/10/deactivate"))
                ))
                .andExpect(content().string(
                        containsString(
                                "Borrowing unavailable while title is inactive"
                        )
                ));
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