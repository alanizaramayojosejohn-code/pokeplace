package com.example.demo.dto;

import com.example.demo.model.Category;
import com.example.demo.model.Edible;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EdibleRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 25, message = "Name must not exceed 25 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Name must contain only letters and spaces")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotBlank(message = "Poke name is required")
    @Size(max = 15, message = "Poke name must not exceed 15 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Poke name must contain only letters and spaces")
    private String pokeName;

    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Cost must be greater than 0")
    private Double cost;

    public Edible toEntity() {
        Edible edible = new Edible();
        edible.setName(this.name);
        edible.setPrice(this.price);
        edible.setPokeName(this.pokeName);
        edible.setDescription(this.description);
        edible.setCost(this.cost);
        Category category = new Category();
        category.setId(this.categoryId);
        edible.setCategory(category);
        return edible;
    }
}
