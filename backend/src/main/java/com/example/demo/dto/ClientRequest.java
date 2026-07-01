package com.example.demo.dto;

import com.example.demo.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "NIT is required")
    private String nit;

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$", message = "Name must contain only letters and spaces")
    private String name;

    @NotBlank(message = "CI is required")
    private String ci;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{8,15}$", message = "Phone must contain only numbers (8-15 digits)")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    public Client toEntity() {
        return Client.builder()
                .nit(this.nit)
                .name(this.name)
                .ci(this.ci)
                .phone(this.phone)
                .email(this.email)
                .build();
    }
}
