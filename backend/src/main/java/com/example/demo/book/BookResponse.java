package com.example.demo.book;

import java.time.Instant;

record BookResponse(Long id, String title, Instant addedAt) {}
