package com.example.demo.saved;

import com.example.demo.book.ReadBookService;
import com.example.demo.dismissed.DismissedBookService;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SavedBookService {

  private final SavedBookRepository repository;
  private final DismissedBookService dismissedBookService;
  private final ReadBookService readBookService;

  SavedBookService(
    SavedBookRepository repository,
    DismissedBookService dismissedBookService,
    ReadBookService readBookService
  ) {
    this.repository = repository;
    this.dismissedBookService = dismissedBookService;
    this.readBookService = readBookService;
  }

  public List<SavedBookResponse> findAll(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(b ->
        new SavedBookResponse(
          b.getId(),
          b.getTitle(),
          b.getAuthor(),
          b.getSavedAt()
        )
      )
      .toList();
  }

  public SavedBookResponse save(
    Long userId,
    String title,
    @Nullable String author
  ) {
    try {
      SavedBook book = this.repository.save(
        new SavedBook(userId, title, author)
      );

      return new SavedBookResponse(
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getSavedAt()
      );
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(409),
        "Book already saved"
      );
    }
  }

  // `deleteByIdAndUserId` uses `em.remove()` internally, which requires an active transaction.
  // Removing a book also dismisses it, so it is never suggested in future generations.
  @Transactional
  public void delete(Long id, Long userId) {
    Optional<SavedBook> book = this.repository.findByIdAndUserId(id, userId);

    // Defensive: an unknown id, or one owned by another user, is a no-op rather than an error.
    if (book.isEmpty()) {
      return;
    }

    this.dismissedBookService.dismiss(userId, book.get().getTitle());

    this.repository.deleteByIdAndUserId(id, userId);
  }

  // `deleteByIdAndUserId` uses `em.remove()` internally, which requires an active transaction.
  // Marking a saved book as read promotes it to the read list, so it seeds future
  // generations, and removes it from the saved list. Unlike removal, it is not dismissed:
  // removing it from the read list later allows it to be suggested again, since it was
  // never rejected.
  @Transactional
  public void markRead(Long id, Long userId) {
    Optional<SavedBook> book = this.repository.findByIdAndUserId(id, userId);

    // Defensive: an unknown id, or one owned by another user, is a no-op rather than an error.
    if (book.isEmpty()) {
      return;
    }

    this.readBookService.addIfAbsent(userId, book.get().getTitle());

    this.repository.deleteByIdAndUserId(id, userId);
  }

  public List<String> getTitles(Long userId) {
    return this.repository
      .findByUserId(userId)
      .stream()
      .map(SavedBook::getTitle)
      .toList();
  }
}
