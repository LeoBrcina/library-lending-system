package hr.algebra.librarylendingsystem.repository;

import hr.algebra.librarylendingsystem.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCode(String code);
}
