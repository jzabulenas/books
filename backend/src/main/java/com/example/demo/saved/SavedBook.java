package com.example.demo.saved;

import jakarta.persistence.*;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "saved_books")
class SavedBook {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String title;

  @Nullable
  private String author;

  @Column(nullable = false, updatable = false)
  private Instant savedAt;

  @SuppressWarnings("NullAway.Init")
  SavedBook() {}

  SavedBook(Long userId, String title, @Nullable String author) {
    this.userId = userId;
    this.title = title;
    this.author = author;
    this.savedAt = Instant.now();
  }

  Long getId() {
    return this.id;
  }

  String getTitle() {
    return this.title;
  }

  @Nullable
  String getAuthor() {
    return this.author;
  }

  Instant getSavedAt() {
    return this.savedAt;
  }
}
