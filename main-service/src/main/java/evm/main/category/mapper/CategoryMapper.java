package evm.main.category.mapper;

import evm.main.category.dto.CategoryDto;
import evm.main.category.dto.NewCategoryDto;
import evm.main.category.model.Category;

public class CategoryMapper {

    public static CategoryDto toDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    public static Category toEntity(NewCategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        return category;
    }

}
