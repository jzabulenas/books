import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookService } from '../book/book.service';
import { AuthService } from '../core/auth.service';
import { RecommendationService } from './recommendation.service';
import { SavedBookService } from '../saved/saved-book.service';
import { BlockedBookService } from '../blocked/blocked-book.service';
import { GenerateButtonComponent } from './generate/generate-button.component';
import { RecommendationCardComponent } from './card/recommendation-card.component';
import { RecommendationsEmptyStateComponent } from './empty/recommendations-empty-state.component';
import { Recommendation } from './recommendation.model';

@Component({
  selector: 'app-recommendations-page',
  imports: [
    GenerateButtonComponent,
    RecommendationCardComponent,
    RecommendationsEmptyStateComponent,
  ],
  templateUrl: './recommendations-page.component.html',
  styleUrl: './recommendations-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationsPageComponent implements OnInit {
  private readonly bookService = inject(BookService);
  private readonly recommendationService = inject(RecommendationService);
  private readonly savedBookService = inject(SavedBookService);
  private readonly blockedBookService = inject(BlockedBookService);
  private readonly authService = inject(AuthService);

  protected readonly bookCount = computed(() => this.bookService.books().length);
  protected readonly recommendations = this.recommendationService.recommendations;
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly remainingRequests = computed(
    () => this.authService.currentUser()?.remainingRequests ?? null,
  );

  ngOnInit(): void {
    this.bookService.load().subscribe({
      error: () => this.error.set('Failed to load books.'),
    });

    this.recommendationService.load().subscribe({
      error: () => this.error.set('Failed to load recommendations.'),
    });
  }

  protected onGenerate(): void {
    this.loading.set(true);
    this.error.set(null);

    this.recommendationService.generate().subscribe({
      next: () => this.authService.fetchCurrentUser().subscribe(),
      error: (httpError: HttpErrorResponse) => {
        this.error.set(
          httpError.status === 429
            ? 'Daily generation limit reached. Try again tomorrow.'
            : 'Failed to generate recommendations.',
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected onSave(recommendation: Recommendation): void {
    this.reviewRecommendation(
      recommendation,
      this.savedBookService.save(recommendation.title, recommendation.author),
      'Failed to save book.',
    );
  }

  protected onBlock(recommendation: Recommendation): void {
    this.reviewRecommendation(
      recommendation,
      this.blockedBookService.block(recommendation.title),
      'Failed to block book.',
    );
  }

  // Removing the card right away makes reviewing the queue feel instant. If persisting the
  // decision fails, put the card back at its original position so nothing is lost.
  private reviewRecommendation(
    recommendation: Recommendation,
    action: Observable<unknown>,
    failureMessage: string,
  ): void {
    this.error.set(null);

    const index = this.recommendations().indexOf(recommendation);

    this.recommendationService.remove(recommendation);

    action.subscribe({
      error: () => {
        this.recommendationService.restore(recommendation, index);
        this.error.set(failureMessage);
      },
    });
  }
}
