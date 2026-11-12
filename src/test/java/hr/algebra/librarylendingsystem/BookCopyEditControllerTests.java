package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.dto.BookTitleDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.DuplicateInventoryCodeException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import hr.algebra.librarylendingsystem.web.BookCopyManagementController;
import hr.algebra.librarylendingsystem.web.form.BookCopyForm;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

@WebMvcTest(BookCopyManagementController.class)
class BookCopyEditControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookCopyService bookCopyService;

    @MockitoBean
    private BookTitleService bookTitleService;

    @Test
    void shouldPrepopulateEditForm() throws Exception {
        prepareEditableCopy("AVAILABLE");

        var result = mockMvc.perform(
                        get("/catalog/10/copies/20/edit")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/edit"))
                .andExpect(model().attribute("copyId", 20L))
                .andExpect(model().attributeExists(
                        "copyForm", "book"
                ))
                .andExpect(content().string(
                        containsString("Edit Physical Copy")
                ))
                .andReturn();

        BookCopyForm form = (BookCopyForm) result
                .getModelAndView()
                .getModel()
                .get("copyForm");

        assertNotNull(form);
        assertEquals("LIB-020", form.getInventoryCode());
        assertEquals("GOOD", form.getCopyCondition());
        assertEquals(
                LocalDate.of(2026, 11, 10),
                form.getAcquiredAt()
        );
    }

    @Test
    void shouldUpdateCopyAndRedirect() throws Exception {
        prepareEditableCopy("AVAILABLE");

        mockMvc.perform(
                        post("/catalog/10/copies/20/edit")
                                .param("inventoryCode", "LIB-UPDATED")
                                .param("copyCondition", "FAIR")
                                .param("acquiredAt", "2026-11-11")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalog/10"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Physical copy updated successfully."
                ));

        ArgumentCaptor<BookCopyCommand> captor =
                ArgumentCaptor.forClass(BookCopyCommand.class);

        verify(bookCopyService).update(
                eq(10L),
                eq(20L),
                captor.capture()
        );

        assertEquals(
                "LIB-UPDATED",
                captor.getValue().inventoryCode()
        );
        assertEquals(
                "FAIR",
                captor.getValue().copyCondition()
        );
        assertEquals(
                LocalDate.of(2026, 11, 11),
                captor.getValue().acquiredAt()
        );
    }

    @Test
    void shouldRejectInvalidFormWithoutUpdating()
            throws Exception {
        prepareEditableCopy("AVAILABLE");

        mockMvc.perform(
                        post("/catalog/10/copies/20/edit")
                                .param("inventoryCode", "")
                                .param("copyCondition", "")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/edit"))
                .andExpect(model().attributeHasFieldErrors(
                        "copyForm",
                        "inventoryCode",
                        "copyCondition"
                ));

        verify(bookCopyService, never())
                .update(
                        eq(10L),
                        eq(20L),
                        any(BookCopyCommand.class)
                );
    }

    @Test
    void shouldDisplayDuplicateInventoryCodeError()
            throws Exception {
        prepareEditableCopy("AVAILABLE");

        when(bookCopyService.update(
                eq(10L),
                eq(20L),
                any(BookCopyCommand.class)
        )).thenThrow(
                new DuplicateInventoryCodeException("LIB-EXISTS")
        );

        mockMvc.perform(
                        post("/catalog/10/copies/20/edit")
                                .param("inventoryCode", "LIB-EXISTS")
                                .param("copyCondition", "GOOD")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/edit"))
                .andExpect(model().hasErrors())
                .andExpect(content().string(
                        containsString("Inventory code already exists")
                ));
    }

    @Test
    void shouldRejectFutureAcquisitionDate()
            throws Exception {
        prepareEditableCopy("AVAILABLE");

        LocalDate futureDate = LocalDate.now(
                java.time.ZoneId.of("Europe/Zagreb")
        ).plusDays(3);

        mockMvc.perform(
                        post("/catalog/10/copies/20/edit")
                                .param("inventoryCode", "LIB-020")
                                .param("copyCondition", "GOOD")
                                .param("acquiredAt", futureDate.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/edit"))
                .andExpect(model().attributeHasFieldErrors(
                        "copyForm",
                        "acquiredAt"
                ));

        verify(bookCopyService, never())
                .update(
                        eq(10L),
                        eq(20L),
                        any(BookCopyCommand.class)
                );
    }

    @Test
    void shouldBlockProtectedCirculationStatuses()
            throws Exception {
        for (String status : List.of(
                "ON_LOAN", "ON_HOLD", "WITHDRAWN"
        )) {
            prepareEditableCopy(status);

            mockMvc.perform(
                            get("/catalog/10/copies/20/edit")
                    )
                    .andExpect(status().isConflict());

            mockMvc.perform(
                            post("/catalog/10/copies/20/edit")
                                    .param("inventoryCode", "LIB-020")
                                    .param("copyCondition", "GOOD")
                    )
                    .andExpect(status().isConflict());
        }

        verify(bookCopyService, never())
                .update(
                        eq(10L),
                        eq(20L),
                        any(BookCopyCommand.class)
                );
    }

    @Test
    void shouldReturnNotFoundForMissingCopy()
            throws Exception {
        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        when(bookCopyService.findById(10L, 999L))
                .thenThrow(new BookCopyNotFoundException(999L));

        mockMvc.perform(
                        get("/catalog/10/copies/999/edit")
                )
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        post("/catalog/10/copies/999/edit")
                                .param("inventoryCode", "LIB-999")
                                .param("copyCondition", "GOOD")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDisplayServiceConflictWithoutRedirecting()
            throws Exception {
        prepareEditableCopy("AVAILABLE");

        when(bookCopyService.update(
                eq(10L),
                eq(20L),
                any(BookCopyCommand.class)
        )).thenThrow(new InvalidBookCopyException(
                "Cannot edit a copy with an active loan."
        ));

        mockMvc.perform(
                        post("/catalog/10/copies/20/edit")
                                .param("inventoryCode", "LIB-020")
                                .param("copyCondition", "FAIR")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/copies/edit"))
                .andExpect(model().hasErrors())
                .andExpect(content().string(
                        containsString(
                                "Cannot edit a copy with an active loan."
                        )
                ));
    }

    private void prepareEditableCopy(String status) {
        when(bookTitleService.findById(10L))
                .thenReturn(sampleBook());

        when(bookCopyService.findById(10L, 20L))
                .thenReturn(new BookCopyDetails(
                        20L,
                        10L,
                        "LIB-020",
                        "GOOD",
                        status,
                        LocalDate.of(2026, 11, 10)
                ));
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
