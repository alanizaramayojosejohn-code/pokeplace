package com.example.demo.dto;

import com.example.demo.model.Category;
import com.example.demo.model.Inedible;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InedibleRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 25, message = "Name must not exceed 25 characters")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Name must contain only letters and spaces")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotNull(message = "Stock is required")
    @Min(value = 1, message = "Stock must be positive")
    private Integer stock;

    @NotNull(message = "Min stock is required")
    @Min(value = 0, message = "Min stock cannot be negative")
    private Integer minStock;

    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Cost must be greater than 0")
    private Double cost;

    public Inedible toEntity() {
        Inedible inedible = new Inedible();
        inedible.setName(this.name);
        inedible.setPrice(this.price);
        inedible.setStock(this.stock);
        inedible.setMinStock(this.minStock);
        inedible.setDescription(this.description);
        inedible.setCost(this.cost);
        Category category = new Category();
        category.setId(this.categoryId);
        inedible.setCategory(category);
        return inedible;
    }
}
