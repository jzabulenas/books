package com.example.demo.blocked;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class BlockedBookService {

  private final BlockedBookRepository repository;

  BlockedBookService(BlockedBookRepository repository) {
    this.repository = repository;
  }

  public void block(Long userId, String title) {
    try {
      this.repository.save(new BlockedBook(userId, title));
    } catch (DataIntegrityViolationException e) {
      // Already blocked for this user - blocking again is not an error, just a no-op.
    }
  }

  public List<String> getTitles(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(BlockedBook::getTitle)
      .toList();
  }
}
