package com.example.demo.dto;

import com.example.demo.model.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ClientRequest {

    @NotNull(message = "CI is required")
    @Positive(message = "CI must be positive")
    private Integer ci;

    @NotBlank(message = "Name is required")
    private String name;

    public Client toEntity() {
        return Client.builder()
                .ci(this.ci)
                .name(this.name)
                .build();
    }
}
