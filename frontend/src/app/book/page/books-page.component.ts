import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { BookInputComponent } from './input/book-input.component';
import { BookListComponent } from './list/book-list.component';
import { BookService } from '../book.service';

@Component({
  selector: 'app-books-page',
  imports: [BookInputComponent, BookListComponent],
  templateUrl: './books-page.component.html',
  styleUrl: './books-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BooksPageComponent implements OnInit {
  private readonly bookService = inject(BookService);

  protected readonly books = this.bookService.books;
  protected readonly count = computed(() => this.books().length);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.bookService.load().subscribe({
      error: () => this.error.set('Failed to load books.'),
    });
  }

  protected onBookAdded(title: string): void {
    this.error.set(null);
    this.bookService.add(title).subscribe({
      error: () => this.error.set('Failed to add book.'),
    });
  }

  protected onBookDeleted(id: number): void {
    this.error.set(null);
    this.bookService.remove(id).subscribe({
      error: () => this.error.set('Failed to remove book.'),
    });
  }
}
