package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fineaccruals")
public class FineAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loanid", nullable = false)
    private Loan loan;

    @Column(name = "accrualdate", nullable = false)
    private LocalDate accrualDate;

    @Column(
            name = "amount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal amount;

    protected FineAccrual() {
        // Required by JPA.
    }

    public FineAccrual(
            Loan loan,
            LocalDate accrualDate,
            BigDecimal amount
    ) {
        if (loan == null) {
            throw new IllegalArgumentException(
                    "Loan is required."
            );
        }

        if (accrualDate == null) {
            throw new IllegalArgumentException(
                    "Accrual date is required."
            );
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Fine amount must be greater than zero."
            );
        }

        if (amount.scale() > 2) {
            throw new IllegalArgumentException(
                    "Fine amount must not exceed two decimal places."
            );
        }

        if (amount.precision() - amount.scale() > 8) {
            throw new IllegalArgumentException(
                    "Fine amount exceeds the supported range."
            );
        }

        this.loan = loan;
        this.accrualDate = accrualDate;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Loan getLoan() {
        return loan;
    }

    public LocalDate getAccrualDate() {
        return accrualDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}