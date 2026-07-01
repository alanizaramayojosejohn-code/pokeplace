package com.example.demo.service;

import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Client;
import com.example.demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    public Client getByCi(Integer ci) {
        return clientRepository.findByCi(ci)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ci: " + ci));
    }

    public Client create(Client client) {
        if (clientRepository.existsByCi(client.getCi())) {
            throw new DuplicateResourceException("Client already exists with ci: " + client.getCi());
        }
        return clientRepository.save(client);
    }

    public Client update(Long id, Client updated) {
        Client existing = getById(id);
        existing.setName(updated.getName());
        existing.setCi(updated.getCi());
        return clientRepository.save(existing);
    }

    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }
}
