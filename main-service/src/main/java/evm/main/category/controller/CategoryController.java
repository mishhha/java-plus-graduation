package evm.main.category.controller;

import evm.main.category.dto.CategoryDto;
import evm.main.category.dto.NewCategoryDto;
import evm.main.category.model.Category;
import evm.main.category.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getId(), saved.getName());
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto update(@PathVariable Long catId,
                              @Valid @RequestBody CategoryDto dto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new evm.main.exceptions.NotFoundException(
                        "Категория с id=" + catId + " не найдена"));
        category.setName(dto.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getId(), saved.getName());
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new evm.main.exceptions.NotFoundException("Категория с id=" + catId + " не найдена");
        }
        categoryRepository.deleteById(catId);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getAll(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getById(@PathVariable Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new evm.main.exceptions.NotFoundException(
                        "Категория с id=" + catId + " не найдена"));
        return new CategoryDto(category.getId(), category.getName());
    }
}
