package com.example.demo.book;

import com.example.demo.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/read-books")
class ReadBookController {

  private final ReadBookService readBookService;
  private final UserService userService;

  ReadBookController(ReadBookService readBookService, UserService userService) {
    this.readBookService = readBookService;
    this.userService = userService;
  }

  @GetMapping
  List<BookResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    return this.readBookService.findAll(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  BookResponse add(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @RequestBody AddBookRequest request
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    return this.readBookService.add(userId, request.title());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable Long id
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    this.readBookService.delete(id, userId);
  }
}
