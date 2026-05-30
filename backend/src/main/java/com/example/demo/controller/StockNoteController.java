package com.example.demo.controller;

import com.example.demo.model.StockNote;
import com.example.demo.service.StockNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-notes")
@RequiredArgsConstructor
public class StockNoteController {

    private final StockNoteService stockNoteService;

    @GetMapping
    public ResponseEntity<List<StockNote>> getAll(
            @RequestParam(required = false) StockNote.NoteStatus status) {
        if (status != null) {
            return ResponseEntity.ok(stockNoteService.getByStatus(status));
        }
        return ResponseEntity.ok(stockNoteService.getAll());
    }

    @PostMapping
    public ResponseEntity<StockNote> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(stockNoteService.create(body.get("message")));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<StockNote> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(stockNoteService.resolve(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockNoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}