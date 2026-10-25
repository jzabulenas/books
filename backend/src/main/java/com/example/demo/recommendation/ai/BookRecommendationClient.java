package com.example.demo.recommendation.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class BookRecommendationClient {

  private final ChatClient chatClient;

  BookRecommendationClient(ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  public List<RecommendedBook> recommend(
    List<String> readBooks,
    List<String> rejectedBooks,
    List<String> savedBooks
  ) {
    String bookList = String.join(", ", readBooks);
    String rejectedList = rejectedBooks.isEmpty()
      ? "none"
      : String.join(", ", rejectedBooks);
    String savedList = savedBooks.isEmpty()
      ? "none"
      : String.join(", ", savedBooks);

    RecommendedBookResponse response = this.chatClient
      .prompt()
      .user(u ->
        u
          .text(
            """
            I've read these books: {books}.
            Suggest exactly 5 books I haven't listed that I'd likely enjoy.
            For each provide: title, author, and a short description of what the book is about.
            Don't repeat any book from my list.
            Never suggest any of these books, as I've already rejected them: {rejected}.
            Never suggest any of these books either, as I've already saved them for later: {saved}.
            """
          )
          .param("books", bookList)
          .param("rejected", rejectedList)
          .param("saved", savedList)
      )
      .call()
      .entity(RecommendedBookResponse.class);

    if (response == null) {
      throw new IllegalStateException("AI client returned null response");
    }

    List<RecommendedBook> books = response.books();

    if (books == null) {
      throw new IllegalStateException("AI client returned null books");
    }

    return books;
  }
}
