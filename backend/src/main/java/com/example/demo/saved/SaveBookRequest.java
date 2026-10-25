package com.example.demo.saved;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

record SaveBookRequest(@NotBlank String title, @Nullable String author) {}
