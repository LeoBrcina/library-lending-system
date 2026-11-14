package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.BookCopyService;
import hr.algebra.librarylendingsystem.service.BookTitleService;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookTitleLifecycleConcurrencyTests {

    @Autowired
    private BookTitleService bookTitleService;

    @Autowired
    private BookCopyService bookCopyService;

    @Autowired
    private BookTitleRepository bookTitleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldRejectCopyCreationAfterConcurrentTitleDeactivation()
            throws Exception {

        BookTitle title = bookTitleRepository.saveAndFlush(
                new BookTitle(
                        "Concurrency Test " + UUID.randomUUID(),
                        "Integration Test Author"
                )
        );

        Long titleId = title.getId();

        CountDownLatch titleDeactivated = new CountDownLatch(1);
        CountDownLatch allowCommit = new CountDownLatch(1);
        CountDownLatch creationStarted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> deactivationFuture = executor.submit(() -> {
                transactionTemplate.executeWithoutResult(status -> {
                    bookTitleService.deactivate(titleId);

                    titleDeactivated.countDown();

                    try {
                        if (!allowCommit.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Timed out waiting to commit deactivation."
                            );
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();

                        throw new IllegalStateException(
                                "Deactivation transaction interrupted.",
                                exception
                        );
                    }
                });
            });

            assertTrue(
                    titleDeactivated.await(10, TimeUnit.SECONDS),
                    "Deactivation transaction did not reach the lock checkpoint."
            );

            Future<?> creationFuture = executor.submit(() -> {
                creationStarted.countDown();

                bookCopyService.create(
                        titleId,
                        new BookCopyCommand(
                                "CONCURRENT-" + UUID.randomUUID(),
                                "GOOD",
                                null
                        )
                );
            });

            assertTrue(
                    creationStarted.await(10, TimeUnit.SECONDS),
                    "Copy creation did not start."
            );

            assertThrows(
                    TimeoutException.class,
                    () -> creationFuture.get(
                            300,
                            TimeUnit.MILLISECONDS
                    ),
                    "Copy creation completed before deactivation committed."
            );

            allowCommit.countDown();

            deactivationFuture.get(10, TimeUnit.SECONDS);

            ExecutionException exception = assertThrows(
                    ExecutionException.class,
                    () -> creationFuture.get(10, TimeUnit.SECONDS)
            );

            assertInstanceOf(
                    InvalidBookCopyException.class,
                    exception.getCause()
            );

            BookTitle savedTitle = bookTitleRepository
                    .findById(titleId)
                    .orElseThrow();

            assertFalse(savedTitle.isActive());

            assertFalse(
                    bookTitleRepository.existsPhysicalCopies(titleId)
            );

        } finally {
            allowCommit.countDown();

            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(10, TimeUnit.SECONDS),
                    "Concurrent test threads did not terminate."
            );

            if (!bookTitleRepository.existsPhysicalCopies(titleId)) {
                bookTitleService.delete(titleId);
            }
        }
    }
}