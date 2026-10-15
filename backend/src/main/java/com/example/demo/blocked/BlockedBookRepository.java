package com.example.demo.blocked;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface BlockedBookRepository extends JpaRepository<BlockedBook, Long> {
  List<BlockedBook> findByUserId(Long userId);
}
