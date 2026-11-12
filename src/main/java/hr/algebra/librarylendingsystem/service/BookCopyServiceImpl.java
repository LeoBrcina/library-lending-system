package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.domain.BookCopy;
import hr.algebra.librarylendingsystem.domain.BookCopyCondition;
import hr.algebra.librarylendingsystem.domain.BookCopyStatus;
import hr.algebra.librarylendingsystem.domain.BookTitle;
import hr.algebra.librarylendingsystem.repository.BookCopyRepository;
import hr.algebra.librarylendingsystem.repository.BookTitleRepository;
import hr.algebra.librarylendingsystem.service.dto.BookCopyCommand;
import hr.algebra.librarylendingsystem.service.dto.BookCopyDetails;
import hr.algebra.librarylendingsystem.service.exception.BookCopyNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.BookTitleNotFoundException;
import hr.algebra.librarylendingsystem.service.exception.DuplicateInventoryCodeException;
import hr.algebra.librarylendingsystem.service.exception.InvalidBookCopyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class BookCopyServiceImpl implements BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final BookTitleRepository bookTitleRepository;

    public BookCopyServiceImpl(
            BookCopyRepository bookCopyRepository,
            BookTitleRepository bookTitleRepository
    ) {
        this.bookCopyRepository = bookCopyRepository;
        this.bookTitleRepository = bookTitleRepository;
    }

    @Override
    @Transactional
    public BookCopyDetails create(
            Long bookTitleId,
            BookCopyCommand command
    ) {
        BookTitle title = findTitle(bookTitleId);

        if (!title.isActive()) {
            throw new InvalidBookCopyException(
                    "Cannot add physical copies to an inactive book title."
            );
        }

        validateCommand(command);

        String inventoryCode = command.inventoryCode().trim();

        if (bookCopyRepository.existsByInventoryCode(inventoryCode)) {
            throw new DuplicateInventoryCodeException(inventoryCode);
        }

        BookCopyCondition condition = BookCopyCondition.fromValue(
                command.copyCondition()
        );

        BookCopyStatus initialStatus =
                condition == BookCopyCondition.POOR
                        || condition == BookCopyCondition.DAMAGED
                        ? BookCopyStatus.MAINTENANCE
                        : BookCopyStatus.AVAILABLE;

        BookCopy copy = new BookCopy(
                inventoryCode,
                condition.name(),
                initialStatus.name()
        );

        copy.setAcquiredAt(command.acquiredAt());
        title.addCopy(copy);

        BookCopy savedCopy = bookCopyRepository.saveAndFlush(copy);

        return toDetails(savedCopy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCopyDetails> findByBookTitleId(Long bookTitleId) {
        findTitle(bookTitleId);

        return bookCopyRepository.findByBookTitle_Id(bookTitleId)
                .stream()
                .map(this::toDetails)
                .sorted(
                        Comparator.comparing(
                                BookCopyDetails::inventoryCode
                        ).thenComparing(BookCopyDetails::id)
                )
                .toList();
    }

    @Override
    @Transactional
    public BookCopyDetails update(
            Long bookTitleId,
            Long copyId,
            BookCopyCommand command
    ) {
        findTitle(bookTitleId);
        BookCopy copy = findCopyForUpdate(bookTitleId, copyId);

        BookCopyStatus currentStatus = BookCopyStatus.fromValue(
                copy.getStatus()
        );

        if (currentStatus != BookCopyStatus.AVAILABLE
                && currentStatus != BookCopyStatus.MAINTENANCE) {
            throw new InvalidBookCopyException(
                    "Cannot edit a copy while it is on loan, " +
                            "reserved or withdrawn."
            );
        }

        if (bookCopyRepository.existsActiveLoan(copyId)
                || bookCopyRepository.existsActiveAllocation(copyId)) {
            throw new InvalidBookCopyException(
                    "Cannot edit a copy with an active loan " +
                            "or reservation allocation."
            );
        }

        validateCommand(command);

        String inventoryCode = command.inventoryCode().trim();

        if (!inventoryCode.equals(copy.getInventoryCode())
                && bookCopyRepository.existsByInventoryCode(inventoryCode)) {
            throw new DuplicateInventoryCodeException(inventoryCode);
        }

        BookCopyCondition newCondition = BookCopyCondition.fromValue(
                command.copyCondition()
        );

        copy.setInventoryCode(inventoryCode);
        copy.setCopyCondition(newCondition.name());
        copy.setAcquiredAt(command.acquiredAt());

        if (newCondition == BookCopyCondition.POOR
                || newCondition == BookCopyCondition.DAMAGED) {
            copy.setStatus(BookCopyStatus.MAINTENANCE.name());
        }

        BookCopy savedCopy = bookCopyRepository.saveAndFlush(copy);

        return toDetails(savedCopy);
    }

    @Override
    @Transactional
    public BookCopyDetails withdraw(
            Long bookTitleId,
            Long copyId
    ) {
        findTitle(bookTitleId);
        BookCopy copy = findCopyForUpdate(bookTitleId, copyId);

        BookCopyStatus currentStatus = BookCopyStatus.fromValue(
                copy.getStatus()
        );

        if (bookCopyRepository.existsActiveLoan(copyId)
                || bookCopyRepository.existsActiveAllocation(copyId)) {
            throw new InvalidBookCopyException(
                    "Cannot withdraw a copy with an active loan " +
                            "or reservation allocation."
            );
        }

        if (currentStatus == BookCopyStatus.ON_LOAN
                || currentStatus == BookCopyStatus.ON_HOLD) {
            throw new InvalidBookCopyException(
                    "Cannot withdraw a copy while its circulation " +
                            "status indicates an active loan or hold."
            );
        }

        if (currentStatus != BookCopyStatus.WITHDRAWN) {
            copy.setStatus(BookCopyStatus.WITHDRAWN.name());
            bookCopyRepository.saveAndFlush(copy);
        }

        return toDetails(copy);
    }

    @Override
    @Transactional
    public void delete(
            Long bookTitleId,
            Long copyId
    ) {
        findTitle(bookTitleId);
        BookCopy copy = findCopyForUpdate(bookTitleId, copyId);

        BookCopyStatus currentStatus = BookCopyStatus.fromValue(
                copy.getStatus()
        );

        if (currentStatus == BookCopyStatus.ON_LOAN
                || currentStatus == BookCopyStatus.ON_HOLD) {
            throw new InvalidBookCopyException(
                    "Cannot delete a copy with an active circulation status."
            );
        }

        if (bookCopyRepository.existsLoanHistory(copyId)
                || bookCopyRepository.existsReservationHistory(copyId)) {
            throw new InvalidBookCopyException(
                    "Cannot delete a copy with loan or reservation history. " +
                            "Withdraw the copy instead."
            );
        }

        bookCopyRepository.delete(copy);
        bookCopyRepository.flush();
    }

    private BookTitle findTitle(Long id) {
        if (id == null || id <= 0) {
            throw new BookTitleNotFoundException(id);
        }

        return bookTitleRepository.findById(id)
                .orElseThrow(
                        () -> new BookTitleNotFoundException(id)
                );
    }

    private BookCopy findCopyForUpdate(
            Long bookTitleId,
            Long copyId
    ) {
        if (copyId == null || copyId <= 0) {
            throw new BookCopyNotFoundException(copyId);
        }

        BookCopy copy = bookCopyRepository.findByIdForUpdate(copyId)
                .orElseThrow(
                        () -> new BookCopyNotFoundException(copyId)
                );

        if (!copy.getBookTitle().getId().equals(bookTitleId)) {
            throw new BookCopyNotFoundException(copyId);
        }

        return copy;
    }

    private void validateCommand(BookCopyCommand command) {
        if (command == null) {
            throw new InvalidBookCopyException(
                    "Book copy data is required."
            );
        }

        String inventoryCode = command.inventoryCode();

        if (inventoryCode == null || inventoryCode.isBlank()) {
            throw new InvalidBookCopyException(
                    "Inventory code is required."
            );
        }

        if (inventoryCode.trim().length() > 100) {
            throw new InvalidBookCopyException(
                    "Inventory code must not exceed 100 characters."
            );
        }

        try {
            BookCopyCondition.fromValue(command.copyCondition());
        } catch (IllegalArgumentException exception) {
            throw new InvalidBookCopyException(
                    exception.getMessage()
            );
        }
    }

    private BookCopyDetails toDetails(BookCopy copy) {
        return new BookCopyDetails(
                copy.getId(),
                copy.getBookTitle().getId(),
                copy.getInventoryCode(),
                copy.getCopyCondition(),
                copy.getStatus(),
                copy.getAcquiredAt()
        );
    }


    @Override
    @Transactional
    public BookCopyDetails releaseFromMaintenance(
            Long bookTitleId,
            Long copyId
    ) {
        BookTitle title = findTitle(bookTitleId);
        BookCopy copy = findCopyForUpdate(bookTitleId, copyId);

        if (!title.isActive()) {
            throw new InvalidBookCopyException(
                    "Cannot release a copy belonging to an inactive book title."
            );
        }

        BookCopyStatus currentStatus = BookCopyStatus.fromValue(
                copy.getStatus()
        );

        if (currentStatus != BookCopyStatus.MAINTENANCE) {
            throw new InvalidBookCopyException(
                    "Only copies in maintenance can be released."
            );
        }

        BookCopyCondition condition = BookCopyCondition.fromValue(
                copy.getCopyCondition()
        );

        if (condition != BookCopyCondition.NEW
                && condition != BookCopyCondition.GOOD
                && condition != BookCopyCondition.FAIR) {
            throw new InvalidBookCopyException(
                    "The copy must be in NEW, GOOD or FAIR condition " +
                            "before it can be released."
            );
        }

        if (bookCopyRepository.existsActiveLoan(copyId)
                || bookCopyRepository.existsActiveAllocation(copyId)) {
            throw new InvalidBookCopyException(
                    "Cannot release a copy with an active loan " +
                            "or reservation allocation."
            );
        }

        if (bookCopyRepository.existsWaitingReservations(bookTitleId)) {
            throw new InvalidBookCopyException(
                    "Cannot release this copy while reservations " +
                            "are waiting for the title."
            );
        }

        copy.setStatus(BookCopyStatus.AVAILABLE.name());

        BookCopy savedCopy = bookCopyRepository.saveAndFlush(copy);

        return toDetails(savedCopy);
    }

}
