import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { RecipeService } from '../../app/core/api/recipe.service';
import { RecipeDetail } from '../../app/core/api/models';
import { BatchFormComponent } from '../../batches/batch-form/batch-form';
import { RecipeFormComponent } from '../recipe-form/recipe-form';
import { humanise } from '../../shared/labels';
import { ToastService } from '../../shared/toast.service';

@Component({
  selector: 'app-recipe-detail',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BatchFormComponent, RecipeFormComponent],
  templateUrl: './recipe-detail.html'
})
export class RecipeDetailComponent implements OnInit {
  /** Bound from the `:id` route parameter through `withComponentInputBinding()`. */
  readonly id = input.required<string>();

  private readonly recipeService = inject(RecipeService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  protected readonly detail = signal<RecipeDetail | null>(null);
  protected readonly loading = signal(true);
  protected readonly startOpen = signal(false);
  protected readonly editOpen = signal(false);
  protected readonly busy = signal(false);

  protected readonly humanise = humanise;

  // Required route inputs are only populated after construction, so the fetch waits for `ngOnInit`.
  ngOnInit(): void {
    this.load();
  }

  protected onBatchCreated(batchId: string): void {
    this.startOpen.set(false);
    void this.router.navigate(['/batches', batchId]);
  }

  /** After an edit the detail is re-fetched so the page reflects the new steps and duration. */
  protected onEdited(): void {
    this.editOpen.set(false);
    this.load();
  }

  protected deleteRecipe(): void {
    this.busy.set(true);
    this.recipeService.delete(this.id()).subscribe({
      next: () => {
        this.toastService.success('Recipe deleted.');
        void this.router.navigate(['/recipes']);
      },
      error: () => this.busy.set(false)
    });
  }

  private load(): void {
    this.loading.set(true);
    this.recipeService.findById(this.id()).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
