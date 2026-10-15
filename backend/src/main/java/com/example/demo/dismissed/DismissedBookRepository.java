package com.example.demo.dismissed;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface DismissedBookRepository extends JpaRepository<DismissedBook, Long> {
  List<DismissedBook> findByUserId(Long userId);

  boolean existsByUserIdAndTitle(Long userId, String title);
}
