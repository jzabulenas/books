package com.example.demo.e2e;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class ReadBookE2ETest extends E2ESupport {

  @Test
  void addBook_thenList_containsNewBook() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/read-books")
      .then()
      .statusCode(201);

    spec
      .get("/api/v1/read-books")
      .then()
      .statusCode(200)
      .body("title", hasItem("Dune"));
  }

  @Test
  void addBook_thenDelete_thenList_isEmpty() {
    RequestSpecification spec = login(uniqueEmail());

    int id = addBook(spec, "Dune");

    spec
      .delete("/api/v1/read-books/" + id)
      .then()
      .statusCode(204);

    spec.get("/api/v1/read-books").then().statusCode(200).body("$", empty());
  }

  @Test
  void addDuplicateBook_returns409() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/read-books")
      .then()
      .statusCode(201);

    spec
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/read-books")
      .then()
      .statusCode(409);
  }

  @Test
  void addBookWithBlankTitle_returns400() {
    login(uniqueEmail())
      .body(
        """
        {"title": ""}
        """
      )
      .post("/api/v1/read-books")
      .then()
      .statusCode(400);
  }

  @Test
  void readBooks_areIsolatedBetweenUsers() {
    RequestSpecification userA = login(uniqueEmail());
    RequestSpecification userB = login(uniqueEmail());

    addBook(userA, "Neuromancer");
    addBook(userB, "Dune");

    userA
      .get("/api/v1/read-books")
      .then()
      .statusCode(200)
      .body("title", hasItem("Neuromancer"))
      .body("title", not(hasItem("Dune")));

    userB
      .get("/api/v1/read-books")
      .then()
      .statusCode(200)
      .body("title", hasItem("Dune"))
      .body("title", not(hasItem("Neuromancer")));
  }

  @Test
  void deleteBook_belongingToOtherUser_doesNotDeleteIt() {
    RequestSpecification userA = login(uniqueEmail());
    RequestSpecification userB = login(uniqueEmail());

    int id = addBook(userA, "Dune");

    userB
      .delete("/api/v1/read-books/" + id)
      .then()
      .statusCode(204);

    userA
      .get("/api/v1/read-books")
      .then()
      .statusCode(200)
      .body("title", hasItem("Dune"));
  }

  @Test
  void readBooks_whenUnauthenticated_redirects() {
    unauthenticated().get("/api/v1/read-books").then().statusCode(302);
  }

  private static int addBook(RequestSpecification spec, String title) {
    return spec
      .body(
        """
        {"title": "%s"}
        """.formatted(title)
      )
      .post("/api/v1/read-books")
      .then()
      .statusCode(201)
      .extract()
      .path("id");
  }
}
