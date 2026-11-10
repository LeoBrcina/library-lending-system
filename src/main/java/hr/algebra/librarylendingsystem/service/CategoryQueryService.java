package hr.algebra.librarylendingsystem.service;

import hr.algebra.librarylendingsystem.repository.CategoryRepository;
import hr.algebra.librarylendingsystem.service.dto.CategoryOption;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public CategoryQueryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryOption> findAll() {
        return categoryRepository.findAll(
                        Sort.by(Sort.Direction.ASC, "name")
                ).stream()
                .map(category -> new CategoryOption(
                        category.getId(),
                        category.getName()
                ))
                .toList();
    }
}
