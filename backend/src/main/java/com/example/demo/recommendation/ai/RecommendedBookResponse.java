package com.example.demo.recommendation.ai;

import java.util.List;
import org.jspecify.annotations.Nullable;

record RecommendedBookResponse(@Nullable List<RecommendedBook> books) {}
