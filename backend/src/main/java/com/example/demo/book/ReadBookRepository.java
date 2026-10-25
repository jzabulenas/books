package com.example.demo.book;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReadBookRepository extends JpaRepository<ReadBook, Long> {
  List<ReadBook> findByUserId(Long userId);

  boolean existsByUserIdAndTitle(Long userId, String title);

  void deleteByIdAndUserId(Long id, Long userId);
}
