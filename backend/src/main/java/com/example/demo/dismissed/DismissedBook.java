package com.example.demo.dismissed;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dismissed_books")
class DismissedBook {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, updatable = false)
  private Instant dismissedAt;

  @SuppressWarnings("NullAway.Init")
  DismissedBook() {}

  DismissedBook(Long userId, String title) {
    this.userId = userId;
    this.title = title;
    this.dismissedAt = Instant.now();
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

  Instant getDismissedAt() {
    return this.dismissedAt;
  }
}
