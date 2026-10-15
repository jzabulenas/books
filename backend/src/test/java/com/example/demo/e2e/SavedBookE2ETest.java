package com.example.demo.e2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.specification.RequestSpecification;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class SavedBookE2ETest extends E2ESupport {

  @Test
  void addBook_withAuthor_thenList_containsBookWithAuthor() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Dune", "author": "Frank Herbert"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201);

    spec
      .get("/api/v1/saved-books")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].title", equalTo("Dune"))
      .body("[0].author", equalTo("Frank Herbert"));
  }

  @Test
  void addBook_withoutAuthor_isAccepted() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201);

    spec
      .get("/api/v1/saved-books")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].title", equalTo("Neuromancer"))
      .body("[0].author", nullValue());
  }

  @Test
  void addBook_thenDelete_thenList_isEmpty() {
    RequestSpecification spec = login(uniqueEmail());

    int id = spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .delete("/api/v1/saved-books/" + id)
      .then()
      .statusCode(204);

    spec.get("/api/v1/saved-books").then().statusCode(200).body("$", empty());
  }

  @Test
  void deleteUnknownId_returns204() {
    RequestSpecification spec = login(uniqueEmail());

    spec.delete("/api/v1/saved-books/999999").then().statusCode(204);
  }

  @Test
  void removeBook_thenSaveSameBookAgain_isAccepted() {
    RequestSpecification spec = login(uniqueEmail());

    int id = spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .delete("/api/v1/saved-books/" + id)
      .then()
      .statusCode(204);

    spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201);
  }

  @Test
  void addDuplicateBook_returns409() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201);

    spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(409);
  }

  @Test
  void markSavedBookAsRead_addsItToReadBooks_andRemovesItFromSaved() {
    RequestSpecification spec = login(uniqueEmail());

    int id = spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .post("/api/v1/saved-books/" + id + "/read")
      .then()
      .statusCode(204);

    spec.get("/api/v1/saved-books").then().statusCode(200).body("$", empty());

    spec
      .get("/api/v1/read-books")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].title", equalTo("Neuromancer"));
  }

  @Test
  void markSavedBookAsRead_alreadyRead_isStillASuccess_andDoesNotDuplicateIt() {
    RequestSpecification spec = login(uniqueEmail());

    addReadBooks(spec, "Neuromancer");

    int id = spec
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/saved-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");

    spec
      .post("/api/v1/saved-books/" + id + "/read")
      .then()
      .statusCode(204);

    spec.get("/api/v1/saved-books").then().statusCode(200).body("$", empty());

    spec
      .get("/api/v1/read-books")
      .then()
      .statusCode(200)
      .body("$", hasSize(1))
      .body("[0].title", equalTo("Neuromancer"));
  }

  @Test
  void markReadUnknownId_returns204() {
    RequestSpecification spec = login(uniqueEmail());

    spec.post("/api/v1/saved-books/999999/read").then().statusCode(204);
  }

  @Test
  void readSavedBooks_seedTheNextGeneration_andAreNeverSuggested() {
    RequestSpecification spec = login(uniqueEmail());

    // Marking as read is the only way this user's read list gets populated, so a
    // successful generation proves the read saved books became the seeds.
    for (String title : List.of(
      "Dune",
      "Neuromancer",
      "The Left Hand of Darkness"
    )) {
      int id = spec
        .body(
          """
          {"title": "%s"}
          """.formatted(title)
        )
        .post("/api/v1/saved-books")
        .then()
        .statusCode(201)
        .extract()
        .path("id");

      spec
        .post("/api/v1/saved-books/" + id + "/read")
        .then()
        .statusCode(204);
    }

    spec.get("/api/v1/saved-books").then().statusCode(200).body("$", empty());

    List<String> titles = spec
      .post("/api/v1/recommendations/generate")
      .then()
      .statusCode(200)
      .body("$", hasSize(5))
      .extract()
      .jsonPath()
      .getList("title", String.class);

    assertThat(titles, not(hasItem("Dune")));
    assertThat(titles, not(hasItem("Neuromancer")));
    assertThat(titles, not(hasItem("The Left Hand of Darkness")));
  }
}
