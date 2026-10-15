package com.example.demo.dismissed;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class DismissedBookService {

  private final DismissedBookRepository repository;

  DismissedBookService(DismissedBookRepository repository) {
    this.repository = repository;
  }

  // Check first instead of relying on catching `DataIntegrityViolationException` alone:
  // when called within an outer transaction (e.g. removing a saved book), a constraint
  // violation may only surface at commit time, past any catch block here.
  public void dismiss(Long userId, String title) {
    if (this.repository.existsByUserIdAndTitle(userId, title)) {
      return;
    }

    try {
      this.repository.save(new DismissedBook(userId, title));
    } catch (DataIntegrityViolationException e) {
      // Already dismissed for this user - dismissing again is not an error, just a no-op.
    }
  }

  public List<String> getTitles(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(DismissedBook::getTitle)
      .toList();
  }
}
