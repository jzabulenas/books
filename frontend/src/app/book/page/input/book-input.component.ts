import { ChangeDetectionStrategy, Component, inject, output, viewChild } from '@angular/core';
import { FormBuilder, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-book-input',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './book-input.component.html',
  styleUrl: './book-input.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookInputComponent {
  bookAdded = output<string>();

  private readonly fb = inject(FormBuilder);
  private readonly formDirective = viewChild.required(FormGroupDirective);

  protected form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
  });
  protected titleControl = this.form.controls.title;

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();

      return;
    }

    this.bookAdded.emit(this.titleControl.value!);
    this.formDirective().resetForm();
  }
}
