package com.example.demo.dto;

import com.example.demo.model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientResponse {
    private Long id;
    private String nit;
    private String name;
    private String ci;
    private String phone;
    private String email;

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getNit(),
                client.getName(),
                client.getCi(),
                client.getPhone(),
                client.getEmail()
        );
    }
}
