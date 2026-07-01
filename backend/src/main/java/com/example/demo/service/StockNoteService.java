package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.StockNote;
import com.example.demo.repository.StockNoteRepository;
import com.example.demo.config.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockNoteService {

    private final StockNoteRepository stockNoteRepository;

    public List<StockNote> getAll() {
        return stockNoteRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<StockNote> getByStatus(StockNote.NoteStatus status) {
        return stockNoteRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public StockNote create(String message) {
    StockNote note = StockNote.builder()
            .message(message)
            .status(StockNote.NoteStatus.PENDING)
            .build();
    return stockNoteRepository.save(note);
}

    public StockNote resolve(Long id) {
        StockNote note = stockNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
        note.setStatus(StockNote.NoteStatus.RESOLVED);
        return stockNoteRepository.save(note);
    }

    public void delete(Long id) {
        stockNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
        stockNoteRepository.deleteById(id);
    }
}