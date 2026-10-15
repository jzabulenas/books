package com.example.demo.blocked;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blocked_books")
class BlockedBook {

  @SuppressWarnings("NullAway.Init")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, updatable = false)
  private Instant blockedAt;

  @SuppressWarnings("NullAway.Init")
  BlockedBook() {}

  BlockedBook(Long userId, String title) {
    this.userId = userId;
    this.title = title;
    this.blockedAt = Instant.now();
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

  Instant getBlockedAt() {
    return this.blockedAt;
  }
}
