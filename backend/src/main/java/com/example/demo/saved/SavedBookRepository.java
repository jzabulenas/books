package com.example.demo.saved;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface SavedBookRepository extends JpaRepository<SavedBook, Long> {
  List<SavedBook> findByUserId(Long userId);

  Optional<SavedBook> findByIdAndUserId(Long id, Long userId);

  void deleteByIdAndUserId(Long id, Long userId);
}
