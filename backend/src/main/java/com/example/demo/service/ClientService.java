package com.example.demo.service;

import com.example.demo.config.SecurityUtils;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Audit;
import com.example.demo.model.Client;
import com.example.demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public List<Client> getAll() {
        return clientRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Client> getAll(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    public Client getByNit(String nit) {
        return clientRepository.findByNit(nit)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with nit: " + nit));
    }

    @Transactional
    public Client create(Client client) {
        if (clientRepository.existsByNit(client.getNit())) {
            throw new DuplicateResourceException("Client already exists with nit: " + client.getNit());
        }
        Client saved = clientRepository.save(client);
        auditService.log("CLIENT", saved.getId(), Audit.AuditAction.CREATE,
                null, sanitized(saved), securityUtils.getCurrentUsername());
        return saved;
    }

    @Transactional
    public Client update(Long id, Client updated) {
        Client existing = getById(id);
        Client previous = sanitized(existing);
        existing.setNit(updated.getNit());
        existing.setName(updated.getName());
        existing.setCi(updated.getCi());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        Client saved = clientRepository.save(existing);
        auditService.log("CLIENT", id, Audit.AuditAction.UPDATE,
                previous, sanitized(saved), securityUtils.getCurrentUsername());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Client existing = getById(id);
        clientRepository.deleteById(id);
        auditService.log("CLIENT", id, Audit.AuditAction.DELETE,
                sanitized(existing), null, securityUtils.getCurrentUsername());
    }

    private Client sanitized(Client c) {
        Client copy = new Client();
        copy.setId(c.getId());
        copy.setNit(c.getNit());
        copy.setName(c.getName());
        copy.setCi(c.getCi());
        copy.setPhone(c.getPhone());
        copy.setEmail(c.getEmail());
        return copy;
    }
}
