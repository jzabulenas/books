package com.example.demo.recommendation.ai;

/**
 * Signals that the AI recommendation exchange failed: the provider was unreachable, the
 * model returned output that stayed malformed even after validation retries, or the
 * response was otherwise unusable.
 */
public class AiRecommendationException extends RuntimeException {

  AiRecommendationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AiRecommendationException(String message) {
    super(message);
  }
}
