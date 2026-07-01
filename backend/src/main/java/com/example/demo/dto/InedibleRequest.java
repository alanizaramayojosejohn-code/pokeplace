package com.example.demo.dto;

import com.example.demo.model.Category;
import com.example.demo.model.Inedible;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class InedibleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Min stock is required")
    @Min(value = 0, message = "Min stock cannot be negative")
    private Integer minStock;

    public Inedible toEntity() {
        Inedible inedible = new Inedible();
        inedible.setName(this.name);
        inedible.setPrice(this.price);
        inedible.setStock(this.stock);
        inedible.setMinStock(this.minStock);
        Category category = new Category();
        category.setId(this.categoryId);
        inedible.setCategory(category);
        return inedible;
    }
}
