package com.example.demo.dto;

import com.example.demo.model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientResponse {
    private Long id;
    private Integer ci;
    private String name;

    public static ClientResponse from(Client client) {
        return new ClientResponse(client.getId(), client.getCi(), client.getName());
    }
}
