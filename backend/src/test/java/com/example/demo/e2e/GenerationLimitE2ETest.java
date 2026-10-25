package com.example.demo.e2e;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class GenerationLimitE2ETest extends E2ESupport {

  @Test
  void me_forNewUser_returnsUserRoleAndFiveRemainingRequests() {
    RequestSpecification spec = login(uniqueEmail());

    spec
      .get("/api/v1/me")
      .then()
      .statusCode(200)
      .body("role", equalTo("USER"))
      .body("remainingRequests", equalTo(5));
  }

  @Test
  void me_afterOneGeneration_returnsFourRemainingRequests() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    spec.post("/api/v1/recommendations/generate").then().statusCode(200);

    spec
      .get("/api/v1/me")
      .then()
      .statusCode(200)
      .body("role", equalTo("USER"))
      .body("remainingRequests", equalTo(4));
  }

  @Test
  void generate_afterFiveGenerations_returns429() {
    RequestSpecification spec = login(uniqueEmail());
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    for (int i = 0; i < 5; i++) {
      spec.post("/api/v1/recommendations/generate").then().statusCode(200);
    }

    spec.post("/api/v1/recommendations/generate").then().statusCode(429);
  }

  @Test
  void generate_asAdmin_generatesUnlimitedAndMeReturnsNullRemainingRequests() {
    String email = uniqueEmail();
    RequestSpecification spec = login(email);
    addReadBooks(spec, "Dune", "Neuromancer", "The Left Hand of Darkness");

    promoteToAdmin(email);

    spec
      .get("/api/v1/me")
      .then()
      .statusCode(200)
      .body("role", equalTo("ADMIN"))
      .body("remainingRequests", nullValue());

    for (int i = 0; i < 6; i++) {
      spec.post("/api/v1/recommendations/generate").then().statusCode(200);
    }
  }
}
