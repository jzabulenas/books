import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { ReadBook } from '../../../read-book.model';

@Component({
  selector: 'app-book-chip',
  imports: [MatChipsModule, MatIconModule],
  templateUrl: './book-chip.component.html',
  styleUrl: './book-chip.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookChipComponent {
  book = input.required<ReadBook>();
  deleted = output<number>();

  protected onRemove(): void {
    this.deleted.emit(this.book().id);
  }
}
