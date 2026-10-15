package com.example.demo.recommendation;

import org.jspecify.annotations.Nullable;

record RecommendationResponse(
  Long id,
  String title,
  @Nullable String author,
  @Nullable String description
) {}
