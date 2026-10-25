package com.example.demo.recommendation;

import com.example.demo.blocked.BlockedBookService;
import com.example.demo.book.ReadBookService;
import com.example.demo.dismissed.DismissedBookService;
import com.example.demo.recommendation.ai.BookRecommendationClient;
import com.example.demo.recommendation.ai.RecommendedBook;
import com.example.demo.saved.SavedBookService;
import com.example.demo.user.GenerationQuotaService;
import com.example.demo.user.User;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class RecommendationService {

  private final RecommendationRepository repository;
  private final ReadBookService readBookService;
  private final BlockedBookService blockedBookService;
  private final DismissedBookService dismissedBookService;
  private final SavedBookService savedBookService;
  private final BookRecommendationClient aiClient;
  private final GenerationQuotaService quotaService;

  RecommendationService(
    RecommendationRepository repository,
    ReadBookService readBookService,
    BlockedBookService blockedBookService,
    DismissedBookService dismissedBookService,
    SavedBookService savedBookService,
    BookRecommendationClient aiClient,
    GenerationQuotaService quotaService
  ) {
    this.repository = repository;
    this.readBookService = readBookService;
    this.blockedBookService = blockedBookService;
    this.dismissedBookService = dismissedBookService;
    this.savedBookService = savedBookService;
    this.aiClient = aiClient;
    this.quotaService = quotaService;
  }

  List<RecommendationResponse> getAll(Long userId) {
    Set<String> excludedLower = this.excludedTitlesLower(userId);

    return this.repository
      .findByUserId(userId)
      .stream()
      .filter(
        r -> !excludedLower.contains(r.getTitle().toLowerCase(Locale.ROOT))
      )
      .map(r ->
        new RecommendationResponse(
          r.getId(),
          r.getTitle(),
          r.getAuthor(),
          r.getDescription()
        )
      )
      .toList();
  }

  @Transactional
  public List<RecommendationResponse> generate(User user) {
    Long userId = user.getId();
    List<String> titles = this.readBookService.getTitles(userId);

    if (titles.size() < 3) {
      throw new ResponseStatusException(
        HttpStatusCode.valueOf(422),
        "Need at least 3 read books to generate recommendations"
      );
    }

    // Enforce the daily quota before spending an AI call.
    this.quotaService.assertWithinDailyLimit(user);

    List<String> blockedTitles = this.blockedBookService.getTitles(userId);
    List<String> dismissedTitles = this.dismissedBookService.getTitles(userId);
    List<String> savedTitles = this.savedBookService.getTitles(userId);

    // Blocked and dismissed books mean the same thing to the model: already rejected.
    List<String> rejectedTitles = Stream.concat(
      blockedTitles.stream(),
      dismissedTitles.stream()
    ).toList();

    List<RecommendedBook> suggested = this.aiClient.recommend(
      titles,
      rejectedTitles,
      savedTitles
    );

    Set<String> excludedLower = this.excludedTitlesLower(userId);

    // Defensive safety net: the prompt already asks the model to avoid read, rejected or
    // saved books, but drop any it suggests anyway rather than reintroduce something the
    // user already knows. Blocked and dismissed titles are excluded permanently, but a read
    // title is only excluded while it stays in the read list: this filter reads live state,
    // so removing the book from My Books later makes it suggestable again.
    List<RecommendedBook> filtered = suggested
      .stream()
      .filter(r -> !excludedLower.contains(r.title().toLowerCase(Locale.ROOT)))
      .toList();

    // Replace previous recommendations so repeated generation does not accumulate rows.
    this.repository.deleteByUserId(userId);
    List<Recommendation> saved = this.repository.saveAll(
      filtered
        .stream()
        .map(r ->
          new Recommendation(userId, r.title(), r.author(), r.description())
        )
        .toList()
    );

    // Record the successful generation so it counts against the daily quota.
    this.quotaService.recordGeneration(userId);

    return saved
      .stream()
      .map(r ->
        new RecommendationResponse(
          r.getId(),
          r.getTitle(),
          r.getAuthor(),
          r.getDescription()
        )
      )
      .toList();
  }

  // Read, blocked, dismissed and saved titles all mean the user has already acted on the
  // book: `getAll` drops such rows so a reviewed recommendation never shows up again,
  // and `generate` never re-suggests them.
  private Set<String> excludedTitlesLower(Long userId) {
    return Stream.of(
      this.readBookService.getTitles(userId),
      this.blockedBookService.getTitles(userId),
      this.dismissedBookService.getTitles(userId),
      this.savedBookService.getTitles(userId)
    )
      .flatMap(List::stream)
      .map(n -> n.toLowerCase(Locale.ROOT))
      .collect(Collectors.toSet());
  }
}
