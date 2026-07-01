package com.example.demo.dto;

import com.example.demo.model.Edible;
import com.example.demo.model.Inedible;
import com.example.demo.model.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Long id;
    private String name;
    private Double price;
    private String type;
    private Long categoryId;
    private String categoryName;

    // Solo para Edible
    private String pokeName;

    // Solo para Inedible
    private Integer stock;
    private Integer minStock;

    public static ProductResponse from(Product product) {
        ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null);

        if (product instanceof Edible e) {
            builder.type("EDIBLE").pokeName(e.getPokeName());
        } else if (product instanceof Inedible i) {
            builder.type("INEDIBLE").stock(i.getStock()).minStock(i.getMinStock());
        } else {
            builder.type("PRODUCT");
        }

        return builder.build();
    }
}
