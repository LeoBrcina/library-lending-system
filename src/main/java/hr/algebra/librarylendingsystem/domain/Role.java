package hr.algebra.librarylendingsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String name;

    protected Role() {
        // Required by JPA.
    }

    public Role(String name) {
        setName(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Role name is required."
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 30) {
            throw new IllegalArgumentException(
                    "Role name must not exceed 30 characters."
            );
        }

        this.name = normalizedName;
    }
}