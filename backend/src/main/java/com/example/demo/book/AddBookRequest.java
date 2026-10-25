package com.example.demo.book;

import jakarta.validation.constraints.NotBlank;

record AddBookRequest(@NotBlank String title) {}
