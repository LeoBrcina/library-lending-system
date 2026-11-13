package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "appusers")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "passwordhash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "firstname", nullable = false, length = 100)
    private String firstName;

    @Column(name = "lastname", nullable = false, length = 100)
    private String lastName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "createdat", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "userroles",
            joinColumns = @JoinColumn(
                    name = "userid"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "roleid"
            )
    )
    private Set<Role> roles = new HashSet<>();

    protected AppUser() {
        // Required by JPA.
    }

    public AppUser(
            String email,
            String passwordHash,
            String firstName,
            String lastName
    ) {
        setEmail(email);
        setPasswordHash(passwordHash);
        setFirstName(firstName);
        setLastName(lastName);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 255) {
            throw new IllegalArgumentException(
                    "Email must not exceed 255 characters."
            );
        }

        this.email = normalizedEmail;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash is required."
            );
        }

        if (passwordHash.length() > 255) {
            throw new IllegalArgumentException(
                    "Password hash must not exceed 255 characters."
            );
        }

        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException(
                    "First name is required."
            );
        }

        String normalizedFirstName = firstName.trim();

        if (normalizedFirstName.length() > 100) {
            throw new IllegalArgumentException(
                    "First name must not exceed 100 characters."
            );
        }

        this.firstName = normalizedFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Last name is required."
            );
        }

        String normalizedLastName = lastName.trim();

        if (normalizedLastName.length() > 100) {
            throw new IllegalArgumentException(
                    "Last name must not exceed 100 characters."
            );
        }

        this.lastName = normalizedLastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null."
            );
        }

        roles.add(role);
    }

    public void removeRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null."
            );
        }

        roles.remove(role);
    }
}