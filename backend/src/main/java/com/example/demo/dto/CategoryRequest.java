package com.example.demo.dto;

import com.example.demo.model.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    public Category toEntity() {
        Category category = new Category();
        category.setName(this.name);
        return category;
    }
}
