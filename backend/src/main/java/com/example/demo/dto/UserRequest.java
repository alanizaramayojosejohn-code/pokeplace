package com.example.demo.dto;

import com.example.demo.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Lastname is required")
    private String lastname;

    private String phone;

    @NotNull(message = "CI is required")
    @Positive(message = "CI must be positive")
    private Integer ci;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Password opcional en update; obligatorio en create (se valida en el service/controller)
    private String password;

    @NotNull(message = "Role is required")
    private User.Role role;

    // Sueldo quincenal (opcional al crear, puede asignarse luego)
    @PositiveOrZero(message = "Salary cannot be negative")
    private Double salary;

    public User toEntity() {
        User user = new User();
        user.setName(this.name);
        user.setLastname(this.lastname);
        user.setPhone(this.phone);
        user.setCi(this.ci);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setRole(this.role);
        user.setSalary(this.salary);
        return user;
    }
}
