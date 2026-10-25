package com.example.demo.saved;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

record SavedBookResponse(
  Long id,
  String title,
  @Nullable String author,
  Instant savedAt
) {}
