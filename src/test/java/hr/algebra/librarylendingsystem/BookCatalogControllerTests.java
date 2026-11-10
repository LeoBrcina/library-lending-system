package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.CategoryQueryService;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogCategory;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogItem;
import hr.algebra.librarylendingsystem.service.dto.BookCatalogSearchCriteria;
import hr.algebra.librarylendingsystem.service.dto.CategoryOption;
import hr.algebra.librarylendingsystem.web.BookCatalogController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(BookCatalogController.class)
class BookCatalogControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookTitleService bookTitleService;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @Test
    void shouldRenderCatalogWithBooksAndCategories() throws Exception {
        when(bookTitleService.search(any(BookCatalogSearchCriteria.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook())));

        when(categoryQueryService.findAll()).thenReturn(
                List.of(
                        new CategoryOption(1L, "Fiction"),
                        new CategoryOption(2L, "History")
                )
        );

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/list"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("categories", hasSize(2)))
                .andExpect(content().string(
                        containsString("Test Library Book")
                ))
                .andExpect(content().string(
                        containsString("Fiction")
                ));
    }

    @Test
    void shouldPassSelectedFiltersToService() throws Exception {
        when(bookTitleService.search(any(BookCatalogSearchCriteria.class)))
                .thenReturn(new PageImpl<>(List.of()));

        when(categoryQueryService.findAll()).thenReturn(
                List.of(new CategoryOption(2L, "History"))
        );

        mockMvc.perform(
                        get("/catalog")
                                .param("searchText", "history")
                                .param("categoryId", "2")
                                .param("active", "true")
                                .param("page", "0")
                                .param("sortBy", "authors")
                                .param("ascending", "false")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/list"))
                .andExpect(model().attribute("categoryId", 2L))
                .andExpect(content().string(
                        containsString("History")
                ));

        ArgumentCaptor<BookCatalogSearchCriteria> captor =
                ArgumentCaptor.forClass(
                        BookCatalogSearchCriteria.class
                );

        verify(bookTitleService).search(captor.capture());

        BookCatalogSearchCriteria criteria = captor.getValue();

        assertEquals("history", criteria.searchText());
        assertEquals(2L, criteria.categoryId());
        assertEquals(true, criteria.active());
        assertEquals(0, criteria.page());
        assertEquals(10, criteria.size());
        assertEquals("authors", criteria.sortBy());
        assertFalse(criteria.ascending());
    }

    @Test
    void shouldRenderEmptyCatalogMessage() throws Exception {
        when(bookTitleService.search(any(BookCatalogSearchCriteria.class)))
                .thenReturn(new PageImpl<>(List.of()));

        when(categoryQueryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(
                                "No book titles match the selected filters."
                        )
                ));
    }

    @Test
    void shouldRenderPaginationForMultiplePages() throws Exception {
        when(bookTitleService.search(any(BookCatalogSearchCriteria.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(sampleBook()),
                                PageRequest.of(0, 10),
                                12
                        )
                );

        when(categoryQueryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/catalog"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Page 1 of 2")
                ))
                .andExpect(content().string(
                        containsString("Next")
                ));
    }

    private BookCatalogItem sampleBook() {
        return new BookCatalogItem(
                1L,
                "Test Library Book",
                "Test Author",
                "9781234567890",
                true,
                List.of(
                        new BookCatalogCategory(
                                1L,
                                "Fiction",
                                true
                        )
                ),
                3,
                2
        );
    }
}
