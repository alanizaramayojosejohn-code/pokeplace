package com.example.demo.controller;

import com.example.demo.dto.ClientRequest;
import com.example.demo.dto.ClientResponse;
import com.example.demo.model.Client;
import com.example.demo.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(
                clientService.getAll().stream().map(ClientResponse::from).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ClientResponse.from(clientService.getById(id)));
    }

    @GetMapping("/ci/{ci}")
    public ResponseEntity<ClientResponse> getByCi(@PathVariable Integer ci) {
        return ResponseEntity.ok(ClientResponse.from(clientService.getByCi(ci)));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client created = clientService.create(request.toEntity());
        return ResponseEntity.ok(ClientResponse.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody ClientRequest request) {
        Client updated = clientService.update(id, request.toEntity());
        return ResponseEntity.ok(ClientResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
