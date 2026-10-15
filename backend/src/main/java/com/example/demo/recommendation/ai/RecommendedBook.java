package com.example.demo.recommendation.ai;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record RecommendedBook(
  String title,
  @Nullable String author,
  @Nullable String description
) {}
