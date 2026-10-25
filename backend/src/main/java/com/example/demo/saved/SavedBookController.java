package com.example.demo.saved;

import com.example.demo.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/saved-books")
class SavedBookController {

  private final SavedBookService savedBookService;
  private final UserService userService;

  SavedBookController(
    SavedBookService savedBookService,
    UserService userService
  ) {
    this.savedBookService = savedBookService;
    this.userService = userService;
  }

  @GetMapping
  List<SavedBookResponse> list(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    return this.savedBookService.findAll(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  SavedBookResponse save(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @RequestBody SaveBookRequest request
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    return this.savedBookService.save(
      userId,
      request.title(),
      request.author()
    );
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

    this.savedBookService.delete(id, userId);
  }

  @PostMapping("/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void markRead(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable Long id
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    this.savedBookService.markRead(id, userId);
  }
}
