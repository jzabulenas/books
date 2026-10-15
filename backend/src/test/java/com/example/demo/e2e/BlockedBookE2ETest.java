package com.example.demo.e2e;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class BlockedBookE2ETest extends E2ESupport {

  @Test
  void blockBook_returns204() {
    login(uniqueEmail())
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);
  }

  @Test
  void blockBook_thenList_containsBlockedBook() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);

    spec
      .get("/api/v1/blocked-books")
      .then()
      .statusCode(200)
      .body("$", hasItem("Dune"));
  }

  @Test
  void listBlockedBooks_whenNoneBlocked_isEmpty() {
    login(uniqueEmail())
      .get("/api/v1/blocked-books")
      .then()
      .statusCode(200)
      .body("$", empty());
  }

  @Test
  void blockedBooks_areIsolatedBetweenUsers() {
    RequestSpecification userA = login(uniqueEmail());
    RequestSpecification userB = login(uniqueEmail());

    userA
      .body(
        """
        {"title": "Neuromancer"}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);

    userB
      .get("/api/v1/blocked-books")
      .then()
      .statusCode(200)
      .body("$", not(hasItem("Neuromancer")));
  }

  @Test
  void listBlockedBooks_whenUnauthenticated_redirects() {
    unauthenticated().get("/api/v1/blocked-books").then().statusCode(302);
  }

  @Test
  void blockBook_whenAlreadyBlocked_isStillAccepted() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);

    spec
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(204);
  }

  @Test
  void blockBook_withBlankTitle_returns400() {
    login(uniqueEmail())
      .body(
        """
        {"title": ""}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(400);
  }

  @Test
  void blockBook_whenUnauthenticated_redirects() {
    unauthenticated()
      .body(
        """
        {"title": "Dune"}
        """
      )
      .post("/api/v1/blocked-books")
      .then()
      .statusCode(302);
  }
}
