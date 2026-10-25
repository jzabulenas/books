import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ReadBook } from './read-book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);

  readonly books = signal<ReadBook[]>([]);

  load(): Observable<ReadBook[]> {
    return this.http
      .get<ReadBook[]>('/api/v1/read-books')
      .pipe(tap((books) => this.books.set(books)));
  }

  add(title: string): Observable<ReadBook> {
    return this.http
      .post<ReadBook>('/api/v1/read-books', { title })
      .pipe(tap((book) => this.books.update((list) => [...list, book])));
  }

  remove(id: number): Observable<void> {
    return this.http
      .delete<void>(`/api/v1/read-books/${id}`)
      .pipe(tap(() => this.books.update((list) => list.filter((b) => b.id !== id))));
  }
}
