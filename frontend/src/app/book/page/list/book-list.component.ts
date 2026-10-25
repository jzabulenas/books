import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { BookChipComponent } from './item/book-chip.component';
import { ReadBook } from '../../read-book.model';

@Component({
  selector: 'app-book-list',
  imports: [BookChipComponent],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookListComponent {
  books = input.required<ReadBook[]>();
  deleted = output<number>();
}
