import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { SavedBook } from '../saved-book.model';

@Component({
  selector: 'app-saved-book-card',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './saved-book-card.component.html',
  styleUrl: './saved-book-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SavedBookCardComponent {
  savedBook = input.required<SavedBook>();
  markedRead = output<number>();
  removed = output<number>();

  protected onMarkRead(): void {
    this.markedRead.emit(this.savedBook().id);
  }

  protected onRemove(): void {
    this.removed.emit(this.savedBook().id);
  }
}
