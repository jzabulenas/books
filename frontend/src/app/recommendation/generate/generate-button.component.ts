import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-generate-button',
  imports: [MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './generate-button.component.html',
  styleUrl: './generate-button.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenerateButtonComponent {
  bookCount = input.required<number>();
  loading = input<boolean>(false);
  generate = output<void>();

  protected readonly needsMoreBooks = computed(() => this.bookCount() < 3);
  protected readonly remaining = computed(() => 3 - this.bookCount());
  protected readonly isDisabled = computed(() => this.loading() || this.needsMoreBooks());

  protected onGenerate(): void {
    this.generate.emit();
  }
}
