import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { SavedBookService } from './saved-book.service';
import { SavedBookCardComponent } from './card/saved-book-card.component';
import { EmptyStateComponent } from './empty/empty-state.component';

@Component({
  selector: 'app-saved-books-page',
  imports: [SavedBookCardComponent, EmptyStateComponent],
  templateUrl: './saved-books-page.component.html',
  styleUrl: './saved-books-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SavedBooksPageComponent implements OnInit {
  private readonly savedBookService = inject(SavedBookService);

  protected readonly savedBooks = this.savedBookService.savedBooks;
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.savedBookService.load().subscribe({
      error: () => this.error.set('Failed to load saved books.'),
    });
  }

  protected onRemove(id: number): void {
    this.error.set(null);
    this.savedBookService.remove(id).subscribe({
      error: () => this.error.set('Failed to remove book.'),
    });
  }

  protected onMarkRead(id: number): void {
    this.error.set(null);
    this.savedBookService.markRead(id).subscribe({
      error: () => this.error.set('Failed to mark book as read.'),
    });
  }
}
