package com.example.demo.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.blocked.BlockedBookService;
import com.example.demo.book.ReadBookService;
import com.example.demo.dismissed.DismissedBookService;
import com.example.demo.recommendation.ai.AiRecommendationException;
import com.example.demo.recommendation.ai.BookRecommendationClient;
import com.example.demo.saved.SavedBookService;
import com.example.demo.user.GenerationQuotaService;
import com.example.demo.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

class RecommendationServiceTest {

  @Test
  void generateMapsAiFailureToBadGatewayWithoutRecordingAGeneration() {
    RecommendationRepository repository = mock(RecommendationRepository.class);
    ReadBookService readBookService = mock(ReadBookService.class);
    BlockedBookService blockedBookService = mock(BlockedBookService.class);
    DismissedBookService dismissedBookService = mock(
      DismissedBookService.class
    );
    SavedBookService savedBookService = mock(SavedBookService.class);
    BookRecommendationClient aiClient = mock(BookRecommendationClient.class);
    GenerationQuotaService quotaService = mock(GenerationQuotaService.class);
    RecommendationService service = new RecommendationService(
      repository,
      readBookService,
      blockedBookService,
      dismissedBookService,
      savedBookService,
      aiClient,
      quotaService
    );

    User user = mock(User.class);
    List<String> readTitles = List.of("The Hobbit", "Dune", "Hyperion");

    when(user.getId()).thenReturn(1L);
    when(readBookService.getTitles(1L)).thenReturn(readTitles);
    when(blockedBookService.getTitles(1L)).thenReturn(List.of());
    when(dismissedBookService.getTitles(1L)).thenReturn(List.of());
    when(savedBookService.getTitles(1L)).thenReturn(List.of());
    when(aiClient.recommend(readTitles, List.of(), List.of())).thenThrow(
      new AiRecommendationException("AI recommendation request failed")
    );

    assertThatThrownBy(() -> service.generate(user)).isInstanceOfSatisfying(
      ResponseStatusException.class,
      e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(502))
    );

    // A failed generation must not be recorded against the daily quota.
    verify(quotaService, never()).recordGeneration(any());

    // Nothing may be persisted: old recommendations must survive a failed generation.
    verify(repository, never()).deleteByUserId(any());
    verify(repository, never()).saveAll(any());
  }
}
