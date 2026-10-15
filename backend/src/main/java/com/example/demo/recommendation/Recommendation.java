package com.example.demo.recommendation;

import jakarta.persistence.*;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "recommendations")
class Recommendation {

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

  @Column(length = 1000)
  @Nullable
  private String description;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @SuppressWarnings("NullAway.Init")
  Recommendation() {}

  Recommendation(
    Long userId,
    String title,
    @Nullable String author,
    @Nullable String description
  ) {
    this.userId = userId;
    this.title = title;
    this.author = author;
    this.description = description;
    this.createdAt = Instant.now();
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

  @Nullable
  String getDescription() {
    return this.description;
  }
}
