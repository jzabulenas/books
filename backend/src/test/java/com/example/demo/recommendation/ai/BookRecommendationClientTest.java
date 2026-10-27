package com.example.demo.recommendation.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class BookRecommendationClientTest {

  private static final String VALID_RESPONSE = """
  {"books": [
    {"title": "Dune", "author": "Frank Herbert", "description": "Desert planet epic."},
    {"title": "Project Hail Mary", "author": "Andy Weir", "description": "Lone astronaut saves his species."},
    {"title": "The Left Hand of Darkness", "author": "Ursula K. Le Guin", "description": "Winter world diplomacy."},
    {"title": "Hyperion", "author": "Dan Simmons", "description": "Pilgrims share their tales."},
    {"title": "Blindsight", "author": "Peter Watts", "description": "First contact, reimagined."}
  ]}
  """;

  // Mirrors the real-world failure that motivated this hardening: an unescaped quote
  // inside a description breaks JSON parsing.
  private static final String MALFORMED_RESPONSE = """
  {"books": [
    {"title": "Dune", "author": "Frank Herbert", "description": "A "classic" of the genre"}
  ]}
  """;

  @Test
  void recommendReturnsBooksFromValidResponse() {
    StubChatModel chatModel = new StubChatModel(List.of(VALID_RESPONSE));
    BookRecommendationClient client = new BookRecommendationClient(
      ChatClient.builder(chatModel)
    );

    List<RecommendedBook> books = client.recommend(
      List.of("The Hobbit"),
      List.of(),
      List.of()
    );

    assertThat(books)
      .extracting(RecommendedBook::title)
      .containsExactly(
        "Dune",
        "Project Hail Mary",
        "The Left Hand of Darkness",
        "Hyperion",
        "Blindsight"
      );

    assertThat(books.getFirst().author()).isEqualTo("Frank Herbert");

    assertThat(chatModel.callCount()).isEqualTo(1);
  }

  @Test
  void recommendRetriesAfterMalformedResponseAndSucceeds() {
    StubChatModel chatModel = new StubChatModel(
      List.of(MALFORMED_RESPONSE, VALID_RESPONSE)
    );
    BookRecommendationClient client = new BookRecommendationClient(
      ChatClient.builder(chatModel)
    );

    List<RecommendedBook> books = client.recommend(
      List.of("The Hobbit"),
      List.of(),
      List.of()
    );

    assertThat(books)
      .extracting(RecommendedBook::title)
      .containsExactly(
        "Dune",
        "Project Hail Mary",
        "The Left Hand of Darkness",
        "Hyperion",
        "Blindsight"
      );

    // The first, malformed response must have been retried, not returned.
    assertThat(chatModel.callCount()).isEqualTo(2);
  }

  @Test
  void recommendThrowsAfterExhaustingValidationRetries() {
    StubChatModel chatModel = new StubChatModel(List.of(MALFORMED_RESPONSE));
    BookRecommendationClient client = new BookRecommendationClient(
      ChatClient.builder(chatModel)
    );

    // One initial call plus the framework default of 3 validation retries.
    assertThatThrownBy(() ->
      client.recommend(List.of("The Hobbit"), List.of(), List.of())
    ).isInstanceOf(AiRecommendationException.class);

    assertThat(chatModel.callCount()).isEqualTo(4);
  }

  @Test
  void recommendThrowsWhenResponseHasNoBooks() {
    StubChatModel chatModel = new StubChatModel(List.of("{\"books\": null}"));
    BookRecommendationClient client = new BookRecommendationClient(
      ChatClient.builder(chatModel)
    );

    assertThatThrownBy(() ->
      client.recommend(List.of("The Hobbit"), List.of(), List.of())
    )
      .isInstanceOf(AiRecommendationException.class)
      .hasMessage("AI client returned null books");

    assertThat(chatModel.callCount()).isEqualTo(4);
  }

  // A minimal `ChatModel` replaying the given response texts in order, reusing the
  // last one once it runs out, and counting the calls it served.
  private static final class StubChatModel implements ChatModel {

    private final List<String> responseTexts;
    private final AtomicInteger calls = new AtomicInteger();

    StubChatModel(List<String> responseTexts) {
      this.responseTexts = responseTexts;
    }

    int callCount() {
      return this.calls.get();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
      int call = this.calls.getAndIncrement();
      int index = Math.min(call, this.responseTexts.size() - 1);
      String text = this.responseTexts.get(index);

      return new ChatResponse(
        List.of(new Generation(new AssistantMessage(text)))
      );
    }
  }
}
