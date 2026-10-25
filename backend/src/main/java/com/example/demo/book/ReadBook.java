package com.example.demo.book;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "read_books")
class ReadBook {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, updatable = false)
  private Instant addedAt;

  @SuppressWarnings("NullAway.Init")
  ReadBook() {}

  ReadBook(Long userId, String title) {
    this.userId = userId;
    this.title = title;
    this.addedAt = Instant.now();
  }

  Long getId() {
    return this.id;
  }

  Long getUserId() {
    return this.userId;
  }

  String getTitle() {
    return this.title;
  }

  Instant getAddedAt() {
    return this.addedAt;
  }
}
