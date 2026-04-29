package com.example.demo.dto;

import com.example.demo.model.Category;
import com.example.demo.model.Edible;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EdibleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotBlank(message = "Poke name is required")
    private String pokeName;

    public Edible toEntity() {
        Edible edible = new Edible();
        edible.setName(this.name);
        edible.setPrice(this.price);
        edible.setPokeName(this.pokeName);
        Category category = new Category();
        category.setId(this.categoryId);
        edible.setCategory(category);
        return edible;
    }
}
