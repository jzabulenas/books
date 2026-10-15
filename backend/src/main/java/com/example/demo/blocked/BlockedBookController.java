package com.example.demo.blocked;

import com.example.demo.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blocked-books")
class BlockedBookController {

  private final BlockedBookService blockedBookService;
  private final UserService userService;

  BlockedBookController(
    BlockedBookService blockedBookService,
    UserService userService
  ) {
    this.blockedBookService = blockedBookService;
    this.userService = userService;
  }

  @GetMapping
  List<String> list(@AuthenticationPrincipal UserDetails userDetails) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    return this.blockedBookService.getTitles(userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void block(
    @AuthenticationPrincipal UserDetails userDetails,
    @Valid @RequestBody BlockBookRequest request
  ) {
    Long userId = this.userService
      .findByEmail(userDetails.getUsername())
      .getId();

    this.blockedBookService.block(userId, request.title());
  }
}
