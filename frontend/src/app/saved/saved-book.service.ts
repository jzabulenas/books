import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { SavedBook } from './saved-book.model';

@Injectable({ providedIn: 'root' })
export class SavedBookService {
  private readonly http = inject(HttpClient);

  readonly savedBooks = signal<SavedBook[]>([]);

  load(): Observable<SavedBook[]> {
    return this.http
      .get<SavedBook[]>('/api/v1/saved-books')
      .pipe(tap((books) => this.savedBooks.set(books)));
  }

  save(title: string, author: string | null): Observable<SavedBook> {
    return this.http
      .post<SavedBook>('/api/v1/saved-books', { title, author })
      .pipe(tap((book) => this.savedBooks.update((list) => [...list, book])));
  }

  remove(id: number): Observable<void> {
    return this.http
      .delete<void>(`/api/v1/saved-books/${id}`)
      .pipe(tap(() => this.savedBooks.update((list) => list.filter((b) => b.id !== id))));
  }

  markRead(id: number): Observable<void> {
    return this.http
      .post<void>(`/api/v1/saved-books/${id}/read`, null)
      .pipe(tap(() => this.savedBooks.update((list) => list.filter((b) => b.id !== id))));
  }
}
