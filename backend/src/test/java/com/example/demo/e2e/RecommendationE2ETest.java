package com.example.demo.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class RecommendationE2ETest extends E2ESupport {

  @Test
  void generateRecommendations_withThreeReadBooks_returnsExactlyFiveBooks() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .body("$", hasSize(5))
      .body("title", everyItem(not(emptyOrNullString())))
      .body("author", everyItem(anyOf(nullValue(), not(emptyString()))))
      .body("description", everyItem(not(emptyOrNullString())));
  }

  @Test
  void getRecommendations_afterGenerate_returnsSameBooks() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> generatedTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("title", equalTo(generatedTitles));
  }

  @Test
  void getRecommendations_afterSavingBook_excludesItFromTheList() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> generatedTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String saved = generatedTitles.get(0);

    spec
      .body(
        """
        {"title": "%s"}
        """.formatted(saved)
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201);

    List<String> remainingTitles = spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(remainingTitles, not(hasItem(saved)));
    assertThat(remainingTitles, hasSize(generatedTitles.size() - 1));
  }

  @Test
  void getRecommendations_afterBlockingBook_excludesItFromTheList() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> generatedTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String blocked = generatedTitles.get(0);

    spec
      .body(
        """
        {"title": "%s"}
        """.formatted(blocked)
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);

    List<String> remainingTitles = spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(remainingTitles, not(hasItem(blocked)));
    assertThat(remainingTitles, hasSize(generatedTitles.size() - 1));
  }

  @Test
  void getRecommendations_afterRemovingSavedBook_excludesItFromTheList() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> generatedTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String removed = generatedTitles.get(0);

    int savedId = spec
      .body(
        """
        {"title": "%s"}
        """.formatted(removed)
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .delete("/api/v1/saved-books/" + savedId)
      .then()
      .statusCode(204);

    List<String> remainingTitles = spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(remainingTitles, not(hasItem(removed)));
    assertThat(remainingTitles, hasSize(generatedTitles.size() - 1));
  }

  @Test
  void getRecommendations_afterMarkingSavedBookAsRead_excludesItFromTheList() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> generatedTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String read = generatedTitles.get(0);

    int savedId = spec
      .body(
        """
        {"title": "%s"}
        """.formatted(read)
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .post("/api/v1/saved-books/" + savedId + "/read")
      .then()
      .statusCode(204);

    List<String> remainingTitles = spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(remainingTitles, not(hasItem(read)));
    assertThat(remainingTitles, hasSize(generatedTitles.size() - 1));
  }

  @Test
  void generateTwice_replacesOldRecommendations() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<Integer> firstIds = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("id", Integer.class);

    List<Integer> secondIds = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("id", Integer.class);

    spec
      .get("/api/v1/recommendations")
      .then()
      .statusCode(200)
      .body("id", equalTo(secondIds))
      .body("id", not(equalTo(firstIds)));
  }

  @Test
  void generateRecommendations_afterBlockingBook_neverSuggestsItAgain() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> firstTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String blocked = firstTitles.get(0);

    spec
      .body(
        """
        {"title": "%s"}
        """.formatted(blocked)
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);

    List<String> secondTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(secondTitles, not(hasItem(blocked)));
  }

  @Test
  void generateRecommendations_afterSavingBook_neverSuggestsItAgain() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> firstTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String saved = firstTitles.get(0);

    spec
      .body(
        """
        {"title": "%s"}
        """.formatted(saved)
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201);

    List<String> secondTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(secondTitles, not(hasItem(saved)));
  }

  @Test
  void generateRecommendations_afterRemovingSavedBook_neverSuggestsItAgain() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    List<String> firstTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);
    String removed = firstTitles.get(0);

    int savedId = spec
      .body(
        """
        {"title": "%s"}
        """.formatted(removed)
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .delete("/api/v1/saved-books/" + savedId)
      .then()
      .statusCode(204);

    List<String> secondTitles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(secondTitles, not(hasItem(removed)));
  }

  @Test
  void generate_withFewerThanThreeReadBooks_returns422() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer");

    spec.post("/api/v1/recommendations/generate").then().statusCode(422);
  }

  @Test
  void recommendations_whenUnauthenticated_redirects() {
    unauthenticated()
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(302);
  }
}
