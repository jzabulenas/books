package com.example.demo.blocked;

import jakarta.validation.constraints.NotBlank;

record BlockBookRequest(@NotBlank String title) {}
