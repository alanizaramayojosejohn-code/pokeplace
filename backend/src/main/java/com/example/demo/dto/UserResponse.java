package com.example.demo.dto;

import com.example.demo.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String lastname;
    private String phone;
    private Integer ci;
    private String email;
    private String role;
    private Double salary;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setLastname(user.getLastname());
        response.setPhone(user.getPhone());
        response.setCi(user.getCi());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().name() : null);
        response.setSalary(user.getSalary());
        return response;
    }
}
