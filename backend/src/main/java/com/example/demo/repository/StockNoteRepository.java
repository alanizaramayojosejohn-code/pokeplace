package com.example.demo.repository;

import com.example.demo.model.StockNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockNoteRepository extends JpaRepository<StockNote, Long> {
    List<StockNote> findAllByOrderByCreatedAtDesc();
    List<StockNote> findByStatusOrderByCreatedAtDesc(StockNote.NoteStatus status);
}