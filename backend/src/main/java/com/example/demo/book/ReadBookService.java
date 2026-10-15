package com.example.demo.book;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReadBookService {

  private final ReadBookRepository repository;

  ReadBookService(ReadBookRepository repository) {
    this.repository = repository;
  }

  public List<BookResponse> findAll(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(b -> new BookResponse(b.getId(), b.getTitle(), b.getAddedAt()))
      .toList();
  }

  public BookResponse add(Long userId, String title) {
    try {
      ReadBook book = this.repository.save(new ReadBook(userId, title));

      return new BookResponse(book.getId(), book.getTitle(), book.getAddedAt());
    } catch (DataIntegrityViolationException e) {
      // Rely on the DB unique constraint rather than a pre-check query: two concurrent
      // requests could both pass an existsByUserIdAndTitle check and then both attempt
      // the insert, so we would need to catch this exception either way.
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(409),
        "Book already added"
      );
    }
  }

  // Check first instead of relying on catching `DataIntegrityViolationException` alone:
  // when called within an outer transaction (e.g. reading a saved book), a constraint
  // violation may only surface at commit time, past any catch block here.
  // Adding a book that is already read is not an error, just a no-op.
  public void addIfAbsent(Long userId, String title) {
    if (this.repository.existsByUserIdAndTitle(userId, title)) {
      return;
    }

    try {
      this.repository.save(new ReadBook(userId, title));
    } catch (DataIntegrityViolationException e) {
      // Already read for this user - adding it again is not an error, just a no-op.
    }
  }

  // `deleteByIdAndUserId` uses `em.remove()` internally, which requires an active transaction.
  @Transactional
  public void delete(Long id, Long userId) {
    this.repository.deleteByIdAndUserId(id, userId);
  }

  public List<String> getTitles(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(ReadBook::getTitle)
      .toList();
  }
}
